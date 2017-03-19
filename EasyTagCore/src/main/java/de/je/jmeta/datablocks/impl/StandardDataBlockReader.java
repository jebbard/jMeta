/**
 *
 * {@link StandardDataBlockReader}.java
 *
 * @author Jens Ebert
 *
 * @date 17.01.2011
 */

package de.je.jmeta.datablocks.impl;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.je.jmeta.datablocks.BinaryValueConversionException;
import de.je.jmeta.datablocks.IContainer;
import de.je.jmeta.datablocks.IField;
import de.je.jmeta.datablocks.IHeader;
import de.je.jmeta.datablocks.IPayload;
import de.je.jmeta.datablocks.ITransformationHandler;
import de.je.jmeta.datablocks.export.FieldFunctionStack;
import de.je.jmeta.datablocks.export.IDataBlockReader;
import de.je.jmeta.datablocks.export.IExtendedDataBlockFactory;
import de.je.jmeta.dataformats.BinaryValue;
import de.je.jmeta.dataformats.ChildOrder;
import de.je.jmeta.dataformats.DataBlockDescription;
import de.je.jmeta.dataformats.DataBlockId;
import de.je.jmeta.dataformats.DataFormat;
import de.je.jmeta.dataformats.DataTransformationType;
import de.je.jmeta.dataformats.FieldFunction;
import de.je.jmeta.dataformats.FieldFunctionType;
import de.je.jmeta.dataformats.FieldProperties;
import de.je.jmeta.dataformats.FieldType;
import de.je.jmeta.dataformats.IDataFormatSpecification;
import de.je.jmeta.dataformats.LocationProperties;
import de.je.jmeta.dataformats.MagicKey;
import de.je.jmeta.dataformats.PhysicalDataBlockType;
import de.je.jmeta.media.api.IMediumReference;
import de.je.jmeta.media.api.IMediumStore;
import de.je.jmeta.media.api.datatype.AbstractMedium;
import de.je.jmeta.media.api.exception.EndOfMediumException;
import de.je.util.javautil.common.charset.Charsets;
import de.je.util.javautil.common.err.Contract;
import de.je.util.javautil.common.err.Reject;

// TODO document001: MagicKey inclusion and exclusion key mechanism
// TODO document002: Determine payload size by reading children
// TODO document003: ID_OF facility
// TODO document004: Reverse reading: Rules, preconditions, implementation, testing
// TODO document005: Test approach for data blocks component
// TODO document006: Detailed description how field functions work

// TODO stage2_000: Better strategy for reading large data (everything block-wise, not
// only large payload and fields) => document possible cases of data size to block types

/**
 * {@link StandardDataBlockReader}
 *
 */
public class StandardDataBlockReader implements IDataBlockReader {

   private static final Logger LOGGER = LoggerFactory.getLogger(StandardDataBlockReader.class);

   /**
    * @return the {@link IExtendedDataBlockFactory}
    */
   protected IExtendedDataBlockFactory getDataBlockFactory() {

      return m_dataBlockFactory;
   }

   @Override
   public void free(IMediumReference startReference, long size) {

      m_cache.discard(startReference, size);
   }

   // TODO stage2_005: It is a validation / parsing error if a field for which a fixed value is
   // defined does have another value during reading.
   private static final String LOGGING_BINARY_TO_INTERPRETED_FAILED = "Field conversion from binary to interpreted value failed for field id <%1$s>. Exception see below.";

   /**
    * Creates a new {@link StandardDataBlockReader}.
    * 
    * @param spec
    * @param transformationHandlers
    * @param maxFieldBlockSize
    * @param logging
    */
   public StandardDataBlockReader(IDataFormatSpecification spec,
      Map<DataTransformationType, ITransformationHandler> transformationHandlers, int maxFieldBlockSize) {
      Reject.ifNull(spec, "spec");
      Reject.ifTrue(maxFieldBlockSize < 1, "Maximum field block size may not be smaller than 1");

      m_spec = spec;
      m_maxFieldBlockSize = maxFieldBlockSize;

      // Determine order of the transformations
      if (!transformationHandlers.isEmpty())
         addTransformationHandlers(transformationHandlers);
   }

   @Override
   public void initDataBlockFactory(IExtendedDataBlockFactory dataBlockFactory) {

      Reject.ifNull(dataBlockFactory, "dataBlockFactory");
      Contract.checkPrecondition(m_dataBlockFactory == null, "The data block factory may only be initiated once");

      m_dataBlockFactory = dataBlockFactory;

      m_dataBlockFactory.setDataBlockReader(this);
   }

   /**
    * @see de.je.jmeta.datablocks.export.IDataBlockReader#hasContainerWithId(IMediumReference,
    *      de.je.jmeta.dataformats.DataBlockId, de.je.jmeta.datablocks.IPayload, long)
    */
   @Override
   public boolean hasContainerWithId(IMediumReference reference, DataBlockId id, IPayload parent,
      long remainingDirectParentByteCount) {

      Reject.ifNull(reference, "reference");
      Reject.ifNull(id, "id");

      DataBlockDescription containerDesc = m_spec.getDataBlockDescription(id);

      List<MagicKey> magicKeys = containerDesc.getMagicKeys();

      // No magic key means: "Everything is a container"
      if (magicKeys.isEmpty())
         return true;

      // TODO primeRefactor001: Does this loop really always yields the wanted results?
      // Is it possible that sometimes the headers magic key gets preferred?
      for (int i = 0; i < magicKeys.size(); ++i) {
         MagicKey magicKey = magicKeys.get(i);

         // Does the magic key equal the medium bytes at the given reference?
         int magicKeySize = magicKey.getBitLength() / Byte.SIZE + (magicKey.getBitLength() % Byte.SIZE != 0 ? 1 : 0);

         // This container cannot be stored in the parent, as there are not enough bytes in
         // left the parent for its magic key.
         if (remainingDirectParentByteCount != DataBlockDescription.UNKNOWN_SIZE
            && magicKeySize > remainingDirectParentByteCount)
            return false;

         IMediumReference magicKeyReference = reference.advance(magicKey.getOffsetFromStartOfHeaderOrFooter());

         final ByteBuffer readBytes = readBytes(magicKeyReference, magicKeySize);
         final boolean equalsBytes = magicKey.equalsBytes(readBytes);

         // Everything BUT the something that starts with an exclusion key is a container
         if (magicKey.isExclusionKey())
            return !equalsBytes;

         // Everything that starts with the magic key bytes is a container
         if (equalsBytes)
            return equalsBytes;
      }

      return false;
   }

