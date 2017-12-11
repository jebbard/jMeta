/**
 *
 * {@link TestMedia}.java
 *
 * @author Jens Ebert
 *
 * @date 17.04.2011
 */
package com.github.jmeta.library.media.api.helper;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.github.jmeta.library.media.api.types.FileMedium;
import com.github.jmeta.library.media.api.types.InMemoryMedium;
import com.github.jmeta.library.media.api.types.Medium;
import com.github.jmeta.library.media.api.types.MediumOffset;
import com.github.jmeta.library.media.impl.offset.StandardMediumOffset;
import com.github.jmeta.utility.charset.api.services.Charsets;
import com.github.jmeta.utility.testsetup.api.exceptions.InvalidTestDataException;

/**
 * {@link TestMedia} provides some globally available media and files for the test cases of the media component.
 */
public interface TestMedia {

   /**
    * Validates the correctness of the existing test files, i.e. that they exist, have the correct content etc. Should
    * be called in the before phase of a test case that uses these test files. It helps to check the expected
    * preconditions of a test and generates meaningful error message in case they are not met (which is an unexpected
    * abnormal event).
    * 
    * You should call this method ideally only once per test class, e.g. in the \@BeforeClass method, or in \@Before
    * before each test case. Ensure to call it at the very beginning of the method to not interfere with any locking
    * activities that your other setup code might perform on the files.
    */
   public static void validateTestFiles() {
      checkDirectory(TEST_FILE_DIRECTORY_PATH);
      checkDirectory(TEST_FILE_TEMP_OUTPUT_DIRECTORY_PATH);

      checkFile(FIRST_TEST_FILE_PATH, FIRST_TEST_FILE_CONTENT, "FIRST_TEST_FILE_CONTENT");
      checkFile(SECOND_TEST_FILE_PATH, SECOND_TEST_FILE_CONTENT, "SECOND_TEST_FILE_CONTENT");
      checkFile(EMPTY_TEST_FILE_PATH, EMPTY_TEST_FILE_CONTENT, "EMPTY_TEST_FILE_CONTENT");
   }

   // TODO make private once migrated to Java 9
   /**
    * Checks the content of the given file to be an actual existing file with correct content.
    * 
    * @param path
    *           The file to check
    * @param expectedContent
    *           The expected file content as string
    * @param constantName
    *           The constant in this class holding the file's expected content
    */
   public static void checkFile(Path path, String expectedContent, String constantName) {
      if (!Files.isRegularFile(path)) {
         throw new InvalidTestDataException("Test data file prerequisites: The file <" + path
            + "> unexpectedly does not exist, ensure that all files are existing and or not deleted by any automatically executed code",
            null);
      }

      String actualFileContent = new String(MediaTestUtility.readFileContent(path), Charsets.CHARSET_UTF8);

      if (!actualFileContent.equals(expectedContent)) {
         throw new InvalidTestDataException("The actual content of the file <" + path
            + "> does not match the expected content as given in the constant <" + constantName + ">", null);
      }
   }

   /**
    * Checks a directory to be valid
    * 
    * @param path
    *           The directory to check
    */
   public static void checkDirectory(Path path) {
      if (!Files.isDirectory(path)) {
         throw new InvalidTestDataException("Test data file prerequisites: The directory <" + path
            + "> unexpectedly does not exist, ensure that all folders are existing and or not deleted by any automatically executed code",
            null);
      }
   }

   /**
    * @return the content of the file {@link TestMedia#FIRST_TEST_FILE_PATH} as UTF-8 encoded string
    */
   public static String getFirstTestFileContentAsString() {
      return new String(MediaTestUtility.readFileContent(TestMedia.FIRST_TEST_FILE_PATH), Charsets.CHARSET_UTF8);
   }

   /**
    * @return the content of the file {@link TestMedia#SECOND_TEST_FILE_PATH} as UTF-8 encoded string
    */
   public static String getSecondTestFileContentAsString() {
      return new String(MediaTestUtility.readFileContent(TestMedia.SECOND_TEST_FILE_PATH), Charsets.CHARSET_UTF8);
   }

   /**
    * @return the content of the file {@link TestMedia#EMPTY_TEST_FILE_PATH} as UTF-8 encoded string
    */
   public static String getEmptyTestFileContentAsString() {
      return new String(MediaTestUtility.readFileContent(TestMedia.EMPTY_TEST_FILE_PATH), Charsets.CHARSET_UTF8);
   }

   /**
    * Convenience method returning a new {@link MediumOffset} pointing to the specified offset on the given
    * {@link Medium}. Also encapsulates the use of the {@link MediumOffset} implementation class in test cases to just a
    * few places.
    * 
    * @param medium
    *           The {@link Medium} to use
    * @param offset
    *           The offset to use
    * @return a new {@link MediumOffset} pointing to the specified offset on the specified {@link Medium}
    */
   public static MediumOffset at(Medium<?> medium, long offset) {
      return new StandardMediumOffset(medium, offset);
   }

