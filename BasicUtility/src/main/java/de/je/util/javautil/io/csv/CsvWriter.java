/**
 *
 * {@link CsvWriter}.java
 *
 * @author Jens Ebert
 *
 * @date 03.05.2010
 *
 */
package de.je.util.javautil.io.csv;

import java.io.IOException;

import de.je.util.javautil.common.err.Reject;
import de.je.util.javautil.io.stream.NamedWriter;

/**
 * {@link CsvWriter} writes CSV data to a file.
 */
public class CsvWriter extends AbstractCsvHandler<NamedWriter> {

   /**
    * Creates a new {@link CsvWriter}.
    * 
    * @param elementCount
    *           The number of elements in a record. Must be at least 1.
    */
   public CsvWriter(int elementCount) {
      Reject.ifTrue(elementCount < 1, "elementCount < 1");

      m_elementCount = elementCount;
      setSeparator(null);
      setQuote(null);
   }

   /**
    * Returns the element count in one record.
    *
    * @return the element count in one record.
    */
   public int getColumnCount() {
      return m_elementCount;
   }

   /**
    * Appends a new data set to the CSV file using the given data. The string representation of each object is written
    * in the same order to the CSV file using its toString() method.
    *
    * @param columns
    *           The record to append. Each object represents an element of the record.
    * @throws IOException
    *            if an I/O operation fails.
    *
    * @pre record.length == {@link #getColumnCount()} - The length of the given record must equal the initially
    *      specified element count
    */
   public void writeNextRow(Object[] columns) throws IOException {
      Reject.ifFalse(columns.length == getColumnCount(), "columns.length == getColumnCount()");

      for (int i = 0; i < columns.length; i++) {
         writeNextRow(m_csvLineBuilder, columns[i]);
      }

      getCurrentCsvResource().write(m_csvLineBuilder.toString());
      getCurrentCsvResource().newLine();
      m_csvLineBuilder.setLength(0);
   }

   /**
    * Appends an element to the current CSV data set, i.e. to the given {@link StringBuilder}. The element string
    * representation is taken using its toString() method. The string representation is automatically quoted if it
    * contains a separator character. The element is appended with a separator character.
    *
    * @param builder
    *           The {@link StringBuilder} to append the element to.
    * @param toAppend
    *           The element to be appended.
    */
   private void writeNextRow(StringBuilder builder, Object toAppend) {
      String appendedString = toAppend.toString();

      if (appendedString.contains(new String(new char[] { getSeparator() })))
         appendedString = getQuote() + appendedString + getQuote();

      builder.append(appendedString);
      builder.append(getSeparator());
   }

   private int m_elementCount;

   private final StringBuilder m_csvLineBuilder = new StringBuilder(500);
}
