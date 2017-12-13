/**
 *
 * {@link TagFinderFormatter}.java
 *
 * @author jebert
 *
 * @date 03.03.2011
 */
package com.github.jmeta.tools.tagfinder.api.types;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

/**
 * {@link TagFinderFormatter}
 *
 */
public class TagFinderFormatter extends Formatter {

   private static final String LINE_SEPARATOR = System
      .getProperty("line.separator");

   private static final SimpleDateFormat SDF = new SimpleDateFormat(
      "yyyy-MM-dd HH:mm:ss", Locale.ENGLISH);

   /**
    * @see java.util.logging.Formatter#format(java.util.logging.LogRecord)
    */
   @Override
   public String format(LogRecord record) {

      StringBuffer logString = new StringBuffer();

      logString.append(record.getLevel().getName());
      logString.append(": ");
      logString.append(SDF.format(new Date(record.getMillis())));
      logString.append("   ");

      final String message = record.getMessage();

      if (!message.equals(LINE_SEPARATOR))
         logString.append(message);

      logString.append(LINE_SEPARATOR);

      return logString.toString();
   }
}
