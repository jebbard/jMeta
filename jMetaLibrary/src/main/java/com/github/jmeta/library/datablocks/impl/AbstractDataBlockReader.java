/**
 *
 * {@link AbstractDataBlockReader}.java
 *
 * @author Jens Ebert
 *
 * @date 17.01.2011
 */

package com.github.jmeta.library.datablocks.impl;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.jmeta.library.datablocks.api.exceptions.BinaryValueConversionException;
import com.github.jmeta.library.datablocks.api.services.CountProvider;
import com.github.jmeta.library.datablocks.api.services.DataBlockFactory;
import com.github.jmeta.library.datablocks.api.services.DataBlockReader;
import com.github.jmeta.library.datablocks.api.services.SizeProvider;
import com.github.jmeta.library.datablocks.api.types.ContainerContext;
import com.github.jmeta.library.datablocks.api.types.Field;
import com.github.jmeta.library.datablocks.api.types.Header;
import com.github.jmeta.library.datablocks.api.types.Payload;
import com.github.jmeta.library.dataformats.api.services.DataFormatSpecification;
import com.github.jmeta.library.dataformats.api.types.DataBlockDescription;
import com.github.jmeta.library.dataformats.api.types.DataBlockId;
import com.github.jmeta.library.dataformats.api.types.FieldProperties;
import com.github.jmeta.library.dataformats.api.types.FieldType;
import com.github.jmeta.library.dataformats.api.types.MagicKey;
import com.github.jmeta.library.dataformats.api.types.PhysicalDataBlockType;
import com.github.jmeta.library.media.api.services.MediumStore;
import com.github.jmeta.library.media.api.types.MediumOffset;
import com.github.jmeta.utility.dbc.api.services.Reject;

/**
 * {@link AbstractDataBlockReader}
 *
 */
public abstract class AbstractDataBlockReader implements DataBlockReader {

   private static final Logger LOGGER = LoggerFactory.getLogger(AbstractDataBlockReader.class);

   private static final String LOGGING_BINARY_TO_INTERPRETED_FAILED = "Field conversion from binary to interpreted value failed for field id <%1$s>. Exception see below.";

   private MediumDataProvider mediumDataProvider;

   private DataBlockFactory m_dataBlockFactory;

   private final DataFormatSpecification m_spec;

   private SizeProvider customSizeProvider;

   private CountProvider customCountProvider;

   /**
    * Creates a new {@link AbstractDataBlockReader}.
    *
    * @param spec
    * @param transformationHandlers
    */
   public AbstractDataBlockReader(DataFormatSpecification spec) {
      Reject.ifNull(spec, "spec");

      m_spec = spec;
      m_dataBlockFactory = new StandardDataBlockFactory();
   }

   /**
    * Returns the attribute {@link #mediumDataProvider}.
    *
    * @return the attribute {@link #mediumDataProvider}
    */
   public MediumDataProvider getMediumDataProvider() {
      return mediumDataProvider;
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

      DataBlockId unknownBlockId = new DataBlockId(parentId, DataFormatSpecification.UNKNOWN_FIELD_ID);

      FieldProperties<byte[]> unknownFieldProperties = new FieldProperties<>(FieldType.BINARY, new byte[] { 0 }, null,
         null, null, null, null, null, false, DataBlockDescription.UNDEFINED, null);

      return new DataBlockDescription(unknownBlockId, DataFormatSpecification.UNKNOWN_FIELD_ID,
         DataFormatSpecification.UNKNOWN_FIELD_ID, PhysicalDataBlockType.FIELD, new ArrayList<>(),
         unknownFieldProperties, 1, 1, DataBlockDescription.UNDEFINED, DataBlockDescription.UNDEFINED, false, null);
   }

