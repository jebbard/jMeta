/**
 * {@link DataBlockDescription}.java
 *
 * @author Jens Ebert
 * @date 31.12.10 19:47:06 (December 31, 2010)
 */

package com.github.jmeta.library.dataformats.api.types;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import com.github.jmeta.library.datablocks.api.types.DataBlock;
import com.github.jmeta.library.dataformats.api.services.DataFormatSpecification;
import com.github.jmeta.utility.dbc.api.services.Reject;

/**
 *
 */
public class DataBlockDescription {

   public static final long UNLIMITED = Long.MAX_VALUE;

   /**
    * States that the total size of an {@link DataBlock} is unknown. This is a possible return value of the method
    * {@link DataBlock#getTotalSize()}.
    */
   public final static long UNKNOWN_SIZE = -1;

   public final static long UNKNOWN_OFFSET = -1;

   private final DataBlockId id;
   private final String name;
   private final String description;
   private final PhysicalDataBlockType physicalType;
   private final List<DataBlockDescription> orderedChildren = new ArrayList<>();
   private final FieldProperties<?> fieldProperties;
   private final long maximumByteLength;
   private final long minimumByteLength;
   private final int minimumOccurrences;
   private final int maximumOccurrences;
   private final boolean isGeneric;

   private long fixedByteOffsetInContainer;
   private final List<MagicKey> headerMagicKeys = new ArrayList<>();
   private final List<MagicKey> footerMagicKeys = new ArrayList<>();

   /**
    * Creates a new {@link DataBlockDescription}.
    * 
    * @param id
    *           The {@link DataBlockId} of the data block, must not be null
    * @param name
    *           The human-readable name of the data block from its specification, must not be null
    * @param description
    *           The specification description of the data block, must not be null
    * @param physicalType
    *           The {@link PhysicalDataBlockType} of the data block, must not be null
    * @param children
    *           The ordered list of child ids (or empty, if no children) as they would appear in this data block, must
    *           not be null
    * @param fieldProperties
    *           The {@link FieldProperties} of the data block, must be non-null if this {@link PhysicalDataBlockType} is
    *           {@link PhysicalDataBlockType#FIELD}
    * @param minimumOccurrences
    *           The minimum number of occurrences of this data block or {@link DataBlockDescription#UNLIMITED}, must be
    *           smaller than or equal to the maximum number of occurrences
    * @param maximumOccurrences
    *           The maximum number of occurrences of this data block or {@link DataBlockDescription#UNLIMITED}, must be
    *           bigger than or equal to the minimum number of occurrences
    * @param minimumByteLength
    *           The minimum length of this data block in bytes or {@link DataBlockDescription#UNKNOWN_SIZE} or
    *           {@link DataBlockDescription#UNLIMITED}, must be smaller than or equal to the maximum byte length
    * @param maximumByteLength
    *           The maximum length of this data block in bytes or {@link DataBlockDescription#UNKNOWN_SIZE} or
    *           {@link DataBlockDescription#UNLIMITED}, must be bigger than or equal to the minimum byte length
    * @param isGeneric
    *           true if instances of this data block generic, false otherwise
    */
   public DataBlockDescription(DataBlockId id, String name, String description, PhysicalDataBlockType physicalType,
      List<DataBlockDescription> children, FieldProperties<?> fieldProperties, int minimumOccurrences,
      int maximumOccurrences, long minimumByteLength, long maximumByteLength, boolean isGeneric) {
      this.id = id;
      this.name = name;
      this.description = description;
      this.physicalType = physicalType;
      this.orderedChildren.addAll(children);
      this.fieldProperties = fieldProperties;
      this.minimumOccurrences = minimumOccurrences;
      this.maximumOccurrences = maximumOccurrences;
      this.minimumByteLength = minimumByteLength;
      this.maximumByteLength = maximumByteLength;
      this.isGeneric = isGeneric;

      validateDataBlockDescription();
   }

   /**
    * @param spec
    * @param headerDesc
    * @return the total minimum size of the data block
    */
   public static long getTotalMinimumSize(DataFormatSpecification spec, DataBlockDescription headerDesc) {

      Reject.ifNull(headerDesc, "headerDesc");
      Reject.ifNull(spec, "spec");

      long totalMinimumSize = 0;

      for (Iterator<DataBlockDescription> childIterator = headerDesc.getOrderedChildren().iterator(); childIterator
         .hasNext();) {
         DataBlockDescription childDesc = childIterator.next();

         totalMinimumSize += childDesc.getMinimumByteLength();
      }

      return totalMinimumSize;
   }

