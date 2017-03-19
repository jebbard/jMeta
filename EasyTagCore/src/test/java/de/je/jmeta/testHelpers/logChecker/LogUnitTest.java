/**
 *
 * {@link LogUnitTest}.java
 *
 * @author Jens Ebert
 *
 * @date 18.07.2011
 */
package de.je.jmeta.testHelpers.logChecker;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.regex.Pattern;

import de.je.jmeta.common.ILoggingMessageConstants;
import de.je.util.javautil.common.err.Contract;
import de.je.util.javautil.common.err.Reject;
import de.je.util.javautil.testUtil.setup.TestDataException;
import junit.framework.AssertionFailedError;

/**
 * {@link LogUnitTest} provides methods to check a log file for specific contents.
 */
public class LogUnitTest {

   /**
    * Loads a new log file to this {@link LogUnitTest}.
    *
    * @param logFile
    *           The log file to load. Must exist.
    */
   public void load(File logFile) {

      Reject.ifNull(logFile, "logFile");
      Contract.checkPrecondition(logFile.exists(),
         "logFile.exists() was false");
      Contract.checkPrecondition(logFile.isFile(),
         "logFile.isFile() was false");

      m_logFileContent = null;

      try (
         BufferedReader binput = new BufferedReader(new FileReader(logFile))) {

         StringBuffer buffer = new StringBuffer((int) logFile.length());

         String readLine = null;
         do {
            readLine = binput.readLine();

            if (readLine != null)
               buffer
                  .append(readLine + ILoggingMessageConstants.LINE_SEPARATOR);
         } while (readLine != null);

         m_logFileContent = buffer.toString();
      } catch (IOException e) {
         throw new TestDataException(
            "Could not read log file content due to exception", e);
      }

      m_currentFile = logFile;
   }

   /**
    * Returns whether currently log file content is loaded.
    *
    * @return true if currently log file content is loaded, false otherwise.
    */
   public boolean isLoaded() {

      return m_currentFile != null;
   }

   /**
    * Asserts that the currently loaded log file contains the specified pattern.
    *
    * @param pattern
    *           The regular expression search pattern.
    *
    * @pre {@link #isLoaded()} == true
    */
   public void assertContains(String pattern) {

      if (!contains(pattern))
         throw new AssertionFailedError(
            "Pattern <" + pattern + "> not found in log file <"
               + m_currentFile.getAbsolutePath() + ">.");
   }

   /**
    * Asserts that the currently loaded log file does not contain the specified pattern.
    *
    * @param pattern
    *           The regular expression search pattern.
    *
    * @pre {@link #isLoaded()} == true
    */
   public void assertContainsNot(String pattern) {

      if (contains(pattern))
         throw new AssertionFailedError("Pattern <" + pattern
            + "> found in log file <" + m_currentFile.getAbsolutePath() + ">.");
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
      Contract.checkPrecondition(isLoaded(), "isLoaded() was false");

      Pattern regexPattern = Pattern.compile(Pattern.quote(pattern));

      return regexPattern.matcher(m_logFileContent).find();
   }

   private File m_currentFile;

   private String m_logFileContent;
}
