/**
 *
 * {@link StandardDataFormatSpecification}.java
 *
 * @author Jens Ebert
 *
 * @date 31.12.2010
 */

package com.github.jmeta.library.dataformats.impl;

import java.nio.ByteOrder;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.github.jmeta.library.dataformats.api.services.DataFormatSpecification;
import com.github.jmeta.library.dataformats.api.types.ContainerDataFormat;
import com.github.jmeta.library.dataformats.api.types.DataBlockDescription;
import com.github.jmeta.library.dataformats.api.types.DataBlockId;
import com.github.jmeta.library.dataformats.api.types.FieldProperties;
import com.github.jmeta.library.dataformats.api.types.FieldType;
import com.github.jmeta.library.dataformats.api.types.MagicKey;
import com.github.jmeta.library.dataformats.api.types.PhysicalDataBlockType;
import com.github.jmeta.utility.charset.api.services.Charsets;
import com.github.jmeta.utility.dbc.api.services.Reject;

/**
 * {@link StandardDataFormatSpecification}
 *
 */
public class StandardDataFormatSpecification implements DataFormatSpecification {

   private static final String GENERIC_ID_REPLACE_PATTERN = "([^\\.]+)";

   private static final Pattern GENERIC_PLACEHOLDER_PATTERN = Pattern.compile("(\\$\\{.+?\\})");

   /**
    * Creates a new {@link StandardDataFormatSpecification}.
    * 
    * @param dataFormat
    * @param dataBlockDescriptions
    * @param topLevelDataBlockDescriptions
    * @param genericDataBlocks
    * @param supportedByteOrders
    * @param supportedCharacterEncodings
    * @param defaultNestedContainerId
    *           TODO
    */
   public StandardDataFormatSpecification(ContainerDataFormat dataFormat,
      Map<DataBlockId, DataBlockDescription> dataBlockDescriptions,
      List<DataBlockDescription> topLevelDataBlockDescriptions, Set<DataBlockId> genericDataBlocks,
      List<ByteOrder> supportedByteOrders, List<Charset> supportedCharacterEncodings,
      DataBlockId defaultNestedContainerId) {
      Reject.ifNull(dataBlockDescriptions, "dataBlockDescriptions");
      Reject.ifNull(topLevelDataBlockDescriptions, "topLevelDataBlockDescriptions");
      Reject.ifNull(dataFormat, "dataFormat");
      Reject.ifNull(supportedCharacterEncodings, "supportedCharacterEncodings");
      Reject.ifNull(supportedByteOrders, "supportedByteOrders");
      Reject.ifNull(genericDataBlocks, "genericDataBlocks");

      initGenericIdPatterns(genericDataBlocks);

      m_supportedByteOrders.addAll(supportedByteOrders);
      m_supportedCharacterEncodings.addAll(supportedCharacterEncodings);
      m_dataFormat = dataFormat;
      m_topLevelDataBlocks.addAll(topLevelDataBlockDescriptions);
      m_dataBlockDescriptions.putAll(dataBlockDescriptions);
      this.defaultNestedContainerId = defaultNestedContainerId;

      validateDefaultNestedContainerDefined(defaultNestedContainerId);

      autoDetectMagicKeys(m_topLevelDataBlocks);

      validateTopLevelMagicKeys();

      calculateContainerFieldsFixedByteOffsets(m_topLevelDataBlocks);
   }

   @Override
   public DataBlockDescription getDefaultNestedContainerDescription() {

      if (defaultNestedContainerId == null) {
         return null;
      }

      return getDataBlockDescription(defaultNestedContainerId);
   }

   /**
    * @see com.github.jmeta.library.dataformats.api.services.DataFormatSpecification#getDataBlockDescription(com.github.jmeta.library.dataformats.api.types.DataBlockId)
    */
   @Override
   public DataBlockDescription getDataBlockDescription(DataBlockId id) {

      Reject.ifNull(id, "id");
      Reject.ifFalse(specifiesBlockWithId(id), "specifiesBlockWithId(id)");

      // The requested id is an unspecified id that must match a generic id
      if (!m_dataBlockDescriptions.containsKey(id)) {
         return createConcreteDescription(id);
      }

      return m_dataBlockDescriptions.get(id);
   }

