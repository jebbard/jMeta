/**
 *
 * {@link JMetaTestCaseConstants}.java
 *
 * @author Jens Ebert
 *
 * @date 27.06.2011
 */
package com.github.jmeta.utility.testsetup.api.services;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

import com.github.jmeta.utility.compregistry.api.services.ComponentRegistry;
import com.github.jmeta.utility.extmanager.api.services.ExtensionManager;
import com.github.jmeta.utility.logchecker.api.services.LogChecker;

import junit.framework.AssertionFailedError;

/**
 * {@link JMetaTestBasics} provides static methods to be used by most of the jMeta integration test cases for
 * cross-functional purposes. It is the basic test framework of jMeta integration testing.
 *
 * It basically does what the productive library does: Load the extensions, instantiate component registry and
 * initialize all components.
 */
public class JMetaTestBasics {

   /**
    * The default path where test case log files are stored - ATTENTION: Must be the same as configured in
    * log4j2-test.xml
    */
   public static final File DEFAULT_UNITTEST_LOG_PATH = new File(new File(new File("."), "logs"), "unitTest");

   /**
    * The default log file - ATTENTION: Must be the same as configured in log4j2-test.xml
    */
   public static final File DEFAULT_UNITTEST_LOG_FILE = new File(DEFAULT_UNITTEST_LOG_PATH, "jMeta.log");

   /**
    * The default path where log files with errors are copied to.
    */
   private static final File FAILED_LOG_CHECK_PATH = new File(DEFAULT_UNITTEST_LOG_PATH, "failedLogChecks");

   private static final LogChecker LOG_CHECKER = new LogChecker();

   /**
    * Does an additional, generic log check after test case execution. If the log check fails, the method copies the log
    * file with the failures inside, so that the user may check it after the test run.
    * 
    * @param logFile
    *           The log file to check
    */
   public static void performGeneralLogCheck(File logFile) {

      try {
         LOG_CHECKER.logCheck(logFile);
      } catch (AssertionFailedError e1) {
         // Copy failed log file into separate directory
         File failed = new File(FAILED_LOG_CHECK_PATH, logFile.getName() + "_" + System.currentTimeMillis());

         try {
            Files.copy(logFile.toPath(), failed.toPath(), StandardCopyOption.REPLACE_EXISTING);
         } catch (IOException e) {
            throw new RuntimeException("Exception during log file copying", e);
         }

         throw e1;
      }
   }

   /**
    * Method for truncating the whole contents of the given log file to ensure no output from previous test cases is
    * contained.
    * 
    * @param logFile
    *           The log file to truncate.
    */
   public static void emptyLogFile(File logFile) {
      try (RandomAccessFile raf = new RandomAccessFile(logFile, "rw")) {
         raf.getChannel().truncate(0);
      } catch (IOException e) {
         throw new RuntimeException("Exception during log file truncation", e);
      }
   }

   /**
    * Initially loads the extensions into the {@link ExtensionManager} from the central test extension point
    * configuration file.
    */
   public static void setupExtensions() {
      ComponentRegistry.lookupService(ExtensionManager.class);
   }
}
