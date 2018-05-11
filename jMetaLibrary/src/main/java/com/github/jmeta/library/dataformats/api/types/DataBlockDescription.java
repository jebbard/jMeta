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

   /**
    * States that the total size of an {@link DataBlock} is unknown. This is a possible return value of the method
    * {@link DataBlock#getTotalSize()}.
    */
   public final static long UNKNOWN_SIZE = -1;

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

   private final DataBlockId m_overriddenId;

   private final int minimumOccurrences;
   private final int maximumOccurrences;
   private final long fixedByteOffsetInContainer;

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
    * @param fixedByteOffsetInContainer
    *           TODO
    * @param minimumOccurrences
    *           TODO
    * @param maximumOccurrences
    *           TODO
    * @param minimumByteLength
    * @param maximumByteLength
    * @param overriddenId
    * @param childOrder
    */
   public DataBlockDescription(DataBlockId id, String name, String specDescription, PhysicalDataBlockType physicalType,
      List<DataBlockId> childIds, FieldProperties<?> fieldProperties, long fixedByteOffsetInContainer,
      int minimumOccurrences, int maximumOccurrences, long minimumByteLength, long maximumByteLength,
      DataBlockId overriddenId) {
      Reject.ifNull(childIds, "childIds");
      Reject.ifNull(physicalType, "physicalType");
      Reject.ifNull(specDescription, "specDescription");
      Reject.ifNull(name, "name");
      Reject.ifNull(id, "id");

      if (minimumByteLength != DataBlockDescription.UNKNOWN_SIZE
         && maximumByteLength != DataBlockDescription.UNKNOWN_SIZE)
         Reject.ifFalse(minimumByteLength <= maximumByteLength, "minimumByteLength <= maximumByteLength");

      this.fixedByteOffsetInContainer = fixedByteOffsetInContainer;
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
      m_overriddenId = overriddenId;
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

   /**
    * @see java.lang.Object#toString()
    */
   @Override
   public String toString() {

      return getClass().getName() + "[" + "id=" + m_id + ", name=" + m_name + ", specDescription=" + m_specDescription
         + ", physicalType=" + m_physicalType + ", childIds=" + m_childIds + ", fieldProperties=" + m_fieldProperties
         + ", maximumLength=" + m_maximumByteLength + ", minimumLength=" + m_minimumByteLength + "]";
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

   /**
    * @return the overridden id
    */
   public DataBlockId getOverriddenId() {

      return m_overriddenId;
   }

}
