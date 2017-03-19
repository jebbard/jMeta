/**
 * {@link AbstractDataBlock}.java
 *
 * @author Jens Ebert
 * @date 31.12.10 19:47:08 (December 31, 2010)
 */

package de.je.jmeta.datablocks.export;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import de.je.jmeta.datablocks.IDataBlock;
import de.je.jmeta.dataformats.DataBlockDescription;
import de.je.jmeta.dataformats.DataBlockId;
import de.je.jmeta.media.api.IMediumReference;
import de.je.util.javautil.common.err.Contract;
import de.je.util.javautil.common.err.Reject;

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
    * @see de.je.jmeta.datablocks.IDataBlock#getBytes(long, int)
    */
   @Override
   public byte[] getBytes(long offset, int size) {

      Contract.checkPrecondition(offset >= 0, "offset must be bigger than 0");
      Contract.checkPrecondition(size >= 0, "size must be bigger than 0");
      Contract.checkPrecondition(getMediumReference() != null,
         "there must be a medium data reference set");

      if (getTotalSize() != DataBlockDescription.UNKNOWN_SIZE)
         Contract.checkPrecondition(offset + size <= getTotalSize(),
            "offset + size = <" + (offset + size)
               + "> must be smaller than getTotalSize() = <" + getTotalSize()
               + ">.");

      ByteBuffer readBuffer = getDataBlockReader()
         .readBytes(m_mediumReference.advance(offset), size);

      byte[] readBytes = new byte[readBuffer.remaining()];

      readBuffer.get(readBytes);

      return readBytes;
   }

   /**
    * @see de.je.jmeta.datablocks.IDataBlock#getMediumReference()
    */
   @Override
   public IMediumReference getMediumReference() {

      return m_mediumReference;
   }

   /**
    * @see de.je.jmeta.datablocks.IDataBlock#getParent()
    */
   @Override
   public IDataBlock getParent() {

      return m_parent;
   }

   /**
    * @see de.je.jmeta.datablocks.IDataBlock#getId()
    */
   @Override
   public DataBlockId getId() {

      return m_id;
   }

   /**
    * @see de.je.jmeta.datablocks.IDataBlock#initParent(de.je.jmeta.datablocks.IDataBlock)
    */
   @Override
   public void initParent(IDataBlock parent) {

      Reject.ifNull(parent, "parent");
      Contract.checkPrecondition(getParent() == null,
         "The data block already has a parent");

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
    * @see de.je.jmeta.datablocks.IDataBlock#setBytes(byte[][])
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
