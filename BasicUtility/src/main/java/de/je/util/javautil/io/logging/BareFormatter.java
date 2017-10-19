/**
 *
 * {@link MyOwnFormatter}.java
 *
 * @author jebert
 *
 * @date 22.05.2011
 */
package de.je.util.javautil.io.logging;

import java.util.logging.Formatter;
import java.util.logging.LogRecord;

import de.je.util.javautil.common.err.ThrowableToStringConverter;

/**
 * {@link BareFormatter} does not do any formatting at all, it simply writes the given log message or exception.
 */
public class BareFormatter extends Formatter {

   /**
    * @see java.util.logging.Formatter#format(java.util.logging.LogRecord)
    */
   @Override
   public String format(LogRecord record) {
      StringBuffer buf = new StringBuffer();
      final Throwable thrown = record.getThrown();

      if (thrown != null)
         appendThrowables(buf, thrown);

      else
         buf.append(record.getMessage());

      buf.append(System.getProperty("line.separator"));

      return buf.toString();
   }

   /**
    * Appends information for the given {@link Throwable} to the given {@link StringBuffer}.
    *
    * @param buf
    *           The {@link StringBuffer}.
    * @param thrown
    *           The {@link Throwable}.
    */
   private static void appendThrowables(StringBuffer buf, final Throwable thrown) {
      buf.append(ThrowableToStringConverter.getStackTraceAsString(thrown));
   }
}
