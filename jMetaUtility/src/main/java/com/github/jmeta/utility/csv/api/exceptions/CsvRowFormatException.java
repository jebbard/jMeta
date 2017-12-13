/**
 *
 * {@link CsvRowFormatException}.java
 *
 * @author Jens Ebert
 *
 * @date 29.05.2011
 */
package com.github.jmeta.utility.csv.api.exceptions;

/**
 * {@link CsvRowFormatException} is thrown whenever a wrong number of columns is found in a csv file while reading a
 * row.
 */
public class CsvRowFormatException extends Exception {

   /**
    * Creates a new {@link CsvRowFormatException}.
    * 
    * @param message
    *           The message.
    * @param csvResourceName
    *           The name of the csv resource that caused the error.
    * @param rowNr
    *           The row number where the error occurred.
    * @param colNr
    *           The column number where the error occurred, -1 if the column number is irrelevant.
    */
   public CsvRowFormatException(String message, String csvResourceName, int rowNr, int colNr) {
      super(message + " (in row <" + rowNr + ">, " + ((colNr != -1) ? "in column <" + colNr + ">, " : "") + "of file <"
         + csvResourceName + ">).");
   }

   private static final long serialVersionUID = 1L;
}
