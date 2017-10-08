/**
 * {@link WritableFileMediumAccessorTest}.java
 *
 * @author Jens Ebert
 * @date 09.12.10 21:22:53 (December 9, 2010)
 */

package de.je.jmeta.media.impl.mediumAccessor;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import de.je.jmeta.media.api.MediaTestCaseConstants;
import de.je.jmeta.media.api.datatype.FileMedium;
import de.je.jmeta.media.api.exception.MediumAccessException;
import de.je.jmeta.media.impl.FileMediumAccessor;
import de.je.jmeta.media.impl.IMediumAccessor;
import de.je.util.javautil.io.file.FileUtility;

/**
 * Tests the class {@FileMediumAccessor}.
 */
public class WritableFileMediumAccessorTest extends AbstractWritableRandomAccessMediumAccessorTest {

   private static final String TEMP_FOLDER_NAME = "temp/";

   private static int TEST_FILE_COUNTER = 0;

   private File copiedTestFile = new File(MediaTestCaseConstants.TEST_FILE_OUTPUT_DIR + TEMP_FOLDER_NAME
      + MediaTestCaseConstants.STANDARD_TEST_FILE_RESOURCE);

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
    * @see AbstractIMediumAccessorTest#getReadTestDataToUse()
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
    * @see de.je.jmeta.media.impl.mediumAccessor.AbstractIMediumAccessorTest#getExpectedMedium()
    */
   @Override
   protected FileMedium getExpectedMedium() {
      return new FileMedium(copiedTestFile, false);
   }

   /**
    * @see de.je.jmeta.media.impl.mediumAccessor.AbstractIMediumAccessorTest#getReadTestDataUntilEndOfMedium()
    */
   @Override
   protected ReadTestData getReadTestDataUntilEndOfMedium() {
      return new ReadTestData(550, getExpectedFileContents().length - 550);
   }

   /**
    * @see AbstractIMediumAccessorTest#getImplementationToTest()
    */
   @Override
   protected IMediumAccessor<?> createImplementationToTest() {
      return new FileMediumAccessor(getExpectedMedium());
   }

   /**
    * @see AbstractIMediumAccessorTest#prepareMediumData(byte[])
    */
   @Override
   protected void prepareMediumData(byte[] testFileContents) {

      copiedTestFile = new File(MediaTestCaseConstants.TEST_FILE_OUTPUT_DIR,
         TEMP_FOLDER_NAME + TEST_FILE_COUNTER + '_' + MediaTestCaseConstants.STANDARD_TEST_FILE_RESOURCE);

      TEST_FILE_COUNTER++;

      // If the copied file still exists (from a recent test run), delete it
      if (copiedTestFile.exists())
         if (!copiedTestFile.delete())
            throw new RuntimeException(
               "Could not delete copied test file " + copiedTestFile + ". Manuel deletion is necessary.");

      final String message = "Could not copy the test file " + MediaTestCaseConstants.STANDARD_TEST_FILE_RESOURCE
         + " to " + copiedTestFile;
      try {
         if (!FileUtility.copyFile(MediaTestCaseConstants.STANDARD_TEST_FILE, copiedTestFile))
            throw new RuntimeException(message);
      } catch (IOException e) {
         throw new RuntimeException(message, e);
      }
   }

}