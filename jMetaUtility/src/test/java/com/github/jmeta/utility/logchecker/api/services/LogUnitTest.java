/**
 *
 * {@link LogUnitTest}.java
 *
 * @author Jens Ebert
 *
 * @date 18.07.2011
 */
package com.github.jmeta.utility.logchecker.api.services;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.regex.Pattern;

import com.github.jmeta.utility.dbc.api.services.Reject;
import com.github.jmeta.utility.logging.api.services.LoggingConstants;
import com.github.jmeta.utility.testsetup.api.exceptions.InvalidTestDataException;

import junit.framework.AssertionFailedError;

/**
 * {@link LogUnitTest} provides methods to check a log file for specific contents.
 */
public class LogUnitTest {

   private File currentFile;

   private String logFileContent;

   /**
    * Loads a new log file to this {@link LogUnitTest}.
    *
    * @param logFile
    *           The log file to load. Must exist.
    */
   public void load(File logFile) {

      Reject.ifNull(logFile, "logFile");
      Reject.ifFalse(logFile.exists(), "logFile.exists()");
      Reject.ifFalse(logFile.isFile(), "logFile.isFile()");

      logFileContent = null;

      try (BufferedReader binput = new BufferedReader(new FileReader(logFile))) {

         StringBuffer buffer = new StringBuffer((int) logFile.length());

         String readLine = null;
         do {
            readLine = binput.readLine();

            if (readLine != null) {
               buffer.append(readLine + LoggingConstants.LINE_SEPARATOR);
            }
         } while (readLine != null);

         logFileContent = buffer.toString();
      } catch (IOException e) {
         throw new InvalidTestDataException("Could not read log file content due to exception", e);
      }

      currentFile = logFile;
   }

   /**
    * Returns whether currently log file content is loaded.
    *
    * @return true if currently log file content is loaded, false otherwise.
    */
   public boolean isLoaded() {

      return currentFile != null;
   }

   /**
    * Asserts that the currently loaded log file contains the specified pattern. As a precondition, this class must be
    * loaded already.
    *
    * @param pattern
    *           The regular expression search pattern.
    */
   public void assertContains(String pattern) {
      if (!contains(pattern)) {
         throw new AssertionFailedError(
            "Pattern <" + pattern + "> not found in log file <" + currentFile.getAbsolutePath() + ">.");
      }
   }

   /**
    * Asserts that the currently loaded log file does not contain the specified pattern. As a precondition, this class
    * must be loaded already.
    *
    * @param pattern
    *           The regular expression search pattern.
    */
   public void assertContainsNot(String pattern) {
      if (contains(pattern)) {
         throw new AssertionFailedError(
            "Pattern <" + pattern + "> found in log file <" + currentFile.getAbsolutePath() + ">.");
      }
   }

   /**
    * Checks whether the current log file contains the specified search pattern.
    *
    * @param pattern
    *           The search pattern.
    * @return true if the current log file contains the specified search pattern, false otherwise.
    */
   private boolean contains(String pattern) {

      Reject.ifNull(pattern, "pattern");
      Reject.ifFalse(isLoaded(), "isLoaded()");

      Pattern regexPattern = Pattern.compile(Pattern.quote(pattern));

      return regexPattern.matcher(logFileContent).find();
   }
}
