/**
 * {@link AbstractDataBlock}.java
 *
 * @author Jens Ebert
 * @date 31.12.10 19:47:08 (December 31, 2010)
 */

package com.github.jmeta.library.datablocks.api.types;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import com.github.jmeta.library.datablocks.api.services.DataBlockReader;
import com.github.jmeta.library.dataformats.api.types.DataBlockDescription;
import com.github.jmeta.library.dataformats.api.types.DataBlockId;
import com.github.jmeta.library.media.api.types.MediumOffset;
import com.github.jmeta.utility.dbc.api.services.Reject;

/**
 *
 */
public abstract class AbstractDataBlock implements DataBlock {

   private int sequenceNumber;

   /**
    * Creates a new {@link AbstractDataBlock}.
    *
    * @param id
    * @param parent
    * @param reference
    * @param dataBlockReader
    * @param sequenceNumber
    *           TODO
    * @param containerContext
    *           TODO
    */
   public AbstractDataBlock(DataBlockId id, DataBlock parent, MediumOffset reference, DataBlockReader dataBlockReader,
      int sequenceNumber, ContainerContext containerContext) {
      Reject.ifNull(id, "id");
      Reject.ifNull(dataBlockReader, "dataBlockReader");
      Reject.ifNull(reference, "reference");
      Reject.ifNegative(sequenceNumber, "sequenceNumber");

      m_id = id;
      m_dataBlockReader = dataBlockReader;
      m_mediumReference = reference;
      this.sequenceNumber = sequenceNumber;

      this.containerContext = containerContext;

      if (parent != null) {
         initParent(parent);
      }
   }

   private final ContainerContext containerContext;

   /**
    * @see com.github.jmeta.library.datablocks.api.types.Container#getContainerContext()
    */
   @Override
   public ContainerContext getContainerContext() {
      return containerContext;
   }

   /**
    * @see com.github.jmeta.library.datablocks.api.types.DataBlock#getSequenceNumber()
    */
   @Override
   public int getSequenceNumber() {
      return sequenceNumber;
   }

   /**
    * @see com.github.jmeta.library.datablocks.api.types.DataBlock#getBytes(long, int)
    */
   @Override
   public ByteBuffer getBytes(MediumOffset offset, int size) {
      Reject.ifNegative(size, "size");
      Reject.ifNull(offset, "offset");
      Reject.ifTrue(offset.before(getOffset()), "offset.before(getOffset())");

      if (getSize() != DataBlockDescription.UNDEFINED) {
         Reject.ifFalse(offset.getAbsoluteMediumOffset() + size <= getOffset().getAbsoluteMediumOffset() + getSize(),
            "offset.getAbsoluteMediumOffset() + size <= getOffset().getAbsoluteMediumOffset() + getSize()");
      }

      return getDataBlockReader().readBytes(offset, size);
   }

   /**
    * @see com.github.jmeta.library.datablocks.api.types.DataBlock#getOffset()
    */
   @Override
   public MediumOffset getOffset() {

      return m_mediumReference;
   }

   /**
    * @see com.github.jmeta.library.datablocks.api.types.DataBlock#getParent()
    */
   @Override
   public DataBlock getParent() {

      return m_parent;
   }

   /**
    * @see com.github.jmeta.library.datablocks.api.types.DataBlock#getId()
    */
   @Override
   public DataBlockId getId() {

      return m_id;
   }

   /**
    * @see com.github.jmeta.library.datablocks.api.types.DataBlock#initParent(com.github.jmeta.library.datablocks.api.types.DataBlock)
    */
   @Override
   public void initParent(DataBlock parent) {

      Reject.ifNull(parent, "parent");
      Reject.ifFalse(getParent() == null, "getParent() == null");

      m_parent = parent;
   }

   /**
    * @see java.lang.Object#toString()
    */
   @Override
   public String toString() {

      return getClass().getName() + "[id=" + getId() + ", parentId="
         + (getParent() == null ? getParent() : getParent().getId()) + ", medium=" + getOffset() + ", totalSize="
         + getSize() + "]";
   }

   private DataBlock m_parent;

   private MediumOffset m_mediumReference;

   private DataBlockId m_id;

   /**
    * @see com.github.jmeta.library.datablocks.api.types.DataBlock#setBytes(byte[][])
    */
   @Override
   public void setBytes(byte[][] bytes) {

      List<ByteBuffer> cache = new ArrayList<>(bytes.length);

      for (int i = 0; i < bytes.length; i++) {
         cache.add(ByteBuffer.wrap(bytes[i]));
      }

      // m_mediumReference.setCache(cache);
   }

   private final DataBlockReader m_dataBlockReader;

   /**
    * Returns dataBlockReader
    *
    * @return dataBlockReader
    */
   protected DataBlockReader getDataBlockReader() {

      return m_dataBlockReader;
   }
}