   /**
    * @param id
    * @return
    */
   private DataBlockDescription createConcreteDescription(DataBlockId id) {
      DataBlockId matchingGenericId = getMatchingGenericId(id);

      DataBlockDescription genericDescription = getDataBlockDescription(matchingGenericId);

      Matcher matcher = Pattern.compile(m_genericDataBlocks.get(matchingGenericId)).matcher(id.getGlobalId());

      List<String> matchingStrings = new ArrayList<>();

      if (matcher.find()) {
         // Ignore group zero (= the whole match) which is also NOT included in Matcher.groupCount()
         for (int i = 1; i < matcher.groupCount() + 1; i++)
            matchingStrings.add(matcher.group(i));
      }

      // Replace child ids
      List<DataBlockDescription> realChildren = new ArrayList<>();

      for (int i = 0; i < genericDescription.getOrderedChildren().size(); ++i) {
         DataBlockId childId = genericDescription.getOrderedChildren().get(i).getId();

         String replacedChildId = childId.getGlobalId();

         for (int j = 0; j < matchingStrings.size(); ++j) {
            String matchingString = matchingStrings.get(j);

            replacedChildId = replacedChildId.replaceFirst(GENERIC_PLACEHOLDER_PATTERN.pattern(), matchingString);
         }

         DataBlockId replacedChildDataBlockId = new DataBlockId(m_dataFormat, replacedChildId);

         realChildren.add(createConcreteDescription(replacedChildDataBlockId));
      }
      return new DataBlockDescription(id, genericDescription.getName(), "Unspecified data block",
         genericDescription.getPhysicalType(), realChildren, genericDescription.getFieldProperties(),
         genericDescription.getMinimumOccurrences(), genericDescription.getMaximumOccurrences(),
         genericDescription.getMinimumByteLength(), genericDescription.getMaximumByteLength(), false);
   }

   /**
    * @see com.github.jmeta.library.dataformats.api.services.DataFormatSpecification#getDataFormat()
    */
   @Override
   public ContainerDataFormat getDataFormat() {

      return m_dataFormat;
   }

   /**
    * @see com.github.jmeta.library.dataformats.api.services.DataFormatSpecification#getDefaultByteOrder()
    */
   @Override
   public ByteOrder getDefaultByteOrder() {

      return m_supportedByteOrders.get(0);
   }

   /**
    * @see com.github.jmeta.library.dataformats.api.services.DataFormatSpecification#getDefaultCharacterEncoding()
    */
   @Override
   public Charset getDefaultCharacterEncoding() {

      return m_supportedCharacterEncodings.get(0);
   }

   /**
    * @see com.github.jmeta.library.dataformats.api.services.DataFormatSpecification#getSupportedByteOrders()
    */
   @Override
   public List<ByteOrder> getSupportedByteOrders() {

      return Collections.unmodifiableList(m_supportedByteOrders);
   }

   /**
    * @see com.github.jmeta.library.dataformats.api.services.DataFormatSpecification#getSupportedCharacterEncodings()
    */
   @Override
   public List<Charset> getSupportedCharacterEncodings() {

      return Collections.unmodifiableList(m_supportedCharacterEncodings);
   }

   /**
    * @see com.github.jmeta.library.dataformats.api.services.DataFormatSpecification#getTopLevelDataBlockDescriptions()
    */
   @Override
   public List<DataBlockDescription> getTopLevelDataBlockDescriptions() {

      return Collections.unmodifiableList(m_topLevelDataBlocks);
   }

