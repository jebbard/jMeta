/**
 *
 * {@link LoggingHelper}.java
 *
 * @author jebert
 *
 * @date 17.02.2012
 */
package de.je.util.javautil.io.logging;

import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.Logger;
import java.util.logging.StreamHandler;

import de.je.util.javautil.common.err.Reject;

/**
 * {@link LoggingUtility} provides a collection of utility functions for working with Java's {@link Logger} class.
 */
public class LoggingUtility {

   /**
    * Returns a {@link Logger} instance configured for usual usage. This includes a log file output, and optional
    * console logging to System.out. The logger name is set to the given classe's name. The use of parent handlers is
    * set to false.
    *
    * @param forClass
    *           The class for which to create the {@link Logger} instance.
    * @param formatter
    *           The {@link Formatter} to use for both file and optional console logging.
    * @param logFilePattern
    *           The pattern for the log file name as described in the {@link FileHandler} class.
    * @param consoleLogging
    *           true to enable additional console logging to System.out, false to log only to the given log file.
    * @return The {@link Logger} instance.
    * @throws Exception
    *            if any exception occurred during creating and configuring the {@link Logger}.
    */
   public static Logger getDefaultPrivateLogger(Class<?> forClass, Formatter formatter, String logFilePattern,
      boolean consoleLogging) throws Exception {
      Reject.ifNull(logFilePattern, "logFilePattern");
      Reject.ifNull(formatter, "formatter");
      Reject.ifNull(forClass, "forClass");

      Logger theLogger = Logger.getLogger(forClass.getName());

      final FileHandler fileHandler = new FileHandler(logFilePattern);

      fileHandler.setFormatter(formatter);

      theLogger.setUseParentHandlers(false);

      theLogger.addHandler(fileHandler);

      if (consoleLogging) {
         final StreamHandler consoleHandler = new StreamHandler(System.out, formatter);

         theLogger.addHandler(consoleHandler);
      }

      return theLogger;
   }
}
