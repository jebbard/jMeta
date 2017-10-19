/**
 *
 * {@link ConcreteCsvReaderTest}.java
 *
 * @author Jens Ebert
 *
 * @date 29.05.2011
 */
package de.je.util.javautil.io.csv;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * {@link EmptyCsvFileTest} tests the {@link CsvReader} class with an empty CSV file.
 *
 * Furthermore, some of the lines contain empty last columns.
 */
public class EmptyCsvFileTest extends AbstractCsvReaderTest {

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
         m_theCorrectFile = new File(EmptyCsvFileTest.class.getResource("Empty.csv").toURI());

      return m_theCorrectFile;
   }

   /**
    * @see AbstractCsvReaderTest#getIncorrectFileToUse()
    */
   @Override
   protected File getIncorrectFileToUse() throws Exception {
      if (m_theInCorrectFile == null)
         m_theInCorrectFile = new File(EmptyCsvFileTest.class.getResource("EmptyIncorrect.csv").toURI());

      return m_theInCorrectFile;
   }

   /**
    * @see AbstractCsvReaderTest#getExpectedDataInCorrectFile()
    */
   @Override
   protected List<String[]> getExpectedDataInCorrectFile() {
      if (m_expectedDataCorrectFile == null)
         m_expectedDataCorrectFile = new ArrayList<>();

      return m_expectedDataCorrectFile;
   }

   private File m_theCorrectFile;
   private File m_theInCorrectFile;
   private List<String[]> m_expectedDataCorrectFile;
   private CsvReader m_csvReader;
}