   /**
    * @see com.github.jmeta.library.dataformats.api.services.DataFormatSpecification#specifiesBlockWithId(com.github.jmeta.library.dataformats.api.types.DataBlockId)
    */
   @Override
   public boolean specifiesBlockWithId(DataBlockId id) {

      Reject.ifNull(id, "id");

      if (!m_dataBlockDescriptions.containsKey(id))
         return getMatchingGenericId(id) != null;

      return m_dataBlockDescriptions.containsKey(id);
   }

   @Override
   public DataBlockId getMatchingGenericId(DataBlockId id) {

      if (m_genericDataBlocks.containsKey(id))
         return id;

      for (Iterator<DataBlockId> iterator = m_genericDataBlocks.keySet().iterator(); iterator.hasNext();) {
         DataBlockId nextId = iterator.next();
         String nextPattern = m_genericDataBlocks.get(nextId);

         if (id.getGlobalId().matches(nextPattern))
            return nextId;
      }

      return null;
   }

   private void validateTopLevelMagicKeys() {
      for (Iterator<DataBlockDescription> iterator = m_topLevelDataBlocks.iterator(); iterator.hasNext();) {
         DataBlockDescription dataBlockDesc = iterator.next();

         if (dataBlockDesc.getHeaderMagicKeys().isEmpty()) {
            throw new IllegalArgumentException("Every lop-level container must define at least one magic key");
         }
      }
   }

   private void validateDefaultNestedContainerDefined(DataBlockId defaultNestedContainerId) {
      for (Iterator<DataBlockDescription> iterator = m_topLevelDataBlocks.iterator(); iterator.hasNext();) {
         DataBlockDescription dataBlockId = iterator.next();

         List<DataBlockDescription> descs = dataBlockId
            .getChildDescriptionsOfType(PhysicalDataBlockType.CONTAINER_BASED_PAYLOAD);

         if (descs.size() > 0) {
            if (defaultNestedContainerId == null) {
               throw new IllegalArgumentException(
                  "Each data format with nested containers must define a default nested container");
            }
         }
      }
   }

   private void initGenericIdPatterns(Set<DataBlockId> genericDataBlocks) {

      for (Iterator<DataBlockId> iterator = genericDataBlocks.iterator(); iterator.hasNext();) {
         DataBlockId genericBlockId = iterator.next();

         final String idString = genericBlockId.getGlobalId();
         Matcher matcher = GENERIC_PLACEHOLDER_PATTERN.matcher(idString);

         StringBuffer idPattern = new StringBuffer();

         int matchEndIndex = 0;

         while (matcher.find()) {
            int matchStartIndex = matcher.start(0);
            matchEndIndex = matcher.end(0);

            idPattern.append(idString.substring(0, matchStartIndex));
            idPattern.append(GENERIC_ID_REPLACE_PATTERN);
         }

         idPattern.append(idString.substring(matchEndIndex, idString.length()));

         m_genericDataBlocks.put(genericBlockId, idPattern.toString());
      }
   }

   private void autoDetectMagicKeys(List<DataBlockDescription> containerDescs) {
      for (DataBlockDescription containerDescription : containerDescs) {
         List<MagicKey> headerMagicKeys = getMagicKeysOfContainer(containerDescription, PhysicalDataBlockType.HEADER);
         headerMagicKeys.forEach(key -> containerDescription.addHeaderMagicKey(key));
         List<MagicKey> footerMagicKeys = getMagicKeysOfContainer(containerDescription, PhysicalDataBlockType.FOOTER);
         footerMagicKeys.forEach(key -> containerDescription.addFooterMagicKey(key));

         List<DataBlockDescription> payloadDescs = containerDescription
            .getChildDescriptionsOfType(PhysicalDataBlockType.CONTAINER_BASED_PAYLOAD);

         if (payloadDescs.size() == 1) {
            List<DataBlockDescription> childContainerDescriptions = payloadDescs.get(0)
               .getChildDescriptionsOfType(PhysicalDataBlockType.CONTAINER);

            autoDetectMagicKeys(childContainerDescriptions);
         }
      }
   }

