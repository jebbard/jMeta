/**
 *
 * {@link AbstractCsvHandler}.java
 *
 * @author Jens Ebert
 *
 * @date 27.06.2011
 */
package de.je.util.javautil.io.csv;

import java.io.Closeable;
import java.io.IOException;

import de.je.util.javautil.common.err.Reject;

/**
 * {@link AbstractCsvHandler} provides general methods for a class that is able to read or write csv (comma separated
 * value) files.
 *
 * @param <T>
 *           The concrete type of closable class
 */
public abstract class AbstractCsvHandler<T extends Closeable & AutoCloseable> {

   /**
    * The default separator that is used initially by a new {@link CsvReader} instance or if null is passed to
    * {@link #setSeparator(Character)}
    */
   public static final Character DEFAULT_SEPARATOR = ';';
   /**
    * The default quote that is used initially by a new {@link CsvReader} instance or if null is passed to
    * {@link #setQuote(Character)}
    */
   public static final Character DEFAULT_QUOTE = '"';

   /**
    * Creates a new {@link AbstractCsvHandler}.
    * 
    * @param csvResource
    *           The csv resource
    * @throws IOException
    *            if there was any error during closing the previous resource
    */
   public void setNewResource(T csvResource) throws IOException {
      Reject.ifNull(csvResource, "csvResource");

      if (isCsvLoaded())
         closeCurrentCsvResource();

      m_csvResource = csvResource;
   }

   /**
    * Returns the separator character used, which is never null.
    *
    * @return the separator character used, which is never null.
    */
   public Character getSeparator() {
      return m_separator;
   }

   /**
    * Sets the separator to use, may be null to indicate the use of the {@link #DEFAULT_SEPARATOR}.
    *
    * @param separator
    *           the separator to use, may be null to indicate the use of the {@link #DEFAULT_SEPARATOR}.
    */
   public void setSeparator(Character separator) {
      if (separator == null)
         m_separator = DEFAULT_SEPARATOR;
      else
         m_separator = separator;
   }

   /**
    * Returns the quote character used, which is never null.
    *
    * @return the quote character used, which is never null.
    */
   public Character getQuote() {
      return m_quote;
   }

   /**
    * Sets the quote to use, may be null to indicate the use of the {@link #DEFAULT_QUOTE}.
    *
    * @param quote
    *           the quote to use, may be null to indicate the use of the {@link #DEFAULT_QUOTE}.
    */
   public void setQuote(Character quote) {
      if (quote == null)
         m_quote = DEFAULT_QUOTE;
      else
         m_quote = quote;
   }

   /**
    * Returns whether a csv file has already been loaded.
    *
    * @return whether a csv file has already been loaded.
    */
   public boolean isCsvLoaded() {
      return m_csvResource != null;
   }

   /**
    * Returns the currently loaded csv file or null if no file is loaded.
    *
    * @return the currently loaded csv file or null if no file is loaded.
    */
   public T getCurrentCsvResource() {
      return m_csvResource;
   }

   /**
    * Closes the current file of this {@link CsvReader}. A file must be currently loaded to be able to call this method.
    * The {@link CsvReader} can further be used after this method has been called. For this, simply a new source must be
    * set using {@link #setNewResource(Closeable)}.
    * 
    * @throws IOException
    *            If closing the current file failed.
    *
    * @pre {@link #isCsvLoaded()} == true
    * @post {@link #isCsvLoaded()} == false
    */
   public void closeCurrentCsvResource() throws IOException {
      Reject.ifFalse(isCsvLoaded(), "isCsvLoaded()");
      m_csvResource.close();

      m_csvResource = null;
   }

   private Character m_separator;
   private Character m_quote;
   private T m_csvResource;
}
