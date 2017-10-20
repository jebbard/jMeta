/**
 * {@link AbstractDataBlock}.java
 *
 * @author Jens Ebert
 * @date 31.12.10 19:47:08 (December 31, 2010)
 */

package com.github.jmeta.library.datablocks.api.type;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import com.github.jmeta.library.datablocks.api.services.IDataBlockReader;
import com.github.jmeta.library.dataformats.api.type.DataBlockDescription;
import com.github.jmeta.library.dataformats.api.type.DataBlockId;
import com.github.jmeta.library.media.api.type.IMediumReference;
import com.github.jmeta.utility.dbc.api.services.Reject;

/**
 *
 */
public abstract class AbstractDataBlock implements IDataBlock {

   /**
    * Creates a new {@link AbstractDataBlock}.
    * 
    * @param id
    * @param parent
    * @param reference
    * @param dataBlockReader
    */
   public AbstractDataBlock(DataBlockId id, IDataBlock parent,
      IMediumReference reference, IDataBlockReader dataBlockReader) {
      Reject.ifNull(id, "id");
      Reject.ifNull(dataBlockReader, "dataBlockReader");
      Reject.ifNull(reference, "reference");

      m_id = id;
      m_dataBlockReader = dataBlockReader;
      m_mediumReference = reference;

      if (parent != null)
         initParent(parent);
   }

   // TODO use free in test cases
   @Override
   public void free() {

      m_dataBlockReader.free(getMediumReference(), getTotalSize());
   }

   /**
    * @see com.github.jmeta.library.datablocks.api.type.IDataBlock#getBytes(long, int)
    */
   @Override
   public byte[] getBytes(long offset, int size) {

      Reject.ifNegative(offset, "offset");
      Reject.ifNegative(size, "size");
      Reject.ifNull(getMediumReference(), "getMediumReference()");

      if (getTotalSize() != DataBlockDescription.UNKNOWN_SIZE)
    	  Reject.ifFalse(offset + size <= getTotalSize(),
            "offset + size <= getTotalSize()");

      ByteBuffer readBuffer = getDataBlockReader()
         .readBytes(m_mediumReference.advance(offset), size);

      byte[] readBytes = new byte[readBuffer.remaining()];

      readBuffer.get(readBytes);

      return readBytes;
   }

   /**
    * @see com.github.jmeta.library.datablocks.api.type.IDataBlock#getMediumReference()
    */
   @Override
   public IMediumReference getMediumReference() {

      return m_mediumReference;
   }

   /**
    * @see com.github.jmeta.library.datablocks.api.type.IDataBlock#getParent()
    */
   @Override
   public IDataBlock getParent() {

      return m_parent;
   }

   /**
    * @see com.github.jmeta.library.datablocks.api.type.IDataBlock#getId()
    */
   @Override
   public DataBlockId getId() {

      return m_id;
   }

   /**
    * @see com.github.jmeta.library.datablocks.api.type.IDataBlock#initParent(com.github.jmeta.library.datablocks.api.type.IDataBlock)
    */
   @Override
   public void initParent(IDataBlock parent) {

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
         + (getParent() == null ? getParent() : getParent().getId())
         + ", medium=" + getMediumReference() + ", totalSize=" + getTotalSize()
         + "]";
   }

   private IDataBlock m_parent;

   private IMediumReference m_mediumReference;

   private DataBlockId m_id;

   /**
    * @see com.github.jmeta.library.datablocks.api.type.IDataBlock#setBytes(byte[][])
    */
   @Override
   public void setBytes(byte[][] bytes) {

      List<ByteBuffer> cache = new ArrayList<>(bytes.length);

      for (int i = 0; i < bytes.length; i++) {
         cache.add(ByteBuffer.wrap(bytes[i]));
      }

      // m_mediumReference.setCache(cache);
   }

   private final IDataBlockReader m_dataBlockReader;

   /**
    * Returns dataBlockReader
    *
    * @return dataBlockReader
    */
   protected IDataBlockReader getDataBlockReader() {

      return m_dataBlockReader;
   }
}
