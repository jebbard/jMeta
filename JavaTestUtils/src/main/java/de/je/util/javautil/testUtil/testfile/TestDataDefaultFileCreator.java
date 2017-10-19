/**
 *
 * {@link TestDataDefaultFileCreator}.java
 *
 * @author Jens Ebert
 *
 * @date 26.04.2009
 *
 */
package de.je.util.testUtil.testfile;

import java.io.File;

import org.junit.After;
import org.junit.Before;

import de.je.util.common.err.Reject;

/**
 * {@link TestDataDefaultFileCreator} creates some default files required for multiple
 * test cases within this package.
 */
public abstract class TestDataDefaultFileCreator
{
	/**
	 * Instantiates this class.
	 *
	 * @param dataFolder The folder to be used.
	 * @param generatedFileName The name of the generated file.
	 * @param realFileName The name of the real file.
	 */
	public TestDataDefaultFileCreator(String dataFolder, String generatedFileName, String realFileName)
	{
		m_generatedFileName = generatedFileName;
		m_realFileName = realFileName;
		m_dataFolder = dataFolder;
	}

	/**
	 * Sets up the test fixtures.
	 */
	@Before
	public void setUp()
	{
		m_generatedFileId = m_fileUtility.openTestFile(m_dataFolder + m_generatedFileName);
		m_realFileId = m_fileUtility.openTestFile(m_dataFolder + m_realFileName);
	}

	/**
	 * Tears down the test fixtures. Each tear down is even called if {@link #setUp()} fails.
	 */
	@After
	public void tearDown()
	{
		m_fileUtility.closeTestFile(m_generatedFileId);
		m_fileUtility.closeTestFile(m_realFileId);
	}

	/**
	 * Returns the {@link TestFileDescriptor} for the generated file.
	 *
	 * @return The {@link TestFileDescriptor} for the generated file.
	 */
	protected TestFileDescriptor getGeneratedFileDescriptor()
	{
		return m_fileUtility.getFileDescriptor(m_generatedFileId);
	}

	/**
	 * Sets a new file object after e.g. renaming a file.
	 *
	 * @param file The file to set.
	 */
	protected void setGeneratedFile(File file)
	{
		Reject.ifNull(file, "file");

		m_fileUtility.getFileDescriptor(m_generatedFileId).resetFile(file);
	}

	/**
	 * Returns the {@link TestFileDescriptor} for the real file.
	 *
	 * @return The {@link TestFileDescriptor} for the real file.
	 */
	protected TestFileDescriptor getRealFileDescriptor()
	{
		return m_fileUtility.getFileDescriptor(m_realFileId);
	}

	/**
	 * Returns the {@link TestFileUtility} used for creating and releasing test files.
	 *
	 * @return The {@link TestFileUtility} used for creating and releasing test files.
	 */
	protected TestFileUtility getFileUtility()
	{
		return m_fileUtility;
	}

	private final TestFileUtility m_fileUtility = new TestFileUtility();
	private int m_generatedFileId;
	private int m_realFileId;
	private final String m_generatedFileName;
	private final String m_realFileName;
	private final String m_dataFolder;
}