   protected DataBlockId determineActualContainerId(MediumOffset reference, DataBlockId id,
      long remainingParentByteCount, int sequenceNumber, ContainerContext containerContext) {

      DataBlockDescription desc = m_spec.getDataBlockDescription(id);

      if (desc.isGeneric()) {
         DataBlockId idFieldId = desc.getIdField();

         final DataBlockDescription idFieldDesc = m_spec.getDataBlockDescription(idFieldId);

         long byteOffset = idFieldDesc.getByteOffsetFromStartOfContainer();

         if (byteOffset == DataBlockDescription.UNDEFINED) {
            throw new IllegalStateException("For generic data block " + id
               + ", the exact offset of its id field in its container parent " + id + " must be specified.");
         }

         MediumOffset idFieldReference = reference.advance(byteOffset);

         long actualFieldSize = determineActualFieldSize(idFieldDesc, remainingParentByteCount - byteOffset,
            idFieldReference, containerContext, 0);

         if (actualFieldSize == DataBlockDescription.UNDEFINED) {
            throw new IllegalStateException(
               "Could not determine size of field " + idFieldId + " which stores the id of a generic data block");
         }

         // Read the field that defines the actual id for the container
         Field<?> idField = readField(idFieldReference, idFieldDesc, actualFieldSize, sequenceNumber, containerContext);

         return concreteBlockIdFromGenericId(id, idField);
      }

      return id;
   }

   private long determineActualFieldSize(DataBlockDescription fieldDesc, long remainingDirectParentByteCount,
      MediumOffset reference, ContainerContext containerContext, int sequenceNumber) {

      long actualBlockSize = DataBlockDescription.UNDEFINED;

      final Character terminationCharacter = fieldDesc.getFieldProperties().getTerminationCharacter();

      // Determine termination bytes from termination character - Note that e.g. for ID3v1 there exists the case that
      // fields are terminated and have a fixed size at the same time
      if (!fieldDesc.hasFixedSize() && terminationCharacter != null) {
         Charset characterEncoding = containerContext.getCharacterEncodingOf(fieldDesc.getId(), sequenceNumber);

         actualBlockSize = getSizeUpToTerminationBytes(reference, characterEncoding, terminationCharacter,
            remainingDirectParentByteCount);
      }

      if (actualBlockSize == DataBlockDescription.UNDEFINED) {
         actualBlockSize = containerContext.getSizeOf(fieldDesc.getId(), sequenceNumber);
      }

      if (actualBlockSize == DataBlockDescription.UNDEFINED) {
         actualBlockSize = remainingDirectParentByteCount;
      }

      return actualBlockSize;
   }

   /**
    * @return the {@link DataBlockFactory}
    */
   protected DataBlockFactory getDataBlockFactory() {

      return m_dataBlockFactory;
   }

   /**
    * @param actualDesc
    * @return
    */
   protected DataBlockDescription getPayloadDescription(DataBlockDescription actualDesc) {
      List<DataBlockDescription> payloadDescs = actualDesc
         .getChildDescriptionsOfType(PhysicalDataBlockType.FIELD_BASED_PAYLOAD);

      payloadDescs.addAll(actualDesc.getChildDescriptionsOfType(PhysicalDataBlockType.CONTAINER_BASED_PAYLOAD));

      if (payloadDescs.size() != 1) {
         throw new IllegalStateException("For container parents, there must be a single data block of type PAYLOAD");
      }

      return payloadDescs.get(0);
   }

   private long getSizeUpToTerminationBytes(MediumOffset reference, Charset charset, Character terminationCharacter,
      long remainingDirectParentByteCount) {
      FieldTerminationFinder finder = new FieldTerminationFinder();

      return finder.getSizeUntilTermination(charset, terminationCharacter,
         mediumDataProvider.createFieldDataProvider(reference), remainingDirectParentByteCount,
         reference.getMedium().getMaxReadWriteBlockSizeInBytes());
   }

   /**
    * @see com.github.jmeta.library.datablocks.api.services.DataBlockReader#getSpecification()
    */
   @Override
   public DataFormatSpecification getSpecification() {

      return m_spec;
   }