   public List<DataBlockDescription> getChildDescriptionsOfType(PhysicalDataBlockType type) {
      Reject.ifNull(type, "type");

      return getOrderedChildren().stream().filter(desc -> desc.getPhysicalType() == type).collect(Collectors.toList());
   }

   /**
    * @return the {@link DataBlockId}
    */
   public DataBlockId getId() {

      return id;
   }

   /**
    * @return the name
    */
   public String getName() {

      return name;
   }

   /**
    * @return the specification description
    */
   public String getDescription() {

      return description;
   }

   public int getMinimumOccurrences() {
      return minimumOccurrences;
   }

   public int getMaximumOccurrences() {
      return maximumOccurrences;
   }

   public long getFixedByteOffsetInContainer() {
      return fixedByteOffsetInContainer;
   }

   public boolean hasFixedSize() {
      return getMaximumByteLength() == getMinimumByteLength();
   }

   public void setFixedByteOffsetInContainer(long fixedByteOffsetInContainer) {
      this.fixedByteOffsetInContainer = fixedByteOffsetInContainer;
   }

   public void addHeaderMagicKey(MagicKey magicKey) {
      Reject.ifNull(magicKey, "magicKey");

      headerMagicKeys.add(magicKey);
   }

   public void addFooterMagicKey(MagicKey magicKey) {
      Reject.ifNull(magicKey, "magicKey");

      footerMagicKeys.add(magicKey);
   }

   public boolean isGeneric() {
      return isGeneric;
   }

   /**
    * @return the {@link PhysicalDataBlockType}
    */
   public PhysicalDataBlockType getPhysicalType() {

      return physicalType;
   }

   /**
    * @return the child {@link DataBlockId}s
    */
   public List<DataBlockDescription> getOrderedChildren() {

      return Collections.unmodifiableList(orderedChildren);
   }

   /**
    * @return the {@link FieldProperties}
    */
   public FieldProperties<?> getFieldProperties() {

      return fieldProperties;
   }

   /**
    * Returns magicKey
    *
    * @return magicKey
    */
   public List<MagicKey> getHeaderMagicKeys() {

      return Collections.unmodifiableList(headerMagicKeys);
   }

   public List<MagicKey> getFooterMagicKeys() {
      return footerMagicKeys;
   }

   /**
    * Returns maximumByteLength
    *
    * @return maximumByteLength
    */
   public long getMaximumByteLength() {

      return maximumByteLength;
   }

   /**
    * Returns minimumByteLength
    *
    * @return minimumByteLength
    */
   public long getMinimumByteLength() {

      return minimumByteLength;
   }

