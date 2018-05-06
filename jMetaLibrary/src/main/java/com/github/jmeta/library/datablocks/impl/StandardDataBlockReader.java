/**
 *
 * {@link StandardDataBlockReader}.java
 *
 * @author Jens Ebert
 *
 * @date 17.01.2011
 */

package com.github.jmeta.library.datablocks.impl;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.jmeta.library.datablocks.api.exceptions.BinaryValueConversionException;
import com.github.jmeta.library.datablocks.api.services.DataBlockReader;
import com.github.jmeta.library.datablocks.api.services.ExtendedDataBlockFactory;
import com.github.jmeta.library.datablocks.api.types.Container;
import com.github.jmeta.library.datablocks.api.types.Field;
import com.github.jmeta.library.datablocks.api.types.FieldFunctionStack;
import com.github.jmeta.library.datablocks.api.types.Header;
import com.github.jmeta.library.datablocks.api.types.Payload;
import com.github.jmeta.library.dataformats.api.services.DataFormatSpecification;
import com.github.jmeta.library.dataformats.api.types.DataBlockDescription;
import com.github.jmeta.library.dataformats.api.types.DataBlockId;
import com.github.jmeta.library.dataformats.api.types.FieldFunction;
import com.github.jmeta.library.dataformats.api.types.FieldFunctionType;
import com.github.jmeta.library.dataformats.api.types.FieldProperties;
import com.github.jmeta.library.dataformats.api.types.FieldType;
import com.github.jmeta.library.dataformats.api.types.LocationProperties;
import com.github.jmeta.library.dataformats.api.types.MagicKey;
import com.github.jmeta.library.dataformats.api.types.PhysicalDataBlockType;
import com.github.jmeta.library.media.api.exceptions.EndOfMediumException;
import com.github.jmeta.library.media.api.services.MediumStore;
import com.github.jmeta.library.media.api.types.Medium;
import com.github.jmeta.library.media.api.types.MediumOffset;
import com.github.jmeta.utility.charset.api.services.Charsets;
import com.github.jmeta.utility.dbc.api.services.Reject;

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
public class StandardDataBlockReader implements DataBlockReader {

   private static final Logger LOGGER = LoggerFactory.getLogger(StandardDataBlockReader.class);

   /**
    * @return the {@link ExtendedDataBlockFactory}
    */
   protected ExtendedDataBlockFactory getDataBlockFactory() {

      return m_dataBlockFactory;
   }

