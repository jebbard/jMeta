/**
 *
 * {@link CsvReader2Test}.java
 *
 * @author Jens Ebert
 *
 * @date 29.05.2011
 */
package com.github.jmeta.utility.csv.api.services;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.github.jmeta.utility.charset.api.services.Charsets;
import com.github.jmeta.utility.csv.api.exceptions.CsvRowFormatException;
import com.github.jmeta.utility.namedio.api.services.NamedReader;
import com.github.jmeta.utility.testsetup.api.exceptions.InvalidTestDataException;

/**
 * {@link AbstractCsvReaderTest} tests the {@link CsvReader} class.
 */
public abstract class AbstractCsvReaderTest {

	private static final String UNEXPECTED_EXCEPTION = "Unexpected exception: ";

	private CsvReader m_testlingFixedColumns;

	private AbstractCsvHandler<NamedReader> m_testlingVariableColumns;

	private File m_correctFileToUse;

	private File m_incorrectFileToUse;

	/**
	 * Does the actual checking of
	 * {@link AbstractCsvHandler#closeCurrentCsvResource()}.
	 *
	 * @param testling The {@link CsvReader} to use.
	 */
	private void checkCloseFile(AbstractCsvHandler<NamedReader> testling) {
		try {
			testling.closeCurrentCsvResource();

			Assert.assertNull(testling.getCurrentCsvResource());
			Assert.assertFalse(testling.isCsvLoaded());
		} catch (IOException e) {
			Assert.fail(AbstractCsvReaderTest.UNEXPECTED_EXCEPTION + e);
		}
	}

	/**
	 * Does the actual checking of {@link CsvReader#getQuote()} and
	 * {@link CsvReader#getSeparator()}.
	 *
	 * @param testling The {@link CsvReader} to use.
	 */
	private void checkGetQuoteGetSeparator(AbstractCsvHandler<NamedReader> testling) {
		Character quote = testling.getQuote();
		Character separator = testling.getSeparator();

		Assert.assertNotNull(quote);
		Assert.assertNotNull(separator);

		if (getQuoteToUse() != null) {
			Assert.assertEquals(getQuoteToUse(), quote);
		}

		if (getSeparatorToUse() != null) {
			Assert.assertEquals(getSeparatorToUse(), separator);
		}
	}

	/**
	 * Does the actual checking of {@link AbstractCsvHandler#isCsvLoaded()} and
	 * {@link AbstractCsvHandler#setNewResource(java.io.Closeable)}.
	 *
	 * @param testling The {@link CsvReader} to use.
	 */
	private void checkIsFileLoadedLoadNewFile(AbstractCsvHandler<NamedReader> testling) {
		Assert.assertNull(testling.getCurrentCsvResource());
		Assert.assertFalse(testling.isCsvLoaded());

		checkLoadNewFile(testling, m_correctFileToUse);

		Assert.assertTrue(testling.isCsvLoaded());

		checkLoadNewFile(testling, m_incorrectFileToUse);

		Assert.assertTrue(testling.isCsvLoaded());
	}

	/**
	 * Does the actual checking whether load new file works
	 * 
	 * @param testling The {@link CsvReader} to use.
	 * @param file     The file to load.
	 */
	private void checkLoadNewFile(AbstractCsvHandler<NamedReader> testling, File file) {
		try {
			testling.setNewResource(NamedReader.createFromFile(file, Charsets.CHARSET_ASCII));

			Assert.assertEquals(file.getAbsolutePath(), testling.getCurrentCsvResource().getName());
		} catch (IOException e) {
			Assert.fail(AbstractCsvReaderTest.UNEXPECTED_EXCEPTION + e);
		}
	}

