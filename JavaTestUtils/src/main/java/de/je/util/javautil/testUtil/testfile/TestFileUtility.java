/**
 *
 * {@link TestFileUtility}.java
 *
 * @author Jens Ebert
 *
 * @date 26.04.2009
 *
 */
package de.je.util.testUtil.testfile;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.activation.FileDataSource;

import de.je.util.common.err.Contract;
import de.je.util.testUtil.setup.TestDataException;

/**
 * {@link TestFileUtility} can be used to create/open and delete/close test files.
 *
 * Therefore it supports both generated files to delete after the test as well as
 * already existing "real life" files.
 *
 */
public class TestFileUtility
{
	/**
	 * Opens or newly creates a test file. If the file with the given absolute path name
	 * is already existing, it is opened, otherwise it is created and opened.
	 *
	 * @param name The path name of the file to open.
	 *
	 * @return An identifier of the file to be used with other methods.
	 */
	public int openTestFile(String name)
	{
		File file = new File(name);

		final boolean existed = file.exists();

		// Create file if not already existing
		if (!existed)
		{
			try
			{
				if (!file.createNewFile())
					throw new IOException();
			}

			catch (final IOException io)
			{
				throw new TestDataException("Could not create test files!", io);
			}
		}

		IDataSource dataSource = new FileDataSource(file);

		if (dataSource.isOpened())
			throw new TestDataException("Cannot open test file because it is currently opened!", null);

		dataSource.open();

		m_fileDescriptors.put(++m_idCounter, new TestFileDescriptor(file, dataSource, existed));

		return m_idCounter;
	}

	/**
	 * Closes a given test file. If it has been generated (= temporarily created for the
	 * test case) with {@link #openTestFile(String)} it is deleted, too.
	 *
	 * @param id The id of the file to close.
	 *
	 * @pre A file must exist with the given id - {@link #hasFile(int)} == true
	 */
	public void closeTestFile(int id)
	{
		Contract.checkPrecondition(hasFile(id), "A file must exist with the given id " + id);

		TestFileDescriptor descriptor = m_fileDescriptors.get(id);

		if (descriptor.getDataSource().isOpened())
			descriptor.getDataSource().close();

		// Delete files that have been newly created only
		if (!descriptor.fileExistedBeforeOpen())
			if (!descriptor.getFile().delete())
				System.out.println(CLOSS_ERROR_MESSAGE_PART1 + descriptor.getFile().getName()
					+ CLOSS_ERROR_MESSAGE_PART2);

		m_fileDescriptors.remove(id);
	}

	/**
	 * Returns a {@link TestFileDescriptor} object for the given file.
	 *
	 * @param id The id of the file. Must exist.
	 *
	 * @return A {@link TestFileDescriptor} object for the given file.
	 *
	 * @pre A file must exist with the given id - {@link #hasFile(int)} == true
	 */
	public TestFileDescriptor getFileDescriptor(int id)
	{
		Contract.checkPrecondition(hasFile(id), "A file must exist with the given id " + id);

		return m_fileDescriptors.get(id);
	}

	/**
	 * Determines if a given id is belonging to a file held within this {@link TestFileUtility}.
	 *
	 * @param id The id of the file.
	 * @return true if the given id is known, false otherwise.
	 */
	public boolean hasFile(int id)
	{
		return m_fileDescriptors.containsKey(id);
	}

	private static final String CLOSS_ERROR_MESSAGE_PART1 = "File could not be deleted: ";
	private static final String CLOSS_ERROR_MESSAGE_PART2 = ",  manual deletion might be necessary.";

	private int m_idCounter = -1;
	private final Map<Integer, TestFileDescriptor> m_fileDescriptors = new HashMap<Integer, TestFileDescriptor>();
}
