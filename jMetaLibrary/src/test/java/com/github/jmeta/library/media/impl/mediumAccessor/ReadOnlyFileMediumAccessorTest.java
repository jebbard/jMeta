/**
 * {@link WritableFileMediumAccessorTest}.java
 *
 * @author Jens Ebert
 * @date 09.12.10 21:22:53 (December 9, 2010)
 */

package com.github.jmeta.library.media.impl.mediumAccessor;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import com.github.jmeta.library.media.api.exceptions.MediumAccessException;
import com.github.jmeta.library.media.api.helper.MediaTestCaseConstants;
import com.github.jmeta.library.media.api.types.FileMedium;

/**
 * Tests the class {@FileMediumAccessor} for a read-only medium.
 */
public class ReadOnlyFileMediumAccessorTest extends AbstractReadOnlyMediumAccessorTest {

   /**
    * Tests whether the creation of a new {@link FileMediumAccessor} on an already locked medium.
    */
   @Test(expected = MediumAccessException.class)
   public void createNewFileMediumAccessor_forAlreadyLockedMedium_throwsException() {

      // Make sure the first instance is created
      getImplementationToTest();

      // Create second instance on the same medium
      new FileMediumAccessor(new FileMedium(MediaTestCaseConstants.STANDARD_TEST_FILE, true));
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
      return new FileMedium(MediaTestCaseConstants.STANDARD_TEST_FILE, true);
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
   }

}