   /**
    * @see com.github.jmeta.library.datablocks.api.services.DataBlockReader#hasContainerWithId(MediumOffset,
    *      com.github.jmeta.library.dataformats.api.types.DataBlockId,
    *      com.github.jmeta.library.datablocks.api.types.Payload, long)
    */
   @Override
   public boolean hasContainerWithId(MediumOffset reference, DataBlockId id, Payload parent,
      long remainingDirectParentByteCount) {

      Reject.ifNull(reference, "reference");
      Reject.ifNull(id, "id");

      mediumDataProvider.bufferBeforeRead(reference, remainingDirectParentByteCount);

      DataBlockDescription defaultNestedContainerDescription = getSpecification()
         .getDefaultNestedContainerDescription();

      if (defaultNestedContainerDescription != null && id.equals(defaultNestedContainerDescription.getId())) {
         return true;
      }

      DataBlockDescription containerDesc = m_spec.getDataBlockDescription(id);

      List<MagicKey> magicKeys = getMagicKeys(containerDesc);

      for (int i = 0; i < magicKeys.size(); ++i) {
         MagicKey magicKey = magicKeys.get(i);

         // Does the magic key equal the medium bytes at the given reference?
         int magicKeySizeInBytes = magicKey.getByteLength();

         // This container cannot be stored in the parent, as there are not enough bytes in the parent
         // left for its magic key.
         if (!hasEnoughBytesForMagicKey(reference, magicKey, remainingDirectParentByteCount)) {
            return false;
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
    * @param reference
    * @param magicKey
    * @param remainingDirectParentByteCount
    *           TODO
    * @return
    */
   protected abstract boolean hasEnoughBytesForMagicKey(MediumOffset reference, MagicKey magicKey,
      long remainingDirectParentByteCount);

   /**
    * @param containerDesc
    * @return
    */
   protected abstract List<MagicKey> getMagicKeys(DataBlockDescription containerDesc);

   /**
    * @see com.github.jmeta.library.datablocks.api.services.DataBlockReader#identifiesDataFormat(MediumOffset)
    */
   @Override
   public boolean identifiesDataFormat(MediumOffset reference) {

      List<DataBlockDescription> topLevelContainerDescs = m_spec.getTopLevelDataBlockDescriptions();

      for (int i = 0; i < topLevelContainerDescs.size(); ++i) {
         DataBlockDescription desc = topLevelContainerDescs.get(i);

         if (hasContainerWithId(reference, desc.getId(), null, DataBlockDescription.UNDEFINED)) {
            return true;
         }
      }

      return false;
   }

   /**
    * @see com.github.jmeta.library.datablocks.api.services.DataBlockReader#readBytes(com.github.jmeta.library.media.api.types.MediumOffset,
    *      int)
    */
   @Override
   public ByteBuffer readBytes(MediumOffset reference, int size) {
      return mediumDataProvider.getData(reference, size);
   }

   private Field<?> readField(final MediumOffset reference, DataBlockDescription fieldDesc, long fieldSize,
      int sequenceNumber, ContainerContext containerContext) {

      // A lazy field is created if the field size exceeds a maximum size
      // There is no point in making terminated fields lazy as - anyway - to determine their size, they have to be read
      // anyway.
      if (fieldDesc.getFieldProperties().getTerminationCharacter() == null
         && fieldSize > reference.getMedium().getMaxReadWriteBlockSizeInBytes()) {
         return new LazyField(fieldDesc, reference, null, fieldSize, m_dataBlockFactory, this, sequenceNumber,
            containerContext);
      }

      // This cast is ok as we check for upper bound above
      ByteBuffer fieldBuffer = readBytes(reference, (int) fieldSize);

      return m_dataBlockFactory.createFieldFromBytes(fieldDesc.getId(), m_spec, reference, fieldBuffer, sequenceNumber,
         containerContext);
   }

   /**
    * @see com.github.jmeta.library.datablocks.api.services.DataBlockReader#readFields(MediumOffset,
    *      com.github.jmeta.library.dataformats.api.types.DataBlockId, long, ContainerContext)
    */
   @Override
   public List<Field<?>> readFields(MediumOffset reference, DataBlockId parentId, long remainingDirectParentByteCount,
      ContainerContext containerContext) {

      DataBlockDescription parentDesc = m_spec.getDataBlockDescription(parentId);

      List<DataBlockDescription> fieldChildren = parentDesc.getChildDescriptionsOfType(PhysicalDataBlockType.FIELD);

      List<Field<?>> fields = new ArrayList<>();

      MediumOffset currentFieldReference = reference;

      long currentlyRemainingParentByteCount = remainingDirectParentByteCount;

      for (int i = 0; i < fieldChildren.size(); ++i) {
         mediumDataProvider.bufferBeforeRead(currentFieldReference, currentlyRemainingParentByteCount);

         DataBlockDescription fieldDesc = fieldChildren.get(i);

         long actualOccurrences = containerContext.getOccurrencesOf(fieldDesc.getId());

         for (int j = 0; j < actualOccurrences; j++) {
            long fieldSize = determineActualFieldSize(fieldDesc, currentlyRemainingParentByteCount,
               currentFieldReference, containerContext, j);

            Field<?> newField = readField(currentFieldReference, fieldDesc, fieldSize, j, containerContext);

            containerContext.addFieldFunctions(newField);

            fields.add(newField);

            if (currentlyRemainingParentByteCount != DataBlockDescription.UNDEFINED) {
               currentlyRemainingParentByteCount -= fieldSize;
            }

            currentFieldReference = currentFieldReference.advance(fieldSize);
         }
      }

      if (currentlyRemainingParentByteCount > 0) {
         DataBlockDescription unknownFieldDescription = createUnknownFieldDescription(parentId);

         Field<?> unknownField = readField(currentFieldReference, unknownFieldDescription,
            currentlyRemainingParentByteCount, 0, containerContext);

         fields.add(unknownField);
      }

      return fields;
   }

   @Override
   public List<Header> readHeadersOrFootersWithId(MediumOffset reference, DataBlockId headerOrFooterId,
      DataBlockId parentId, List<Header> previousHeadersOrFooters, boolean isFooter,
      ContainerContext containerContext) {

      Reject.ifNull(previousHeadersOrFooters, "previousHeadersOrFooters");
      Reject.ifNull(headerOrFooterId, "headerOrFooterId");
      Reject.ifNull(reference, "reference");
      Reject.ifFalse(m_spec.specifiesBlockWithId(headerOrFooterId), "m_spec.specifiesBlockWithId(headerOrFooterId)");

      DataBlockDescription desc = m_spec.getDataBlockDescription(headerOrFooterId);

      List<Header> nextHeadersOrFooters = new ArrayList<>();

      // Get the actual occurrences of this headerId based on the fields of the previous
      // headers
      long actualOccurrences = containerContext.getOccurrencesOf(desc.getId());

      // Read all header occurrences
      for (int i = 0; i < actualOccurrences; i++) {
         long headerOrFooterSize = containerContext.getSizeOf(headerOrFooterId, i);

         List<Field<?>> headerFields = readFields(reference, headerOrFooterId, headerOrFooterSize, containerContext);

         nextHeadersOrFooters.add(m_dataBlockFactory.createHeaderOrFooter(headerOrFooterId, reference, headerFields,
            isFooter, this, i, containerContext));
      }

      return nextHeadersOrFooters;
   }

   /**
    * @see com.github.jmeta.library.datablocks.api.services.DataBlockReader#setMediumCache(MediumStore)
    */
   @Override
   public void setMediumCache(MediumStore cache) {

      Reject.ifNull(cache, "cache");

      mediumDataProvider = new MediumDataProvider(cache);
   }

   /**
    * @see com.github.jmeta.library.datablocks.api.services.DataBlockReader#setCustomSizeProvider(com.github.jmeta.library.dataformats.api.types.DataBlockId,
    *      com.github.jmeta.library.datablocks.api.services.SizeProvider)
    */
   @Override
   public void setCustomSizeProvider(SizeProvider sizeProvider) {
      Reject.ifNull(sizeProvider, "sizeProvider");

      customSizeProvider = sizeProvider;
   }

   /**
    * @see com.github.jmeta.library.datablocks.api.services.DataBlockReader#setCustomCountProvider(com.github.jmeta.library.dataformats.api.types.DataBlockId,
    *      com.github.jmeta.library.datablocks.api.services.CountProvider)
    */
   @Override
   public void setCustomCountProvider(CountProvider countProvider) {
      Reject.ifNull(countProvider, "countProvider");

      customCountProvider = countProvider;
   }

   /**
    * Returns the attribute {@link #customSizeProvider}.
    *
    * @return the attribute {@link #customSizeProvider}
    */
   protected SizeProvider getCustomSizeProvider() {
      return customSizeProvider;
   }

   /**
    * Returns the attribute {@link #customCountProvider}.
    *
    * @return the attribute {@link #customCountProvider}
    */
   protected CountProvider getCustomCountProvider() {
      return customCountProvider;
   }

}
