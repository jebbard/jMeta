/**
 *
 * {@link CsvReader}.java
 *
 * @author Jens
 *
 * @date 11.05.2014
 *
 */
package de.je.util.javautil.io.csv;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import de.je.util.javautil.common.err.Reject;
import de.je.util.javautil.io.stream.NamedReader;

/**
 * {@link CsvReader} can be used to read a CSV file based on a {@link NamedReader} instance created and passed by the
 * user.
 */
public class CsvReader extends AbstractCsvHandler<NamedReader> {

   /**
    * Creates a new {@link CsvReader}.
    */
   public CsvReader() {
      setQuote(null);
      setSeparator(null);
   }

   /**
    * Creates a new {@link CsvReader} with fixed column names known in advance. The first row in the file is already
    * interpreted as payload that will be returned by a call to {@link #readNextRow(boolean)}.
    *
    * @param columns
    *           The set of column names. At least one column name must be contained, even if the csv file to read is
    *           empty. To ensure the correct interpretation of the column order, a {@link LinkedHashSet} should be used,
    *           otherwise the columns might be mapped in random order.
    */
   public CsvReader(Set<String> columns) {
      this();

      Reject.ifNull(columns, "columns");
      Reject.ifNegativeOrZero(columns.size(), "columns.size()");

      m_columns.addAll(columns);
   }

   /**
    * Reads the next row from the currently loaded csv file. If now column names where specified in the constructor of
    * this {@link CsvReader} instance, the first row is interpreted as header row that specifies column count and names
    * for the whole file currently loaded.
    *
    * @param preserveQuotes
    *           true if quotes should be preserved in the output, false if they should be removed.
    * @return A {@link Map} of columns that build the next row read or null if there are no more rows and EOF has been
    *         reached. In case of an empty last line in the csv file, null is returned, too. No specific order is
    *         guaranteed in the returned {@link Map}. If the method returns a non-null {@link Map} instance, the
    *         {@link Map} is guaranteed to contain a non-null value for each column key that has been specified to the
    *         constructor of this {@link CsvReader}. If no columns have been specified in the constructor, the method
    *         interprets the first row as the header row, where each column contains the column name. Therefore the
    *         first row also determines the number of columns expected in every other row of the current file.
    * @throws CsvRowFormatException
    *            If the row is corrupt because of having too much or too few columns. This might indicate wrong, i.e.
    *            missing or unclosed quotation.
    * @throws IOException
    *            If an I/O error occurred while reading the row.
    */
   public Map<String, String> readNextRow(boolean preserveQuotes) throws IOException, CsvRowFormatException {
      Reject.ifFalse(isCsvLoaded(), "isCsvLoaded()");

      String readLine = getCurrentCsvResource().readLine();

      // EOF has been reached
      if (readLine == null)
         return null;

      Set<String> columns = getColumns();
      Map<String, String> lineMap = new HashMap<>(columns.size());

      String quoteAsString = getQuote().toString();
      String separatorAsString = getSeparator().toString();
      StringTokenizer tokenizer = new StringTokenizer(readLine, quoteAsString + separatorAsString, true);

      boolean quotedRegionStarted = false;
      // If no columns are available yet, they must be determined in this call
      boolean columnDetectionMode = columns.isEmpty();

      StringBuffer nextColumn = new StringBuffer(readLine.length());

      Iterator<String> columnIterator = columns.iterator();

      if (!tokenizer.hasMoreTokens())
         lineMap.put(columnDetectionMode ? "" : columnIterator.next(), "");

      while (tokenizer.hasMoreTokens()) {
         if (!columnDetectionMode && !columnIterator.hasNext())
            throw new CsvRowFormatException(TOO_MUCH_COLUMNS, getCurrentCsvResource().getName(), m_currentLine, -1);

         String token = tokenizer.nextToken();

         if (token.equals(quoteAsString)) {
            quotedRegionStarted = !quotedRegionStarted;

            // Append the quote if quotes must be preserved
            if (preserveQuotes)
               nextColumn.append(token);
         }

         else if (token.equals(separatorAsString)) {
            if (quotedRegionStarted)
               nextColumn.append(token);

            // The previous column is finished
            else {
               String nextColumnAsString = nextColumn.toString();

               lineMap.put(columnDetectionMode ? nextColumnAsString : columnIterator.next(), nextColumnAsString);

               nextColumn.delete(0, nextColumn.length());
            }
         }

         else
            nextColumn.append(token);

         // No more tokens left: Store current contents of column string
         if (!tokenizer.hasMoreTokens()) {
            if (!columnDetectionMode && !columnIterator.hasNext())
               throw new CsvRowFormatException(TOO_MUCH_COLUMNS, getCurrentCsvResource().getName(), m_currentLine, -1);

            String nextColumnAsString = nextColumn.toString();

            lineMap.put(columnDetectionMode ? nextColumnAsString : columnIterator.next(), nextColumnAsString);
         }
      }

      if (!columnDetectionMode && columnIterator.hasNext())
         throw new CsvRowFormatException(TOO_FEW_COLUMNS, getCurrentCsvResource().getName(), m_currentLine, -1);

      m_currentLine++;

      return lineMap;
   }

   /**
    * Returns the columns that are expected to be read by this {@link CsvReader} instance.
    *
    * @return the columns that are expected to be read by this {@link CsvReader} instance.
    */
   public Set<String> getColumns() {
      return Collections.unmodifiableSet(m_columns);
   }

   private static final String TOO_FEW_COLUMNS = "Too few columns found. This may be caused by "
      + "too few separators or unclosed quotations";
   private static final String TOO_MUCH_COLUMNS = "Too much columns found. This may be caused by "
      + "too much separators";
   private final Set<String> m_columns = new LinkedHashSet<>();
   private int m_currentLine;

}