   private void calculateContainerFieldsFixedByteOffsets(List<DataBlockDescription> containerDescs) {
      for (DataBlockDescription containerDescription : containerDescs) {
         List<DataBlockDescription> parentsWithFields = new ArrayList<>();

         parentsWithFields.addAll(containerDescription.getChildDescriptionsOfType(PhysicalDataBlockType.HEADER));
         parentsWithFields.addAll(containerDescription.getChildDescriptionsOfType(PhysicalDataBlockType.FOOTER));
         parentsWithFields
            .addAll(containerDescription.getChildDescriptionsOfType(PhysicalDataBlockType.FIELD_BASED_PAYLOAD));

         setFieldsFixedByteOffsets(parentsWithFields);

         List<DataBlockDescription> payloadDescs = containerDescription
            .getChildDescriptionsOfType(PhysicalDataBlockType.CONTAINER_BASED_PAYLOAD);

         if (payloadDescs.size() == 1) {
            List<DataBlockDescription> childContainerDescriptions = payloadDescs.get(0)
               .getChildDescriptionsOfType(PhysicalDataBlockType.CONTAINER);

            calculateContainerFieldsFixedByteOffsets(childContainerDescriptions);
         }
      }
   }

   /**
    * @param parentsWithFields
    */
   private void setFieldsFixedByteOffsets(List<DataBlockDescription> parentsWithFields) {
      long currentOffset = 0;

      for (DataBlockDescription parentDesc : parentsWithFields) {
         List<DataBlockDescription> fieldDescs = parentDesc.getChildDescriptionsOfType(PhysicalDataBlockType.FIELD);

         for (DataBlockDescription fieldDesc : fieldDescs) {
            fieldDesc.setFixedByteOffsetInContainer(currentOffset);

            if (fieldDesc.hasFixedSize()) {
               currentOffset += fieldDesc.getMaximumByteLength();
            } else {
               currentOffset = DataBlockDescription.UNKNOWN_OFFSET;
            }
         }
      }
   }

   /**
    * @param containerDescription
    * @param header
    *           TODO
    */
   private List<MagicKey> getMagicKeysOfContainer(DataBlockDescription containerDescription,
      PhysicalDataBlockType type) {
      List<MagicKey> magicKeys = new ArrayList<>();

      List<DataBlockDescription> headerOrFooterDescs = containerDescription.getChildDescriptionsOfType(type);

      List<DataBlockId> variableSizeFieldIds = new ArrayList<>();

      long magicKeyOffset = 0;

      if (type == PhysicalDataBlockType.FOOTER) {
         Collections.reverse(headerOrFooterDescs);
      }

      for (DataBlockDescription headerOrFooterDesc : headerOrFooterDescs) {
         List<DataBlockDescription> fieldDescs = headerOrFooterDesc
            .getChildDescriptionsOfType(PhysicalDataBlockType.FIELD);

         if (type == PhysicalDataBlockType.FOOTER) {
            Collections.reverse(fieldDescs);
         }

         for (DataBlockDescription fieldDesc : fieldDescs) {

            if (!fieldDesc.hasFixedSize()) {
               variableSizeFieldIds.add(fieldDesc.getId());
            }

            if (fieldDesc.getFieldProperties().isMagicKey()) {
               if (!variableSizeFieldIds.isEmpty()) {
                  throw new IllegalArgumentException(
                     "Found variable size fields in front of or behind magic key field with id <" + fieldDesc.getId()
                        + ">: " + variableSizeFieldIds);
               }

               if (type == PhysicalDataBlockType.FOOTER) {
                  magicKeyOffset -= fieldDesc.getMinimumByteLength();
               }

               magicKeys.addAll(determineFieldMagicKeys(magicKeyOffset, fieldDesc));
            } else {
               if (type == PhysicalDataBlockType.HEADER) {
                  magicKeyOffset += fieldDesc.getMinimumByteLength();
               } else {
                  magicKeyOffset -= fieldDesc.getMinimumByteLength();
               }
            }
         }
      }

      Set<Long> distinctMagicKeyOffsets = magicKeys.stream().map(key -> key.getDeltaOffset())
         .collect(Collectors.toSet());

      if (distinctMagicKeyOffsets.size() > 1) {
         throw new IllegalArgumentException("Multiple <" + type
            + "> magic keys at different offsets specified for container id <" + containerDescription.getId()
            + ">. At max one header or footer magic key field is allowed. Magic key fields found: "
            + magicKeys.stream().map(key -> key.getFieldId()).collect(Collectors.toList()));
      }

      return magicKeys;
   }

