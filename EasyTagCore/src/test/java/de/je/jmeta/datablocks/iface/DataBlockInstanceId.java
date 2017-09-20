package de.je.jmeta.datablocks.iface;

import de.je.jmeta.datablocks.IDataBlock;
import de.je.jmeta.dataformats.DataBlockId;
import de.je.jmeta.dataformats.PhysicalDataBlockType;
import de.je.jmeta.media.api.IMediumReference;
import de.je.util.javautil.common.err.Reject;

/**
 * {@link DataBlockInstanceId} represents and uniquely determines a single occurrence of a {@link IDataBlock} with a
 * given {@link DataBlockId} in the current medium.
 */
public class DataBlockInstanceId {

   /**
    * Creates a new {@DataBlockInstanceId}.
    * 
    * @param id
    *           the {@link DataBlockId} of the data block.
    * @param parentInstanceId
    *           The {@link DataBlockInstanceId} of the parent data block or null if there is no parent and this
    *           {@link DataBlockInstanceId} refers to a top level block.
    * @param sequenceNumber
    *           A zero based number that corresponds to the absolute occurrence index of the block in its parent block.
    *           The first child block (regarding its {@link IMediumReference} and irrespective of its
    *           {@link DataBlockId} or {@link PhysicalDataBlockType}) gets sequence number 0, the second one gets
    *           sequence number 1, and so on.
    *
    * @pre sequenceNumber >= 0
    */
   public DataBlockInstanceId(DataBlockId id,
      DataBlockInstanceId parentInstanceId, int sequenceNumber) {
      Reject.ifNull(id, "id");
      Reject.ifNegative(sequenceNumber,
         "sequenceNumber");

      m_id = id;
      m_parentInstanceId = parentInstanceId;
      m_sequenceNumber = sequenceNumber;
   }

   /**
    * Returns the {@link DataBlockId} of this {@link DataBlockInstanceId}.
    *
    * @return the {@link DataBlockId} of this {@link DataBlockInstanceId}.
    */
   public DataBlockId getId() {

      return m_id;
   }

   /**
    * Returns the {@link DataBlockInstanceId} of the parent data block or null if there is no parent and this
    * {@link DataBlockInstanceId} refers to a top level block.
    *
    * @return the {@link DataBlockInstanceId} of the parent data block or null if there is no parent and this
    *         {@link DataBlockInstanceId} refers to a top level block.
    */
   public DataBlockInstanceId getParentInstanceId() {

      return m_parentInstanceId;
   }

   /**
    * Returns the sequence number of this {@link DataBlockInstanceId}. The sequence number is a zero based number that
    * corresponds to the absolute occurrence index of the block in its parent block. The first child block (regarding
    * its {@link IMediumReference} and irrespective of its {@link DataBlockId} or {@link PhysicalDataBlockType}) gets
    * sequence number 0, the second one gets sequence number 1, and so on.
    *
    * @return the sequence number of this {@link DataBlockInstanceId}.
    */
   public int getSequenceNumber() {

      return m_sequenceNumber;
   }

   /**
    * @see java.lang.Object#toString()
    */
   @Override
   public String toString() {

      return "DataBlockInstanceId [m_id=" + m_id + ", m_parentInstanceId="
         + m_parentInstanceId + ", m_sequenceNumber=" + m_sequenceNumber + "]";
   }

   private final DataBlockId m_id;

   @Override
   public int hashCode() {

      final int prime = 31;
      int result = 1;
      result = prime * result + ((m_id == null) ? 0 : m_id.hashCode());
      result = prime * result
         + ((m_parentInstanceId == null) ? 0 : m_parentInstanceId.hashCode());
      result = prime * result + m_sequenceNumber;
      return result;
   }

   @Override
   public boolean equals(Object obj) {

      if (this == obj)
         return true;
      if (obj == null)
         return false;
      if (getClass() != obj.getClass())
         return false;
      DataBlockInstanceId other = (DataBlockInstanceId) obj;
      if (m_id == null) {
         if (other.m_id != null)
            return false;
      } else if (!m_id.equals(other.m_id))
         return false;
      if (m_parentInstanceId == null) {
         if (other.m_parentInstanceId != null)
            return false;
      } else if (!m_parentInstanceId.equals(other.m_parentInstanceId))
         return false;
      if (m_sequenceNumber != other.m_sequenceNumber)
         return false;
      return true;
   }

   private final DataBlockInstanceId m_parentInstanceId;

   private final int m_sequenceNumber;
}