   /**
    * Convenience method returning a new {@link MediumOffset} pointing to the specified offset on the
    * {@link #DEFAULT_TEST_MEDIUM}. Also encapsulates the use of the {@link MediumOffset} implementation class in test
    * cases to just a few places.
    * 
    * @param offset
    *           The offset to use
    * @return a new {@link MediumOffset} pointing to the specified offset on the default {@link #DEFAULT_TEST_MEDIUM}
    */
   public static MediumOffset at(long offset) {
      return at(DEFAULT_TEST_MEDIUM, offset);
   }

   /**
    * Path for test files.
    */
   public static final Path TEST_FILE_DIRECTORY_PATH = Paths.get("./data/media/");

   /**
    * Path for temporary output files.
    */
   public static final Path TEST_FILE_TEMP_OUTPUT_DIRECTORY_PATH = TEST_FILE_DIRECTORY_PATH.resolve("temp");

   /**
    * Name of the global test file who's contents can be used by all concrete tests.
    */
   public static final String FIRST_TEST_FILE_NAME = "TestFile_1.txt";

   /**
    * Path to the standard global test file. It is UTF-8 encoded and contains only standard ASCII characters, its
    * content is available in the constant {@link #FIRST_TEST_FILE_CONTENT}.
    */
   public static final Path FIRST_TEST_FILE_PATH = TEST_FILE_DIRECTORY_PATH.resolve(FIRST_TEST_FILE_NAME);

   /**
    * This is the exact content of the file with path {@link #FIRST_TEST_FILE_PATH}. As a precondition, it must always
    * exactly match the content of that file (byte-by-byte). Note that the file is UTF-8 encoded with only standard
    * ASCII characters, thus the string's length equals the total file size in bytes.
    */
   public static final String FIRST_TEST_FILE_CONTENT = "Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed "
      + "diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua. At vero eos et "
      + "accusam et justo duo dolores et ea rebum. Stet clita kasd gubergren, no sea takimata sanctus est Lorem ipsum "
      + "dolor sit amet. Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor "
      + "invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua. At vero eos et accusam et justo duo "
      + "dolores et ea rebum. Stet clita kasd gubergren, no sea takimata sanctus est Lorem ipsum dolor sit amet. Lorem "
      + "ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut labore et dolore "
      + "magna aliquyam erat, sed diam voluptua. At vero eos et accusam et justo duo dolores et ea rebum. Stet clita "
      + "kasd gubergren, no sea takimata sanctus est Lorem ipsum dolor sit amet. Duis autem vel eum iriure dolor in "
      + "hendrerit in vulputate velit esse molestie consequat, vel illum dolore eu feugiat nulla facilisis at vero eros "
      + "et accumsan et iusto odio dignissim qui blandit praesent luptatum zzril delenit augue duis dolore te feugait "
      + "nulla facilisi. Lorem ipsum dolor sit amet,";

   /**
    * Name of another global test file who's contents can be used by all concrete tests.
    */
   public static final String SECOND_TEST_FILE_NAME = "TestFile_2.txt";

   /**
    * Path to another global test file.
    */
   public static final Path SECOND_TEST_FILE_PATH = TEST_FILE_DIRECTORY_PATH.resolve(SECOND_TEST_FILE_NAME);

   /**
    * This is the exact content of the file with path {@link #SECOND_TEST_FILE_PATH}. As a precondition, it must always
    * exactly match the content of that file (byte-by-byte). Note that the file is UTF-8 encoded with only standard
    * ASCII characters, thus the string's length equals the total file size in bytes.
    */
   public static final String SECOND_TEST_FILE_CONTENT = "any text no matter what";

   /**
    * Name of the global empty test file .
    */
   public static final String EMPTY_TEST_FILE_NAME = "EmptyTestFile.txt";

   /**
    * Path to the standard global test file. It is UTF-8 encoded and contains only standard ASCII characters, its
    * content is available in the constant {@link #FIRST_TEST_FILE_CONTENT}.
    */
   public static final Path EMPTY_TEST_FILE_PATH = TEST_FILE_DIRECTORY_PATH.resolve(EMPTY_TEST_FILE_NAME);

   public static final String EMPTY_TEST_FILE_CONTENT = "";

   /**
    * A test {@link Medium} to be used whenever it does not matter which exact type and properties the {@link Medium}
    * used has. This is a read-only {@link FileMedium} with default caching and read-write block size, pointing to an
    * existing file {@link TestMedia#FIRST_TEST_FILE_PATH}.
    */
   public static final FileMedium DEFAULT_TEST_MEDIUM = new FileMedium(TestMedia.FIRST_TEST_FILE_PATH, true);

   /**
    * A second test {@link Medium} to be used whenever checks require another {@link Medium} than the
    * {@link #DEFAULT_TEST_MEDIUM}.
    */
   public static final InMemoryMedium OTHER_MEDIUM = new InMemoryMedium(new byte[] {}, "Fake", false);
}
