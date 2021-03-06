/**
 *
 * {@link MediumAccessor}.java
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
import com.github.jmeta.library.media.api.types.Medium;
import com.github.jmeta.library.media.api.types.MediumOffset;
import com.github.jmeta.utility.dbc.api.exceptions.PreconditionUnfullfilledException;

/**
 * This class provides the mechanisms to read data from and write data to a
 * {@link Medium}. We can basically distinguish two types of media for which the
 * behavior differs correspondingly:
 * <ul>
 * <li><b>Random-access media:</b> Allow random-access to all bytes of the
 * medium. You can use {@link #setCurrentPosition(MediumOffset)} to jump to a
 * specific {@link MediumOffset} on the medium and then call {@link #read(int)},
 * {@link #write(ByteBuffer)}, {@link #truncate()} or
 * {@link #isAtEndOfMedium()}.</li>
 * <li><b>Stream-based media:</b> Doe not allow random-access but only
 * sequential reading of medium bytes. {@link #setCurrentPosition(MediumOffset)}
 * does not have an effect, only {@link #read(int)} advances the current
 * position.</li>
 * </ul>
 *
 * In addition, an {@link Medium} might be read-only, such that the methods
 * {@link #write(ByteBuffer)} and {@link #truncate()} cannot be used on these
 * media and will throwing a runtime exception.
 *
 * Stream-based media are always also read-only, while random-access media might
 * or might not be read-only.
 *
 * There should only be one {@link MediumAccessor} per process for the same
 * {@link Medium}, i.e. when multi-threading, the same instance must be used by
 * all threads for accessing the same {@link Medium}. This is because the
 * locking mechanism: When writing with two different {@link MediumAccessor}
 * instances to the same {@link Medium}, an exception is likely to occur or one
 * thread will block. The implementations of {@link MediumAccessor} should
 * prohibit creating two instances for the same {@link Medium}.
 *
 * @param <T> The type of {@link Medium}
 */
public interface MediumAccessor<T extends Medium<?>> {

	/**
	 * Closes this {@link MediumAccessor}. All other methods except
	 * {@link #getMedium()}, {@link #getCurrentPosition()} and {@link #isOpened()}
	 * cannot be used anymore if the {@link MediumAccessor} is closed. They will
	 * throw a {@link PreconditionUnfullfilledException} in this case.
	 */
	void close();

	/**
	 * Returns the current {@link MediumOffset} position of this
	 * {@link MediumAccessor}. The current position is the position used for the
	 * next calls to {@link #read(int)}, {@link #truncate()} and
	 * {@link #write(ByteBuffer)} as well as for calls to
	 * {@link #isAtEndOfMedium()}.
	 * 
	 * For random-access media, the current position can be changed using
	 * {@link #setCurrentPosition(MediumOffset)}, {@link #write(ByteBuffer)}
	 * (changes by the number of written bytes) and {@link #read(int)} (changes by
	 * the number of read bytes).
	 * 
	 * For non-random-access media, calls to
	 * {@link #setCurrentPosition(MediumOffset)} do not have any effect. Only calls
	 * to {@link #read(int)} will change the current position for these media types.
	 * 
	 * After opening an {@link MediumAccessor}, the current position always points
	 * to medium offset 0.
	 * 
	 * @return The current {@link MediumOffset} position of this
	 *         {@link MediumAccessor}
	 */
	MediumOffset getCurrentPosition();

	/**
	 * @return the {@link Medium} this {@link MediumAccessor} is using.
	 */
	T getMedium();

	/**
	 * Returns whether the current position as returned by
	 * {@link #getCurrentPosition()} is at end of the {@link Medium} . The method
	 * may potentially block forever as it might require reading a byte to detect
	 * end of medium. This call must NOT advance the current position, no matter
	 * which medium type is used.
	 * 
	 * @return Returns whether the current position is at end of the {@link Medium}
	 *         used
	 */
	boolean isAtEndOfMedium();

