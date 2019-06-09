
package com.github.jmeta.tools.fileaccessperformance.api.services.access;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * {@link StreamAccessor} measures performance of {@link FileInputStream} and
 * {@link FileOutputStream} access.
 */
public class StreamAccessor extends AbstractFileAccessor {

	private InputStream m_inputStream;

	private OutputStream m_outputStream;

	/**
	 * Creates a new {@link StreamAccessor}.
	 * 
	 * @param file                 The {@link File} to be tested
	 * @param bytesToWrite         The byte array with the bytes to write.
	 * @param bytesToRead          The number of bytes to read.
	 * @param bytesAtEnd           The number of bytes to preserve at the end of
	 *                             file.
	 * @param deleteFileAfterClose Whether to delete the file after closing or not.
	 * @throws IOException whenever an I/O operation failed.
	 */
	public StreamAccessor(File file, byte[] bytesToWrite, int bytesToRead, int bytesAtEnd, boolean deleteFileAfterClose)
		throws IOException {
		super(file, bytesToWrite, bytesToRead, bytesAtEnd, deleteFileAfterClose);

		m_inputStream = createInputStream(file);
		m_outputStream = createOutputStream(file);
	}

	/**
	 * Creates the {@link InputStream} to use for testing.
	 * 
	 * @param file The file used to create the streams.
	 * @return the {@link InputStream} to use for testing.
	 * @throws FileNotFoundException if the given file has not been found.
	 */
	protected InputStream createInputStream(File file) throws FileNotFoundException {

		return new FileInputStream(file);
	}

	/**
	 * Creates the {@link OutputStream} to use for testing.
	 * 
	 * @param file The file used to create the streams.
	 * @return the {@link OutputStream} to use for testing.
	 * @throws FileNotFoundException if the given file has not been found.
	 */
	protected OutputStream createOutputStream(File file) throws FileNotFoundException {

		return new FileOutputStream(file, true);
	}

	/**
	 * @see com.github.jmeta.tools.fileaccessperformance.api.services.access.AbstractFileAccessor#close()
	 */
	@Override
	protected void doClose() throws IOException {

		m_inputStream.close();
		m_outputStream.close();
	}

	/**
	 * @see com.github.jmeta.tools.fileaccessperformance.api.services.access.AbstractFileAccessor#read(long,
	 *      int)
	 */
	@Override
	protected byte[] read(long offset, int length) throws IOException {

		byte[] bytesRead = new byte[length];

		m_inputStream.skip(offset);
		m_inputStream.read(bytesRead);

		return bytesRead;
	}

	/**
	 * @see com.github.jmeta.tools.fileaccessperformance.api.services.access.AbstractFileAccessor#write(long,
	 *      byte[])
	 */
	@Override
	protected void write(long offset, byte[] bytesToWrite) throws IOException {

		// It is not correct to just append here,
		// but nothing else is possible with OutputStream. For a performance test, this
		// is
		// sufficient.
		m_outputStream.write(bytesToWrite);
	}
}