   /**
    * Returns the next {@link IContainer} with the given {@link DataBlockId} assumed to be stored starting at the given
    * {@link IMediumReference} or null. If the {@link IContainer}s presence is optional, its actual presence is
    * determined
    * 
    * @param parent
    */
   @Override
   public IContainer readContainerWithId(IMediumReference reference, DataBlockId id, IPayload parent,
      FieldFunctionStack context, long remainingDirectParentByteCount) {

      Reject.ifNull(reference, "reference");
      Reject.ifNull(id, "id");
      Contract.checkPrecondition(hasContainerWithId(reference, id, parent, remainingDirectParentByteCount),
         "hasContainerWithId() must return true in order to call this method");

      // TODO the actual current charset and byte order must be known here!
      DataBlockId actualId = determineActualId(reference, id, context, remainingDirectParentByteCount,
         m_spec.getDefaultByteOrder(), m_spec.getDefaultCharacterEncoding());

      FieldFunctionStack theContext = context;

      if (theContext == null)
         theContext = new FieldFunctionStack();

      // Read headers
      IMediumReference nextReference = reference;

      List<IHeader> headers = new ArrayList<>();

      List<DataBlockDescription> headerDescs = DataBlockDescription.getChildDescriptionsOfType(m_spec, actualId,
         PhysicalDataBlockType.HEADER);

      for (int i = 0; i < headerDescs.size(); ++i) {
         DataBlockDescription headerDesc = headerDescs.get(i);

         List<IHeader> nextHeaders = readHeadersWithId(nextReference, headerDesc.getId(), actualId, headers,
            theContext);

         long totalHeaderSize = 0;

         for (int j = 0; j < nextHeaders.size(); ++j) {
            IHeader nextHeader = nextHeaders.get(j);

            totalHeaderSize += nextHeader.getTotalSize();
         }

         nextReference = nextReference.advance(totalHeaderSize);

         headers.addAll(nextHeaders);
      }

      // Hook method for data format specific implementations that want to add further
      // information to the context.
      afterHeaderReading(actualId, theContext, headers);

      // Read payload
      List<DataBlockDescription> payloadDescs = DataBlockDescription.getChildDescriptionsOfType(m_spec, actualId,
         PhysicalDataBlockType.PAYLOAD);

      // CONFIG_CHECK: Any container must specify a single PAYLOAD block
      if (payloadDescs.size() != 1)
         throw new IllegalStateException("For container parents, there must be a single data block of type PAYLOAD");

      DataBlockDescription payloadDesc = payloadDescs.get(0);

      IPayload payload = readPayload(nextReference, payloadDesc.getId(), actualId, headers, theContext,
         remainingDirectParentByteCount);

      // Read footers
      nextReference = nextReference.advance(payload.getTotalSize());

      List<IHeader> footers = new ArrayList<>();
      List<DataBlockDescription> footerDescs = DataBlockDescription.getChildDescriptionsOfType(m_spec, actualId,
         PhysicalDataBlockType.FOOTER);

      for (int i = 0; i < footerDescs.size(); ++i) {
         DataBlockDescription footerDesc = footerDescs.get(i);

         List<IHeader> nextFooters = readFootersWithId(nextReference, footerDesc.getId(), actualId, footers,
            theContext);

         long totalFooterSize = 0;

         for (int j = 0; j < nextFooters.size(); ++j) {
            IHeader nextFooter = nextFooters.get(j);

            totalFooterSize += nextFooter.getTotalSize();
         }

         nextReference = nextReference.advance(totalFooterSize);

         footers.addAll(nextFooters);
      }

      // Hook method for data format specific implementations that want to add further
      // information to the context.
      afterFooterReading(actualId, theContext, footers);

      // Create container
      final IContainer container = m_dataBlockFactory.createContainer(actualId, parent, reference, headers, payload,
         footers);

      return applyTransformationsAfterRead(container);
   }

   // TODO primeRefactor002: Refactor and check readContainerWithIdBackwards as well as
   // readPayloadBackwards
   @Override
   public IContainer readContainerWithIdBackwards(IMediumReference reference, DataBlockId id, IPayload parent,
      FieldFunctionStack context, long remainingDirectParentByteCount) {

      Reject.ifNull(reference, "reference");
      Reject.ifNull(id, "id");
      Contract.checkPrecondition(hasContainerWithId(reference, id, parent, remainingDirectParentByteCount),
         "hasContainerWithId() must return true in order to call this method");

      DataBlockId actualId = determineActualId(reference, id, context, remainingDirectParentByteCount,
         m_spec.getDefaultByteOrder(), m_spec.getDefaultCharacterEncoding());

      FieldFunctionStack theContext = context;

      if (theContext == null)
         theContext = new FieldFunctionStack();

      // Read footers
      IMediumReference nextReference = reference;

      List<IHeader> footers = new ArrayList<>();
      List<DataBlockDescription> footerDescs = DataBlockDescription.getChildDescriptionsOfType(m_spec, actualId,
         PhysicalDataBlockType.FOOTER);

      for (int i = 0; i < footerDescs.size(); ++i) {
         DataBlockDescription footerDesc = footerDescs.get(i);

         List<IHeader> nextFooters = readFootersWithId(nextReference, footerDesc.getId(), actualId, footers,
            theContext);

         @SuppressWarnings("unused")
         long totalFooterSize = 0;

         for (int j = 0; j < nextFooters.size(); ++j) {
            IHeader nextFooter = nextFooters.get(j);

            totalFooterSize += nextFooter.getTotalSize();
         }

         // TODO primeRefactor003: handle multiple footers

         footers.addAll(nextFooters);
      }

      // Hook method for data format specific implementations that want to add further
      // information to the context.
      afterFooterReading(actualId, theContext, footers);

      // Read payload
      List<DataBlockDescription> payloadDescs = DataBlockDescription.getChildDescriptionsOfType(m_spec, actualId,
         PhysicalDataBlockType.PAYLOAD);

      // CONFIG_CHECK: Any container must specify a single PAYLOAD block
      if (payloadDescs.size() != 1)
         throw new IllegalStateException("For container parents, there must be a single data block of type PAYLOAD");

      DataBlockDescription payloadDesc = payloadDescs.get(0);

      IPayload payload = readPayloadBackwards(nextReference, payloadDesc.getId(), actualId, footers, theContext,
         remainingDirectParentByteCount);

      // Read headers
      nextReference = nextReference.advance(-payload.getTotalSize());

      List<IHeader> headers = new ArrayList<>();

      List<DataBlockDescription> headerDescs = DataBlockDescription.getChildDescriptionsOfType(m_spec, actualId,
         PhysicalDataBlockType.HEADER);

      for (int i = 0; i < headerDescs.size(); ++i) {
         DataBlockDescription headerDesc = headerDescs.get(i);

         // TODO primeRefactor003: handle multiple headers?

         // TODO primeRefactor003: expects static header size?
         nextReference = nextReference.advance(-headerDesc.getMaximumByteLength());

         List<IHeader> nextHeaders = readHeadersWithId(nextReference, headerDesc.getId(), actualId, headers,
            theContext);

         @SuppressWarnings("unused")
         long totalHeaderSize = 0;

         for (int j = 0; j < nextHeaders.size(); ++j) {
            IHeader nextHeader = nextHeaders.get(j);

            totalHeaderSize += nextHeader.getTotalSize();
         }

         headers.addAll(nextHeaders);
      }

      // Hook method for data format specific implementations that want to add further
      // information to the context.
      afterHeaderReading(actualId, theContext, headers);

      // Create container
      // IMPORTANT: The containers StandardMediumReference MUST NOT be set to the original passed
      // StandardMediumReference because that one points to the containers back!
      final IContainer container = m_dataBlockFactory.createContainer(actualId, parent, nextReference, headers, payload,
         footers);

      return applyTransformationsAfterRead(container);
   }

