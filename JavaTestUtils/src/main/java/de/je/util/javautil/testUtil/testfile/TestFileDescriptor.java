/**
 *
 * {@link TestFileDescriptor}.java
 *
 * @author Jens Ebert
 *
 * @date 26.04.2009
 *
 */
package de.je.util.testUtil.testfile;

import java.io.File;

import de.je.util.common.err.Reject;

/**
 * {@link TestFileDescriptor} holds objects used to describe and access a {@link File}.
 */
public class TestFileDescriptor
{
	/**
	 * Creates this object.
	 *
	 * @param file The {@link File} to store within this {@link TestFileDescriptor}.
	 * @param dataSource The {@link IDataSource} to use to access the file.
	 * @param existed true if the {@link File} existed before opening, false otherwise.
	 */
	public TestFileDescriptor(File file, IDataSource dataSource, boolean existed)
	{
		Reject.ifNull(dataSource, "dataSource");
		Reject.ifNull(file, "file");

		m_file = file;
		m_dataSource = dataSource;
		m_existed = existed;
	}

	/**
	 * Returns the file stored within this object.
	 *
	 * @return The file stored within this object.
	 */
	public File getFile()
	{
		return m_file;
	}

	/**
	 * Resets the file to a new location after e.g. having renamed it.
	 *
	 * @param file The new file.
	 */
	public void resetFile(File file)
	{
		Reject.ifNull(file, "file");

		m_file = file;
	}

	/**
	 * Returns the {@link IDataSource} used for accessing the file.
	 *
	 * @return The {@link IDataSource} used for accessing the file.
	 */
	public IDataSource getDataSource()
	{
		return m_dataSource;
	}

	/**
	 * Returns true if the file already existed before it had been opened, false otherwise.
	 *
	 * @return true if the file already existed before it had been opened, false otherwise.
	 */
	public boolean fileExistedBeforeOpen()
	{
		return m_existed;
	}

	private File m_file;
	private final IDataSource m_dataSource;
	private final boolean m_existed;
}