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

      try {
         doOpen();
         isOpened = true;
      } catch (Exception e) {
         throw new MediumAccessException("Could not open medium due to exception", e);
      }
   }

   @Override
   public void close() {

      Reject.ifFalse(isOpened(), "isOpened()");

      try {
         doClose();
         isOpened = false;
      }

      catch (Exception e) {
         throw new MediumAccessException("Could not close medium due to exception", e);
      }

   }

   /**
    * Concrete core implementation of {@link #close()}.
    * 
    * @throws Exception
    * 
    * @see de.je.jmeta.media.impl.IMediumAccessor#close()
    */
   protected abstract void doClose() throws Exception;

   /**
    * Opens the underlying {@link AbstractMedium} accessor.
    * 
    * @throws Exception
    *            if an exception occurred during opening.
    */
   protected abstract void doOpen() throws Exception;

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
   protected abstract void doRead(IMediumReference reference, ByteBuffer buffer)
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
   protected abstract void doWrite(IMediumReference reference, ByteBuffer buffer) throws Exception;

   @Override
   public T getMedium() {

      return medium;
   }

   @Override
   public boolean isOpened() {

      return isOpened;
   }

   @Override
   public void read(IMediumReference reference, ByteBuffer buffer) throws EndOfMediumException {

      Reject.ifFalse(isOpened(), "isOpened()");
      Reject.ifNull(buffer, "buffer");
      Reject.ifNull(reference, "reference");
      Reject.ifFalse(reference.getMedium().equals(getMedium()), "reference.getMedium().equals(getMedium())");
      if (getMedium().isRandomAccess())
         Reject.ifFalse(reference.getAbsoluteMediumOffset() < medium.getCurrentLength(),
            "reference.getAbsoluteMediumOffset() < medium.getCurrentLength()");

      if (buffer.remaining() == 0)
         return;

      try {
         buffer.mark();

         doRead(reference, buffer);
      }

      catch (IOException e) {
         throw new MediumAccessException(COULD_NOT_ACCESS_MEDIUM + getMedium(), e);
      }

      finally {
         // Reset initial position
         buffer.reset();
      }
   }

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
         doWrite(reference, buffer);
      }

      catch (Exception e) {
         throw new MediumAccessException(COULD_NOT_ACCESS_MEDIUM + getMedium(), e);
      }
   }
}
