/**
 * {@link DataBlockDescription}.java
 *
 * @author Jens Ebert
 * @date 31.12.10 19:47:06 (December 31, 2010)
 */

package com.github.jmeta.library.dataformats.api.types;

import static com.github.jmeta.library.dataformats.api.exceptions.InvalidSpecificationException.VLD_FIELD_PROPERTIES_MISSING;
import static com.github.jmeta.library.dataformats.api.exceptions.InvalidSpecificationException.VLD_FIELD_PROPERTIES_UNNECESSARY;
import static com.github.jmeta.library.dataformats.api.exceptions.InvalidSpecificationException.VLD_INVALID_CHILDREN_CONTAINER;
import static com.github.jmeta.library.dataformats.api.exceptions.InvalidSpecificationException.VLD_INVALID_CHILDREN_CONTAINER_BASED_PAYLOAD;
import static com.github.jmeta.library.dataformats.api.exceptions.InvalidSpecificationException.VLD_INVALID_CHILDREN_FIELD;
import static com.github.jmeta.library.dataformats.api.exceptions.InvalidSpecificationException.VLD_INVALID_CHILDREN_FIELD_SEQUENCE;
import static com.github.jmeta.library.dataformats.api.exceptions.InvalidSpecificationException.VLD_MAGIC_KEY_TOO_MANY;
import static com.github.jmeta.library.dataformats.api.exceptions.InvalidSpecificationException.VLD_MAGIC_KEY_UNKNOWN_OFFSET;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.github.jmeta.library.datablocks.api.types.DataBlock;
import com.github.jmeta.library.dataformats.api.exceptions.InvalidSpecificationException;
import com.github.jmeta.library.dataformats.api.services.DataFormatSpecification;
import com.github.jmeta.utility.dbc.api.services.Reject;

/**
 *
 */
public class DataBlockDescription {

   /**
    * States that the total size of an {@link DataBlock} is unknown. This is a possible return value of the method
    * {@link DataBlock#getTotalSize()}.
    */
   public static final long UNDEFINED = Long.MIN_VALUE;

   private final DataBlockId id;
   private final String name;
   private final String description;
   private final PhysicalDataBlockType physicalType;
   private final List<DataBlockDescription> orderedChildren = new ArrayList<>();
   private final FieldProperties<?> fieldProperties;
   private long maximumByteLength;
   private long minimumByteLength;
   private final long minimumOccurrences;
   private final long maximumOccurrences;
   private final boolean isGeneric;

