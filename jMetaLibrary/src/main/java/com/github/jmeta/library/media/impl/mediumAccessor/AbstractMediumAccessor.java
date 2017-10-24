/**
 *
 * {@link AbstractMediumAccessor}.java
 *
 * @author Jens
 *
 * @date 16.05.2015
 *
 */
package com.github.jmeta.library.media.impl.mediumAccessor;

import java.io.IOException;
import java.nio.ByteBuffer;

import com.github.jmeta.library.media.api.exceptions.EndOfMediumException;
import com.github.jmeta.library.media.api.exceptions.MediumAccessException;
import com.github.jmeta.library.media.api.exceptions.ReadOnlyMediumException;
import com.github.jmeta.library.media.api.types.Medium;
import com.github.jmeta.library.media.api.types.MediumReference;
import com.github.jmeta.library.media.impl.reference.StandardMediumReference;
import com.github.jmeta.utility.dbc.api.services.Reject;

/**
 * {@link AbstractMediumAccessor} is an abstract base class for easier implementation of an {@link MediumAccessor}.
 *
 * @param <T>
 *           The type of {@link Medium}
 */
public abstract class AbstractMediumAccessor<T extends Medium<?>> implements MediumAccessor<T> {

   private final T medium;

   private boolean isOpened;

   private MediumReference currentPosition;

   /**
    * Creates a new {@link AbstractMediumAccessor}.
    * 
    * @param medium
    *           the {@link Medium} this {@link AbstractMediumAccessor} works on.
    */
   public AbstractMediumAccessor(T medium) {

      Reject.ifNull(medium, "medium");

      this.isOpened = false;
      this.medium = medium;

      try {
         mediumSpecificOpen();
         isOpened = true;
         updateCurrentPosition(new StandardMediumReference(getMedium(), 0));
      } catch (IOException e) {
         throw new MediumAccessException("Could not open medium due to exception", e);
      }
   }

   /**
    * @see com.github.jmeta.library.media.impl.mediumAccessor.MediumAccessor#isOpened()
    */
   @Override
   public boolean isOpened() {
      return isOpened;
   }

   /**
    * @see com.github.jmeta.library.media.impl.mediumAccessor.MediumAccessor#close()
    */
   @Override
   public void close() {

      Reject.ifFalse(isOpened(), "isOpened()");

      try {
         mediumSpecificClose();
         isOpened = false;
      }

      catch (IOException e) {
         throw new MediumAccessException("Could not close medium due to exception", e);
      }

   }

   /**
    * @see com.github.jmeta.library.media.impl.mediumAccessor.MediumAccessor#getMedium()
    */
   @Override
   public T getMedium() {
      return medium;
   }

   /**
    * @see com.github.jmeta.library.media.impl.mediumAccessor.MediumAccessor#getCurrentPosition()
    */
   @Override
   public MediumReference getCurrentPosition() {
      return currentPosition;
   }

   /**
    * @see com.github.jmeta.library.media.impl.mediumAccessor.MediumAccessor#setCurrentPosition(com.github.jmeta.library.media.api.types.MediumReference)
    */
   @Override
   public void setCurrentPosition(MediumReference position) {
      Reject.ifNull(position, "position");
      Reject.ifFalse(position.getMedium().equals(getMedium()), "reference.getMedium().equals(getMedium())");
      Reject.ifFalse(isOpened(), "isOpened()");

      try {
         mediumSpecificSetCurrentPosition(position);
      } catch (IOException e) {
         throw new MediumAccessException("Could not set the current position for " + getMedium(), e);
      }
   }

   /**
    * @see com.github.jmeta.library.media.impl.mediumAccessor.MediumAccessor#read(java.nio.ByteBuffer)
    */
   @Override
   public void read(ByteBuffer buffer) throws EndOfMediumException {

      Reject.ifFalse(isOpened(), "isOpened()");
      Reject.ifNull(buffer, "buffer");

      if (buffer.remaining() == 0) {
         return;
      }

      try {
         buffer.mark();

         mediumSpecificRead(buffer);
      }

      catch (IOException e) {
         throw new MediumAccessException("Could not not read from " + getMedium(), e);
      }

      finally {
         buffer.reset();
      }
   }

   /**
    * @see com.github.jmeta.library.media.impl.mediumAccessor.MediumAccessor#write(java.nio.ByteBuffer)
    */
   @Override
   public void write(ByteBuffer buffer) {

      Reject.ifNull(buffer, "buffer");
      Reject.ifFalse(isOpened(), "isOpened()");

      preventWriteOnReadyOnlyMedium();

      try {
         mediumSpecificWrite(buffer);
      }

      catch (IOException e) {
         throw new MediumAccessException("Could not not write to " + getMedium(), e);
      }
   }

   /**
    * @see com.github.jmeta.library.media.impl.mediumAccessor.MediumAccessor#truncate(com.github.jmeta.library.media.api.types.MediumReference)
    */
   @Override
   public void truncate() {
      Reject.ifFalse(isOpened(), "isOpened()");

      preventWriteOnReadyOnlyMedium();

      try {
         mediumSpecificTruncate();
      } catch (IOException e) {
         throw new MediumAccessException("Could not truncate medium due to exception", e);
      }
   }

   /**
    * Updates the position returned by {@link #getCurrentPosition()}.
    * 
    * @param position
    *           The new {@link MediumReference} position
    */
   protected void updateCurrentPosition(MediumReference position) {

      currentPosition = position;
   }

   /**
    * Concrete core implementation of for opening access to the underlying medium.
    * 
    * @throws IOException
    *            in case of anything goes wrong in the concrete implementation
    */
   protected abstract void mediumSpecificOpen() throws IOException;

   /**
    * Concrete core implementation of {@link #close()}.
    * 
    * @throws IOException
    *            in case of anything goes wrong in the concrete implementation
    */
   protected abstract void mediumSpecificClose() throws IOException;

   /**
    * Concrete core implementation of {@link #read(ByteBuffer)}.
    * 
    * @param buffer
    *           The {@link ByteBuffer} taking the bytes to read
    * @throws IOException
    *            in case of anything goes wrong in the concrete implementation
    */
   protected abstract void mediumSpecificRead(ByteBuffer buffer) throws IOException, EndOfMediumException;

   /**
    * Concrete core implementation of {@link #write(ByteBuffer)}
    * 
    * @param buffer
    *           The {@link ByteBuffer} holding the bytes to write
    * @throws IOException
    *            in case of anything goes wrong in the concrete implementation
    */
   protected abstract void mediumSpecificWrite(ByteBuffer buffer) throws IOException;

   /**
    * Concrete core implementation of {@link #truncate()}
    * 
    * @throws IOException
    *            in case of anything goes wrong in the concrete implementation
    */
   protected abstract void mediumSpecificTruncate() throws IOException;

   /**
    * Concrete core implementation of {@link #setCurrentPosition(MediumReference)}
    * 
    * @param position
    *           The new {@link MediumReference} position to set
    * @throws IOException
    *            in case of anything goes wrong in the concrete implementation
    */
   protected abstract void mediumSpecificSetCurrentPosition(MediumReference position) throws IOException;

   /**
    * Checks if the underlying {@link Medium} is read-only, and if so, it throws a {@link ReadOnlyMediumException}.
    */
   private void preventWriteOnReadyOnlyMedium() {
      if (getMedium().isReadOnly()) {
         throw new ReadOnlyMediumException(getMedium(), null);
      }
   }
}
