/**
 *
 * {@link MappedByteBufferPerformance}.java
 *
 * @author Jens Ebert
 *
 * @date 02.05.2010
 *
 */

package com.github.jmeta.tools.fileaccessperformance.api.services.access;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;

/**
 * {@link MappedByteBufferAccessor} measures performance for
 * {@link MappedByteBuffer} based I/O.
 */
public class MappedByteBufferAccessor extends AbstractFileAccessor {

	private MappedByteBuffer m_mappedBuffer;

	private FileChannel m_channel;

	private RandomAccessFile m_randomAccessFile;

	/**
	 * Creates a new {@link MappedByteBufferAccessor}.
	 * 
	 * @param file                 The {@link File} to be tested
	 * @param bytesToWrite         The byte array with the bytes to write.
	 * @param bytesToRead          The number of bytes to read.
	 * @param bytesAtEnd           The number of bytes to preserve at the end of
	 *                             file.
	 * @param deleteFileAfterClose Whether to delete the file after closing or not.
	 * @throws IOException whenever an I/O operation failed.
	 */
	public MappedByteBufferAccessor(File file, byte[] bytesToWrite, int bytesToRead, int bytesAtEnd,
		boolean deleteFileAfterClose) throws IOException {
		super(file, bytesToWrite, bytesToRead, bytesAtEnd, deleteFileAfterClose);

		m_randomAccessFile = new RandomAccessFile(file, "rw");
		m_channel = m_randomAccessFile.getChannel();
		m_mappedBuffer = m_channel.map(MapMode.READ_WRITE, 0, file.length() + bytesToWrite.length);
	}

	/**
	 * @see com.github.jmeta.tools.fileaccessperformance.api.services.access.AbstractFileAccessor#close()
	 */
	@Override
	protected void doClose() throws IOException {

		m_mappedBuffer = null;

		m_channel.close();
		m_randomAccessFile.close();

		m_channel = null;
		m_randomAccessFile = null;

		// Run GC to be able to delete the file later
		System.gc();
	}

	/**
	 * @see com.github.jmeta.tools.fileaccessperformance.api.services.access.AbstractFileAccessor#read(long,
	 *      int)
	 */
	@Override
	protected byte[] read(long offset, int length) throws IOException {

		byte[] returnedBytes = new byte[length];

		m_mappedBuffer.position((int) offset);

		m_mappedBuffer.get(returnedBytes);

		return returnedBytes;
	}

	/**
	 * @see com.github.jmeta.tools.fileaccessperformance.api.services.access.AbstractFileAccessor#write(long,
	 *      byte[])
	 */
	@Override
	protected void write(long offset, byte[] bytesToWrite) throws IOException {

		m_mappedBuffer.position((int) offset);

		m_mappedBuffer.put(bytesToWrite);
	}
}