   // TODO primeRefactor002: Refactor and check readContainerWithIdBackwards as well as
   // readPayloadBackwards
   @Override
   public IPayload readPayloadBackwards(IMediumReference reference, DataBlockId id, DataBlockId parentId,
      List<IHeader> footers, FieldFunctionStack context, long remainingDirectParentByteCount) {

      Reject.ifNull(footers, "footers");
      Reject.ifNull(id, "id");
      Reject.ifNull(reference, "reference");

      DataBlockDescription payloadDesc = m_spec.getDataBlockDescription(id);

      long totalPayloadSize = determineActualPayloadSize(payloadDesc, parentId, context, footers, null,
         remainingDirectParentByteCount);

      if (totalPayloadSize == DataBlockDescription.UNKNOWN_SIZE)
         throw new IllegalStateException("Payload size could not be determined");

      final IPayload createPayloadAfterRead = m_dataBlockFactory.createPayloadAfterRead(payloadDesc.getId(),
         m_cache.createMediumReference(reference.getAbsoluteMediumOffset() - totalPayloadSize), totalPayloadSize, this,
         context);

      return createPayloadAfterRead;
   }

   /**
    * Returns the next {@link IHeader} with the given {@link DataBlockId} assumed to be stored starting at the given
    * {@link IMediumReference} or null. If the {@link IHeader}s presence is optional, its actual presence is determined
    * using the given previous {@link IHeader}s. The method returns null if no {@link IHeader} with the
    * {@link DataBlockId} is present at the given {@link IMediumReference}.
    *
    * @param reference
    *           The {@link IMediumReference} pointing to the location of the assumed {@link IHeader} in the
    *           {@link AbstractMedium}.
    * @param headerId
    *           The {@link DataBlockId} of the assumed {@link IHeader}.
    * @param parentId
    * @param previousHeaders
    *           The {@link List} of previous {@link IHeader}s belonging to the same {@link IContainer}. Have been
    *           already read beforehand. These {@link IHeader}s can be used to determine the presence of the currently
    *           requested {@link IHeader}. If there are no {@link IHeader}s that have been read beforehand, this
    *           {@link List} must be empty.
    * @return The {@link IHeader} with the given {@link DataBlockId} with its {@link StandardField}s read from the given
    *         {@link IMediumReference}.
    */
   @Override
   public List<IHeader> readHeadersWithId(IMediumReference reference, DataBlockId headerId, DataBlockId parentId,
      List<IHeader> previousHeaders, FieldFunctionStack context) {

      Reject.ifNull(previousHeaders, "previousHeaders");
      Reject.ifNull(headerId, "headerId");
      Reject.ifNull(reference, "reference");
      Contract.checkPrecondition(m_spec.specifiesBlockWithId(headerId),
         "Header id " + headerId + " is not specified by the data format " + m_spec.getDataFormat());

      DataBlockDescription headerDesc = m_spec.getDataBlockDescription(headerId);

      List<IHeader> nextHeaders = new ArrayList<>();

      // Get the actual occurrences of this headerId based on the fields of the previous
      // headers
      int actualOccurrences = determineActualOccurrences(parentId, headerDesc, context);

      long staticHeaderLength = (headerDesc.getMaximumByteLength() == headerDesc.getMinimumByteLength())
         ? headerDesc.getMaximumByteLength() : DataBlockDescription.UNKNOWN_SIZE;

      // Read all header occurrences
      for (int i = 0; i < actualOccurrences; i++) {
         List<IField<?>> headerFields = readFields(reference, headerId, context, staticHeaderLength);

         nextHeaders.add(m_dataBlockFactory.createHeader(headerId, reference, headerFields, false));
      }

      return nextHeaders;
   }

   @Override
   public List<IHeader> readFootersWithId(IMediumReference reference, DataBlockId footerId, DataBlockId parentId,
      List<IHeader> previousFooters, FieldFunctionStack context) {

      Reject.ifNull(previousFooters, "previousFooters");
      Reject.ifNull(footerId, "footerId");
      Reject.ifNull(reference, "reference");
      Contract.checkPrecondition(m_spec.specifiesBlockWithId(footerId),
         "Footer id " + footerId + " is not specified by the data format " + m_spec.getDataFormat());

      DataBlockDescription footerDesc = m_spec.getDataBlockDescription(footerId);

      List<IHeader> nextFooters = new ArrayList<>();

      // Get the actual occurrences of this footerId based on the fields of the previous
      // headers
      int actualOccurrences = determineActualOccurrences(parentId, footerDesc, context);

      long staticFooterLength = (footerDesc.getMaximumByteLength() == footerDesc.getMinimumByteLength())
         ? footerDesc.getMaximumByteLength() : DataBlockDescription.UNKNOWN_SIZE;

      // Read all footer occurrences
      for (int i = 0; i < actualOccurrences; i++) {
         List<IField<?>> footerFields = readFields(reference, footerId, context, staticFooterLength);

         nextFooters.add(m_dataBlockFactory.createHeader(footerId, reference, footerFields, true));
      }

      return nextFooters;
   }

