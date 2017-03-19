/**
 *
 * {@link LogChecker}.java
 *
 * @author Jens Ebert
 *
 * @date 18.07.2011
 */
package de.je.jmeta.testHelpers.logChecker;

import java.io.File;
import java.util.logging.Level;

import de.je.jmeta.common.ILoggingMessageConstants;

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

      lut.assertContainsNot(ILoggingMessageConstants.PREFIX_CRITICAL_ERROR);
      lut.assertContainsNot(ILoggingMessageConstants.PREFIX_CHECKED_EXCEPTION);
      lut.assertContainsNot(ILoggingMessageConstants.PREFIX_RUNTIME_EXCEPTION);
      lut.assertContainsNot(ILoggingMessageConstants.PREFIX_THROWABLE);
      lut.assertContainsNot(ILoggingMessageConstants.PREFIX_TASK_FAILED);
      lut.assertContainsNot(ILoggingMessageConstants.CACHE_MISS);
      lut.assertContainsNot(Level.SEVERE.toString());
   }
}
