/**
 *
 * {@link MediaTestCaseConstants}.java
 *
 * @author Jens Ebert
 *
 * @date 17.04.2011
 */
package de.je.jmeta.media.api;

import java.io.File;

/**
 * {@link MediaTestCaseConstants} provides some globally available constants for the test cases of the media component.
 */
public interface MediaTestCaseConstants {

   /**
    * Path for test output files.
    */
   public static final File TEST_FILE_OUTPUT_DIR = new File("./data/media/");

   /**
    * Name of the global test file who's contents can be used by all concrete tests.
    */
   public static final String STANDARD_TEST_FILE_RESOURCE = "TestFile_1.txt";

   /**
    * Name of another global test file who's contents can be used by all concrete tests.
    */
   public static final String SECOND_TEST_FILE_RESOURCE = "TestFile_2.txt";

   /**
    * Path to the standard global test file.
    */
   public static final File STANDARD_TEST_FILE = new File(TEST_FILE_OUTPUT_DIR,
      STANDARD_TEST_FILE_RESOURCE);

   /**
    * Path to another global test file.
    */
   public static final File SECOND_TEST_FILE = new File(TEST_FILE_OUTPUT_DIR,
      SECOND_TEST_FILE_RESOURCE);
}
