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

import org.junit.After;
import org.junit.Before;

import com.github.jmeta.utility.compregistry.api.services.ComponentRegistry;
import com.github.jmeta.utility.extmanager.api.services.IExtensionManager;
import com.github.jmeta.utility.logchecker.api.services.LogChecker;

import junit.framework.AssertionFailedError;

/**
 * {@link JMetaTestBasics} provides static methods to be used by most of the jMeta integration test cases for
 * cross-functional purposes. It is the basic test framework of jMeta integration testing.
 *
 * It basically does what {@link LibraryJMeta} does in the productive library: Load the extensions, instantiate
 * component registry and initialize all components.
 * 
 * The use of this class in test cases should follow the pattern:
 * <ul>
 * <li>Define a private non-static non-final attribute called registry of type {@link ISimpleComponentRegistry} in your
 * test class</li>
 * <li>Call {@link #setupComponents} in your jUnit setUp method (the one annotated with {@link Before}, assign its
 * return value to the previously mentioned attribute</li>
 * <li>In the setUp method, use the instance of {@link ISimpleComponentRegistry} to retrieve any component interfaces
 * you need for the test case, and assign them to private non-static non-final attributes of the test class</li>
 * <li>If the test cases in your test class need to access the {@link ISimpleComponentRegistry} or the component
 * interfaces, define private methods to retrieve the attributes</li>
 * <li>If you derive test classes from your current test class, make these methods protected to ensure the component
 * registry and component interfaces can be accessed by the subclasses</li>
 * <li>If a log check is required:
 * <ul>
 * <li>Call {@link #performGeneralLogCheck} in your jUnit tearDown method (the one annotated with {@link After}.</li>
 * </ul>
 * </ul>
 * 
 * This ensures that the life cycle of the components and its registry are clearly defined: They life during test case
 * execution, and not longer.
 */
public class JMetaTestBasics {

   /**
    * The default path where test case log files are stored.
    */
   public static final File DEFAULT_LOG_PATH = new File(new File(new File("."), "logs"), "unitTest");

   /**
    * The default log file
    */
   public static final File DEFAULT_LOG_FILE = new File(DEFAULT_LOG_PATH, "unitTest.log");

   /**
    * The default path where log files with errors are copied to.
    */
   private static final File FAILED_LOG_CHECK_PATH = new File(DEFAULT_LOG_PATH, "failedLogChecks");

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
    * Initially loads the extensions into the {@link IExtensionManager} from the central test extension point
    * configuration file.
    */
   public static void setupExtensions() {
      ComponentRegistry.lookupService(IExtensionManager.class);
   }
}
