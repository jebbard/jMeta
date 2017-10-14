/**
 *
 * {@link AbstractMediumAccessor}.java
 *
 * @author Jens
 *
 * @date 16.05.2015
 *
 */
package de.je.jmeta.media.impl;

import java.io.IOException;
import java.nio.ByteBuffer;

import de.je.jmeta.media.api.IMedium;
import de.je.jmeta.media.api.IMediumReference;
import de.je.jmeta.media.api.exception.EndOfMediumException;
import de.je.jmeta.media.api.exception.MediumAccessException;
import de.je.jmeta.media.api.exception.ReadOnlyMediumException;
import de.je.util.javautil.common.err.Reject;

/**
 * {@link AbstractMediumAccessor} is an abstract base class for easier implementation of an {@link IMediumAccessor}.
 *
 * @param <T>
 *           The type of {@link IMedium}
 */
public abstract class AbstractMediumAccessor<T extends IMedium<?>> implements IMediumAccessor<T> {

   private final T medium;

   private boolean isOpened;

   private IMediumReference currentPosition;

   /**
    * Creates a new {@link AbstractMediumAccessor}.
    * 
    * @param medium
    *           the {@link IMedium} this {@link AbstractMediumAccessor} works on.
    */
   public AbstractMediumAccessor(T medium) {

      Reject.ifNull(medium, "medium");
      Reject.ifFalse(medium.exists(), "medium.exists()");

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
    * @see de.je.jmeta.media.impl.IMediumAccessor#isOpened()
    */
   @Override
   public boolean isOpened() {
      return isOpened;
   }

   /**
    * @see de.je.jmeta.media.impl.IMediumAccessor#close()
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
    * @see de.je.jmeta.media.impl.IMediumAccessor#getMedium()
    */
   @Override
   public T getMedium() {
      return medium;
   }

   /**
    * @see de.je.jmeta.media.impl.IMediumAccessor#getCurrentPosition()
    */
   @Override
   public IMediumReference getCurrentPosition() {
      return currentPosition;
   }

   /**
    * @see de.je.jmeta.media.impl.IMediumAccessor#setCurrentPosition(de.je.jmeta.media.api.IMediumReference)
    */
   @Override
   public void setCurrentPosition(IMediumReference position) {
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
    * @see de.je.jmeta.media.impl.IMediumAccessor#read(java.nio.ByteBuffer)
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
    * @see de.je.jmeta.media.impl.IMediumAccessor#write(java.nio.ByteBuffer)
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
    * @see de.je.jmeta.media.impl.IMediumAccessor#truncate(de.je.jmeta.media.api.IMediumReference)
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
    *           The new {@link IMediumReference} position
    */
   protected void updateCurrentPosition(IMediumReference position) {

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
    * Concrete core implementation of {@link #setCurrentPosition(IMediumReference)}
    * 
    * @param position
    *           The new {@link IMediumReference} position to set
    * @throws IOException
    *            in case of anything goes wrong in the concrete implementation
    */
   protected abstract void mediumSpecificSetCurrentPosition(IMediumReference position) throws IOException;

   /**
    * Checks if the underlying {@link IMedium} is read-only, and if so, it throws a {@link ReadOnlyMediumException}.
    */
   private void preventWriteOnReadyOnlyMedium() {
      if (getMedium().isReadOnly()) {
         throw new ReadOnlyMediumException(getMedium(), null);
      }
   }
}