   /**
    * @see java.lang.Object#hashCode()
    */
   @Override
   public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + ((description == null) ? 0 : description.hashCode());
      result = prime * result + ((fieldProperties == null) ? 0 : fieldProperties.hashCode());
      result = prime * result + (int) (fixedByteOffsetInContainer ^ (fixedByteOffsetInContainer >>> 32));
      result = prime * result + ((footerMagicKeys == null) ? 0 : footerMagicKeys.hashCode());
      result = prime * result + ((headerMagicKeys == null) ? 0 : headerMagicKeys.hashCode());
      result = prime * result + ((id == null) ? 0 : id.hashCode());
      result = prime * result + (isGeneric ? 1231 : 1237);
      result = prime * result + (int) (maximumByteLength ^ (maximumByteLength >>> 32));
      result = prime * result + maximumOccurrences;
      result = prime * result + (int) (minimumByteLength ^ (minimumByteLength >>> 32));
      result = prime * result + minimumOccurrences;
      result = prime * result + ((name == null) ? 0 : name.hashCode());
      result = prime * result + ((orderedChildren == null) ? 0 : orderedChildren.hashCode());
      result = prime * result + ((physicalType == null) ? 0 : physicalType.hashCode());
      return result;
   }

   public boolean hasChildWithLocalId(String localId) {

      for (DataBlockDescription dataBlockDescription : orderedChildren) {
         if (dataBlockDescription.getId().getLocalId().equals(localId)) {
            return true;
         }
      }

      return false;
   }

   /**
    * @see java.lang.Object#equals(java.lang.Object)
    */
   @Override
   public boolean equals(Object obj) {
      if (this == obj)
         return true;
      if (obj == null)
         return false;
      if (getClass() != obj.getClass())
         return false;
      DataBlockDescription other = (DataBlockDescription) obj;
      if (description == null) {
         if (other.description != null)
            return false;
      } else if (!description.equals(other.description))
         return false;
      if (fieldProperties == null) {
         if (other.fieldProperties != null)
            return false;
      } else if (!fieldProperties.equals(other.fieldProperties))
         return false;
      if (fixedByteOffsetInContainer != other.fixedByteOffsetInContainer)
         return false;
      if (footerMagicKeys == null) {
         if (other.footerMagicKeys != null)
            return false;
      } else if (!footerMagicKeys.equals(other.footerMagicKeys))
         return false;
      if (headerMagicKeys == null) {
         if (other.headerMagicKeys != null)
            return false;
      } else if (!headerMagicKeys.equals(other.headerMagicKeys))
         return false;
      if (id == null) {
         if (other.id != null)
            return false;
      } else if (!id.equals(other.id))
         return false;
      if (isGeneric != other.isGeneric)
         return false;
      if (maximumByteLength != other.maximumByteLength)
         return false;
      if (maximumOccurrences != other.maximumOccurrences)
         return false;
      if (minimumByteLength != other.minimumByteLength)
         return false;
      if (minimumOccurrences != other.minimumOccurrences)
         return false;
      if (name == null) {
         if (other.name != null)
            return false;
      } else if (!name.equals(other.name))
         return false;
      if (orderedChildren == null) {
         if (other.orderedChildren != null)
            return false;
      } else if (!orderedChildren.equals(other.orderedChildren))
         return false;
      if (physicalType != other.physicalType)
         return false;
      return true;
   }

   /**
    * @see java.lang.Object#toString()
    */
   @Override
   public String toString() {
      return "DataBlockDescription [id=" + id + ", name=" + name + ", description=" + description + ", physicalType="
         + physicalType + ", orderedChildren=" + orderedChildren + ", fieldProperties=" + fieldProperties
         + ", maximumByteLength=" + maximumByteLength + ", minimumByteLength=" + minimumByteLength
         + ", minimumOccurrences=" + minimumOccurrences + ", maximumOccurrences=" + maximumOccurrences + ", isGeneric="
         + isGeneric + ", fixedByteOffsetInContainer=" + fixedByteOffsetInContainer + ", headerMagicKeys="
         + headerMagicKeys + ", footerMagicKeys=" + footerMagicKeys + "]";
   }

   private void validateDataBlockDescription() {
      // Validate that mandatory fields are present
      Reject.ifNull(id, "id");
      Reject.ifNull(name, "name");
      Reject.ifNull(description, "description");
      Reject.ifNull(physicalType, "physicalType");
      Reject.ifNull(orderedChildren, "childIds");

      String messagePrefix = "Error validating [" + id + "]: ";

      // Validate lengths
      Reject.ifNegative(minimumByteLength, "minimumByteLength");
      Reject.ifNegative(maximumByteLength, "maximumByteLength");
      Reject.ifFalse(minimumByteLength <= maximumByteLength, "minimumByteLength <= maximumByteLength");

      // Validate occurrences
      Reject.ifFalse(minimumOccurrences <= maximumOccurrences, "minimumOccurrences <= maximumOccurrences");

      // Validate children
      switch (physicalType) {
         case FIELD:
            if (orderedChildren.size() != 0) {
               throw new IllegalArgumentException(messagePrefix + "Data block is typed as field, but it has children");
            }
         break;
         case HEADER:
         case FOOTER:
         case FIELD_BASED_PAYLOAD:
            List<DataBlockDescription> fieldChildren = getChildDescriptionsOfType(PhysicalDataBlockType.FIELD);

            if (fieldChildren.size() != orderedChildren.size()) {
               throw new IllegalArgumentException(messagePrefix
                  + "Data blocks of types HEADER, FOOTER or FIELD_BASED_PAYLOAD must only have fields as children");
            }
         break;
         case CONTAINER:
            List<DataBlockDescription> payloadChildren = getChildDescriptionsOfType(
               PhysicalDataBlockType.CONTAINER_BASED_PAYLOAD);
            payloadChildren.addAll(getChildDescriptionsOfType(PhysicalDataBlockType.FIELD_BASED_PAYLOAD));

            if (payloadChildren.size() != 1) {
               throw new IllegalArgumentException(
                  messagePrefix + "Data blocks of type CONTAINER must have exactly one payload");
            }
         break;
         case CONTAINER_BASED_PAYLOAD:
            List<DataBlockDescription> containerChildren = getChildDescriptionsOfType(PhysicalDataBlockType.CONTAINER);

            if (containerChildren.size() != orderedChildren.size()) {
               throw new IllegalArgumentException(
                  messagePrefix + "Data blocks of type CONTAINER_BASED_PAYLOAD must only have CONTAINERs as children");
            }
         break;
      }

      // Validate field properties
      if (physicalType == PhysicalDataBlockType.FIELD && fieldProperties == null) {
         throw new IllegalArgumentException(
            messagePrefix + "Data block is typed as FIELD, but its field properties are null");
      } else if (physicalType != PhysicalDataBlockType.FIELD && fieldProperties != null) {
         throw new IllegalArgumentException(
            messagePrefix + "Data block not typed as FIELD, but it has non-null field properties");
      }

      if (physicalType == PhysicalDataBlockType.FIELD) {
         validateFieldProperties(messagePrefix, getFieldProperties());
      }
   }

   /**
    * @param messagePrefix
    */
   private void validateFieldProperties(String messagePrefix, FieldProperties<?> fieldProperties) {
      // Validate magic key
      if (fieldProperties.isMagicKey()) {
         if (physicalType != PhysicalDataBlockType.FIELD) {
            throw new IllegalArgumentException(messagePrefix + "Data block is tagged as magic key, but it is no field");
         }

         if (!hasFixedSize() || maximumByteLength == DataBlockDescription.UNKNOWN_SIZE) {
            throw new IllegalArgumentException(
               messagePrefix + "Field is tagged as magic key, but it has a variable size: min length = <"
                  + minimumByteLength + ">, max length = <" + maximumByteLength + ">");
         }

         if (fieldProperties.getFieldType() == FieldType.FLAGS) {
            throw new IllegalArgumentException(messagePrefix
               + "Data block is tagged as magic key, and it must have field type BINARY, STRING, UNSIGNED_WHOLE_NUMER or ENUMERATED, but it was <"
               + fieldProperties.getFieldType() + ">");
         }

         // TODO fixed length == key length
      }

      // Validate enumerated fields
      if (fieldProperties.getFieldType() == FieldType.ENUMERATED) {
         if (fieldProperties.getEnumeratedValues() == null || fieldProperties.getEnumeratedValues().isEmpty()) {
            throw new IllegalArgumentException(
               messagePrefix + "Enumerated field does not define any enumerated values");
         }

         for (Iterator<?> enumValueIterator = fieldProperties.getEnumeratedValues().keySet()
            .iterator(); enumValueIterator.hasNext();) {
            Object nextKey = enumValueIterator.next();
            byte[] nextValue = fieldProperties.getEnumeratedValues().get(nextKey);

            if (hasFixedSize() && nextValue.length > minimumByteLength) {
               throw new IllegalArgumentException(messagePrefix + "Binary representation of enmuerated value <"
                  + nextKey + "> with length <" + nextValue.length
                  + "> is longer than the field's fixed size which is <" + minimumByteLength + ">");
            }
         }

         if (fieldProperties.getDefaultValue() != null) {
            if (!fieldProperties.getEnumeratedValues().containsKey(fieldProperties.getDefaultValue())) {
               throw new IllegalArgumentException(
                  messagePrefix + "Default field value <" + fieldProperties.getDefaultValue()
                     + "> must be contained in list of enumerated values, but it is not");
            }
         }
      }

      // Validate numeric fields
      if (fieldProperties.getFieldType() == FieldType.UNSIGNED_WHOLE_NUMBER) {
         if (minimumByteLength > Long.BYTES || maximumByteLength > Long.BYTES) {
            throw new IllegalArgumentException(
               messagePrefix + "Field has Numeric type, but its minimum or maximum length is bigger than " + Long.BYTES
                  + ": min length = <" + minimumByteLength + ">, max length = <" + maximumByteLength + ">");
         }
      } else {
         if (fieldProperties.getFixedByteOrder() != null) {
            throw new IllegalArgumentException(
               messagePrefix + "Field has not Numeric type, but a fixed byte order is defined for it");
         }
      }

      // Validate string fields
      if (fieldProperties.getFieldType() != FieldType.STRING) {
         if (fieldProperties.getFixedCharacterEncoding() != null) {
            throw new IllegalArgumentException(
               messagePrefix + "Field has not String type, but a fixed character encoding is defined for it");
         }

         if (fieldProperties.getTerminationCharacter() != null) {
            throw new IllegalArgumentException(
               messagePrefix + "Field has not String type, but a termincation character is defined for it");
         }
      }

      // Validate default value
      // TODO default value length == fixed length
   }
}