	/**
	 * Checks the reading of each row of the given csv file.
	 * 
	 * @param testling       The {@link CsvReader} to use.
	 * @param csvFile        The csv file.
	 * @param preserveQuotes true if quotes should be preserved, false otherwise.
	 *
	 * @throws CsvRowFormatException If the format of a row in the csv file is
	 *                               incorrect.
	 */
	private void checkReadNextLine(CsvReader testling, File csvFile, boolean preserveQuotes)
		throws CsvRowFormatException {
		checkLoadNewFile(testling, csvFile);

		Map<String, String> nextRow = null;

		Set<String> expectedColumns = testling.getColumns();

		List<String[]> expectedLines = getExpectedDataInCorrectFile();

		int rowIndex = 0;
		try {
			while ((nextRow = testling.readNextRow(preserveQuotes)) != null) {
				int columnIndex = 0;

				// Fetch columns if they are determined first after the first call to
				// readNextRow
				if (expectedColumns.isEmpty()) {
					expectedColumns = testling.getColumns();
				}

				String[] rowColumns = expectedLines.get(rowIndex);

				if (rowColumns.length != expectedColumns.size()) {
					throw new InvalidTestDataException("Number of the columns for the csv file " + csvFile
						+ " does not match number of expected columns for row index " + rowIndex, null);
				}

				Assert.assertEquals(rowColumns.length, nextRow.size());

				for (Iterator<String> columnIterator = expectedColumns.iterator(); columnIterator.hasNext();) {
					String nextColumnName = columnIterator.next();

					Assert.assertTrue(nextRow.containsKey(nextColumnName));
					String actualColumnValue = nextRow.get(nextColumnName);
					String expectedColumnValue = rowColumns[columnIndex];

					String quoteAsString = testling.getQuote().toString();

					if (!preserveQuotes) {
						expectedColumnValue = expectedColumnValue.replace(quoteAsString, "");

						Assert.assertFalse(actualColumnValue.startsWith(quoteAsString));
						Assert.assertFalse(actualColumnValue.endsWith(quoteAsString));
					}

					else if (isQuotedColumn(rowIndex, columnIndex)) {
						Assert.assertTrue(actualColumnValue.startsWith(quoteAsString));
						Assert.assertTrue(actualColumnValue.endsWith(quoteAsString));
					}

					Assert.assertEquals(expectedColumnValue, actualColumnValue);

					columnIndex++;
				}

				rowIndex++;

				Assert.assertTrue(rowIndex <= expectedLines.size());
			}
		} catch (IOException e) {
			Assert.fail(AbstractCsvReaderTest.UNEXPECTED_EXCEPTION + e);
		}

		Assert.assertEquals(expectedLines.size(), rowIndex);
	}

	/**
	 * Returns the correctly formatted csv file for positive test cases.
	 *
	 * @return the correctly formatted csv file for positive test cases.
	 * @throws Exception in case of any problem during determination of the file
	 */
	protected abstract File getCorrectFileToUse() throws Exception;

	/**
	 * Returns the expected data in the correct csv file returned by
	 * {@link #getCorrectFileToUse()}.
	 *
	 * @return the expected data in the correct csv file returned by
	 *         {@link #getCorrectFileToUse()}.
	 */
	protected abstract List<String[]> getExpectedDataInCorrectFile();

	/**
	 * Returns the incorrectly formatted csv file for negative test cases.
	 *
	 * @return the incorrectly formatted csv file for negative test cases.
	 * @throws Exception in case of any problem during determination of the file
	 */
	protected abstract File getIncorrectFileToUse() throws Exception;

	/**
	 * Returns the quote character to be used in the test.
	 * 
	 * @return the quote character to be used in the test. Null for default.
	 */
	protected abstract Character getQuoteToUse();

	/**
	 * Returns the separator character to be used in the test.
	 * 
	 * @return the separator character to be used in the test. Null for default.
	 */
	protected abstract Character getSeparatorToUse();

	/**
	 * Returns a {@link CsvReader} instance created with fixed columns for test.
	 *
	 * @return a {@link CsvReader} instance created with fixed columns for test.
	 */
	protected abstract CsvReader getTestlingWithFixedColumns();

	/**
	 * Determines whether the given csv element in the expected data is quoted or
	 * not.
	 *
	 * @param rowIndex    The zero-based index of the row in the csv file.
	 * @param columnIndex The zero-based index of the column in the csv file.
	 * @return true if it is expected to be quoted, false otherwise.
	 */
	private boolean isQuotedColumn(int rowIndex, int columnIndex) {
		String[] expectedRowContents = getExpectedDataInCorrectFile().get(rowIndex);
		String quoteToUse = getTestlingWithFixedColumns().getQuote().toString();
		return expectedRowContents[columnIndex].startsWith(quoteToUse);
	}

