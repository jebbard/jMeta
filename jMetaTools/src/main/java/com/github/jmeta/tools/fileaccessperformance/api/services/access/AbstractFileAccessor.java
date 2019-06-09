/**
 *
 * {@link AbstractFileAccessor}.java
 *
 * @author Jens Ebert
 *
 * @date 02.05.2010
 *
 */

package com.github.jmeta.tools.fileaccessperformance.api.services.access;

import java.io.File;
import java.io.IOException;

import com.github.jmeta.tools.benchmark.api.types.MeasuredCommand;
import com.github.jmeta.utility.dbc.api.services.Reject;

/**
 * {@link AbstractFileAccessor} implements the basic file access test performed.
 */
public abstract class AbstractFileAccessor implements MeasuredCommand {

	private final long m_initialLength;

	private final File m_file;

	private final byte[] m_bytesToWrite;

	private final int m_bytesToRead;

	private final int m_bytesAtEnd;

	private final boolean m_deleteFileAfterClose;

	/**
	 * Creates a new {@link AbstractFileAccessor}.
	 * 
	 * @param file                 The {@link File} to be tested
	 * @param bytesToWrite         The byte array with the bytes to write.
	 * @param bytesToRead          The number of bytes to read.
	 * @param bytesAtEnd           The number of bytes to preserve at the end of
	 *                             file.
	 * @param deleteFileAfterClose Whether to delete the file after closing or not.
	 */
	public AbstractFileAccessor(File file, byte[] bytesToWrite, int bytesToRead, int bytesAtEnd,
		boolean deleteFileAfterClose) {
		Reject.ifNull(file, "file");
		Reject.ifNull(bytesToWrite, "bytesToWrite");
		Reject.ifTrue(bytesAtEnd < bytesToRead, "bytesAtEndOffset < readOffset");

		m_file = file;
		m_initialLength = file.length();
		m_bytesToWrite = bytesToWrite;
		m_bytesToRead = bytesToRead;
		m_bytesAtEnd = bytesAtEnd;
		m_deleteFileAfterClose = deleteFileAfterClose;
	}

	/**
	 * Closes the underlying file. The file is deleted if this has been requested
	 * within the constructor call.
	 * 
	 * @throws IOException if an I/O error occurred during close operation.
	 */
	public void close() throws IOException {

		doClose();

		if (m_deleteFileAfterClose) {
			m_file.deleteOnExit();
			// throw new IOException("Unable to delete cloned file <" + m_file + ">");
		}
	}

	/**
	 * Closes the file. This method's implementation depends on the concrete object
	 * to close.
	 * 
	 * @throws IOException if closing the file failed.
	 */
	protected abstract void doClose() throws IOException;

	/**
	 * @see com.github.jmeta.tools.benchmark.api.types.Command#execute()
	 */
	@Override
	public void execute() {

		try {
			// Read the bytes to read
			// Working with the initial length is important, as at least the
			// MappedByteBuffer
			// implementation has already changed the original size within its constructor
			final long bytesToReadOfs = m_initialLength - m_bytesToRead - m_bytesAtEnd;

			byte[] readBytes = read(bytesToReadOfs, m_bytesToRead);
			// Read the remaining bytes
			byte[] bytesAtEnd = read(m_initialLength - m_bytesAtEnd, m_bytesAtEnd);
			// Write the bytes to write
			write(bytesToReadOfs, m_bytesToWrite);

			// Write the read bytes
			write(bytesToReadOfs + m_bytesToWrite.length, readBytes);
			// Write the remaining bytes
			write(bytesToReadOfs + m_bytesToWrite.length + m_bytesToRead, bytesAtEnd);
		}

		catch (IOException e) {
			throw new RuntimeException("I/O exception occurred during command execution", e);
		}
	}

	/**
	 * Returns the {@link File} used for the access test.
	 * 
	 * @return the {@link File} used for the access test.
	 */
	public File getFile() {

		return m_file;
	}

	/**
	 * @see com.github.jmeta.tools.benchmark.api.types.MeasuredCommand#getUniqueName()
	 */
	@Override
	public String getUniqueName() {

		return getClass().getName();
	}

	/**
	 * Reads bytes from the given offset of given length.
	 * 
	 * @param offset The offset.
	 * @param length The length.
	 * @return The bytes read.
	 * 
	 * @throws IOException whenever an I/O operation failed.
	 */
	protected abstract byte[] read(long offset, int length) throws IOException;

	/**
	 * Writes bytes to the given offset.
	 * 
	 * @param offset       The offset.
	 * @param bytesToWrite The byte to write.
	 * 
	 * @throws IOException whenever an I/O operation failed.
	 */
	protected abstract void write(long offset, byte[] bytesToWrite) throws IOException;
}