   /**
    * @see de.je.jmeta.datablocks.export.IDataBlockReader#readPayload(IMediumReference,
    *      de.je.jmeta.dataformats.DataBlockId, DataBlockId, java.util.List, FieldFunctionStack, long)
    */
   @Override
   public IPayload readPayload(IMediumReference reference, DataBlockId id, DataBlockId parentId, List<IHeader> headers,
      FieldFunctionStack context, long remainingDirectParentByteCount) {

      Reject.ifNull(headers, "headers");
      Reject.ifNull(id, "id");
      Reject.ifNull(reference, "reference");

      DataBlockDescription payloadDesc = m_spec.getDataBlockDescription(id);

      long totalPayloadSize = determineActualPayloadSize(payloadDesc, parentId, context, headers, null,
         remainingDirectParentByteCount);

      // If the medium is a stream-based medium, all the payload bytes must be cached first
      if (!m_cache.getMedium().isRandomAccess()) {
         try {
            cache(reference, totalPayloadSize);
         } catch (EndOfMediumException e) {
            throw new RuntimeException("Unexpected end of file " + e);
         }
      }

      final IPayload createPayloadAfterRead = m_dataBlockFactory.createPayloadAfterRead(payloadDesc.getId(), reference,
         totalPayloadSize, this, context);

      return createPayloadAfterRead;
   }

   /**
    * @see de.je.jmeta.datablocks.export.IDataBlockReader#readFields(IMediumReference,
    *      de.je.jmeta.dataformats.DataBlockId, FieldFunctionStack, long)
    */
   @Override
   public List<IField<?>> readFields(IMediumReference reference, DataBlockId parentId, FieldFunctionStack context,
      long remainingDirectParentByteCount) {

      List<DataBlockDescription> fieldChildren = DataBlockDescription.getChildDescriptionsOfType(m_spec, parentId,
         PhysicalDataBlockType.FIELD);

      List<IField<?>> fields = new ArrayList<>();

      if (reference.getAbsoluteMediumOffset() == 72095) {
         System.out.println("TEST");
      }

      IMediumReference currentFieldReference = reference;
      ByteOrder currentByteOrder = m_spec.getDefaultByteOrder();
      Charset currentCharset = m_spec.getDefaultCharacterEncoding();
      ByteOrder actualByteOrder = currentByteOrder;
      Charset actualCharacterEncoding = currentCharset;

      long currentlyRemainingParentByteCount = remainingDirectParentByteCount;

      for (int i = 0; i < fieldChildren.size(); ++i) {
         DataBlockDescription fieldDesc = fieldChildren.get(i);

         // Update current character encoding via CHARACTER_ENCODING_OF
         if (context.hasFieldFunction(fieldDesc.getId(), FieldFunctionType.CHARACTER_ENCODING_OF))
            currentCharset = context.popFieldFunction(fieldDesc.getId(), FieldFunctionType.CHARACTER_ENCODING_OF);

         // Update current byte order via BYTE_ORDER_OF
         if (context.hasFieldFunction(fieldDesc.getId(), FieldFunctionType.BYTE_ORDER_OF))
            currentByteOrder = context.popFieldFunction(fieldDesc.getId(), FieldFunctionType.BYTE_ORDER_OF);

         // Fixed charset and byte order override currently set charset or byte order
         actualByteOrder = currentByteOrder;
         actualCharacterEncoding = currentCharset;

         final ByteOrder fixedByteOrder = fieldDesc.getFieldProperties().getFixedByteOrder();

         if (fixedByteOrder != null)
            actualByteOrder = fixedByteOrder;

         final Charset fixedCharset = fieldDesc.getFieldProperties().getFixedCharacterEncoding();

         if (fixedCharset != null)
            actualCharacterEncoding = fixedCharset;

         int actualOccurrences = determineActualOccurrences(parentId, fieldDesc, context);

         for (int j = 0; j < actualOccurrences; j++) {
            long fieldSize = determineActualFieldSize(fieldDesc, parentId, context, currentlyRemainingParentByteCount,
               currentFieldReference, actualByteOrder, actualCharacterEncoding);

            IField<?> newField = readField(currentFieldReference, actualByteOrder, actualCharacterEncoding, fieldDesc,
               fieldSize, currentlyRemainingParentByteCount);

            context.pushFieldFunctions(fieldDesc, newField);

            fields.add(newField);

            if (currentlyRemainingParentByteCount != DataBlockDescription.UNKNOWN_SIZE)
               currentlyRemainingParentByteCount -= fieldSize;

            currentFieldReference = currentFieldReference.advance(fieldSize);
         }
      }

      if (currentlyRemainingParentByteCount > 0) {
         IField<?> unknownField = readField(currentFieldReference, actualByteOrder, actualCharacterEncoding,
            createUnknownFieldDescription(parentId), currentlyRemainingParentByteCount,
            currentlyRemainingParentByteCount);

         fields.add(unknownField);
      }

      return fields;
   }

   /**
    * @see de.je.jmeta.datablocks.export.IDataBlockReader#setMediumCache(de.je.jmeta.media.api.IMediumStore)
    */
   @Override
   public void setMediumCache(IMediumStore cache) {

      Reject.ifNull(cache, "cache");

      m_cache = cache;
   }

