/**
 *
 * {@link ConcreteCsvReaderTest}.java
 *
 * @author Jens Ebert
 *
 * @date 29.05.2011
 */
package com.github.jmeta.utility.csv.api.services;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * {@link TwoColumnCsvFileTest} tests the {@link CsvReader} class with a two
 * column CSV file, with default quote and specific separator, with some quoted
 * columns that contain separators within the quote.
 *
 * Furthermore, some of the lines contain empty last columns and some lines just
 * consist of one separator.
 */
public class TwoColumnCsvFileTest extends AbstractCsvReaderTest {

	private File m_theCorrectFile;

	private File m_theInCorrectFile;

	private List<String[]> m_expectedDataCorrectFile;

	private CsvReader m_csvReader;

	/**
	 * @see AbstractCsvReaderTest#getCorrectFileToUse()
	 */
	@Override
	protected File getCorrectFileToUse() throws Exception {
		if (m_theCorrectFile == null) {
			m_theCorrectFile = new File(EmptyCsvFileTest.class.getResource("TwoColumns.csv").toURI());
		}

		return m_theCorrectFile;
	}

	/**
	 * @see AbstractCsvReaderTest#getExpectedDataInCorrectFile()
	 */
	@Override
	protected List<String[]> getExpectedDataInCorrectFile() {
		if (m_expectedDataCorrectFile == null) {
			m_expectedDataCorrectFile = new ArrayList<>();
			m_expectedDataCorrectFile.add(new String[] { "HALLO_1", "Hall" });
			m_expectedDataCorrectFile.add(new String[] { "", "" });
			m_expectedDataCorrectFile.add(new String[] { "Ha", "" });
			m_expectedDataCorrectFile.add(new String[] { "Hallo", "\"Hallo\"", });
			m_expectedDataCorrectFile.add(new String[] { "\"HALL+O_11\"", "" });
			m_expectedDataCorrectFile.add(new String[] { "", "" });
			m_expectedDataCorrectFile.add(new String[] { "\"\"", "\"\"" });
			m_expectedDataCorrectFile.add(new String[] { "HALLO_11", "\"++\"" });
			m_expectedDataCorrectFile.add(new String[] { "", "" });
		}

		return m_expectedDataCorrectFile;
	}

	/**
	 * @see AbstractCsvReaderTest#getIncorrectFileToUse()
	 */
	@Override
	protected File getIncorrectFileToUse() throws Exception {
		if (m_theInCorrectFile == null) {
			m_theInCorrectFile = new File(EmptyCsvFileTest.class.getResource("TwoColumnsIncorrect.csv").toURI());
		}

		return m_theInCorrectFile;
	}

	/**
	 * @see AbstractCsvReaderTest#getQuoteToUse()
	 */
	@Override
	protected Character getQuoteToUse() {
		return '"';
	}

	/**
	 * @see AbstractCsvReaderTest#getSeparatorToUse()
	 */
	@Override
	protected Character getSeparatorToUse() {
		return '+';
	}

	/**
	 * @see AbstractCsvReaderTest#getTestlingWithFixedColumns()
	 */
	@Override
	protected CsvReader getTestlingWithFixedColumns() {
		if (m_csvReader == null) {
			Set<String> columns = new LinkedHashSet<>();
			columns.add("col1");
			columns.add("col2");
			m_csvReader = new CsvReader(columns);
		}

		return m_csvReader;
	}
}
