/**
 *
 * {@link RandomAccessFileTest}.java
 *
 * @author Jens Ebert
 *
 * @date 02.05.2010
 *
 */

package com.github.jmeta.tools.fileaccessperformance.api.services.access;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

/**
 * {@link FileChannelAccessor} implements a performance test for {@link FileChannel}.
 */
public class FileChannelAccessor extends AbstractFileAccessor {

   /**
    * Creates a new {@link FileChannelAccessor}.
    * 
    * @param file
    *           The {@link File} to be tested
    * @param bytesToWrite
    *           The byte array with the bytes to write.
    * @param bytesToRead
    *           The number of bytes to read.
    * @param bytesAtEnd
    *           The number of bytes to preserve at the end of file.
    * @param deleteFileAfterClose
    *           Whether to delete the file after closing or not.
    * @throws IOException
    *            whenever an I/O operation failed.
    */
   public FileChannelAccessor(File file, byte[] bytesToWrite, int bytesToRead,
      int bytesAtEnd, boolean deleteFileAfterClose) throws IOException {
      super(file, bytesToWrite, bytesToRead, bytesAtEnd, deleteFileAfterClose);

      m_raf = new RandomAccessFile(file, "rw");
   }

   /**
    * @see com.github.jmeta.tools.fileaccessperformance.api.services.access.AbstractFileAccessor#close()
    */
   @Override
   protected void doClose() throws IOException {

      m_raf.close();
   }

   /**
    * @see com.github.jmeta.tools.fileaccessperformance.api.services.access.AbstractFileAccessor#read(long, int)
    */
   @Override
   protected byte[] read(long offset, int length) throws IOException {

      ByteBuffer dataBuffer = ByteBuffer.allocate(length);

      final FileChannel channel = m_raf.getChannel();

      channel.position(offset);
      channel.read(dataBuffer);

      dataBuffer.flip();

      return dataBuffer.array();
   }

   /**
    * @see com.github.jmeta.tools.fileaccessperformance.api.services.access.AbstractFileAccessor#write(long, byte[])
    */
   @Override
   protected void write(long offset, byte[] bytesToWrite) throws IOException {

      final FileChannel channel = m_raf.getChannel();

      channel.position(offset);
      channel.write(ByteBuffer.wrap(bytesToWrite));
   }

   private RandomAccessFile m_raf;
}