   /**
    * @see de.je.jmeta.datablocks.export.IDataBlockReader#getLongestMinimumContainerHeaderSize(DataBlockId)
    */
   @Override
   public long getLongestMinimumContainerHeaderSize(DataBlockId payloadId) {

      long m_longestMinHeaderSize = 0;

      List<DataBlockDescription> containerDescs = DataBlockDescription.getChildDescriptionsOfType(m_spec, payloadId,
         PhysicalDataBlockType.CONTAINER);

      for (int i = 0; i < containerDescs.size(); ++i) {
         DataBlockDescription containerDesc = containerDescs.get(i);

         List<MagicKey> magicKeys = containerDesc.getMagicKeys();

         for (int j = 0; j < magicKeys.size(); ++j) {
            MagicKey magicKey = magicKeys.get(j);

            DataBlockDescription desc = m_spec.getDataBlockDescription(magicKey.getHeaderOrFooterId());

            final long minimumHeaderSize = DataBlockDescription.getTotalMinimumSize(m_spec, desc);
            if (minimumHeaderSize > m_longestMinHeaderSize)
               m_longestMinHeaderSize = minimumHeaderSize;
         }
      }

      return m_longestMinHeaderSize;
   }

   @Override
   public long getShortestMinimumContainerHeaderSize(DataBlockId payloadId) {

      long shortestMinHeaderSize = DataBlockDescription.UNKNOWN_SIZE;

      List<DataBlockDescription> containerDescs = DataBlockDescription.getChildDescriptionsOfType(m_spec, payloadId,
         PhysicalDataBlockType.CONTAINER);

      for (int i = 0; i < containerDescs.size(); ++i) {
         DataBlockDescription containerDesc = containerDescs.get(i);

         List<MagicKey> magicKeys = containerDesc.getMagicKeys();

         for (int j = 0; j < magicKeys.size(); ++j) {
            MagicKey magicKey = magicKeys.get(j);

            DataBlockDescription desc = m_spec.getDataBlockDescription(magicKey.getHeaderOrFooterId());

            final long minimumHeaderSize = DataBlockDescription.getTotalMinimumSize(m_spec, desc);
            if (minimumHeaderSize != DataBlockDescription.UNKNOWN_SIZE && minimumHeaderSize < shortestMinHeaderSize)
               shortestMinHeaderSize = minimumHeaderSize;
         }
      }

      return shortestMinHeaderSize;
   }

   /**
    * @see de.je.jmeta.datablocks.export.IDataBlockReader#identifiesDataFormat(IMediumReference)
    */
   @Override
   public boolean identifiesDataFormat(IMediumReference reference) {

      List<DataBlockDescription> topLevelContainerDescs = DataBlockDescription.getChildDescriptionsOfType(m_spec, null,
         PhysicalDataBlockType.CONTAINER);

      for (int i = 0; i < topLevelContainerDescs.size(); ++i) {
         DataBlockDescription desc = topLevelContainerDescs.get(i);

         // TODO stage2_010: What value should remaining parent byte count really have here?
         if (hasContainerWithId(reference, desc.getId(), null, DataBlockDescription.UNKNOWN_SIZE))
            return true;
      }

      return false;
   }

   /**
    * @see de.je.jmeta.datablocks.export.IDataBlockReader#getSpecification()
    */
   @Override
   public IDataFormatSpecification getSpecification() {

      return m_spec;
   }

   /**
    * @see de.je.jmeta.datablocks.IDataBlockAccessor#getTransformationHandlers(DataFormat)
    */
   @Override
   public Map<DataTransformationType, ITransformationHandler> getTransformationHandlers() {

      return Collections.unmodifiableMap(m_transformationsReadOrder);
   }

   /**
    * @see de.je.jmeta.datablocks.IDataBlockAccessor#setTransformationHandler(DataFormat, DataTransformationType,
    *      de.je.jmeta.datablocks.ITransformationHandler)
    */
   @Override
   public void setTransformationHandler(DataTransformationType transformationType, ITransformationHandler handler) {

      Contract.checkPrecondition(m_transformationsReadOrder.containsKey(transformationType),
         "Given transformation type " + transformationType + " is not defined by the data format "
            + m_spec.getDataFormat());

      if (handler != null)
         Contract.checkPrecondition(transformationType.equals(handler.getTransformationType()),
            "The given data transformation type " + transformationType
               + " must be equal to the handlers data transformation type " + handler.getTransformationType());

      // Set the handler
      if (handler != null)
         m_transformationsReadOrder.put(transformationType, handler);

      // Remove an already set handler
      else
         m_transformationsReadOrder.remove(transformationType);
   }

   private void addTransformationHandlers(Map<DataTransformationType, ITransformationHandler> transformationHandlers) {

      Map<Integer, DataTransformationType> transformationsReadOrder = new TreeMap<>();

      for (int i = 0; i < m_spec.getDataTransformations().size(); ++i) {
         DataTransformationType dtt = m_spec.getDataTransformations().get(i);

         transformationsReadOrder.put(dtt.getReadOrder(), dtt);
      }

      // Add data transformation handlers
      Iterator<DataTransformationType> readOrderIterator = transformationsReadOrder.values().iterator();

      while (readOrderIterator.hasNext()) {
         DataTransformationType nextTransformationType = readOrderIterator.next();

         if (transformationHandlers.containsKey(nextTransformationType))
            m_transformationsReadOrder.put(nextTransformationType, transformationHandlers.get(nextTransformationType));

         else
            m_transformationsReadOrder.put(nextTransformationType, null);
      }
   }

   private IField<?> readField(final IMediumReference reference, ByteOrder currentByteOrder, Charset currentCharset,
      DataBlockDescription fieldDesc, long fieldSize, long remainingDirectParentByteCount) {

      ByteBuffer fieldBuffer = null;

      try {
         if (remainingDirectParentByteCount > fieldSize) {
            if (fieldSize <= m_maxFieldBlockSize) {
               if (m_cache.getBufferedByteCountAt(reference) >= 1) {
                  if (m_cache.getBufferedByteCountAt(reference) < fieldSize)
                     cache(reference, fieldSize);
               }

               else
                  cache(reference, Math.min(remainingDirectParentByteCount, m_maxFieldBlockSize));
            }
         }

         else {
            if (m_cache.getBufferedByteCountAt(reference) < fieldSize)
               if (remainingDirectParentByteCount == DataBlockDescription.UNKNOWN_SIZE)
                  cache(reference, fieldSize);

               else
                  cache(reference, remainingDirectParentByteCount);
         }
      } catch (EndOfMediumException e) {
         throw new IllegalStateException(
            buildEOFExceptionMessage(reference, e.getByteCountTriedToRead(), e.getBytesReallyRead()));
      }

      // A lazy field is created if the field size exceeds a maximum size
      if (fieldSize > m_maxFieldBlockSize)
         return new LazyField(fieldDesc, reference, null, fieldSize, m_dataBlockFactory, this, currentByteOrder,
            currentCharset);

      fieldBuffer = readBytes(reference, (int) fieldSize);

      return m_dataBlockFactory.createFieldFromBytes(fieldDesc.getId(), m_spec, reference, new BinaryValue(fieldBuffer),
         currentByteOrder, currentCharset);
   }

