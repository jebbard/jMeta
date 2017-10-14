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
import de.je.jmeta.media.api.datatype.AbstractMedium;
import de.je.jmeta.media.api.exception.EndOfMediumException;
import de.je.jmeta.media.api.exception.MediumAccessException;
import de.je.jmeta.media.api.exception.ReadOnlyMediumException;
import de.je.jmeta.media.api.exception.ReadTimedOutException;
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

   private static final String INVALID_REFERENCE = "The given reference does not point to the same medium";

   private static final String MEDIUM_IS_NOT_OPENED = "Medium is not opened: ";

   private static final String COULD_NOT_ACCESS_MEDIUM = "Could not access medium: ";

   /**
    * Creates a new {@link AbstractMediumAccessor}.
    * 
    * @param medium
    *           the {@link IMedium} this {@link AbstractMediumAccessor} works on.
    */
   public AbstractMediumAccessor(T medium) {

      Reject.ifNull(medium, "medium");

      this.isOpened = false;
      this.medium = medium;

      Reject.ifFalse(medium.exists(), "medium.exists()");
      try {
         mediumSpecificOpen();
         isOpened = true;
      } catch (Exception e) {
         throw new MediumAccessException("Could not open medium due to exception", e);
      }
   }

   /**
    * @see de.je.jmeta.media.impl.IMediumAccessor#getMedium()
    */
   @Override
   public T getMedium() {

      return medium;
   }

   @Override
   public void close() {

      Reject.ifFalse(isOpened(), "isOpened()");

      try {
         mediumSpecificClose();
         isOpened = false;
      }

      catch (Exception e) {
         throw new MediumAccessException("Could not close medium due to exception", e);
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
    * @see de.je.jmeta.media.impl.IMediumAccessor#read(de.je.jmeta.media.api.IMediumReference, java.nio.ByteBuffer)
    */
   @Override
   public void read(IMediumReference reference, ByteBuffer buffer) throws EndOfMediumException {

      Reject.ifFalse(isOpened(), "isOpened()");
      Reject.ifNull(buffer, "buffer");

      if (getMedium().isRandomAccess()) {

         Reject.ifNull(reference, "reference");
         Reject.ifFalse(reference.getMedium().equals(getMedium()), "reference.getMedium().equals(getMedium())");
         Reject.ifFalse(reference.getAbsoluteMediumOffset() < medium.getCurrentLength(),
            "reference.getAbsoluteMediumOffset() < medium.getCurrentLength()");
      } else {
         Reject.ifTrue(reference != IMediumAccessor.NEXT_BYTES, "reference != IMediumAccessor.NEXT_BYTES");
      }

      if (buffer.remaining() == 0)
         return;

      try {
         buffer.mark();

         mediumSpecificRead(reference, buffer);
      }

      catch (IOException e) {
         throw new MediumAccessException(COULD_NOT_ACCESS_MEDIUM + getMedium(), e);
      }

      finally {
         // Reset initial position
         buffer.reset();
      }
   }

   /**
    * @see de.je.jmeta.media.impl.IMediumAccessor#write(de.je.jmeta.media.api.IMediumReference, java.nio.ByteBuffer)
    */
   @Override
   public void write(IMediumReference reference, ByteBuffer buffer) {

      Reject.ifFalse(isOpened(), "isOpened()");
      Reject.ifNull(buffer, "buffer");
      Reject.ifNull(reference, "reference");
      Reject.ifFalse(reference.getMedium().equals(getMedium()), "reference.getMedium().equals(getMedium())");

      if (getMedium().isRandomAccess())
         Reject.ifFalse(reference.getAbsoluteMediumOffset() < medium.getCurrentLength(),
            "reference.getAbsoluteMediumOffset() < medium.getCurrentLength()");

      if (medium.isReadOnly())
         throw new ReadOnlyMediumException(medium, null);

      try {
         mediumSpecificWrite(reference, buffer);
      }

      catch (Exception e) {
         throw new MediumAccessException(COULD_NOT_ACCESS_MEDIUM + getMedium(), e);
      }
   }

   /**
    * @see de.je.jmeta.media.impl.IMediumAccessor#truncate(de.je.jmeta.media.api.IMediumReference)
    */
   @Override
   public void truncate(IMediumReference newEndOffset) {
      Reject.ifFalse(isOpened(), "isOpened()");
      Reject.ifNull(newEndOffset, "newEndOffset");
      Reject.ifFalse(newEndOffset.getMedium().equals(getMedium()), "newEndOffset.getMedium().equals(getMedium())");

      if (getMedium().isReadOnly()) {
         throw new ReadOnlyMediumException(getMedium(), null);
      }

      if (newEndOffset.getAbsoluteMediumOffset() < getMedium().getCurrentLength()) {
         mediumSpecificTruncate(newEndOffset);
      }
   }

   /**
    * Concrete core implementation of {@link #close()}.
    * 
    * @throws Exception
    * 
    * @see de.je.jmeta.media.impl.IMediumAccessor#close()
    */
   protected abstract void mediumSpecificClose() throws Exception;

   /**
    * Opens the underlying {@link AbstractMedium} accessor.
    * 
    * @throws Exception
    *            if an exception occurred during opening.
    */
   protected abstract void mediumSpecificOpen() throws Exception;

   /**
    * Concrete core implementation of {@link #read(IMediumReference, ByteBuffer)}
    * 
    * @param reference
    * @see de.je.jmeta.media.impl.IMediumAccessor#read(IMediumReference, ByteBuffer)
    * @param buffer
    * @see de.je.jmeta.media.impl.IMediumAccessor#read(IMediumReference, ByteBuffer)
    * @throws IOException
    *            If an I/O operation failed.
    * @throws EndOfMediumException
    *            If end of medium has been reached.
    * @throws ReadTimedOutException
    *            If the read operation timed out.
    */
   protected abstract void mediumSpecificRead(IMediumReference reference, ByteBuffer buffer)
      throws IOException, EndOfMediumException;

   /**
    * Concrete core implementation of {@link #write(IMediumReference, ByteBuffer)}
    * 
    * @param reference
    * @see de.je.jmeta.media.impl.IMediumAccessor#write(IMediumReference, ByteBuffer)
    * @param buffer
    * @see de.je.jmeta.media.impl.IMediumAccessor#write(IMediumReference, ByteBuffer)
    * 
    * @throws Exception
    */
   protected abstract void mediumSpecificWrite(IMediumReference reference, ByteBuffer buffer) throws Exception;

   /**
    * Performs the actual {@link IMedium} specific truncation up to the given {@link IMediumReference}.
    * 
    * @param newEndOffset
    *           The new {@link IMediumReference} pointing to the new end offset
    */
   protected abstract void mediumSpecificTruncate(IMediumReference newEndOffset);
}
