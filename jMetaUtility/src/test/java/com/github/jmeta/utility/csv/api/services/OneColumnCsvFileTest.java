/**
 *
 * {@link ConcreteCsvReader2Test}.java
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
 * {@link OneColumnCsvFileTest} tests the {@link CsvReader} class with one column CSV file, with specific separator and
 * default quote, with some quoted columns that contain separators within the quote.
 *
 * Furthermore, some of the lines contain empty last columns.
 */
public class OneColumnCsvFileTest extends AbstractCsvReaderTest {

   /**
    * @see AbstractCsvReaderTest#getTestlingWithFixedColumns()
    */
   @Override
   protected CsvReader getTestlingWithFixedColumns() {
      if (m_csvReader == null) {
         Set<String> columns = new LinkedHashSet<>();
         columns.add("col1");
         m_csvReader = new CsvReader(columns);
      }

      return m_csvReader;
   }

   /**
    * @see AbstractCsvReaderTest#getSeparatorToUse()
    */
   @Override
   protected Character getSeparatorToUse() {
      return '+';
   }

   /**
    * @see AbstractCsvReaderTest#getQuoteToUse()
    */
   @Override
   protected Character getQuoteToUse() {
      return '"';
   }

   /**
    * @see AbstractCsvReaderTest#getCorrectFileToUse()
    */
   @Override
   protected File getCorrectFileToUse() throws Exception {
      if (m_theCorrectFile == null)
         m_theCorrectFile = new File(EmptyCsvFileTest.class.getResource("OneColumn.csv").toURI());

      return m_theCorrectFile;
   }

   /**
    * @see AbstractCsvReaderTest#getIncorrectFileToUse()
    */
   @Override
   protected File getIncorrectFileToUse() throws Exception {
      if (m_theInCorrectFile == null)
         m_theInCorrectFile = new File(EmptyCsvFileTest.class.getResource("OneColumnIncorrect.csv").toURI());

      return m_theInCorrectFile;
   }

   /**
    * @see AbstractCsvReaderTest#getExpectedDataInCorrectFile()
    */
   @Override
   protected List<String[]> getExpectedDataInCorrectFile() {
      if (m_expectedDataCorrectFile == null) {
         m_expectedDataCorrectFile = new ArrayList<>();
         m_expectedDataCorrectFile.add(new String[] { "HALLO_1", });
         m_expectedDataCorrectFile.add(new String[] { "", });
         m_expectedDataCorrectFile.add(new String[] { "H", });
         m_expectedDataCorrectFile.add(new String[] { "H", });
         m_expectedDataCorrectFile.add(new String[] { "\"HALLO_11\"", });
         m_expectedDataCorrectFile.add(new String[] { "", });
         m_expectedDataCorrectFile.add(new String[] { "\"\"", });
         m_expectedDataCorrectFile.add(new String[] { "HALLO_11", });
      }

      return m_expectedDataCorrectFile;
   }

   private File m_theCorrectFile;
   private File m_theInCorrectFile;
   private List<String[]> m_expectedDataCorrectFile;
   private CsvReader m_csvReader;
}