   private List<MagicKey> determineFieldMagicKeys(long magicKeyOffset, DataBlockDescription fieldDesc) {
      List<MagicKey> fieldMagicKeys = new ArrayList<>();

      FieldProperties<?> fieldProperties = fieldDesc.getFieldProperties();

      if (!fieldProperties.getEnumeratedValues().isEmpty()) {
         // TODO implement
      } else if (fieldProperties.getDefaultValue() != null) {
         byte[] magicKeyBytes = null;

         if (fieldProperties.getFieldType() == FieldType.STRING) {
            magicKeyBytes = ((String) fieldProperties.getDefaultValue()).getBytes(Charsets.CHARSET_ASCII);
         } else if (fieldProperties.getFieldType() == FieldType.BINARY) {
            magicKeyBytes = (byte[]) fieldProperties.getDefaultValue();
         } else if (fieldProperties.getFieldType() == FieldType.FLAGS) {
            magicKeyBytes = fieldProperties.getFlagSpecification().getDefaultFlagBytes();
         } else {
            // Note that validation is already done in DataBlockDescription so this should never be thrown
            throw new IllegalStateException("Invalid magic key field type");
         }

         if (fieldProperties.getMagicKeyBitLength() != null) {
            if (fieldProperties.getMagicKeyBitLength() > magicKeyBytes.length * Byte.SIZE) {
               throw new IllegalArgumentException("Magic key bit length <" + fieldProperties.getMagicKeyBitLength()
                  + "> specified for magic key field <" + fieldDesc.getId() + "> exceeds its binary value length of <"
                  + (magicKeyBytes.length * Byte.SIZE) + "> bits");
            }

            int actualMagicKeyByteLength = fieldProperties.getMagicKeyBitLength() / Byte.SIZE
               + (fieldProperties.getMagicKeyBitLength() % Byte.SIZE > 0 ? 1 : 0);

            byte[] adaptedMagicKeyBytes = magicKeyBytes;

            if (actualMagicKeyByteLength < magicKeyBytes.length) {
               adaptedMagicKeyBytes = new byte[actualMagicKeyByteLength];

               System.arraycopy(magicKeyBytes, 0, adaptedMagicKeyBytes, 0, actualMagicKeyByteLength);
            }

            fieldMagicKeys.add(new MagicKey(adaptedMagicKeyBytes, fieldProperties.getMagicKeyBitLength(),
               fieldDesc.getId(), magicKeyOffset));
         } else {
            fieldMagicKeys.add(new MagicKey(magicKeyBytes, fieldDesc.getId(), magicKeyOffset));
         }

      } else {
         // Note that validation is already done in DataBlockDescription so this should never be thrown
         throw new IllegalStateException("Invalid magic key field");
      }

      return fieldMagicKeys;
   }

   private final DataBlockId defaultNestedContainerId;

   private final Map<DataBlockId, String> m_genericDataBlocks = new HashMap<>();

   private final List<ByteOrder> m_supportedByteOrders = new ArrayList<>();

   private final List<Charset> m_supportedCharacterEncodings = new ArrayList<>();

   private final ContainerDataFormat m_dataFormat;

   private final List<DataBlockDescription> m_topLevelDataBlocks = new ArrayList<>();

   private final Map<DataBlockId, DataBlockDescription> m_dataBlockDescriptions = new HashMap<>();
}
