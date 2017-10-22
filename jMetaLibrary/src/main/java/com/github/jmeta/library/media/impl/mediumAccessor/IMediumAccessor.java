/**
 *
 * {@link IMediumAccessor}.java
 *
 * @author Jens
 *
 * @date 16.05.2015
 *
 */
package com.github.jmeta.library.media.impl.mediumAccessor;

import java.nio.ByteBuffer;

import com.github.jmeta.library.media.api.exceptions.EndOfMediumException;
import com.github.jmeta.library.media.api.exceptions.MediumAccessException;
import com.github.jmeta.library.media.api.exceptions.ReadOnlyMediumException;
import com.github.jmeta.library.media.api.types.IMedium;
import com.github.jmeta.library.media.api.types.IMediumReference;
import com.github.jmeta.utility.dbc.api.exceptions.PreconditionUnfullfilledException;

/**
 * This class provides the mechanisms to read data from and write data to an {@link IMedium}. We can basically
 * distinguish two types of media for which the behavior differs correspondingly:
 * <ul>
 * <li><b>Random-access media:</b> Allow random-access to all bytes of the medium. You can use
 * {@link #setCurrentPosition(IMediumReference)} to jump to a specific {@link IMediumReference} on the medium and then
 * call {@link #read(ByteBuffer)}, {@link #write(ByteBuffer)}, {@link #truncate(IMediumReference)} or
 * {@link #isAtEndOfMedium(IMediumReference)}.</li>
 * <li><b>Stream-based media:</b> Doe not allow random-access but only sequential reading of medium bytes.
 * {@link #setCurrentPosition(IMediumReference)} does not have an effect, only {@link #read(ByteBuffer)} advances the
 * current position.</li>
 * </ul>
 * 
 * In addition, an {@link IMedium} might be read-only, such that the methods {@link #write(ByteBuffer)} and
 * {@link #truncate(IMediumReference)} cannot be used on these media and will throwing a runtime exception.
 * 
 * Stream-based media are always also read-only, while random-access media might or might not be read-only.
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
    * Returns whether this {@link IMediumAccessor} is currently opened or closed. A newly created instance of an
    * implementing class must be initially open. When this {@link IMediumAccessor} is closed, all other methods except
    * {@link #getMedium()} and {@link #getCurrentPosition()} when called on this instance will throw a
    * {@link PreconditionUnfullfilledException}.
    * 
    * @return Returns whether this {@link IMediumAccessor} is currently opened (true) or closed (false).
    */
   public boolean isOpened();

   /**
    * Closes this {@link IMediumAccessor}. All other methods except {@link #getMedium()}, {@link #getCurrentPosition()}
    * and {@link #isOpened()} cannot be used anymore if the {@link IMediumAccessor} is closed. They will throw a
    * {@link PreconditionUnfullfilledException} in this case.
    */
   public void close();

   /**
    * @return the {@link IMedium} this {@link IMediumAccessor} is using.
    */
   public T getMedium();

   /**
    * Returns the current {@link IMediumReference} position of this {@link IMediumAccessor}. The current position is the
    * position used for the next calls to {@link #read(ByteBuffer)}, {@link #truncate(IMediumReference)} and
    * {@link #write(ByteBuffer)} as well as for * calls to {@link #isAtEndOfMedium(IMediumReference)}.
    * 
    * For random-access media, the current position can be changed using {@link #setCurrentPosition(IMediumReference)},
    * {@link #write(ByteBuffer)} (changes by the number of written bytes) and {@link #read(ByteBuffer)} (changes by the
    * number of read bytes).
    * 
    * For non-random-access media, calls to {@link #setCurrentPosition(IMediumReference)} do not have any effect. Only
    * calls to {@link #read(ByteBuffer)} will change the current position for these media types.
    * 
    * After opening an {@link IMediumAccessor}, the current position always points to medium offset 0.
    * 
    * @return The current {@link IMediumReference} position of this {@link IMediumAccessor}
    */
   public IMediumReference getCurrentPosition();

   /**
    * Sets the current position of this {@link IMediumAccessor}. Only has an effect for random-access media. See
    * {@link #getCurrentPosition()} for more details. Positions set must not be behind the current medium length.
    * However, you can call this method with a position just behind the last byte of the medium, e.g. for extending it.
    * 
    * @param position
    *           The new {@link IMediumReference} position to set. Must not be behind the current medium length. Must
    *           refer to the same {@link IMedium} as this {@link IMediumAccessor}.
    */
   public void setCurrentPosition(IMediumReference position);

   /**
    * Returns whether the current position as returned by {@link #getCurrentPosition()} is at end of the {@link IMedium}
    * . The method may potentially block forever as it might require reading a byte to detect end of medium.
    * 
    * @return Returns whether the current position is at end of the {@link IMedium} used
    */
   public boolean isAtEndOfMedium();

   /**
    * Reads bytes from the reading {@link IMedium} starting from the current position as returned by
    * {@link #getCurrentPosition()}. This method may block until the number of bytes requested are available
    * (potentially forever). This method advances the current position of the medium by the bytes really read.
    * 
    * @param buffer
    *           The {@link ByteBuffer} to be filled with read bytes. The buffer is filled starting with its current
    *           position up to its limit, i.e. the read byte count, at maximum, is buffer.remaining() at the moment of
    *           method invocation. The returned {@link ByteBuffer} is reset, i.e. its position is set to its position
    *           before the method call. Its mark is also set to that position. Its limit is set to its position plus the
    *           bytes really read.
    * 
    * @throws EndOfMediumException
    *            If the end of {@link IMedium} has been reached during reading. The {@link ByteBuffer} returned is
    *            guaranteed to hold all the bytes up to end of {@link IMedium} when this exception is thrown. The
    *            exception itself contains the number of bytes really read, the {@link ByteBuffer}s remaining bytes
    *            equals that value.
    * @throws MediumAccessException
    *            in case of any errors during medium access
    */
   public void read(ByteBuffer buffer) throws EndOfMediumException;

   /**
    * Writes bytes to the {@link IMediumAccessor} starting at the current position as returned by
    * {@link #getCurrentPosition()}. This operation must only be called for a writable {@link IMedium}, otherwise it
    * will throw a {@link PreconditionUnfullfilledException}. This operation might block until all bytes are written
    * successfully, possibly forever. The writing {@link IMedium} length might extend if the current position plus the
    * remaining size of the written {@link ByteBuffer} is bigger than the current {@link IMedium} length. The
    * {@link IMedium} is always only extended by this difference. This method advances the current position of the
    * medium by the bytes written.
    * 
    * The contents and properties of the specified {@link ByteBuffer} are not changed except for its position which is
    * increased by the number of bytes written.
    * 
    * @param buffer
    *           The {@link ByteBuffer} to be written. It is written starting from its current position up to its limit.
    *           After the method returned, its position has increased by the number of bytes written, i.e. its
    *           {@link ByteBuffer#remaining()} method returns 0.
    * 
    * @throws ReadOnlyMediumException
    *            if the {@link IMedium} is read-only
    * @throws MediumAccessException
    *            in case of any errors during medium access
    */
   public void write(ByteBuffer buffer);

   /**
    * Truncates the {@link IMedium} at the current position as returned by {@link #getCurrentPosition()}. This
    * effectively shortens the {@link IMedium} by clipping at and all previously contained bytes behind the new current
    * position. The current position remains unchanged, i.e. points to the end of the medium after truncation.
    * 
    * @throws ReadOnlyMediumException
    *            if the {@link IMedium} is read-only
    * @throws MediumAccessException
    *            in case of any errors during medium access
    */
   public void truncate();
}