   @Override
   public void free(MediumOffset startReference, long size) {
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
    */
   public StandardDataBlockReader(DataFormatSpecification spec, int maxFieldBlockSize) {
      Reject.ifNull(spec, "spec");
      Reject.ifTrue(maxFieldBlockSize < 1, "Maximum field block size may not be smaller than 1");

      m_spec = spec;
      m_maxFieldBlockSize = maxFieldBlockSize;
   }

   @Override
   public void initDataBlockFactory(ExtendedDataBlockFactory dataBlockFactory) {

      Reject.ifNull(dataBlockFactory, "dataBlockFactory");
      Reject.ifFalse(m_dataBlockFactory == null, "m_dataBlockFactory");

      m_dataBlockFactory = dataBlockFactory;

      m_dataBlockFactory.setDataBlockReader(this);
   }

   /**
    * @see com.github.jmeta.library.datablocks.api.services.DataBlockReader#hasContainerWithId(MediumOffset,
    *      com.github.jmeta.library.dataformats.api.types.DataBlockId,
    *      com.github.jmeta.library.datablocks.api.types.Payload, long, boolean)
    */
   @Override
   public boolean hasContainerWithId(MediumOffset reference, DataBlockId id, Payload parent,
      long remainingDirectParentByteCount, boolean forwardRead) {

      Reject.ifNull(reference, "reference");
      Reject.ifNull(id, "id");

      DataBlockDescription defaultNestedContainerDescription = getSpecification()
         .getDefaultNestedContainerDescription();

      if (defaultNestedContainerDescription != null && id.equals(defaultNestedContainerDescription.getId())) {
         return true;
      }

      DataBlockDescription containerDesc = m_spec.getDataBlockDescription(id);

      List<MagicKey> magicKeys = forwardRead ? containerDesc.getHeaderMagicKeys() : containerDesc.getFooterMagicKeys();

      for (int i = 0; i < magicKeys.size(); ++i) {
         MagicKey magicKey = magicKeys.get(i);

         // Does the magic key equal the medium bytes at the given reference?
         int magicKeySizeInBytes = magicKey.getByteLength();

         // This container cannot be stored in the parent, as there are not enough bytes in
         // left the parent for its magic key.
         if (forwardRead) {
            if (remainingDirectParentByteCount != DataBlockDescription.UNKNOWN_SIZE
               && magicKeySizeInBytes > remainingDirectParentByteCount)
               return false;
         } else {
            if (reference.getAbsoluteMediumOffset() + magicKey.getDeltaOffset() < 0) {
               return false;
            }
         }

         MediumOffset magicKeyReference = reference.advance(magicKey.getDeltaOffset());

         final ByteBuffer readBytes = readBytes(magicKeyReference, magicKeySizeInBytes);

         if (magicKey.isPresentIn(readBytes)) {
            return true;
         }
      }

      return false;
   }

   /**
    * Returns the next {@link Container} with the given {@link DataBlockId} assumed to be stored starting at the given
    * {@link MediumOffset} or null. If the {@link Container}s presence is optional, its actual presence is determined
    * 
    * @param parent
    */
   @Override
   public Container readContainerWithId(MediumOffset reference, DataBlockId id, Payload parent,
      FieldFunctionStack context, long remainingDirectParentByteCount) {

      Reject.ifNull(reference, "reference");
      Reject.ifNull(id, "id");
      Reject.ifFalse(hasContainerWithId(reference, id, parent, remainingDirectParentByteCount, true),
         "hasContainerWithId(reference, id, parent, remainingDirectParentByteCount)");

      // TODO the actual current charset and byte order must be known here!
      DataBlockId actualId = determineActualId(reference, id, context, remainingDirectParentByteCount,
         m_spec.getDefaultByteOrder(), m_spec.getDefaultCharacterEncoding());

      FieldFunctionStack theContext = context;

      if (theContext == null)
         theContext = new FieldFunctionStack();

      // Read headers
      MediumOffset nextReference = reference;

      List<Header> headers = new ArrayList<>();

      List<DataBlockDescription> headerDescs = DataBlockDescription.getChildDescriptionsOfType(m_spec, actualId,
         PhysicalDataBlockType.HEADER);

      long overallHeaderSize = 0;

      for (int i = 0; i < headerDescs.size(); ++i) {
         DataBlockDescription headerDesc = headerDescs.get(i);

         List<Header> nextHeaders = readHeadersOrFootersWithId(nextReference, headerDesc.getId(), actualId, headers,
            theContext, true);

         long totalHeaderSize = 0;

         for (int j = 0; j < nextHeaders.size(); ++j) {
            Header nextHeader = nextHeaders.get(j);

            totalHeaderSize += nextHeader.getTotalSize();
         }

         overallHeaderSize += totalHeaderSize;

         nextReference = nextReference.advance(totalHeaderSize);

         headers.addAll(nextHeaders);
      }

      // Hook method for data format specific implementations that want to add further
      // information to the context.
      afterHeaderReading(actualId, theContext, headers);

      // Read payload
      List<DataBlockDescription> payloadDescs = DataBlockDescription.getChildDescriptionsOfType(m_spec, actualId,
         PhysicalDataBlockType.FIELD_BASED_PAYLOAD);

      payloadDescs.addAll(DataBlockDescription.getChildDescriptionsOfType(m_spec, actualId,
         PhysicalDataBlockType.CONTAINER_BASED_PAYLOAD));

      // CONFIG_CHECK: Any container must specify a single PAYLOAD block
      if (payloadDescs.size() != 1)
         throw new IllegalStateException("For container parents, there must be a single data block of type PAYLOAD");

      DataBlockDescription payloadDesc = payloadDescs.get(0);

      long remainingPayloadByteCount = DataBlockDescription.UNKNOWN_SIZE;

      if (remainingDirectParentByteCount != DataBlockDescription.UNKNOWN_SIZE) {
         remainingPayloadByteCount = remainingDirectParentByteCount - overallHeaderSize;
      }

      Payload payload = readPayload(nextReference, payloadDesc.getId(), actualId, headers, theContext,
         remainingPayloadByteCount);

      // Read footers
      nextReference = nextReference.advance(payload.getTotalSize());

      List<Header> footers = new ArrayList<>();
      List<DataBlockDescription> footerDescs = DataBlockDescription.getChildDescriptionsOfType(m_spec, actualId,
         PhysicalDataBlockType.FOOTER);

      for (int i = 0; i < footerDescs.size(); ++i) {
         DataBlockDescription footerDesc = footerDescs.get(i);

         List<Header> nextFooters = readHeadersOrFootersWithId(nextReference, footerDesc.getId(), actualId, footers,
            theContext, true);

         long totalFooterSize = 0;

         for (int j = 0; j < nextFooters.size(); ++j) {
            Header nextFooter = nextFooters.get(j);

            totalFooterSize += nextFooter.getTotalSize();
         }

         nextReference = nextReference.advance(totalFooterSize);

         footers.addAll(nextFooters);
      }

      // Hook method for data format specific implementations that want to add further
      // information to the context.
      afterFooterReading(actualId, theContext, footers);

      // Create container
      final Container container = m_dataBlockFactory.createContainer(actualId, parent, reference, headers, payload,
         footers);

      return container;
   }

   // TODO primeRefactor002: Refactor and check readContainerWithIdBackwards as well as
   // readPayloadBackwards
   @Override
   public Container readContainerWithIdBackwards(MediumOffset reference, DataBlockId id, Payload parent,
      FieldFunctionStack context, long remainingDirectParentByteCount) {

      Reject.ifNull(reference, "reference");
      Reject.ifNull(id, "id");
      Reject.ifFalse(hasContainerWithId(reference, id, parent, remainingDirectParentByteCount, false),
         "hasContainerWithId(reference, id, parent, remainingDirectParentByteCount)");

      DataBlockId actualId = determineActualId(reference, id, context, remainingDirectParentByteCount,
         m_spec.getDefaultByteOrder(), m_spec.getDefaultCharacterEncoding());

      FieldFunctionStack theContext = context;

      if (theContext == null)
         theContext = new FieldFunctionStack();

      // Read footers
      MediumOffset nextReference = reference;

      List<Header> footers = new ArrayList<>();
      List<DataBlockDescription> footerDescs = DataBlockDescription.getChildDescriptionsOfType(m_spec, actualId,
         PhysicalDataBlockType.FOOTER);

      for (int i = 0; i < footerDescs.size(); ++i) {
         DataBlockDescription footerDesc = footerDescs.get(i);

         long staticLength = (footerDesc.getMaximumByteLength() == footerDesc.getMinimumByteLength())
            ? footerDesc.getMaximumByteLength()
            : DataBlockDescription.UNKNOWN_SIZE;

         nextReference = nextReference.advance(-staticLength);

         List<Header> nextFooters = readHeadersOrFootersWithId(nextReference, footerDesc.getId(), actualId, footers,
            theContext, true);

         @SuppressWarnings("unused")
         long totalFooterSize = 0;

         for (int j = 0; j < nextFooters.size(); ++j) {
            Header nextFooter = nextFooters.get(j);

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
         PhysicalDataBlockType.FIELD_BASED_PAYLOAD);

      payloadDescs.addAll(DataBlockDescription.getChildDescriptionsOfType(m_spec, actualId,
         PhysicalDataBlockType.CONTAINER_BASED_PAYLOAD));

      // CONFIG_CHECK: Any container must specify a single PAYLOAD block
      if (payloadDescs.size() != 1)
         throw new IllegalStateException("For container parents, there must be a single data block of type PAYLOAD");

      DataBlockDescription payloadDesc = payloadDescs.get(0);

      Payload payload = readPayloadBackwards(nextReference, payloadDesc.getId(), actualId, footers, theContext,
         remainingDirectParentByteCount);

      // Read headers
      nextReference = nextReference.advance(-payload.getTotalSize());

      List<Header> headers = new ArrayList<>();

      List<DataBlockDescription> headerDescs = DataBlockDescription.getChildDescriptionsOfType(m_spec, actualId,
         PhysicalDataBlockType.HEADER);

      for (int i = 0; i < headerDescs.size(); ++i) {
         DataBlockDescription headerDesc = headerDescs.get(i);

         // TODO primeRefactor003: handle multiple headers?

         // TODO primeRefactor003: expects static header size?
         nextReference = nextReference.advance(-headerDesc.getMaximumByteLength());

         List<Header> nextHeaders = readHeadersOrFootersWithId(nextReference, headerDesc.getId(), actualId, headers,
            theContext, false);

         @SuppressWarnings("unused")
         long totalHeaderSize = 0;

         for (int j = 0; j < nextHeaders.size(); ++j) {
            Header nextHeader = nextHeaders.get(j);

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
      final Container container = m_dataBlockFactory.createContainer(actualId, parent, nextReference, headers, payload,
         footers);

      return container;
   }

   // TODO primeRefactor002: Refactor and check readContainerWithIdBackwards as well as
   // readPayloadBackwards
   @Override
   public Payload readPayloadBackwards(MediumOffset reference, DataBlockId id, DataBlockId parentId,
      List<Header> footers, FieldFunctionStack context, long remainingDirectParentByteCount) {

      Reject.ifNull(footers, "footers");
      Reject.ifNull(id, "id");
      Reject.ifNull(reference, "reference");

      DataBlockDescription payloadDesc = m_spec.getDataBlockDescription(id);

      long totalPayloadSize = determineActualPayloadSize(payloadDesc, parentId, context, footers, null,
         remainingDirectParentByteCount);

      if (totalPayloadSize == DataBlockDescription.UNKNOWN_SIZE)
         throw new IllegalStateException("Payload size could not be determined");

      final Payload createPayloadAfterRead = m_dataBlockFactory.createPayloadAfterRead(payloadDesc.getId(),
         m_cache.createMediumOffset(reference.getAbsoluteMediumOffset() - totalPayloadSize), totalPayloadSize, this,
         context);

      return createPayloadAfterRead;
   }

   @Override
   public List<Header> readHeadersOrFootersWithId(MediumOffset reference, DataBlockId headerOrFooterId,
      DataBlockId parentId, List<Header> previousHeadersOrFooters, FieldFunctionStack context, boolean isFooter) {

      Reject.ifNull(previousHeadersOrFooters, "previousHeadersOrFooters");
      Reject.ifNull(headerOrFooterId, "headerOrFooterId");
      Reject.ifNull(reference, "reference");
      Reject.ifFalse(m_spec.specifiesBlockWithId(headerOrFooterId), "m_spec.specifiesBlockWithId(headerOrFooterId)");

      DataBlockDescription desc = m_spec.getDataBlockDescription(headerOrFooterId);

      List<Header> nextHeadersOrFooters = new ArrayList<>();

      // Get the actual occurrences of this headerId based on the fields of the previous
      // headers
      int actualOccurrences = determineActualOccurrences(parentId, desc, context);

      long staticLength = (desc.getMaximumByteLength() == desc.getMinimumByteLength()) ? desc.getMaximumByteLength()
         : DataBlockDescription.UNKNOWN_SIZE;

      // Read all header occurrences
      for (int i = 0; i < actualOccurrences; i++) {
         List<Field<?>> headerFields = readFields(reference, headerOrFooterId, context, staticLength);

         nextHeadersOrFooters
            .add(m_dataBlockFactory.createHeaderOrFooter(headerOrFooterId, reference, headerFields, isFooter));
      }

      return nextHeadersOrFooters;
   }

   /**
    * @see com.github.jmeta.library.datablocks.api.services.DataBlockReader#readPayload(MediumOffset,
    *      com.github.jmeta.library.dataformats.api.types.DataBlockId, DataBlockId, java.util.List, FieldFunctionStack,
    *      long)
    */
   @Override
   public Payload readPayload(MediumOffset reference, DataBlockId id, DataBlockId parentId, List<Header> headers,
      FieldFunctionStack context, long remainingDirectParentByteCount) {

      Reject.ifNull(headers, "headers");
      Reject.ifNull(id, "id");
      Reject.ifNull(reference, "reference");

      DataBlockDescription payloadDesc = m_spec.getDataBlockDescription(id);

      long totalPayloadSize = determineActualPayloadSize(payloadDesc, parentId, context, headers, null,
         remainingDirectParentByteCount);

      // If the medium is a stream-based medium, all the payload bytes must be cached first
      if (!m_cache.getMedium().isRandomAccess() && totalPayloadSize != Medium.UNKNOWN_LENGTH) {
         try {
            cache(reference, totalPayloadSize);
         } catch (EndOfMediumException e) {
            throw new RuntimeException("Unexpected end of file " + e);
         }
      }

      final Payload createPayloadAfterRead = m_dataBlockFactory.createPayloadAfterRead(payloadDesc.getId(), reference,
         totalPayloadSize, this, context);

      return createPayloadAfterRead;
   }

   /**
    * @see com.github.jmeta.library.datablocks.api.services.DataBlockReader#readFields(MediumOffset,
    *      com.github.jmeta.library.dataformats.api.types.DataBlockId, FieldFunctionStack, long)
    */
   @Override
   public List<Field<?>> readFields(MediumOffset reference, DataBlockId parentId, FieldFunctionStack context,
      long remainingDirectParentByteCount) {

      List<DataBlockDescription> fieldChildren = DataBlockDescription.getChildDescriptionsOfType(m_spec, parentId,
         PhysicalDataBlockType.FIELD);

      List<Field<?>> fields = new ArrayList<>();

      MediumOffset currentFieldReference = reference;
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

            Field<?> newField = readField(currentFieldReference, actualByteOrder, actualCharacterEncoding, fieldDesc,
               fieldSize, currentlyRemainingParentByteCount);

            context.pushFieldFunctions(fieldDesc, newField);

            fields.add(newField);

            if (currentlyRemainingParentByteCount != DataBlockDescription.UNKNOWN_SIZE)
               currentlyRemainingParentByteCount -= fieldSize;

            currentFieldReference = currentFieldReference.advance(fieldSize);
         }
      }

      if (currentlyRemainingParentByteCount > 0) {
         Field<?> unknownField = readField(currentFieldReference, actualByteOrder, actualCharacterEncoding,
            createUnknownFieldDescription(parentId), currentlyRemainingParentByteCount,
            currentlyRemainingParentByteCount);

         fields.add(unknownField);
      }

      return fields;
   }

   /**
    * @see com.github.jmeta.library.datablocks.api.services.DataBlockReader#setMediumCache(MediumStore)
    */
   @Override
   public void setMediumCache(MediumStore cache) {

      Reject.ifNull(cache, "cache");

      m_cache = cache;
   }

   /**
    * @see com.github.jmeta.library.datablocks.api.services.DataBlockReader#identifiesDataFormat(MediumOffset, boolean)
    */
   @Override
   public boolean identifiesDataFormat(MediumOffset reference, boolean forwardRead) {

      List<DataBlockDescription> topLevelContainerDescs = DataBlockDescription.getChildDescriptionsOfType(m_spec, null,
         PhysicalDataBlockType.CONTAINER);

      for (int i = 0; i < topLevelContainerDescs.size(); ++i) {
         DataBlockDescription desc = topLevelContainerDescs.get(i);

         // TODO stage2_010: What value should remaining parent byte count really have here?
         if (hasContainerWithId(reference, desc.getId(), null, DataBlockDescription.UNKNOWN_SIZE, forwardRead))
            return true;
      }

      return false;
   }

   /**
    * @see com.github.jmeta.library.datablocks.api.services.DataBlockReader#getSpecification()
    */
   @Override
   public DataFormatSpecification getSpecification() {

      return m_spec;
   }

   private Field<?> readField(final MediumOffset reference, ByteOrder currentByteOrder, Charset currentCharset,
      DataBlockDescription fieldDesc, long fieldSize, long remainingDirectParentByteCount) {

      ByteBuffer fieldBuffer = null;

      try {
         if (remainingDirectParentByteCount > fieldSize) {
            if (fieldSize <= m_maxFieldBlockSize) {
               if (m_cache.getCachedByteCountAt(reference) >= 1) {
                  if (m_cache.getCachedByteCountAt(reference) < fieldSize)
                     cache(reference, fieldSize);
               }

               else
                  cache(reference, Math.min(remainingDirectParentByteCount, m_maxFieldBlockSize));
            }
         }

         else {
            if (m_cache.getCachedByteCountAt(reference) < fieldSize)
               if (remainingDirectParentByteCount == DataBlockDescription.UNKNOWN_SIZE)
                  cache(reference, fieldSize);

               else
                  cache(reference, remainingDirectParentByteCount);
         }
      } catch (EndOfMediumException e) {
         throw new IllegalStateException(
            buildEOFExceptionMessage(reference, e.getByteCountTriedToRead(), e.getByteCountActuallyRead()));
      }

      // A lazy field is created if the field size exceeds a maximum size
      if (fieldSize > m_maxFieldBlockSize)
         return new LazyField(fieldDesc, reference, null, fieldSize, m_dataBlockFactory, this, currentByteOrder,
            currentCharset);

      fieldBuffer = readBytes(reference, (int) fieldSize);

      return m_dataBlockFactory.createFieldFromBytes(fieldDesc.getId(), m_spec, reference, fieldBuffer,
         currentByteOrder, currentCharset);
   }

   private long getSizeFromFieldFunction(FieldFunctionStack context, DataBlockId sizeBlockId, DataBlockId parentId) {

      long sizeFromFieldFunction = DataBlockDescription.UNKNOWN_SIZE;

      final Long size = context.popFieldFunction(sizeBlockId, FieldFunctionType.SIZE_OF);

      DataBlockDescription blockDesc = m_spec.getDataBlockDescription(sizeBlockId);

      // The given SIZE_OF may be a total size of payload plus headers and /or footers
      // FIXME: FIELD_BASED and CONTAINER_BASED_PAYLOAD
      if (blockDesc.getPhysicalType().equals(PhysicalDataBlockType.FIELD_BASED_PAYLOAD)
         || blockDesc.getPhysicalType().equals(PhysicalDataBlockType.CONTAINER_BASED_PAYLOAD)) {
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
   protected void afterFooterReading(DataBlockId containerId, FieldFunctionStack context, List<Header> footers) {

      // Default behavior: Do nothing. This method is intended to be overridden by a
      // data format specific implementation.
   }

   /**
    * @param containerId
    * @param context
    * @param headers
    */
   protected void afterHeaderReading(DataBlockId containerId, FieldFunctionStack context, List<Header> headers) {

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
      FieldFunctionStack context, List<Header> headers, List<Header> footers, long remainingDirectParentByteCount) {

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
      FieldFunctionStack context, long remainingDirectParentByteCount, MediumOffset reference, ByteOrder byteOrder,
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

   private long getSizeUpToTerminationBytes(MediumOffset reference, ByteOrder byteOrder, byte[] terminationBytes,
      long remainingDirectParentByteCount) {

      long sizeUpToTerminationBytes = 0;

      MediumOffset currentReference = reference;

      boolean endOfMediumReached = false;

      // The block size read is adapted to be a multiple of the termination bytes' length,
      // to be sure the termination bytes are not split up between to subsequent read
      // blocks, but found in a single block read.
      int fittingBlockSize = m_maxFieldBlockSize + terminationBytes.length
         - m_maxFieldBlockSize % terminationBytes.length;

      while (!endOfMediumReached) {
         int bytesToRead = fittingBlockSize;

         long cachedByteCountAt = m_cache.getCachedByteCountAt(currentReference);
         // As long as no termination bytes are found: Cache from medium, if not already
         // done.
         try {
            // TODO: Problem using cache here - For potentially large terminated (lazy)
            // fields, the whole field would - at the end - be cached in memory. This
            // will cause an OutOfMemory condition soon. Instead, use a "checkedRead"
            // approach that won't memorize read bytes...
            if (cachedByteCountAt < bytesToRead)
               m_cache.cache(currentReference, bytesToRead);
         } catch (EndOfMediumException e) {
            bytesToRead = (int) e.getByteCountActuallyRead();

            if (bytesToRead < cachedByteCountAt) {
               bytesToRead = (int) cachedByteCountAt;
            }

            // End condition for the loop
            endOfMediumReached = true;
         }

         // Workaround for InMemoryMedia: They cannot be used for caching, and then there is no EOM Exception, thus
         // bytesToRead will still be too big, we have to change it, otherwise Unexpected EOM during readBytes
         if (bytesToRead == 0 || (currentReference.getMedium().getCurrentLength() != Medium.UNKNOWN_LENGTH
            && bytesToRead > currentReference.getMedium().getCurrentLength()
               - currentReference.getAbsoluteMediumOffset())) {
            bytesToRead = (int) (currentReference.getMedium().getCurrentLength()
               - currentReference.getAbsoluteMediumOffset());
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

   private DataBlockId determineActualId(MediumOffset reference, DataBlockId id, FieldFunctionStack context,
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

         MediumOffset idFieldReference = reference.advance(byteOffset);

         long actualFieldSize = determineActualFieldSize(idFieldDesc, id, context,
            remainingParentByteCount - byteOffset, idFieldReference, byteOrder, characterEncoding);

         if (actualFieldSize == DataBlockDescription.UNKNOWN_SIZE)
            throw new IllegalStateException(
               "Could not determine size of field " + idFieldId + " which stores the id of a generic data block");

         // Read the field that defines the actual id for the container
         ByteOrder currentByteOrder = m_spec.getDefaultByteOrder();
         Charset currentCharacterEncoding = m_spec.getDefaultCharacterEncoding();

         Field<?> idField = readField(idFieldReference, currentByteOrder, currentCharacterEncoding, idFieldDesc,
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

   private DataBlockId concreteBlockIdFromGenericId(DataBlockId genericBlockId, Field<?> headerField) {

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
         DataFormatSpecification.UNKNOWN_FIELD_ID);

      FieldProperties<byte[]> unknownFieldProperties = new FieldProperties<>(FieldType.BINARY, new byte[] { 0 }, null,
         null, DataBlockDescription.UNKNOWN_SIZE, DataBlockDescription.UNKNOWN_SIZE, null, null, null, null, null, null,
         null, null, false);

      return new DataBlockDescription(unknownBlockId, DataFormatSpecification.UNKNOWN_FIELD_ID,
         DataFormatSpecification.UNKNOWN_FIELD_ID, PhysicalDataBlockType.FIELD, new ArrayList<DataBlockId>(),
         unknownFieldProperties, new HashMap<DataBlockId, LocationProperties>(), DataBlockDescription.UNKNOWN_SIZE,
         DataBlockDescription.UNKNOWN_SIZE, null);
   }

   private String buildEOFExceptionMessage(MediumOffset reference, long byteCount, final int bytesRead) {

      return "Unexpected EOF occurred during read from medium " + reference.getMedium() + " [offset="
         + reference.getAbsoluteMediumOffset() + ", byteCount=" + byteCount + "]. Only " + bytesRead
         + " were read before EOF.";
   }

   private ExtendedDataBlockFactory m_dataBlockFactory;

   private final DataFormatSpecification m_spec;

   /**
    * @see com.github.jmeta.library.datablocks.api.services.DataBlockReader#readBytes(com.github.jmeta.library.media.api.types.MediumOffset,
    *      int)
    */
   @Override
   public ByteBuffer readBytes(MediumOffset reference, int size) {

      Reject.ifNull(reference, "reference");

      try {
         return m_cache.getData(reference, size);
      } catch (EndOfMediumException e) {
         throw new RuntimeException("Unexpected end of medium", e);
      }
   }

   /**
    * @see com.github.jmeta.library.datablocks.api.services.DataBlockReader#cache(com.github.jmeta.library.media.api.types.MediumOffset,
    *      long)
    */
   @Override
   public void cache(MediumOffset reference, long size) throws EndOfMediumException {

      m_cache.cache(reference, (int) size);
   }

   protected MediumStore m_cache;

   private int m_maxFieldBlockSize;

   @Override
   public void setMaxFieldBlockSize(int maxFieldBlockSize) {

      Reject.ifNull(maxFieldBlockSize, "maxFieldBlockSize");

      m_maxFieldBlockSize = maxFieldBlockSize;
   }
}
