/**
 * {@link FileMediumAccessor}.java
 *
 * @author Jens Ebert
 * @date 09.12.10 21:18:22 (December 9, 2010)
 */

package de.je.jmeta.media.impl;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.channels.OverlappingFileLockException;

import de.je.jmeta.media.api.IMediumReference;
import de.je.jmeta.media.api.datatype.AbstractMedium;
import de.je.jmeta.media.api.datatype.FileMedium;
import de.je.jmeta.media.api.exception.EndOfMediumException;
import de.je.jmeta.media.api.exception.MediumAccessException;
import de.je.util.javautil.common.err.Reject;

/**
 * Represents an {@link IMediumAccessor} that is a physical file with random access.
 */
public class FileMediumAccessor extends AbstractMediumAccessor<FileMedium> {

   private File file;

   private RandomAccessFile raf;

   private FileChannel fileChannel;

   private FileLock lock;

   /**
    * Creates a new {@link FileMediumAccessor}.
    * 
    * @param medium
    *           The {@link AbstractMedium} this class works on.
    */
   public FileMediumAccessor(FileMedium medium) {

      super(medium);
      lockMedium();
   }

   /**
    * @see de.je.jmeta.media.impl.IMediumAccessor#isAtEndOfMedium(de.je.jmeta.media.api.IMediumReference)
    */
   @Override
   public boolean isAtEndOfMedium(IMediumReference reference) {
      Reject.ifFalse(isOpened(), "isOpened()");

      return reference.getAbsoluteMediumOffset() >= getMedium().getCurrentLength();
   }

   /**
    * @see de.je.jmeta.media.impl.AbstractMediumAccessor#doClose()
    */
   @Override
   protected void doClose() throws IOException {

      unlockMedium();

      fileChannel.close();
      raf.close();
   }

   /**
    * @see de.je.jmeta.media.impl.AbstractMediumAccessor#doOpen()
    */
   @Override
   protected void doOpen() throws Exception {

      File wrappedfile = getMedium().getWrappedMedium();

      // Check if the file really exists. Because in rw mode RandomAccessFile
      // does simply
      // create a new file, which is not what we want.
      Reject.ifFalse(wrappedfile.exists(), "wrappedfile.exists()");

      this.file = wrappedfile;

      String mode = getMedium().isReadOnly() ? "r" : "rw";

      raf = new RandomAccessFile(wrappedfile, mode);
      fileChannel = raf.getChannel();
   }

   /**
    * @see de.je.jmeta.media.impl.AbstractMediumAccessor#doRead(IMediumReference, ByteBuffer)
    */
   @Override
   protected void doRead(IMediumReference reference, ByteBuffer buffer) throws IOException, EndOfMediumException {

      int bytesRead = 0;
      int size = buffer.remaining();
      int initialPosition = buffer.position();

      while (bytesRead < size) {
         final long readOffset = reference.getAbsoluteMediumOffset() + bytesRead;
         int returnCode = fileChannel.read(buffer, readOffset);

         if (returnCode == -1) {
            buffer.limit(initialPosition + bytesRead);

            throw new EndOfMediumException(bytesRead, reference, size);
         }

         bytesRead += returnCode;
      }
   }

   /**
    * @see de.je.jmeta.media.impl.AbstractMediumAccessor#doWrite(IMediumReference, ByteBuffer)
    */
   @Override
   protected void doWrite(IMediumReference reference, ByteBuffer buffer) throws Exception {

      int bytesWritten = 0;

      while (bytesWritten < buffer.capacity()) {
         bytesWritten += fileChannel.write(buffer, reference.getAbsoluteMediumOffset() + bytesWritten);
      }
   }

   /**
    * Locks the file medium, if it is not read-only.
    */
   private void lockMedium() {

      if (!getMedium().isReadOnly()) {
         // Using lock() instead blocks on Windows if a lock is already held
         try {
            lock = fileChannel.tryLock();
         } catch (IOException e) {
            throw new MediumAccessException("Could not lock medium due to exception", e);
         } catch (OverlappingFileLockException e) {
            throw new MediumAccessException("File is already locked in this JVM", e);
         }

         // Another process has locked the file already
         if (lock == null)
            throw new MediumAccessException("File is already locked by another process", null);
      }
   }

   /**
    * Locks the file medium, if locked.
    */
   private void unlockMedium() {

      if (lock != null)
         try {
            lock.release();
         } catch (IOException e) {
            throw new MediumAccessException("Could not unlock medium due to exception", e);
         }
   }
}