   private IContainer applyTransformationsAfterRead(IContainer container) {

      Reject.ifNull(container, "container");

      IContainer transformedContainer = container;

      Iterator<ITransformationHandler> handlerIterator = m_transformationsReadOrder.values().iterator();

      while (handlerIterator.hasNext()) {
         ITransformationHandler transformationHandler = handlerIterator.next();

         if (transformationHandler.requiresUntransform(transformedContainer)) {
            if (m_cache.getBufferedByteCountAt(transformedContainer.getMediumReference()) < transformedContainer
               .getTotalSize())
               try {
                  m_cache.buffer(transformedContainer.getMediumReference(), transformedContainer.getTotalSize());
               } catch (EndOfMediumException e) {
                  throw new IllegalStateException("Unexpected end of medium", e);
               }

            transformedContainer = transformationHandler.untransform(transformedContainer);
         }
      }

      return transformedContainer;
   }

   private long getSizeFromFieldFunction(FieldFunctionStack context, DataBlockId sizeBlockId, DataBlockId parentId) {

      long sizeFromFieldFunction = DataBlockDescription.UNKNOWN_SIZE;

      final Long size = context.popFieldFunction(sizeBlockId, FieldFunctionType.SIZE_OF);

      DataBlockDescription blockDesc = m_spec.getDataBlockDescription(sizeBlockId);

      // The given SIZE_OF may be a total size of payload plus headers and /or footers
      if (blockDesc.getPhysicalType().equals(PhysicalDataBlockType.PAYLOAD)) {
         long totalSizeToSubtract = 0;

         List<DataBlockDescription> headerAndFooterDescs = DataBlockDescription.getChildDescriptionsOfType(m_spec,
            parentId, PhysicalDataBlockType.HEADER);
         headerAndFooterDescs
            .addAll(DataBlockDescription.getChildDescriptionsOfType(m_spec, parentId, PhysicalDataBlockType.FOOTER));

         for (int i = 0; i < headerAndFooterDescs.size(); ++i) {
            DataBlockDescription headerOrFooterDesc = headerAndFooterDescs.get(i);

            if (context.hasFieldFunction(headerOrFooterDesc.getId(), FieldFunctionType.SIZE_OF)) {
               if (headerOrFooterDesc.getMinimumByteLength() != headerOrFooterDesc.getMaximumByteLength())
                  return DataBlockDescription.UNKNOWN_SIZE;

               int actualOccurrences = determineActualOccurrences(parentId, headerOrFooterDesc, context);

               totalSizeToSubtract += actualOccurrences * headerOrFooterDesc.getMinimumByteLength();
            }
         }

         sizeFromFieldFunction = size - totalSizeToSubtract;

         if (sizeFromFieldFunction < 0)
            sizeFromFieldFunction = DataBlockDescription.UNKNOWN_SIZE;
      }

      else
         sizeFromFieldFunction = size;

      return sizeFromFieldFunction;
   }

   /**
    * @param containerId
    * @param context
    * @param footers
    */
   protected void afterFooterReading(DataBlockId containerId, FieldFunctionStack context, List<IHeader> footers) {

      // Default behavior: Do nothing. This method is intended to be overridden by a
      // data format specific implementation.
   }

   /**
    * @param containerId
    * @param context
    * @param headers
    */
   protected void afterHeaderReading(DataBlockId containerId, FieldFunctionStack context, List<IHeader> headers) {

      // Default behavior: Do nothing. This method is intended to be overridden by a
      // data format specific implementation.
   }

   /**
    * @param payloadDesc
    * @param parentId
    * @param context
    * @param headers
    * @param footers
    * @param remainingDirectParentByteCount
    * @return the actual payload size
    */
   private long determineActualPayloadSize(DataBlockDescription payloadDesc, DataBlockId parentId,
      FieldFunctionStack context, List<IHeader> headers, List<IHeader> footers, long remainingDirectParentByteCount) {

      long actualBlockSize = DataBlockDescription.UNKNOWN_SIZE;

      // Payload has a dynamic size which is determined by further context information
      if (payloadDesc.getMinimumByteLength() != payloadDesc.getMaximumByteLength()) {
         // Search for a SIZE_OF function, i.e. the size of the current payload is
         // determined by the value of a field already read before
         if (context.hasFieldFunction(payloadDesc.getId(), FieldFunctionType.SIZE_OF))
            actualBlockSize = getSizeFromFieldFunction(context, payloadDesc.getId(), parentId);

         // If a size could not be determined this way, try the generic id
         else {
            DataBlockId matchingGenericId = m_spec.getMatchingGenericId(payloadDesc.getId());

            if (matchingGenericId != null && context.hasFieldFunction(matchingGenericId, FieldFunctionType.SIZE_OF))
               actualBlockSize = getSizeFromFieldFunction(context, matchingGenericId, parentId);
         }
      }

      // Static length: Payload has always the same length
      else
         actualBlockSize = payloadDesc.getMinimumByteLength();

      if (actualBlockSize == DataBlockDescription.UNKNOWN_SIZE)
         actualBlockSize = remainingDirectParentByteCount;

      return actualBlockSize;
   }

