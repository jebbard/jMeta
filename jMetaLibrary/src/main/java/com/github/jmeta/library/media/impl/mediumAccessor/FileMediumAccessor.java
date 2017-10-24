/**
 * {@link FileMediumAccessor}.java
 *
 * @author Jens Ebert
 * @date 09.12.10 21:18:22 (December 9, 2010)
 */

package com.github.jmeta.library.media.impl.mediumAccessor;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.channels.OverlappingFileLockException;
import java.nio.file.StandardOpenOption;

import com.github.jmeta.library.media.api.exceptions.EndOfMediumException;
import com.github.jmeta.library.media.api.exceptions.MediumAccessException;
import com.github.jmeta.library.media.api.types.AbstractMedium;
import com.github.jmeta.library.media.api.types.FileMedium;
import com.github.jmeta.library.media.api.types.MediumReference;
import com.github.jmeta.utility.dbc.api.services.Reject;

/**
 * Represents an {@link MediumAccessor} that is a physical file with random access.
 */
public class FileMediumAccessor extends AbstractMediumAccessor<FileMedium> {

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
   }

   /**
    * @see com.github.jmeta.library.media.impl.mediumAccessor.MediumAccessor#isAtEndOfMedium(com.github.jmeta.library.media.api.types.MediumReference)
    */
   @Override
   public boolean isAtEndOfMedium() {
      Reject.ifFalse(isOpened(), "isOpened()");

      return getCurrentPosition().getAbsoluteMediumOffset() >= getMedium().getCurrentLength();
   }

   /**
    * @see com.github.jmeta.library.media.impl.mediumAccessor.AbstractMediumAccessor#mediumSpecificOpen()
    */
   @Override
   protected void mediumSpecificOpen() throws IOException {

      if (getMedium().isReadOnly()) {
         fileChannel = FileChannel.open(getMedium().getWrappedMedium(), StandardOpenOption.READ);
      } else {
         fileChannel = FileChannel.open(getMedium().getWrappedMedium(), StandardOpenOption.READ,
            StandardOpenOption.WRITE);

         lockMedium();
      }
   }

   /**
    * @see com.github.jmeta.library.media.impl.mediumAccessor.AbstractMediumAccessor#mediumSpecificClose()
    */
   @Override
   protected void mediumSpecificClose() throws IOException {
      unlockMedium();

      fileChannel.close();
   }

   /**
    * @see com.github.jmeta.library.media.impl.mediumAccessor.AbstractMediumAccessor#mediumSpecificRead(MediumReference, ByteBuffer)
    */
   @Override
   protected void mediumSpecificRead(ByteBuffer buffer) throws IOException, EndOfMediumException {

      int bytesRead = 0;
      int size = buffer.remaining();
      int initialPosition = buffer.position();
      MediumReference readOffsetRef = getCurrentPosition();

      while (bytesRead < size) {
         final long readOffset = readOffsetRef.getAbsoluteMediumOffset() + bytesRead;
         int returnCode = fileChannel.read(buffer, readOffset);

         if (returnCode == -1) {
            buffer.limit(initialPosition + bytesRead);

            updateCurrentPosition(readOffsetRef.advance(bytesRead));

            throw new EndOfMediumException(bytesRead, readOffsetRef, size);
         }

         bytesRead += returnCode;
      }

      updateCurrentPosition(getCurrentPosition().advance(bytesRead));
   }

   /**
    * @see com.github.jmeta.library.media.impl.mediumAccessor.AbstractMediumAccessor#mediumSpecificWrite(MediumReference, ByteBuffer)
    */
   @Override
   protected void mediumSpecificWrite(ByteBuffer buffer) throws IOException {

      int bytesWritten = 0;

      while (bytesWritten < buffer.capacity()) {
         bytesWritten += fileChannel.write(buffer, getCurrentPosition().getAbsoluteMediumOffset() + bytesWritten);
      }

      updateCurrentPosition(getCurrentPosition().advance(bytesWritten));
   }

   /**
    * @see com.github.jmeta.library.media.impl.mediumAccessor.AbstractMediumAccessor#mediumSpecificTruncate()
    */
   @Override
   protected void mediumSpecificTruncate() throws IOException {
      fileChannel.truncate(getCurrentPosition().getAbsoluteMediumOffset());
   }

   /**
    * @see com.github.jmeta.library.media.impl.mediumAccessor.AbstractMediumAccessor#mediumSpecificSetCurrentPosition(com.github.jmeta.library.media.api.types.MediumReference)
    */
   @Override
   protected void mediumSpecificSetCurrentPosition(MediumReference position) throws IOException {
      Reject.ifTrue(position.getAbsoluteMediumOffset() > getMedium().getCurrentLength(),
         "position.getAbsoluteMediumOffset() > getMedium().getCurrentLength()");

      updateCurrentPosition(position);
   }

   /**
    * Locks the file medium, if it is not read-only.
    */
   private void lockMedium() {

      try {
         // Using lock() instead blocks on Windows if a lock is already held
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
