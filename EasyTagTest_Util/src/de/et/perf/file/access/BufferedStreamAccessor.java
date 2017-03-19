/**
 *
 * {@link BufferedStreamPerformance}.java
 *
 * @author Jens Ebert
 *
 * @date 02.05.2010
 *
 */

package de.et.perf.file.access;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * {@link BufferedStreamAccessor} measures performance of {@link FileInputStream} and {@link FileOutputStream} access
 * with {@link BufferedInputStream} and {@link BufferedOutputStream}.
 */
public class BufferedStreamAccessor extends StreamAccessor {

   /**
    * Creates a new {@link BufferedStreamAccessor}.
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
    *           true to delete the file after closing it, false otherwise
    * @throws IOException
    *            whenever an I/O operation failed.
    */
   public BufferedStreamAccessor(File file, byte[] bytesToWrite,
      int bytesToRead, int bytesAtEnd, boolean deleteFileAfterClose)
         throws IOException {
      super(file, bytesToWrite, bytesToRead, bytesAtEnd, deleteFileAfterClose);
   }

   /**
    * @see de.et.perf.file.access.StreamAccessor#createInputStream(java.io.File)
    */
   @Override
   protected InputStream createInputStream(File file)
      throws FileNotFoundException {

      return new BufferedInputStream(super.createInputStream(file));
   }

   /**
    * @see de.et.perf.file.access.StreamAccessor#createOutputStream(java.io.File)
    */
   @Override
   protected OutputStream createOutputStream(File file)
      throws FileNotFoundException {

      return new BufferedOutputStream(super.createOutputStream(file));
   }
}