   private long determineActualFieldSize(DataBlockDescription fieldDesc, DataBlockId parentId,
      FieldFunctionStack context, long remainingDirectParentByteCount, IMediumReference reference, ByteOrder byteOrder,
      Charset characterEncoding) {

      long actualBlockSize = DataBlockDescription.UNKNOWN_SIZE;

      // Field has a dynamic size which is determined by further context information
      if (fieldDesc.getMinimumByteLength() != fieldDesc.getMaximumByteLength()) {
         // Search for a SIZE_OF function, i.e. the size of the current field is
         // determined by the value of a field already read before
         if (context.hasFieldFunction(fieldDesc.getId(), FieldFunctionType.SIZE_OF))
            actualBlockSize = getSizeFromFieldFunction(context, fieldDesc.getId(), parentId);

         // If a size could not be determined this way, try the generic id
         else {
            DataBlockId matchingGenericId = m_spec.getMatchingGenericId(fieldDesc.getId());

            if (matchingGenericId != null && context.hasFieldFunction(matchingGenericId, FieldFunctionType.SIZE_OF))
               actualBlockSize = getSizeFromFieldFunction(context, matchingGenericId, parentId);
         }

         if (actualBlockSize == DataBlockDescription.UNKNOWN_SIZE) {
            byte[] terminationBytes = fieldDesc.getFieldProperties().getTerminationBytes();
            final Character terminationCharacter = fieldDesc.getFieldProperties().getTerminationCharacter();

            // Determine termination bytes from termination character
            if (terminationCharacter != null)
               terminationBytes = Charsets.getBytesWithoutBOM(new String("" + terminationCharacter), characterEncoding);

            if (terminationBytes != null)
               actualBlockSize = getSizeUpToTerminationBytes(reference, byteOrder, terminationBytes,
                  remainingDirectParentByteCount);
         }
      }

      // Static length: Field has always the same length
      else
         actualBlockSize = fieldDesc.getMinimumByteLength();

      if (actualBlockSize == DataBlockDescription.UNKNOWN_SIZE)
         actualBlockSize = remainingDirectParentByteCount;

      return actualBlockSize;
   }

   private long getSizeUpToTerminationBytes(IMediumReference reference, ByteOrder byteOrder, byte[] terminationBytes,
      long remainingDirectParentByteCount) {

      long sizeUpToTerminationBytes = 0;

      IMediumReference currentReference = reference;

      boolean endOfMediumReached = false;

      // The block size read is adapted to be a multiple of the termination bytes' length,
      // to be sure the termination bytes are not split up between to subsequent read
      // blocks, but found in a single block read.
      int fittingBlockSize = m_maxFieldBlockSize + terminationBytes.length
         - m_maxFieldBlockSize % terminationBytes.length;

      while (!endOfMediumReached) {
         int bytesToRead = fittingBlockSize;

         // As long as no termination bytes are found: Cache from medium, if not already
         // done.
         try {
            // TODO: Problem using cache here - For potentially large terminated (lazy)
            // fields, the whole field would - at the end - be cached in memory. This
            // will cause an OutOfMemory condition soon. Instead, use a "checkedRead"
            // approach that won't memorize read bytes...
            if (m_cache.getBufferedByteCountAt(currentReference) < bytesToRead)
               m_cache.buffer(currentReference, bytesToRead);
         } catch (EndOfMediumException e) {
            bytesToRead = (int) e.getBytesReallyRead();
            // End condition for the loop
            endOfMediumReached = true;
         }

         ByteBuffer bufferedBytes = readBytes(currentReference, bytesToRead);
         bufferedBytes.order(byteOrder);

         int findStartIndex = findTerminationBytes(bufferedBytes, terminationBytes);

         // Termination bytes have been found!
         if (findStartIndex != -1) {
            sizeUpToTerminationBytes += findStartIndex + terminationBytes.length;

            return sizeUpToTerminationBytes;
         }

         // Otherwise try to locate them in the next block read
         sizeUpToTerminationBytes += bytesToRead;

         // In case that the number of remaining bytes in the current field's parent
         // is known, the algorithm terminates already if this number is exceeded, instead
         // of reading further up to the end of medium and potentially spotting wrong
         // termination bytes already belonging to a subsequent field.
         if (remainingDirectParentByteCount != DataBlockDescription.UNKNOWN_SIZE)
            if (sizeUpToTerminationBytes >= remainingDirectParentByteCount)
               return remainingDirectParentByteCount;

         currentReference = currentReference.advance(bytesToRead);
      }

      return DataBlockDescription.UNKNOWN_SIZE;
   }

   private int findTerminationBytes(ByteBuffer fieldBytes, final byte[] terminationBytes) {

      int terminationStartIndex = 0;

      byte[] terminationByteBuffer = new byte[terminationBytes.length];

      // Find termination bytes - Only at offsets that are multiples of the termination byte length!
      // Because this is interpreted as the size of one "character", especially for strings
      // (e.g. UTF-16 null character consisting of two null bytes)
      while (fieldBytes.hasRemaining()) {
         int filledCount = terminationStartIndex % terminationBytes.length;

         terminationByteBuffer[filledCount] = fieldBytes.get();

         if (filledCount == terminationBytes.length - 1)
            if (Arrays.equals(terminationByteBuffer, terminationBytes))
               return terminationStartIndex - terminationBytes.length + 1;

         terminationStartIndex++;
      }

      return -1;
   }

   private DataBlockId determineActualId(IMediumReference reference, DataBlockId id, FieldFunctionStack context,
      long remainingParentByteCount, ByteOrder byteOrder, Charset characterEncoding) {

      if (m_spec.isGeneric(id)) {
         DataBlockId idFieldId = findFirstIdField(id);

         // CONFIG_CHECK For generic data blocks, exactly one child field with ID_OF(genericDataBlockId) must be defined
         if (idFieldId == null)
            throw new IllegalStateException(
               "For generic data block " + id + ", no child field with ID_OF function is defined.");

         final DataBlockDescription idFieldDesc = m_spec.getDataBlockDescription(idFieldId);

         long byteOffset = DataBlockDescription.UNKNOWN_SIZE;

         if (idFieldDesc.getAllParentsForLocationProperties().contains(id)) {
            LocationProperties locProps = idFieldDesc.getLocationPropertiesForParent(id);

            byteOffset = locProps.getByteOffset();
         }

         if (byteOffset == DataBlockDescription.UNKNOWN_SIZE)
            throw new IllegalStateException("For generic data block " + id
               + ", a LocationProperties object with the exact offset for its container parent " + id
               + " must be specified.");

         IMediumReference idFieldReference = reference.advance(byteOffset);

         long actualFieldSize = determineActualFieldSize(idFieldDesc, id, context,
            remainingParentByteCount - byteOffset, idFieldReference, byteOrder, characterEncoding);

         if (actualFieldSize == DataBlockDescription.UNKNOWN_SIZE)
            throw new IllegalStateException(
               "Could not determine size of field " + idFieldId + " which stores the id of a generic data block");

         // Read the field that defines the actual id for the container
         ByteOrder currentByteOrder = m_spec.getDefaultByteOrder();
         Charset currentCharacterEncoding = m_spec.getDefaultCharacterEncoding();

         IField<?> idField = readField(idFieldReference, currentByteOrder, currentCharacterEncoding, idFieldDesc,
            actualFieldSize, actualFieldSize);

         return concreteBlockIdFromGenericId(id, idField);
      }

      return id;
   }

