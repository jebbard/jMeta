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

   public static final long UNLIMITED = Long.MAX_VALUE;

   /**
    * States that the total size of an {@link DataBlock} is unknown. This is a possible return value of the method
    * {@link DataBlock#getTotalSize()}.
    */
   public final static long UNKNOWN_SIZE = -1;
   public final static long UNKNOWN_OFFSET = -1;

   private final DataBlockId m_id;

   private final String m_name;

   private final String m_specDescription;

   private final PhysicalDataBlockType m_physicalType;

   private final List<DataBlockId> m_childIds = new ArrayList<>();

   private final FieldProperties<?> m_fieldProperties;

   private final long m_maximumByteLength;

   private final long m_minimumByteLength;

   private final List<MagicKey> m_headerMagicKeys = new ArrayList<>();

   private final List<MagicKey> m_footerMagicKeys = new ArrayList<>();

   private final int minimumOccurrences;
   private final int maximumOccurrences;
   private long fixedByteOffsetInContainer;

   private final boolean isGeneric;

   public void setFixedByteOffsetInContainer(long fixedByteOffsetInContainer) {
      this.fixedByteOffsetInContainer = fixedByteOffsetInContainer;
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
    * Creates a new {@link DataBlockDescription}.
    * 
    * @param id
    * @param name
    * @param specDescription
    * @param physicalType
    * @param childIds
    * @param fieldProperties
    * @param minimumOccurrences
    *           TODO
    * @param maximumOccurrences
    *           TODO
    * @param minimumByteLength
    * @param maximumByteLength
    * @param isGeneric
    *           TODO
    * @param childOrder
    */
   public DataBlockDescription(DataBlockId id, String name, String specDescription, PhysicalDataBlockType physicalType,
      List<DataBlockId> childIds, FieldProperties<?> fieldProperties, int minimumOccurrences, int maximumOccurrences,
      long minimumByteLength, long maximumByteLength, boolean isGeneric) {
      Reject.ifNull(childIds, "childIds");
      Reject.ifNull(physicalType, "physicalType");
      Reject.ifNull(specDescription, "specDescription");
      Reject.ifNull(name, "name");
      Reject.ifNull(id, "id");

      Reject.ifNegative(minimumByteLength, "minimumByteLength");
      Reject.ifNegative(maximumByteLength, "maximumByteLength");
      Reject.ifFalse(minimumByteLength <= maximumByteLength, "minimumByteLength <= maximumByteLength");
      Reject.ifFalse(minimumOccurrences <= maximumOccurrences, "minimumOccurrences <= maximumOccurrences");

      this.isGeneric = isGeneric;
      this.minimumOccurrences = minimumOccurrences;
      this.maximumOccurrences = maximumOccurrences;
      m_id = id;
      m_name = name;
      m_specDescription = specDescription;
      m_physicalType = physicalType;
      m_childIds.addAll(childIds);
      m_fieldProperties = fieldProperties;
      m_minimumByteLength = minimumByteLength;
      m_maximumByteLength = maximumByteLength;
   }

   public boolean isGeneric() {
      return isGeneric;
   }

   /**
    * @return the {@link DataBlockId}
    */
   public DataBlockId getId() {

      return m_id;
   }

   /**
    * @return the name
    */
   public String getName() {

      return m_name;
   }

   /**
    * @return the specification description
    */
   public String getSpecDescription() {

      return m_specDescription;
   }

   /**
    * @return the {@link PhysicalDataBlockType}
    */
   public PhysicalDataBlockType getPhysicalType() {

      return m_physicalType;
   }

   /**
    * @return the child {@link DataBlockId}s
    */
   public List<DataBlockId> getOrderedChildIds() {

      return Collections.unmodifiableList(m_childIds);
   }

   /**
    * @return the {@link FieldProperties}
    */
   public FieldProperties<?> getFieldProperties() {

      return m_fieldProperties;
   }

   /**
    * Returns magicKey
    *
    * @return magicKey
    */
   public List<MagicKey> getHeaderMagicKeys() {

      return Collections.unmodifiableList(m_headerMagicKeys);
   }

   public List<MagicKey> getFooterMagicKeys() {
      return m_footerMagicKeys;
   }

   public void addHeaderMagicKey(MagicKey magicKey) {
      Reject.ifNull(magicKey, "magicKey");

      m_headerMagicKeys.add(magicKey);
   }

   public void addFooterMagicKey(MagicKey magicKey) {
      Reject.ifNull(magicKey, "magicKey");

      m_footerMagicKeys.add(magicKey);
   }

   /**
    * Returns maximumByteLength
    *
    * @return maximumByteLength
    */
   public long getMaximumByteLength() {

      return m_maximumByteLength;
   }

   /**
    * Returns minimumByteLength
    *
    * @return minimumByteLength
    */
   public long getMinimumByteLength() {

      return m_minimumByteLength;
   }

   @Override
   public String toString() {
      return "DataBlockDescription [m_id=" + m_id + ", m_name=" + m_name + ", m_specDescription=" + m_specDescription
         + ", m_physicalType=" + m_physicalType + ", m_childIds=" + m_childIds + ", m_fieldProperties="
         + m_fieldProperties + ", m_maximumByteLength=" + m_maximumByteLength + ", m_minimumByteLength="
         + m_minimumByteLength + ", m_headerMagicKeys=" + m_headerMagicKeys + ", m_footerMagicKeys=" + m_footerMagicKeys
         + ", minimumOccurrences=" + minimumOccurrences + ", maximumOccurrences=" + maximumOccurrences
         + ", fixedByteOffsetInContainer=" + fixedByteOffsetInContainer + "]";
   }

   /**
    * @see java.lang.Object#hashCode()
    */
   @Override
   public int hashCode() {

      final int prime = 31;
      int result = 1;
      result = prime * result + ((m_id == null) ? 0 : m_id.hashCode());
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
      if (m_id == null) {
         if (other.m_id != null)
            return false;
      } else if (!m_id.equals(other.m_id))
         return false;
      return true;
   }
}
