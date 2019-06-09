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
 * {@link NineColumnCsvFileTest} tests the {@link CsvReader} class with some
 * typical nine column CSV file, with specific separator and default quote, with
 * some quoted columns that contain separators within the quote.
 *
 * Furthermore, some of the lines contain empty last columns.
 */
public class NineColumnCsvFileTest extends AbstractCsvReaderTest {

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
			m_theCorrectFile = new File(EmptyCsvFileTest.class.getResource("NineColumns.csv").toURI());
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
			m_expectedDataCorrectFile.add(new String[] { "HALLO_1", "HALLO_2", "HALLO_3", "HALLO_4", "HALLO_5",
				"HALLO_6", "HALLO_7", "HALLO_8", "", });
			m_expectedDataCorrectFile.add(new String[] { "HALLO_11", "HALLO_21", "HALLO_31", "HALLO_41", "HALLO_51",
				"HALLO_61", "HALLO_71", "HALLO_81", "HALLO_91", });
			m_expectedDataCorrectFile.add(new String[] { "HALLO_12", "\"a/b/c/d/e\"", "HALLO_32", "HALLO_42",
				"HALLO_52", "HALLO_62", "HALLO_72", "HALLO_82", "HALLO_92", });
			m_expectedDataCorrectFile.add(new String[] { "HALLO_11", "HALLO_21", "HALLO_31", "HALLO_41", "HALLO_51",
				"HALLO_61", "HALLO_71", "HALLO_81", "HALLO_91", });
			m_expectedDataCorrectFile.add(new String[] { "HALLO_11", "HALLO_21", "HALLO_31", "HALLO_41", "tach",
				"HALLO_61", "\"/HALLO_71\"", "HALLO_81", "", });
			m_expectedDataCorrectFile.add(new String[] { "HALLO_11", "HALLO_21", "HALLO_31", "HALLO_41", "HALLO_51",
				"HALLO_61", "HALLO_71", "HALLO_81", "HALLO_91", });
			m_expectedDataCorrectFile.add(new String[] { "HALLO_11", "HALLO_21", "\"HALLO_31\"", "HALLO_41", "HALLO_51",
				"HALLO_61", "HALLO_71", "HALLO_81", "HALLO_91", });
			m_expectedDataCorrectFile.add(new String[] { "HALLO_11", "HALLO_21", "HALLO_31", "HALLO_41",
				"\"hallo mit /\"", "HALLO_61", "HALLO_71", "HALLO_81", "HALLO_91", });
		}

		return m_expectedDataCorrectFile;
	}

	/**
	 * @see AbstractCsvReaderTest#getIncorrectFileToUse()
	 */
	@Override
	protected File getIncorrectFileToUse() throws Exception {
		if (m_theInCorrectFile == null) {
			m_theInCorrectFile = new File(EmptyCsvFileTest.class.getResource("NineColumnsIncorrect.csv").toURI());
		}

		return m_theInCorrectFile;
	}

	/**
	 * @see AbstractCsvReaderTest#getQuoteToUse()
	 */
	@Override
	protected Character getQuoteToUse() {
		return null;
	}

	/**
	 * @see AbstractCsvReaderTest#getSeparatorToUse()
	 */
	@Override
	protected Character getSeparatorToUse() {
		return '/';
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
			columns.add("col3");
			columns.add("col4");
			columns.add("col5");
			columns.add("col6");
			columns.add("col7");
			columns.add("col8");
			columns.add("col9");
			m_csvReader = new CsvReader(columns);
		}

		return m_csvReader;
	}
}