   private DataBlockId findFirstIdField(DataBlockId parentId) {

      DataBlockDescription parentDesc = m_spec.getDataBlockDescription(parentId);

      if (parentDesc.getPhysicalType().equals(PhysicalDataBlockType.FIELD)) {
         for (int i = 0; i < parentDesc.getFieldProperties().getFieldFunctions().size(); ++i) {
            FieldFunction function = parentDesc.getFieldProperties().getFieldFunctions().get(i);

            if (function.getFieldFunctionType().equals(FieldFunctionType.ID_OF))
               return parentId;
         }
      }

      for (int i = 0; i < parentDesc.getOrderedChildIds().size(); ++i) {
         final DataBlockId id = findFirstIdField(parentDesc.getOrderedChildIds().get(i));
         if (id != null)
            return id;
      }

      return null;
   }

   private int determineActualOccurrences(DataBlockId parentId, DataBlockDescription desc, FieldFunctionStack context) {

      int minOccurrences = 0;
      int maxOccurrences = 0;
      int actualOccurrences = 0;

      if (desc.getAllParentsForLocationProperties().contains(parentId)) {
         LocationProperties location = desc.getLocationPropertiesForParent(parentId);
         maxOccurrences = location.getMaxOccurrences();
         minOccurrences = location.getMinOccurrences();
      }

      // Data block has fixed amount of mandatory occurrences
      if (minOccurrences == maxOccurrences)
         actualOccurrences = minOccurrences;

      // Data block is optional
      else if (minOccurrences == 0 && maxOccurrences == 1) {
         if (context.hasFieldFunction(desc.getId(), FieldFunctionType.PRESENCE_OF)) {
            boolean present = context.popFieldFunction(desc.getId(), FieldFunctionType.PRESENCE_OF);

            if (present)
               actualOccurrences = 1;
         }
      }

      // Data block has a variable number of occurrences
      else if (minOccurrences != maxOccurrences) {
         if (context.hasFieldFunction(desc.getId(), FieldFunctionType.COUNT_OF)) {
            final Long count = context.popFieldFunction(desc.getId(), FieldFunctionType.COUNT_OF);

            actualOccurrences = (int) count.longValue();
         }
      }
      return actualOccurrences;
   }

   private DataBlockId concreteBlockIdFromGenericId(DataBlockId genericBlockId, IField<?> headerField) {

      String concreteLocalId = "";
      try {
         concreteLocalId = headerField.getInterpretedValue().toString();
      } catch (BinaryValueConversionException e) {
         // Silently ignore: The local id remains by its previous value
         LOGGER.warn(LOGGING_BINARY_TO_INTERPRETED_FAILED, headerField.getId());
         LOGGER.error("concreteBlockIdFromGenericId", e);
      }

      String concreteGlobalId = genericBlockId.getGlobalId().replace(genericBlockId.getLocalId(), concreteLocalId);

      return new DataBlockId(genericBlockId.getDataFormat(), concreteGlobalId);
   }

   private DataBlockDescription createUnknownFieldDescription(DataBlockId parentId) {

      DataBlockId unknownBlockId = new DataBlockId(m_spec.getDataFormat(), parentId,
         IDataFormatSpecification.UNKNOWN_FIELD_ID);

      FieldProperties<BinaryValue> unknownFieldProperties = new FieldProperties<>(FieldType.BINARY,
         new BinaryValue(new byte[] { 0 }), null, null, DataBlockDescription.UNKNOWN_SIZE,
         DataBlockDescription.UNKNOWN_SIZE, null, null, null, null, null, null, null, null);

      return new DataBlockDescription(unknownBlockId, IDataFormatSpecification.UNKNOWN_FIELD_ID,
         IDataFormatSpecification.UNKNOWN_FIELD_ID, PhysicalDataBlockType.FIELD, new ArrayList<DataBlockId>(),
         ChildOrder.SEQUENTIAL, unknownFieldProperties, new HashMap<DataBlockId, LocationProperties>(),
         DataBlockDescription.UNKNOWN_SIZE, DataBlockDescription.UNKNOWN_SIZE, null, null);
   }

   private String buildEOFExceptionMessage(IMediumReference reference, long byteCount, final long bytesRead) {

      return "Unexpected EOF occurred during read from medium " + reference.getMedium() + " [offset="
         + reference.getAbsoluteMediumOffset() + ", byteCount=" + byteCount + "]. Only " + bytesRead
         + " were read before EOF.";
   }

   private IExtendedDataBlockFactory m_dataBlockFactory;

   private final IDataFormatSpecification m_spec;

   private Map<DataTransformationType, ITransformationHandler> m_transformationsReadOrder = new LinkedHashMap<>();

   /**
    * @see de.je.jmeta.datablocks.export.IDataBlockReader#readBytes(de.je.jmeta.media.api.IMediumReference, int)
    */
   @Override
   public ByteBuffer readBytes(IMediumReference reference, int size) {

      Reject.ifNull(reference, "reference");

      try {
         return m_cache.getData(reference, size);
      } catch (EndOfMediumException e) {
         throw new RuntimeException("Unexpected end of medium", e);
      }
   }

   /**
    * @see de.je.jmeta.datablocks.export.IDataBlockReader#cache(de.je.jmeta.media.api.IMediumReference, long)
    */
   @Override
   public void cache(IMediumReference reference, long size) throws EndOfMediumException {

      m_cache.buffer(reference, size);
   }

   private IMediumStore m_cache;

   private int m_maxFieldBlockSize;

   @Override
   public void setMaxFieldBlockSize(int maxFieldBlockSize) {

      Reject.ifNull(maxFieldBlockSize, "maxFieldBlockSize");

      m_maxFieldBlockSize = maxFieldBlockSize;
   }
}
