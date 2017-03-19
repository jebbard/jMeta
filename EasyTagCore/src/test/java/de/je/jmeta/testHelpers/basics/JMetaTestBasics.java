/**
 *
 * {@link JMetaTestCaseConstants}.java
 *
 * @author Jens Ebert
 *
 * @date 27.06.2011
 */
package de.je.jmeta.testHelpers.basics;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

import org.junit.After;
import org.junit.Before;

import de.je.jmeta.context.impl.LibraryJMeta;
import de.je.jmeta.datablocks.IDataBlockAccessor;
import de.je.jmeta.datablocks.impl.StandardDataBlockAccessor;
import de.je.jmeta.dataformats.IDataFormatRepository;
import de.je.jmeta.dataformats.impl.StandardDataFormatRepository;
import de.je.jmeta.extmanager.export.IExtensionManager;
import de.je.jmeta.extmanager.impl.StandardExtensionManager;
import de.je.jmeta.media.api.IMediaAPI;
import de.je.jmeta.media.impl.MediaAPI;
import de.je.jmeta.testHelpers.logChecker.LogChecker;
import de.je.util.javautil.io.file.FileUtility;
import de.je.util.javautil.io.stream.NamedInputStream;
import de.je.util.javautil.simpleregistry.ISimpleComponentRegistry;
import de.je.util.javautil.simpleregistry.SimpleComponentRegistry;
import de.je.util.javautil.testUtil.setup.TestDataException;
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
    * Stores the relative path to the central extension management configuration file.
    */
   private final static String EXTENSION_MANAGEMENT_CONFIG_RESOURCE = "AllExtensionPoints.xml";

   /**
    * The default path where log files with errors are copied to.
    */
   private static final File FAILED_LOG_CHECK_PATH = new File(DEFAULT_LOG_PATH, "failedLogChecks");

   private static final LogChecker LOG_CHECKER = new LogChecker();

   /**
    * Creates and returns the {@link ISimpleComponentRegistry} to use for testing. Initializes all components of jMeta
    * and loads all extensions. To be used for full integration testing only.
    *
    * @return the {@link ISimpleComponentRegistry} to use for testing.
    */
   public static ISimpleComponentRegistry setupComponents() {

      ISimpleComponentRegistry registry = new SimpleComponentRegistry();

      IExtensionManager extensionManager = new StandardExtensionManager(registry);

      // Must be done BEFORE initializing any of the other components
      setupExtensions(extensionManager);

      IMediaAPI mediaAPI = new MediaAPI(registry);
      IDataFormatRepository repository = new StandardDataFormatRepository(registry);
      IDataBlockAccessor dataBlockAccessor = new StandardDataBlockAccessor(registry);

      // To avoid unused warnings
      assert(mediaAPI != null);
      assert(repository != null);
      assert(dataBlockAccessor != null);

      return registry;
   }

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
            FileUtility.copyFile(logFile, failed);
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
    * 
    * @param extensionManager
    *           The {@link IExtensionManager} to use
    */
   private static void setupExtensions(IExtensionManager extensionManager) {

      // Load all extensions BEFORE initializing any other component
      try {
         extensionManager.load(
            NamedInputStream.createFromResource(JMetaTestBasics.class, EXTENSION_MANAGEMENT_CONFIG_RESOURCE), null);
      } catch (IOException e) {
         throw new TestDataException(
            "Could not load extension configuration stream: " + EXTENSION_MANAGEMENT_CONFIG_RESOURCE, e);
      }
   }
}
