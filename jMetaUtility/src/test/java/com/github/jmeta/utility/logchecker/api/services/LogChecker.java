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

import com.github.jmeta.utility.logging.api.services.LoggingConstants;

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

      lut.assertContainsNot(LoggingConstants.PREFIX_CRITICAL_ERROR);
      lut.assertContainsNot(LoggingConstants.PREFIX_CHECKED_EXCEPTION);
      lut.assertContainsNot(LoggingConstants.PREFIX_RUNTIME_EXCEPTION);
      lut.assertContainsNot(LoggingConstants.PREFIX_THROWABLE);
      lut.assertContainsNot(LoggingConstants.PREFIX_TASK_FAILED);
      lut.assertContainsNot(LoggingConstants.CACHE_MISS);
      lut.assertContainsNot(Level.SEVERE.toString());
   }
}
