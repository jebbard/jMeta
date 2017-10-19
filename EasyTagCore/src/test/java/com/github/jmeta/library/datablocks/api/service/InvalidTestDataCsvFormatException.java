/**
 *
 * {@link InvalidTestDataCsvFormatException}.java
 *
 * @author Jens Ebert
 *
 * @date 03.06.2011
 */
package com.github.jmeta.library.datablocks.api.service;

/**
 * {@link InvalidTestDataCsvFormatException} is thrown by {@link CsvFileDataFormatExpectationProvider} in case of
 * erroneous csv test data.
 */
public class InvalidTestDataCsvFormatException extends Exception {

   /**
    * Creates a new {@InvalidTestDataCsvFormatException}.
    * 
    * @param message
    *           The message.
    * @param cause
    *           The cause.
    */
   public InvalidTestDataCsvFormatException(String message, Throwable cause) {
      super(message, cause);
   }

   private static final long serialVersionUID = 1L;
}
