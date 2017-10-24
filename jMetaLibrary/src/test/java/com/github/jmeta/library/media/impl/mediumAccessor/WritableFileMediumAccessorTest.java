/**
 * {@link WritableFileMediumAccessorTest}.java
 *
 * @author Jens Ebert
 * @date 09.12.10 21:22:53 (December 9, 2010)
 */

package com.github.jmeta.library.media.impl.mediumAccessor;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import com.github.jmeta.library.media.api.exceptions.MediumAccessException;
import com.github.jmeta.library.media.api.helper.MediaTestCaseConstants;
import com.github.jmeta.library.media.api.types.FileMedium;

/**
 * Tests the class {@FileMediumAccessor} for a writable medium.
 */
public class WritableFileMediumAccessorTest extends AbstractWritableRandomAccessMediumAccessorTest {

   private static final String TEMP_FOLDER_NAME = "temp/";

   /**
    * A container increased per call to {@link #prepareMediumData(byte[])}, i.e. per test case, to ensure after
    * execution of all test cases that the modified files are still present for each of them.
    */
   private static int TEST_FILE_COUNTER = 0;

   private Path copiedTestFile = MediaTestCaseConstants.TEST_FILE_OUTPUT_DIR
      .resolve(TEMP_FOLDER_NAME + MediaTestCaseConstants.STANDARD_TEST_FILE_RESOURCE);

   /**
    * Tests whether the creation of a new {@link FileMediumAccessor} on an already locked medium.
    */
   @Test(expected = MediumAccessException.class)
   public void createNewFileMediumAccessor_forAlreadyLockedMedium_throwsException() {

      // Make sure the first instance is created
      getImplementationToTest();

      // Create second instance on the same medium
      new FileMediumAccessor(new FileMedium(copiedTestFile, false));
   }

   /**
    * @see AbstractMediumAccessorTest#getReadTestDataToUse()
    */
   @Override
   protected List<ReadTestData> getReadTestDataToUse() {

      List<ReadTestData> readOffsetsAndSizes = new ArrayList<>();

      readOffsetsAndSizes.add(new ReadTestData(16, 7));
      readOffsetsAndSizes.add(new ReadTestData(93, 157));
      readOffsetsAndSizes.add(new ReadTestData(610, 133));
      readOffsetsAndSizes.add(new ReadTestData(0, 17));
      readOffsetsAndSizes.add(new ReadTestData(211, 45));

      return readOffsetsAndSizes;
   }

   /**
    * @see com.github.jmeta.library.media.impl.mediumAccessor.AbstractMediumAccessorTest#getExpectedMedium()
    */
   @Override
   protected FileMedium getExpectedMedium() {
      return new FileMedium(copiedTestFile, false);
   }

   /**
    * @see com.github.jmeta.library.media.impl.mediumAccessor.AbstractMediumAccessorTest#getReadTestDataUntilEndOfMedium()
    */
   @Override
   protected ReadTestData getReadTestDataUntilEndOfMedium() {
      return new ReadTestData(550, EXPECTED_FILE_CONTENTS.length - 550);
   }

   /**
    * @see AbstractMediumAccessorTest#getImplementationToTest()
    */
   @Override
   protected MediumAccessor<?> createImplementationToTest() {
      return new FileMediumAccessor(getExpectedMedium());
   }

   /**
    * @see AbstractMediumAccessorTest#prepareMediumData(byte[])
    */
   @Override
   protected void prepareMediumData(byte[] testFileContents) {

      copiedTestFile = MediaTestCaseConstants.TEST_FILE_OUTPUT_DIR
         .resolve(TEMP_FOLDER_NAME + TEST_FILE_COUNTER + '_' + MediaTestCaseConstants.STANDARD_TEST_FILE_RESOURCE);

      TEST_FILE_COUNTER++;

      String message = "Could not copy the test file " + MediaTestCaseConstants.STANDARD_TEST_FILE_RESOURCE + " to "
         + copiedTestFile;

      try {
         Files.copy(MediaTestCaseConstants.STANDARD_TEST_FILE, copiedTestFile, StandardCopyOption.REPLACE_EXISTING);
      } catch (IOException e) {
         throw new RuntimeException(message, e);
      }
   }

}