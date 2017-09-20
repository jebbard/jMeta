/**
 * {@link DataBlockDescription}.java
 *
 * @author Jens Ebert
 * @date 31.12.10 19:47:06 (December 31, 2010)
 */

package de.je.jmeta.dataformats;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.je.jmeta.datablocks.IDataBlock;
import de.je.util.javautil.common.err.Reject;

/**
 *
 */
public class DataBlockDescription {

   /**
    * Creates a new {@link DataBlockDescription}.
    * 
    * @param id
    * @param name
    * @param specDescription
    * @param physicalType
    * @param childIds
    * @param childOrder
    * @param fieldProperties
    * @param locationProperties
    * @param minimumByteLength
    * @param maximumByteLength
    * @param magicKeys
    * @param overriddenId
    */
   public DataBlockDescription(DataBlockId id, String name,
      String specDescription, PhysicalDataBlockType physicalType,
      List<DataBlockId> childIds, ChildOrder childOrder,
      FieldProperties<?> fieldProperties,
      Map<DataBlockId, LocationProperties> locationProperties,
      long minimumByteLength, long maximumByteLength, List<MagicKey> magicKeys,
      DataBlockId overriddenId) {
      Reject.ifNull(childOrder, "childOrder");
      Reject.ifNull(childIds, "childIds");
      Reject.ifNull(physicalType, "physicalType");
      Reject.ifNull(specDescription, "specDescription");
      Reject.ifNull(name, "name");
      Reject.ifNull(id, "id");

      if (minimumByteLength != DataBlockDescription.UNKNOWN_SIZE
         && maximumByteLength != DataBlockDescription.UNKNOWN_SIZE)
    	  Reject.ifFalse(minimumByteLength <= maximumByteLength,
            "minimumByteLength <= maximumByteLength");

      m_id = id;
      m_name = name;
      m_specDescription = specDescription;
      m_physicalType = physicalType;
      m_childIds.addAll(childIds);
      m_childOrder = childOrder;
      m_fieldProperties = fieldProperties;
      m_minimumByteLength = minimumByteLength;
      m_maximumByteLength = maximumByteLength;
      m_overriddenId = overriddenId;

      if (locationProperties != null)
         m_locationProperties.putAll(locationProperties);
      m_magicKeys = new ArrayList<>();

      if (magicKeys != null)
         m_magicKeys.addAll(magicKeys);
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
    * @return the {@link ChildOrder}
    */
   public ChildOrder getChildOrder() {

      return m_childOrder;
   }

   /**
    * @return the {@link FieldProperties}
    */
   public FieldProperties<?> getFieldProperties() {

      return m_fieldProperties;
   }

   /**
    * @return the parents for the location properties
    */
   public Set<DataBlockId> getAllParentsForLocationProperties() {

      return Collections.unmodifiableSet(m_locationProperties.keySet());
   }

   /**
    * @param parentId
    * @return the {@link LocationProperties}
    */
   public LocationProperties getLocationPropertiesForParent(
      DataBlockId parentId) {

	  Reject.ifFalse(
         getAllParentsForLocationProperties().contains(parentId),
         "getAllParentsForLocationProperties().contains(parentId)");

      return m_locationProperties.get(parentId);
   }

   /**
    * Returns magicKey
    *
    * @return magicKey
    */
   public List<MagicKey> getMagicKeys() {

      return Collections.unmodifiableList(m_magicKeys);
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

      return getClass().getName() + "[" + "id=" + m_id + ", name=" + m_name
         + ", specDescription=" + m_specDescription + ", physicalType="
         + m_physicalType + ", childIds=" + m_childIds + ", childOrder="
         + m_childOrder + ", fieldProperties=" + m_fieldProperties
         + ", locationProperties=" + m_locationProperties + ", maximumLength="
         + m_maximumByteLength + ", minimumLength=" + m_minimumByteLength + "]";
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
    * @param spec
    * @param headerDesc
    * @return the total minimum size of the data block
    */
   public static long getTotalMinimumSize(IDataFormatSpecification spec,
      DataBlockDescription headerDesc) {

      Reject.ifNull(headerDesc, "headerDesc");
      Reject.ifNull(spec, "spec");

      long totalMinimumSize = 0;

      for (Iterator<DataBlockId> childIterator = headerDesc.getOrderedChildIds()
         .iterator(); childIterator.hasNext();) {
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
   public static List<DataBlockDescription> getChildDescriptionsOfType(
      IDataFormatSpecification spec, DataBlockId parentId,
      PhysicalDataBlockType type) {

      Reject.ifNull(spec, "spec");
      Reject.ifNull(type, "type");

      List<DataBlockDescription> childDescs = new ArrayList<>();

      Iterator<DataBlockId> childIterator = null;

      // Need to get the top-level children
      if (parentId == null)
         childIterator = spec.getTopLevelDataBlockIds().iterator();

      else {
         DataBlockDescription parentDesc = spec
            .getDataBlockDescription(parentId);

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

   private final DataBlockId m_id;

   private final String m_name;

   private final String m_specDescription;

   private final PhysicalDataBlockType m_physicalType;

   private final List<DataBlockId> m_childIds = new ArrayList<>();

   private final ChildOrder m_childOrder;

   private final FieldProperties<?> m_fieldProperties;

   private final Map<DataBlockId, LocationProperties> m_locationProperties = new HashMap<>();

   private final long m_maximumByteLength;

   private final long m_minimumByteLength;

   private final List<MagicKey> m_magicKeys;

   private final DataBlockId m_overriddenId;

   /**
    * @return the overridden id
    */
   public DataBlockId getOverriddenId() {

      return m_overriddenId;
   }

   /**
    * States that the total size of an {@link IDataBlock} is unknown. This is a possible return value of the method
    * {@link IDataBlock#getTotalSize()}.
    */
   public final static long UNKNOWN_SIZE = -1;

}
