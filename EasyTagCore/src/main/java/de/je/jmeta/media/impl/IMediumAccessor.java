/**
 *
 * {@link IMediumAccessor}.java
 *
 * @author Jens
 *
 * @date 16.05.2015
 *
 */
package de.je.jmeta.media.impl;

import java.nio.ByteBuffer;

import de.je.jmeta.media.api.IMedium;
import de.je.jmeta.media.api.IMediumReference;
import de.je.jmeta.media.api.exception.EndOfMediumException;
import de.je.jmeta.media.api.exception.MediumAccessException;
import de.je.jmeta.media.api.exception.ReadTimedOutException;

/**
 * This class represents an object that reads from and stores data on a {@link IMedium}. The {@link IMediumAccessor} can
 * be used in either a reading or writing way or both. To use the {@link IMediumAccessor}, it must be opened, which it
 * initially is. If there are any access errors when reading or writing using an {@link IMediumAccessor}, a
 * {@link MediumAccessException} is thrown. As soon as the {@link IMediumAccessor} is closed, it cannot be used anymore.
 *
 * An {@link IMediumAccessor} has the following additional properties:
 * <li>
 * <ul>
 * Allows random access or only sequential access. A random-access {@link IMedium} has a size, too.
 * </ul>
 * <ul>
 * Is read-only or additionally allows write access.
 * </ul>
 * <ul>
 * May block when reading, or may not block. Due to the underlying Java APIs, there are only two options to read with
 * {@link IMediumAccessor} that exclude each other: either read with a timeout which prohibits finding out if end of
 * medium is reached, or read without timeout which possibly blocks forever but allows for end of medium detection.
 * </ul>
 * </li>
 *
 * There should only be one {@link IMediumAccessor} per process for the same {@link IMedium}, i.e. when multithreading,
 * the same instance must be used by all threads for accessing the same {@link IMedium}. This is because the locking
 * mechanism: When writing with two different {@link IMediumAccessor} instances to the same {@link IMedium}, an
 * exception is likely to occur or one thread will block. The implementations of {@link IMediumAccessor} should prohibit
 * creating two instances for the same {@link IMedium}.
 * 
 * @param <T>
 *           The type of {@link IMedium}
 */
public interface IMediumAccessor<T extends IMedium<?>> {

   /**
    * Closes this {@link IMediumAccessor}. All access methods cannot be used if the {@link IMediumAccessor} is closed.
    * 
    * @post #isOpened() == false
    */
   public void close();

   /**
    * Returns the {@link IMedium} this {@link IMediumAccessor} is using.
    * 
    * @return the {@link IMedium} this {@link IMediumAccessor} is using.
    */
   public T getMedium();

   /**
    * Returns whether the given offset is at end of the {@link IMedium} used for reading. The method may potentially
    * block forever as it might require reading a byte to detect end of medium. There is not timeout option.
    * 
    * @param reference
    *           The {@link IMediumReference} to check for end of medium. Must refer to the same {@link IMedium} as this
    *           {@link IMediumAccessor} uses.
    * 
    * @return Returns whether the given {@link IMediumReference} is at end of the {@link IMedium} used.
    * 
    * @pre {@link #getMedium()}.equals(reference.getMedium())
    */
   public boolean isAtEndOfMedium(IMediumReference reference);

   /**
    * Returns whether this {@link IMediumAccessor} is currently opened or closed. A newly created instance of an
    * implementing class must be initially open.
    * 
    * @return Returns whether this {@link IMediumAccessor} is currently opened (true) or closed (false).
    */
   public boolean isOpened();

   /**
    * Reads bytes from the reading {@link IMedium} starting with the given {@link IMediumReference}. This method may
    * block until the number of bytes requested are available (potentially forever). Some specific {@link IMedium}
    * implementations might allow to specify a maximum blocking timeout - see these method descriptions for details on
    * their influence on read behavior.
    * 
    * @param reference
    *           The {@link IMediumReference} to read the data from the {@link IMedium}. Must not exceed current
    *           {@link IMedium} length. The value of this parameter is ignored if this is not a random-access
    *           {@link IMediumAccessor}. In this case, the read is done from the current position of the {@link IMedium}
    *           . Must refer to the same {@link IMedium} as this {@link IMediumAccessor}.
    * @param buffer
    *           The {@link ByteBuffer} to be filled with read bytes. The buffer is filled starting with its current
    *           position up to its limit, i.e. the read byte count, at maximum, is buffer.remaining() at the moment of
    *           method invocation. The returned {@link ByteBuffer} is reset, i.e. its position is set to its position
    *           before the method call. Its mark is also set to that position. Its limit is set to its position plus the
    *           bytes really read.
    * @throws EndOfMediumException
    *            If the end of {@link IMedium} has been reached during reading. The {@link ByteBuffer} returned is
    *            guaranteed to hold all the bytes up to end of {@link IMedium} when this exception is thrown. The
    *            exception itself contains the number of bytes really read, the {@link ByteBuffer}s remaining bytes
    *            equals that value.
    * @throws ReadTimedOutException
    *            If during reading the timeout (if set) has expired prematurely, i.e. before the full number of bytes
    *            requested has been read. The {@link ByteBuffer} returned is guaranteed to hold all the bytes read up to
    *            expiration of the timeout when this exception is thrown. The exception itself contains the number of
    *            bytes really read, the {@link ByteBuffer}s remaining bytes equals that value.
    * 
    * @pre reference.getAbsoluteMediumOffset() < {@link IMedium#getCurrentLength()}
    * @pre {@link #isOpened()}
    * @pre {@link #getMedium()}.equals(reference.getMedium())
    */
   public void read(IMediumReference reference, ByteBuffer buffer)
      throws EndOfMediumException;

   /**
    * Writes bytes to the {@link IMediumAccessor} at the given {@link IMediumReference}. This operation is only possible
    * if the {@link IMediumAccessor} is not read-only. This operation might block until all bytes are written
    * successfully, possibly forever. The writing {@link IMedium} length might extend if the {@link IMediumReference}
    * plus the remaining size of the written {@link ByteBuffer} is bigger than the current writing {@link IMedium}
    * length. The writing {@link IMedium} is always only extended by this difference.
    * 
    * The contents and properties of the specified {@link ByteBuffer} are not changed except for its position which is
    * increased by the number of bytes written.
    * 
    * Note that there is currently no possibility for an unblocked write.
    * 
    * @param reference
    *           The {@link IMediumReference} to write the data. Must not exceed current writing {@link IMedium} length.
    *           The value of this parameter is ignored if this is not a random-access writing {@link IMedium}. In this
    *           case, the write is done at the current position of the writing {@link IMedium}. The
    *           {@link IMediumReference} must refer to the reading {@link IMedium} of this {@link IMediumAccessor}.
    * @param buffer
    *           The {@link ByteBuffer} to be written. It is written starting from its current position up to its limit.
    *           After the method returned, its position has increased by the number of bytes written, i.e. its remaining
    *           method returns 0.
    * 
    * @pre #isReadOnly() == false
    * @pre {@link #isOpened()}
    * @pre {@link #getMedium()}.equals(reference.getMedium())
    * @pre reference.getAbsoluteMediumOffset() < {@link IMedium#getCurrentLength()}
    * 
    * @post buffer.remaining() == 0
    */
   public void write(IMediumReference reference, ByteBuffer buffer);
}
