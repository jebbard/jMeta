/**
 *
 * {@link LogChecker}.java
 *
 * @author Jens Ebert
 *
 * @date 18.07.2011
 */
package com.github.jmeta.utility.logchecker.api.services;

import java.io.File;
import java.util.logging.Level;

import com.github.jmeta.utility.logging.api.services.LoggingMessageConstants;

/**
 * {@link LogChecker} performs standard checks on a JMeta log file.
 */
public class LogChecker {

   /**
    * Performs standard checks on a JMeta log file.
    *
    * @param logFile
    *           The log file to check.
    */
   public void logCheck(File logFile) {

      LogUnitTest lut = new LogUnitTest();

      lut.load(logFile);

      lut.assertContainsNot(LoggingMessageConstants.PREFIX_CRITICAL_ERROR);
      lut.assertContainsNot(LoggingMessageConstants.PREFIX_CHECKED_EXCEPTION);
      lut.assertContainsNot(LoggingMessageConstants.PREFIX_RUNTIME_EXCEPTION);
      lut.assertContainsNot(LoggingMessageConstants.PREFIX_THROWABLE);
      lut.assertContainsNot(LoggingMessageConstants.PREFIX_TASK_FAILED);
      lut.assertContainsNot(LoggingMessageConstants.CACHE_MISS);
      lut.assertContainsNot(Level.SEVERE.toString());
   }
}