   private long byteOffsetFromStartOfContainer = UNDEFINED;
   private long byteOffsetFromEndOfContainer = UNDEFINED;

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
    *           The minimum number of occurrences of this data block or {@link DataBlockDescription#UNDEFINED}, must be
    *           smaller than or equal to the maximum number of occurrences
    * @param maximumOccurrences
    *           The maximum number of occurrences of this data block or {@link DataBlockDescription#UNDEFINED}, must be
    *           bigger than or equal to the minimum number of occurrences
    * @param minimumByteLength
    *           The minimum length of this data block in bytes or {@link DataBlockDescription#UNDEFINED}, must be
    *           smaller than or equal to the maximum byte length
    * @param maximumByteLength
    *           The maximum length of this data block in bytes or {@link DataBlockDescription#UNDEFINED}, must be bigger
    *           than or equal to the minimum byte length
    * @param isGeneric
    *           true if instances of this data block generic, false otherwise
    */
   public DataBlockDescription(DataBlockId id, String name, String description, PhysicalDataBlockType physicalType,
      List<DataBlockDescription> children, FieldProperties<?> fieldProperties, long minimumOccurrences,
      long maximumOccurrences, long minimumByteLength, long maximumByteLength, boolean isGeneric) {

      Reject.ifNull(id, "id");
      Reject.ifNull(name, "name");
      Reject.ifNull(description, "description");
      Reject.ifNull(physicalType, "physicalType");
      Reject.ifNull(children, "children");

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

      // (A) Stand-alone validation of all fields
      validateDataBlockDescription();

      // (B) Derive properties
      if (physicalType != PhysicalDataBlockType.FIELD
         && physicalType != PhysicalDataBlockType.CONTAINER_BASED_PAYLOAD) {
         // Note that fields do not have children, while a container-based payload by definition allows the occurrence
         // or absence of any of its child containers, thus calculating its size from its defined children would be
         // wrong
         determineAndValidateLengthsFromChildren();
      }

      if (physicalType == PhysicalDataBlockType.CONTAINER) {
         determineFixedByteOffsetsFromStartOfContainerForAllFields();
         determineFixedByteOffsetsFromEndOfContainerForAllFields();
         autoDetectMagicKeys();
      }
   }

   /**
    * Returns the attribute {@link #byteOffsetFromEndOfContainer}.
    * 
    * @return the attribute {@link #byteOffsetFromEndOfContainer}
    */
   public long getByteOffsetFromEndOfContainer() {
      return byteOffsetFromEndOfContainer;
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

   public long getMinimumOccurrences() {
      return minimumOccurrences;
   }

   public long getMaximumOccurrences() {
      return maximumOccurrences;
   }

   public long getByteOffsetFromStartOfContainer() {
      return byteOffsetFromStartOfContainer;
   }

   public boolean hasFixedSize() {
      return getMaximumByteLength() != UNDEFINED && getMaximumByteLength() == getMinimumByteLength();
   }

   public boolean hasChildWithLocalId(String localId) {

      for (DataBlockDescription dataBlockDescription : orderedChildren) {
         if (dataBlockDescription.getId().getLocalId().equals(localId)) {
            return true;
         }
      }

      return false;
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
      result = prime * result + (int) (byteOffsetFromEndOfContainer ^ (byteOffsetFromEndOfContainer >>> 32));
      result = prime * result + (int) (byteOffsetFromStartOfContainer ^ (byteOffsetFromStartOfContainer >>> 32));
      result = prime * result + ((description == null) ? 0 : description.hashCode());
      result = prime * result + ((fieldProperties == null) ? 0 : fieldProperties.hashCode());
      result = prime * result + ((footerMagicKeys == null) ? 0 : footerMagicKeys.hashCode());
      result = prime * result + ((headerMagicKeys == null) ? 0 : headerMagicKeys.hashCode());
      result = prime * result + ((id == null) ? 0 : id.hashCode());
      result = prime * result + (isGeneric ? 1231 : 1237);
      result = prime * result + (int) (maximumByteLength ^ (maximumByteLength >>> 32));
      result = prime * result + (int) (maximumOccurrences ^ (maximumOccurrences >>> 32));
      result = prime * result + (int) (minimumByteLength ^ (minimumByteLength >>> 32));
      result = prime * result + (int) (minimumOccurrences ^ (minimumOccurrences >>> 32));
      result = prime * result + ((name == null) ? 0 : name.hashCode());
      result = prime * result + ((orderedChildren == null) ? 0 : orderedChildren.hashCode());
      result = prime * result + ((physicalType == null) ? 0 : physicalType.hashCode());
      return result;
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
      if (byteOffsetFromEndOfContainer != other.byteOffsetFromEndOfContainer)
         return false;
      if (byteOffsetFromStartOfContainer != other.byteOffsetFromStartOfContainer)
         return false;
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
         + isGeneric + ", fixedByteOffsetInContainer=" + byteOffsetFromStartOfContainer + ", headerMagicKeys="
         + headerMagicKeys + ", footerMagicKeys=" + footerMagicKeys + "]";
   }

   public void validateChildren() {
      switch (physicalType) {
         case FIELD:
            if (orderedChildren.size() != 0) {
               throw new InvalidSpecificationException(VLD_INVALID_CHILDREN_FIELD, this);
            }
         break;
         case HEADER:
         case FOOTER:
         case FIELD_BASED_PAYLOAD:
            List<DataBlockDescription> fieldChildren = getChildDescriptionsOfType(PhysicalDataBlockType.FIELD);

            if (fieldChildren.size() != orderedChildren.size()) {
               throw new InvalidSpecificationException(VLD_INVALID_CHILDREN_FIELD_SEQUENCE, this);
            }
         break;
         case CONTAINER:
            List<DataBlockDescription> payloadChildren = getChildDescriptionsOfType(
               PhysicalDataBlockType.CONTAINER_BASED_PAYLOAD);
            payloadChildren.addAll(getChildDescriptionsOfType(PhysicalDataBlockType.FIELD_BASED_PAYLOAD));

            if (payloadChildren.size() != 1) {
               throw new InvalidSpecificationException(VLD_INVALID_CHILDREN_CONTAINER, this);
            }
         break;
         case CONTAINER_BASED_PAYLOAD:
            List<DataBlockDescription> containerChildren = getChildDescriptionsOfType(PhysicalDataBlockType.CONTAINER);

            if (containerChildren.size() != orderedChildren.size()) {
               throw new InvalidSpecificationException(VLD_INVALID_CHILDREN_CONTAINER_BASED_PAYLOAD, this);
            }
         break;
      }
   }

   private void validateDataBlockDescription() {
      // Validate lengths
      if (minimumByteLength != UNDEFINED) {
         Reject.ifNegative(minimumByteLength, "minimumByteLength");
      }

      if (maximumByteLength != UNDEFINED) {
         Reject.ifNegativeOrZero(maximumByteLength, "maximumByteLength");
      }

      if (minimumByteLength != UNDEFINED && maximumByteLength != UNDEFINED) {
         Reject.ifFalse(minimumByteLength <= maximumByteLength, "minimumByteLength <= maximumByteLength");
      }

      // Validate occurrences
      Reject.ifNegative(minimumOccurrences, "minimumOccurrences");
      Reject.ifNegative(maximumOccurrences, "maximumOccurrences");
      Reject.ifFalse(minimumOccurrences <= maximumOccurrences, "minimumOccurrences <= maximumOccurrences");

      // Validate children
      validateChildren();

      // Validate field properties
      if (physicalType == PhysicalDataBlockType.FIELD && fieldProperties == null) {
         throw new InvalidSpecificationException(VLD_FIELD_PROPERTIES_MISSING, this);
      } else if (physicalType != PhysicalDataBlockType.FIELD && fieldProperties != null) {
         throw new InvalidSpecificationException(VLD_FIELD_PROPERTIES_UNNECESSARY, this);
      }

      if (physicalType == PhysicalDataBlockType.FIELD) {
         getFieldProperties().validateFieldProperties(this);
      }
   }

   /**
    * 
    */
   private void determineAndValidateLengthsFromChildren() {
      long determinedMinLength = 0;

      for (DataBlockDescription childDescription : orderedChildren) {
         if (childDescription.getMinimumByteLength() != UNDEFINED) {
            determinedMinLength += childDescription.getMinimumByteLength() * childDescription.getMinimumOccurrences();
         }
      }

      minimumByteLength = determinedMinLength;

      long determinedMaxLength = 0;

      for (DataBlockDescription childDescription : orderedChildren) {
         if (childDescription.getMaximumByteLength() != UNDEFINED && determinedMaxLength != UNDEFINED) {
            determinedMaxLength += childDescription.getMaximumByteLength() * childDescription.getMaximumOccurrences();
         } else {
            determinedMaxLength = UNDEFINED;
         }
      }

      maximumByteLength = determinedMaxLength;
   }

   private void determineFixedByteOffsetsFromStartOfContainerForAllFields() {
      boolean hasContainerBasedPayload = getChildDescriptionsOfType(PhysicalDataBlockType.CONTAINER_BASED_PAYLOAD)
         .size() == 1;

      long currentOffset = setFixedByteOffsetsFromStartOfContainer(
         getChildDescriptionsOfType(PhysicalDataBlockType.HEADER), 0L);

      if (hasContainerBasedPayload) {
         currentOffset = UNDEFINED;
      } else {
         currentOffset = setFixedByteOffsetsFromStartOfContainer(
            getChildDescriptionsOfType(PhysicalDataBlockType.FIELD_BASED_PAYLOAD), currentOffset);
      }

      setFixedByteOffsetsFromStartOfContainer(getChildDescriptionsOfType(PhysicalDataBlockType.FOOTER), currentOffset);
   }

   private long setFixedByteOffsetsFromStartOfContainer(List<DataBlockDescription> parentsWithFields, long baseOffset) {
      for (DataBlockDescription parentDesc : parentsWithFields) {
         List<DataBlockDescription> fieldDescs = parentDesc.getChildDescriptionsOfType(PhysicalDataBlockType.FIELD);

         for (DataBlockDescription fieldDesc : fieldDescs) {
            fieldDesc.byteOffsetFromStartOfContainer = baseOffset;

            if (baseOffset != UNDEFINED && fieldDesc.hasFixedSize()) {
               baseOffset += fieldDesc.getMaximumByteLength();
            } else {
               baseOffset = UNDEFINED;
            }
         }
      }
      return baseOffset;
   }

   private void determineFixedByteOffsetsFromEndOfContainerForAllFields() {
      boolean hasContainerBasedPayload = getChildDescriptionsOfType(PhysicalDataBlockType.CONTAINER_BASED_PAYLOAD)
         .size() == 1;

      long currentOffset = setFixedByteOffsetsFromEndOfContainer(
         getChildDescriptionsOfType(PhysicalDataBlockType.FOOTER), 0L);

      if (hasContainerBasedPayload) {
         currentOffset = UNDEFINED;
      } else {
         currentOffset = setFixedByteOffsetsFromEndOfContainer(
            getChildDescriptionsOfType(PhysicalDataBlockType.FIELD_BASED_PAYLOAD), currentOffset);
      }

      setFixedByteOffsetsFromEndOfContainer(getChildDescriptionsOfType(PhysicalDataBlockType.HEADER), currentOffset);
   }

   private long setFixedByteOffsetsFromEndOfContainer(List<DataBlockDescription> parentsWithFields, long baseOffset) {

      List<DataBlockDescription> parentsWithFieldsReversed = new ArrayList<>(parentsWithFields);

      Collections.reverse(parentsWithFieldsReversed);

      for (DataBlockDescription parentDesc : parentsWithFieldsReversed) {
         List<DataBlockDescription> fieldDescs = parentDesc.getChildDescriptionsOfType(PhysicalDataBlockType.FIELD);

         List<DataBlockDescription> fieldDescsReversed = new ArrayList<>(fieldDescs);

         Collections.reverse(fieldDescsReversed);

         for (DataBlockDescription fieldDesc : fieldDescsReversed) {
            if (baseOffset != UNDEFINED && fieldDesc.hasFixedSize()) {
               baseOffset -= fieldDesc.getMaximumByteLength();
            } else {
               baseOffset = UNDEFINED;
            }

            fieldDesc.byteOffsetFromEndOfContainer = baseOffset;
         }
      }
      return baseOffset;
   }

   private void autoDetectMagicKeys() {
      List<MagicKey> detectedHeaderMagicKeys = detectMagicKeys(PhysicalDataBlockType.HEADER);
      detectedHeaderMagicKeys.forEach(key -> headerMagicKeys.add(key));
      List<MagicKey> detectedFooterMagicKeys = detectMagicKeys(PhysicalDataBlockType.FOOTER);
      detectedFooterMagicKeys.forEach(key -> footerMagicKeys.add(key));
   }

   private List<MagicKey> detectMagicKeys(PhysicalDataBlockType type) {
      List<MagicKey> magicKeys = new ArrayList<>();

      List<DataBlockDescription> headerOrFooterDescs = getChildDescriptionsOfType(type);

      for (DataBlockDescription headerOrFooterDesc : headerOrFooterDescs) {
         List<DataBlockDescription> fieldDescs = headerOrFooterDesc
            .getChildDescriptionsOfType(PhysicalDataBlockType.FIELD);

         for (DataBlockDescription fieldDesc : fieldDescs) {

            long magicKeyOffset = UNDEFINED;

            if (fieldDesc.getFieldProperties().isMagicKey()) {
               if (type == PhysicalDataBlockType.HEADER) {
                  magicKeyOffset = fieldDesc.getByteOffsetFromStartOfContainer();
               } else {
                  magicKeyOffset = fieldDesc.getByteOffsetFromEndOfContainer();
               }

               if (magicKeyOffset == UNDEFINED) {
                  throw new InvalidSpecificationException(VLD_MAGIC_KEY_UNKNOWN_OFFSET, fieldDesc);
               }

               magicKeys.addAll(fieldDesc.getFieldProperties().determineFieldMagicKeys(fieldDesc, magicKeyOffset));
            }
         }
      }

      Set<Long> distinctMagicKeyOffsets = magicKeys.stream().map(key -> key.getDeltaOffset())
         .collect(Collectors.toSet());

      if (distinctMagicKeyOffsets.size() > 1) {
         throw new InvalidSpecificationException(VLD_MAGIC_KEY_TOO_MANY, this, type,
            magicKeys.stream().map(key -> key.getFieldId()).collect(Collectors.toList()));
      }

      return magicKeys;
   }
}
