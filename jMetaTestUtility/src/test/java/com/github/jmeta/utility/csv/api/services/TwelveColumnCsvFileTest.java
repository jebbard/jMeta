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

import com.github.jmeta.utility.csv.api.services.CsvReader;

/**
 * {@link TwelveColumnCsvFileTest} tests the {@link CsvReader} class with some typical twelve column CSV file, with
 * default separator and default quote, without any data quoted.
 */
public class TwelveColumnCsvFileTest extends AbstractCsvReaderTest {

   /**
    * @see AbstractCsvReaderTest#getTestlingWithFixedColumns()
    */
   @Override
   protected CsvReader getTestlingWithFixedColumns() {
      if (m_csvReader == null) {
         Set<String> columns = new LinkedHashSet<>();
         columns.add("HALLO_1");
         columns.add("HALLO_2");
         columns.add("HALLO_3");
         columns.add("HALLO_4");
         columns.add("HALLO_5");
         columns.add("HALLO_6");
         columns.add("HALLO_7");
         columns.add("HALLO_8");
         columns.add("HALLO_9");
         columns.add("HALLO_10");
         columns.add("HALLO_11");
         columns.add("HALLO_12");
         m_csvReader = new CsvReader(columns);
      }

      return m_csvReader;
   }

   /**
    * @see AbstractCsvReaderTest#getSeparatorToUse()
    */
   @Override
   protected Character getSeparatorToUse() {
      return null;
   }

   /**
    * @see AbstractCsvReaderTest#getQuoteToUse()
    */
   @Override
   protected Character getQuoteToUse() {
      return null;
   }

   /**
    * @see AbstractCsvReaderTest#getCorrectFileToUse()
    */
   @Override
   protected File getCorrectFileToUse() throws Exception {
      if (m_theCorrectFile == null)
         m_theCorrectFile = new File(EmptyCsvFileTest.class.getResource("TwelveColumns.csv").toURI());

      return m_theCorrectFile;
   }

   /**
    * @see AbstractCsvReaderTest#getIncorrectFileToUse()
    */
   @Override
   protected File getIncorrectFileToUse() throws Exception {
      if (m_theInCorrectFile == null)
         m_theInCorrectFile = new File(EmptyCsvFileTest.class.getResource("TwelveColumnsIncorrect.csv").toURI());

      return m_theInCorrectFile;
   }

   /**
    * @see AbstractCsvReaderTest#getExpectedDataInCorrectFile()
    */
   @Override
   protected List<String[]> getExpectedDataInCorrectFile() {
      if (m_expectedDataCorrectFile == null) {
         m_expectedDataCorrectFile = new ArrayList<>();
         m_expectedDataCorrectFile.add(new String[] { "HALLO_1", "HALLO_2", "HALLO_3", "HALLO_4", "HALLO_5", "HALLO_6",
            "HALLO_7", "HALLO_8", "HALLO_9", "HALLO_10", "HALLO_11", "HALLO_12", });
         m_expectedDataCorrectFile.add(new String[] { "HALLO_11", "HALLO_21", "HALLO_31", "HALLO_41", "HALLO_51",
            "HALLO_61", "HALLO_71", "HALLO_81", "HALLO_91", "HALLO_101", "HALLO_111", "HALLO_121", });
         m_expectedDataCorrectFile.add(new String[] { "HALLO_12", "HALLO_22", "HALLO_32", "HALLO_42", "HALLO_52",
            "HALLO_62", "HALLO_72", "HALLO_82", "HALLO_92", "HALLO_102", "HALLO_112", "HALLO_122", });
         m_expectedDataCorrectFile.add(new String[] { "HALLO_11", "HALLO_21", "HALLO_31", "HALLO_41", "HALLO_51",
            "HALLO_61", "HALLO_71", "HALLO_81", "HALLO_91", "HALLO_101", "HALLO_111", "HALLO_123", });
         m_expectedDataCorrectFile.add(new String[] { "HALLO_11", "HALLO_21", "HALLO_31", "HALLO_41", "tach",
            "HALLO_61", "HALLO_71", "HALLO_81", "HALLO_91", "HALLO_101", "HALLO_111", "HALLO_124", });
         m_expectedDataCorrectFile.add(new String[] { "HALLO_11", "HALLO_21", "HALLO_31", "HALLO_41", "HALLO_51",
            "HALLO_61", "HALLO_71", "HALLO_81", "HALLO_91", "HALLO_101", "HALLO_111", "HALLO_125", });
         m_expectedDataCorrectFile.add(new String[] { "HALLO_11", "HALLO_21", "HALLO_31", "HALLO_41", "HALLO_51",
            "HALLO_61", "HALLO_71", "HALLO_81", "HALLO_91", "HALLO_101", "HALLO_111", "HALLO_126", });
         m_expectedDataCorrectFile.add(new String[] { "HALLO_11", "HALLO_21", "HALLO_31", "HALLO_41", "HALLO_51",
            "HALLO_61", "HALLO_71", "HALLO_81", "HALLO_91", "HALLO_101", "HALLO_111", "HALLO_127", });
         m_expectedDataCorrectFile.add(new String[] { "HALLO_11", "HALLO_21", "HALLO_31", "HALLO_41", "HALLO_51",
            "HALLO_61", "HALLO_71", "HALLO_81", "HALLO_91", "HALLO_101", "HALLO_111", "HALLO_128", });
         m_expectedDataCorrectFile.add(new String[] { "HALLO_11", "HALLO_21", "HALLO_31", "HALLO_41", "HALLO_51",
            "HALLO_61", "HALLO_71", "HALLO_81", "HALLO_91", "HALLO_101", "HALLO_111", "HALLO_129", });
         m_expectedDataCorrectFile.add(new String[] { "HALLO_11", "HALLO_21", "HALLO_31", "HALLO_41", "meineFresse",
            "HALLO_61", "HALLO_71", "HALLO_81", "HALLO_91", "HALLO_101", "HALLO_111", "HALLO_120", });
      }

      return m_expectedDataCorrectFile;
   }

   private File m_theCorrectFile;
   private File m_theInCorrectFile;
   private List<String[]> m_expectedDataCorrectFile;
   private CsvReader m_csvReader;
}
