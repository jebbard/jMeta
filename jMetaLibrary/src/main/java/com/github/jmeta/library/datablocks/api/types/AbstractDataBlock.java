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

   /**
    * Creates a new {@link AbstractDataBlock}.
    * 
    * @param id
    * @param parent
    * @param reference
    * @param dataBlockReader
    */
   public AbstractDataBlock(DataBlockId id, DataBlock parent, MediumOffset reference, DataBlockReader dataBlockReader) {
      Reject.ifNull(id, "id");
      Reject.ifNull(dataBlockReader, "dataBlockReader");
      Reject.ifNull(reference, "reference");

      m_id = id;
      m_dataBlockReader = dataBlockReader;
      m_mediumReference = reference;

      if (parent != null)
         initParent(parent);
   }

   /**
    * @see com.github.jmeta.library.datablocks.api.types.DataBlock#getBytes(long, int)
    */
   @Override
   public ByteBuffer getBytes(long offset, int size) {

      Reject.ifNegative(offset, "offset");
      Reject.ifNegative(size, "size");
      Reject.ifNull(getMediumReference(), "getMediumReference()");

      if (getTotalSize() != DataBlockDescription.UNDEFINED)
         Reject.ifFalse(offset + size <= getTotalSize(), "offset + size <= getTotalSize()");

      return getDataBlockReader().readBytes(m_mediumReference.advance(offset), size);
   }

   /**
    * @see com.github.jmeta.library.datablocks.api.types.DataBlock#getMediumReference()
    */
   @Override
   public MediumOffset getMediumReference() {

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
         + (getParent() == null ? getParent() : getParent().getId()) + ", medium=" + getMediumReference()
         + ", totalSize=" + getTotalSize() + "]";
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
