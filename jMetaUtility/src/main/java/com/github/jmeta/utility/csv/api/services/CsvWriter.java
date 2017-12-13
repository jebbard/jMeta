/**
 *
 * {@link CsvWriter}.java
 *
 * @author Jens Ebert
 *
 * @date 03.05.2010
 *
 */
package com.github.jmeta.utility.csv.api.services;

import java.io.IOException;

import com.github.jmeta.utility.dbc.api.services.Reject;
import com.github.jmeta.utility.namedio.api.services.NamedWriter;

/**
 * {@link CsvWriter} writes CSV data to a file.
 */
public class CsvWriter extends AbstractCsvHandler<NamedWriter> {

   private int elementCount;

   private final StringBuilder csvLineBuilder = new StringBuilder(500);

   /**
    * Creates a new {@link CsvWriter}.
    * 
    * @param elementCount
    *           The number of elements in a record. Must be at least 1.
    */
   public CsvWriter(int elementCount) {
      Reject.ifNegativeOrZero(elementCount, "elementCount");

      this.elementCount = elementCount;
      setSeparator(null);
      setQuote(null);
   }

   /**
    * Returns the element count in one record.
    *
    * @return the element count in one record.
    */
   public int getColumnCount() {
      return elementCount;
   }

   /**
    * Appends a new data set to the CSV file using the given data. The string representation of each object is written
    * in the same order to the CSV file using its toString() method.
    *
    * @param columns
    *           The record to append, each object represents an element of the record; it must have the same length as
    *           returned by {@link #getColumnCount()}
    * @throws IOException
    *            if an I/O operation fails.
    */
   public void writeNextRow(Object[] columns) throws IOException {
      Reject.ifFalse(columns.length == getColumnCount(), "columns.length == getColumnCount()");

      for (int i = 0; i < columns.length; i++) {
         writeNextRow(csvLineBuilder, columns[i]);
      }

      getCurrentCsvResource().write(csvLineBuilder.toString());
      getCurrentCsvResource().newLine();
      csvLineBuilder.setLength(0);
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
}