	/**
	 * Returns whether this {@link MediumAccessor} is currently opened or closed. A
	 * newly created instance of an implementing class must be initially open. When
	 * this {@link MediumAccessor} is closed, all other methods except
	 * {@link #getMedium()} and {@link #getCurrentPosition()} when called on this
	 * instance will throw a {@link PreconditionUnfullfilledException}.
	 * 
	 * @return Returns whether this {@link MediumAccessor} is currently opened
	 *         (true) or closed (false).
	 */
	boolean isOpened();

	/**
	 * Opens this {@link MediumAccessor}. Only {@link #getMedium()},
	 * {@link #getCurrentPosition()} and {@link #isOpened()} can be used if the
	 * {@link MediumAccessor} is closed. If this {@link MediumAccessor} is already
	 * opened, this method throws a {@link PreconditionUnfullfilledException}.
	 */
	void open();

	/**
	 * Reads bytes from the reading {@link Medium} starting from the current
	 * position as returned by {@link #getCurrentPosition()}. This method may block
	 * until the number of bytes requested are available (potentially forever). This
	 * method advances the current position of the medium by the bytes really read.
	 * 
	 * The position and the limit of the returned {@link ByteBuffer} will be the
	 * same after the method returns.
	 * 
	 * @param numberOfBytes The number of bytes to read, must not be negative.
	 * 
	 * @throws EndOfMediumException  If the end of {@link Medium} has been reached
	 *                               during reading. The {@link ByteBuffer} returned
	 *                               in the exception is guaranteed to hold all the
	 *                               bytes up to end of {@link Medium} when this
	 *                               exception is thrown. The exception itself
	 *                               contains the number of bytes really read, the
	 *                               {@link ByteBuffer}s remaining bytes equals that
	 *                               value.
	 * @throws MediumAccessException in case of any errors during medium access
	 * 
	 * @return The bytes read. The returned {@link ByteBuffer} is reset, i.e. its
	 *         position and mark are set to 0. Its limit is set to the number of
	 *         bytes really read.
	 */
	ByteBuffer read(int numberOfBytes) throws EndOfMediumException;

	/**
	 * Sets the current position of this {@link MediumAccessor}. Only has an effect
	 * for random-access media. See {@link #getCurrentPosition()} for more details.
	 * Positions set are allowed to be behind the current medium length, e.g. to
	 * write new bytes at the indicated position.
	 * 
	 * @param position The new {@link MediumOffset} position to set. May be behind
	 *                 the current medium length. Must refer to the same
	 *                 {@link Medium} as this {@link MediumAccessor}.
	 */
	void setCurrentPosition(MediumOffset position);

	/**
	 * Truncates the {@link Medium} at the current position as returned by
	 * {@link #getCurrentPosition()}. This effectively shortens the {@link Medium}
	 * by clipping at and all previously contained bytes behind the new current
	 * position. The current position remains unchanged, i.e. points to the end of
	 * the medium after truncation.
	 * 
	 * @throws ReadOnlyMediumException if the {@link Medium} is read-only
	 * @throws MediumAccessException   in case of any errors during medium access
	 */
	void truncate();

	/**
	 * Writes bytes to the {@link MediumAccessor} starting at the current position
	 * as returned by {@link #getCurrentPosition()}. This operation must only be
	 * called for a writable {@link Medium}, otherwise it will throw a
	 * {@link PreconditionUnfullfilledException}. This operation might block until
	 * all bytes are written successfully, possibly forever. The writing
	 * {@link Medium} length might extend if the current position plus the remaining
	 * size of the written {@link ByteBuffer} is bigger than the current
	 * {@link Medium} length. The {@link Medium} is always only extended by this
	 * difference. This method advances the current position of the medium by the
	 * bytes written.
	 * 
	 * The contents and properties of the specified {@link ByteBuffer} are not
	 * changed, especially for its limit and position which are the same as before
	 * the write.
	 * 
	 * @param buffer The {@link ByteBuffer} to be written. It is written starting
	 *               from its current position up to its limit. After the method
	 *               returned, its position has increased by the number of bytes
	 *               written, i.e. its {@link ByteBuffer#remaining()} method returns
	 *               0.
	 * 
	 * @throws ReadOnlyMediumException if the {@link Medium} is read-only
	 * @throws MediumAccessException   in case of any errors during medium access
	 */
	void write(ByteBuffer buffer);
}