	/**
	 * Sets up the test fixtures.
	 */
	@Before
	public void setUp() {
		m_testlingFixedColumns = getTestlingWithFixedColumns();
		m_testlingVariableColumns = new CsvReader();

		try {
			m_correctFileToUse = getCorrectFileToUse();
			m_incorrectFileToUse = getIncorrectFileToUse();
		} catch (Exception e) {
			throw new InvalidTestDataException("Could not load test data files due to exception", e);
		}

		if ((m_testlingFixedColumns == null) || (m_testlingVariableColumns == null) || (m_correctFileToUse == null)
			|| (m_incorrectFileToUse == null)) {
			throw new InvalidTestDataException("Test data may not be null", null);
		}

		m_testlingFixedColumns.setQuote(getQuoteToUse());
		m_testlingFixedColumns.setSeparator(getSeparatorToUse());
		m_testlingVariableColumns.setQuote(getQuoteToUse());
		m_testlingVariableColumns.setSeparator(getSeparatorToUse());
	}

	/**
	 * Tests {@link AbstractCsvHandler#isCsvLoaded()},
	 * {@link AbstractCsvHandler#setNewResource(java.io.Closeable)},
	 * {@link AbstractCsvHandler#closeCurrentCsvResource()} and
	 * {@link AbstractCsvHandler#getCurrentCsvResource()}.
	 */
	@Test
	public void test_fileManagement() {
		checkIsFileLoadedLoadNewFile(m_testlingFixedColumns);
		checkIsFileLoadedLoadNewFile(m_testlingVariableColumns);

		checkCloseFile(m_testlingFixedColumns);
		checkCloseFile(m_testlingVariableColumns);
	}

	/**
	 * Tests {@link CsvReader#getQuote()} and {@link CsvReader#getSeparator()}.
	 */
	@Test
	public void test_getQuoteGetSeparator() {
		checkGetQuoteGetSeparator(m_testlingFixedColumns);
		checkGetQuoteGetSeparator(m_testlingVariableColumns);
	}

	/**
	 * Tests {@link CsvReader#readNextRow} in negative case without removal of
	 * quotes in read columns.
	 * 
	 * @throws CsvRowFormatException As expected
	 */
	@Test(expected = CsvRowFormatException.class)
	public void test_readNextLine_negative_preserveQuotes() throws CsvRowFormatException {
		checkReadNextLine(m_testlingFixedColumns, m_incorrectFileToUse, true);
	}

	/**
	 * Tests {@link CsvReader#readNextRow} in positive case with removal of quotes
	 * in read columns.
	 */
	@Test
	public void test_readNextLine_positive_removeQuotes() {
		try {
			checkReadNextLine(m_testlingFixedColumns, m_correctFileToUse, false);
		} catch (CsvRowFormatException e) {
			Assert.fail(AbstractCsvReaderTest.UNEXPECTED_EXCEPTION + e);
		}
	}

	/**
	 * Tests {@link CsvReader#readNextRow} in negative case with removal of quotes
	 * in read columns.
	 * 
	 * @throws CsvRowFormatException As expected
	 */
	@Test(expected = CsvRowFormatException.class)
	public void test_readNextRow_negative_removeQuotes() throws CsvRowFormatException {
		checkReadNextLine(m_testlingFixedColumns, m_incorrectFileToUse, false);
	}

	/**
	 * Tests {@link CsvReader#readNextRow} in positive case without removal of
	 * quotes in read columns.
	 */
	@Test
	public void test_readNextRow_positive_preserveQuotes() {
		try {
			checkReadNextLine(m_testlingFixedColumns, m_correctFileToUse, true);
		} catch (CsvRowFormatException e) {
			Assert.fail(AbstractCsvReaderTest.UNEXPECTED_EXCEPTION + e);
		}
	}
}
