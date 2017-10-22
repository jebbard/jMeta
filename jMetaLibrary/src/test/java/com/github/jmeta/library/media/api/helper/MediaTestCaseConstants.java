/**
 *
 * {@link MediaTestCaseConstants}.java
 *
 * @author Jens Ebert
 *
 * @date 17.04.2011
 */
package com.github.jmeta.library.media.api.helper;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * {@link MediaTestCaseConstants} provides some globally available constants for the test cases of the media component.
 */
public interface MediaTestCaseConstants {

   /**
    * Path for test output files.
    */
   public static final Path TEST_FILE_OUTPUT_DIR = Paths.get("./data/media/");

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
   public static final Path STANDARD_TEST_FILE = TEST_FILE_OUTPUT_DIR.resolve(STANDARD_TEST_FILE_RESOURCE);

   /**
    * Path to another global test file.
    */
   public static final Path SECOND_TEST_FILE = TEST_FILE_OUTPUT_DIR.resolve(SECOND_TEST_FILE_RESOURCE);
}
