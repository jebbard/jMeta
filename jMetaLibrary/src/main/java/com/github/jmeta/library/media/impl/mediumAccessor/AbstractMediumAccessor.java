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
import com.github.jmeta.library.media.api.types.MediumOffset;
import com.github.jmeta.library.media.impl.offset.StandardMediumOffset;
import com.github.jmeta.utility.dbc.api.services.Reject;

/**
 * {@link AbstractMediumAccessor} is an abstract base class for easier
 * implementation of an {@link MediumAccessor}.
 *
 * @param <T> The type of {@link Medium}
 */
public abstract class AbstractMediumAccessor<T extends Medium<?>> implements MediumAccessor<T> {

	private final T medium;

	private boolean isOpened;

	private MediumOffset currentPosition;

	/**
	 * Creates a new {@link AbstractMediumAccessor}.
	 *
	 * @param medium the {@link Medium} this {@link AbstractMediumAccessor} works
	 *               on.
	 */
	public AbstractMediumAccessor(T medium) {

		Reject.ifNull(medium, "medium");

		this.isOpened = false;
		this.medium = medium;
		// NOTE: We are intentionally using an "unmanaged" MediumOffset here, means it
		// won't change by updating offsets
		// during a flush. This avoids a direct dependency to MediumOffsetFactory as
		// well as unexpected changes for
		// outside users
		updateCurrentPosition(new StandardMediumOffset(getMedium(), 0));
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
	 * @see com.github.jmeta.library.media.impl.mediumAccessor.MediumAccessor#getCurrentPosition()
	 */
	@Override
	public MediumOffset getCurrentPosition() {
		return currentPosition;
	}

	/**
	 * @see com.github.jmeta.library.media.impl.mediumAccessor.MediumAccessor#getMedium()
	 */
	@Override
	public T getMedium() {
		return medium;
	}

	/**
	 * @see com.github.jmeta.library.media.impl.mediumAccessor.MediumAccessor#isOpened()
	 */
	@Override
	public boolean isOpened() {
		return isOpened;
	}

	/**
	 * @see com.github.jmeta.library.media.impl.mediumAccessor.MediumAccessor#open()
	 */
	@Override
	public void open() {
		Reject.ifTrue(isOpened(), "isOpened");

		try {
			mediumSpecificOpen();
			isOpened = true;
		} catch (IOException e) {
			throw new MediumAccessException("Could not open medium due to exception", e);
		}
	}

	/**
	 * @see com.github.jmeta.library.media.impl.mediumAccessor.MediumAccessor#read(int)
	 */
	@Override
	public ByteBuffer read(int numberOfBytes) throws EndOfMediumException {

		Reject.ifFalse(isOpened(), "isOpened()");
		Reject.ifNegative(numberOfBytes, "numberOfBytes");

		if (numberOfBytes == 0) {
			return ByteBuffer.allocate(0);
		}

		try {
			return mediumSpecificRead(numberOfBytes);
		}

		catch (IOException e) {
			throw new MediumAccessException("Could not not read from " + getMedium(), e);
		}
	}

	/**
	 * @see com.github.jmeta.library.media.impl.mediumAccessor.MediumAccessor#setCurrentPosition(com.github.jmeta.library.media.api.types.MediumOffset)
	 */
	@Override
	public void setCurrentPosition(MediumOffset position) {
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
	 * @see com.github.jmeta.library.media.impl.mediumAccessor.MediumAccessor#truncate()
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
	 * @see com.github.jmeta.library.media.impl.mediumAccessor.MediumAccessor#write(java.nio.ByteBuffer)
	 */
	@Override
	public void write(ByteBuffer buffer) {

		Reject.ifNull(buffer, "buffer");
		Reject.ifFalse(isOpened(), "isOpened()");

		preventWriteOnReadyOnlyMedium();

		try {
			buffer.mark();

			mediumSpecificWrite(buffer);
		}

		catch (IOException e) {
			throw new MediumAccessException("Could not not write to " + getMedium(), e);
		}

		finally {
			buffer.reset();
		}
	}

	/**
	 * Checks if the underlying {@link Medium} is read-only, and if so, it throws a
	 * {@link ReadOnlyMediumException}.
	 */
	private void preventWriteOnReadyOnlyMedium() {
		if (getMedium().isReadOnly()) {
			throw new ReadOnlyMediumException(getMedium(), null);
		}
	}

	/**
	 * Concrete core implementation of {@link #close()}.
	 *
	 * @throws IOException in case of anything goes wrong in the concrete
	 *                     implementation
	 */
	protected abstract void mediumSpecificClose() throws IOException;

	/**
	 * Concrete core implementation of for opening access to the underlying medium.
	 *
	 * @throws IOException in case of anything goes wrong in the concrete
	 *                     implementation
	 */
	protected abstract void mediumSpecificOpen() throws IOException;

	/**
	 * Concrete core implementation of {@link #read(int)}.
	 *
	 * @param numberOfBytes The number of bytes to read
	 * @throws IOException in case of anything goes wrong in the concrete
	 *                     implementation
	 */
	protected abstract ByteBuffer mediumSpecificRead(int numberOfBytes) throws IOException, EndOfMediumException;

	/**
	 * Concrete core implementation of {@link #setCurrentPosition(MediumOffset)}
	 *
	 * @param position The new {@link MediumOffset} position to set
	 * @throws IOException in case of anything goes wrong in the concrete
	 *                     implementation
	 */
	protected abstract void mediumSpecificSetCurrentPosition(MediumOffset position) throws IOException;

	/**
	 * Concrete core implementation of {@link #truncate()}
	 *
	 * @throws IOException in case of anything goes wrong in the concrete
	 *                     implementation
	 */
	protected abstract void mediumSpecificTruncate() throws IOException;

	/**
	 * Concrete core implementation of {@link #write(ByteBuffer)}
	 *
	 * @param buffer The {@link ByteBuffer} holding the bytes to write
	 * @throws IOException in case of anything goes wrong in the concrete
	 *                     implementation
	 */
	protected abstract void mediumSpecificWrite(ByteBuffer buffer) throws IOException;

	/**
	 * Updates the position returned by {@link #getCurrentPosition()}.
	 *
	 * @param position The new {@link MediumOffset} position
	 */
	protected void updateCurrentPosition(MediumOffset position) {

		currentPosition = position;
	}
}
