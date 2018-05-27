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
   private final List<DataBlockId> childIds = new ArrayList<>();
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
    * @param childIds
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
      List<DataBlockId> childIds, FieldProperties<?> fieldProperties, int minimumOccurrences, int maximumOccurrences,
      long minimumByteLength, long maximumByteLength, boolean isGeneric) {
      Reject.ifNull(id, "id");
      Reject.ifNull(name, "name");
      Reject.ifNull(description, "description");
      Reject.ifNull(physicalType, "physicalType");
      Reject.ifNull(childIds, "childIds");
      Reject.ifNegative(minimumByteLength, "minimumByteLength");
      Reject.ifNegative(maximumByteLength, "maximumByteLength");
      Reject.ifFalse(minimumByteLength <= maximumByteLength, "minimumByteLength <= maximumByteLength");
      Reject.ifFalse(minimumOccurrences <= maximumOccurrences, "minimumOccurrences <= maximumOccurrences");

      if (physicalType == PhysicalDataBlockType.FIELD) {
         Reject.ifNull(fieldProperties, "fieldProperties");
      } else {
         Reject.ifNotNull(fieldProperties, "fieldProperties");
      }

      this.id = id;
      this.name = name;
      this.description = description;
      this.physicalType = physicalType;
      this.childIds.addAll(childIds);
      this.fieldProperties = fieldProperties;
      this.minimumOccurrences = minimumOccurrences;
      this.maximumOccurrences = maximumOccurrences;
      this.minimumByteLength = minimumByteLength;
      this.maximumByteLength = maximumByteLength;
      this.isGeneric = isGeneric;
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

      for (Iterator<DataBlockId> childIterator = headerDesc.getOrderedChildIds().iterator(); childIterator.hasNext();) {
         DataBlockId childId = childIterator.next();

         DataBlockDescription childDesc = spec.getDataBlockDescription(childId);

         totalMinimumSize += childDesc.getMinimumByteLength();
      }

      return totalMinimumSize;
   }

   /**
    * @param spec
    * @param parentId
    * @param type
    * @return a list of child {@link DataBlockDescription}s
    */
   public static List<DataBlockDescription> getChildDescriptionsOfType(DataFormatSpecification spec,
      DataBlockId parentId, PhysicalDataBlockType type) {

      Reject.ifNull(spec, "spec");
      Reject.ifNull(type, "type");

      List<DataBlockDescription> childDescs = new ArrayList<>();

      Iterator<DataBlockId> childIterator = null;

      // Need to get the top-level children
      if (parentId == null)
         childIterator = spec.getTopLevelDataBlockIds().iterator();

      else {
         DataBlockDescription parentDesc = spec.getDataBlockDescription(parentId);

         childIterator = parentDesc.getOrderedChildIds().iterator();
      }

      while (childIterator.hasNext()) {
         DataBlockId childId = childIterator.next();

         DataBlockDescription childDesc = spec.getDataBlockDescription(childId);

         if (childDesc.getPhysicalType().equals(type))
            childDescs.add(childDesc);
      }

      return childDescs;
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
   public String getSpecDescription() {

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
   public List<DataBlockId> getOrderedChildIds() {

      return Collections.unmodifiableList(childIds);
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

   @Override
   public String toString() {
      return "DataBlockDescription [m_id=" + id + ", m_name=" + name + ", m_specDescription=" + description
         + ", m_physicalType=" + physicalType + ", m_childIds=" + childIds + ", m_fieldProperties=" + fieldProperties
         + ", m_maximumByteLength=" + maximumByteLength + ", m_minimumByteLength=" + minimumByteLength
         + ", m_headerMagicKeys=" + headerMagicKeys + ", m_footerMagicKeys=" + footerMagicKeys + ", minimumOccurrences="
         + minimumOccurrences + ", maximumOccurrences=" + maximumOccurrences + ", fixedByteOffsetInContainer="
         + fixedByteOffsetInContainer + "]";
   }

   /**
    * @see java.lang.Object#hashCode()
    */
   @Override
   public int hashCode() {

      final int prime = 31;
      int result = 1;
      result = prime * result + ((id == null) ? 0 : id.hashCode());
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
      if (id == null) {
         if (other.id != null)
            return false;
      } else if (!id.equals(other.id))
         return false;
      return true;
   }
}
