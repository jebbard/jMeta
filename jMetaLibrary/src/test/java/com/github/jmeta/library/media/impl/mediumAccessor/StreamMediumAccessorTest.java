/**
 * {@link StreamMediumAccessorTest}.java
 *
 * @author Jens Ebert
 * @date 09.12.10 21:22:53 (December 9, 2010)
 */

package com.github.jmeta.library.media.impl.mediumAccessor;

import static com.github.jmeta.library.media.api.helper.MediaTestUtility.at;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.github.jmeta.library.media.api.helper.MediaTestFiles;
import com.github.jmeta.library.media.api.types.InputStreamMedium;
import com.github.jmeta.library.media.api.types.MediumReference;

/**
 * Tests the class {@StreamMediumAccessor}.
 */
public class StreamMediumAccessorTest extends AbstractReadOnlyMediumAccessorTest {

   private InputStream testStream;

   /**
    * Tests {@link MediumAccessor#setCurrentPosition(MediumReference)} and {@link MediumAccessor#getCurrentPosition()}.
    */
   @Test
   public void setCurrentPosition_onStreamMedium_doesNotChangeCurrentPosition() {

      MediumAccessor<?> mediumAccessor = getImplementationToTest();

      mediumAccessor.open();

      int newOffsetOne = 20;
      MediumReference changeReferenceOne = at(mediumAccessor.getMedium(), newOffsetOne);

      mediumAccessor.setCurrentPosition(changeReferenceOne);

      Assert.assertEquals(0, mediumAccessor.getCurrentPosition().getAbsoluteMediumOffset());

      int newOffsetTwo = 10;
      MediumReference changeReferenceTwo = at(mediumAccessor.getMedium(), newOffsetTwo);

      mediumAccessor.setCurrentPosition(changeReferenceTwo);

      Assert.assertEquals(0, mediumAccessor.getCurrentPosition().getAbsoluteMediumOffset());
   }

   /**
    * @see AbstractMediumAccessorTest#getReadTestDataToUse()
    */
   @Override
   protected List<ReadTestData> getReadTestDataToUse() {

      List<ReadTestData> readOffsetsAndSizes = new ArrayList<>();

      readOffsetsAndSizes.add(new ReadTestData(5, 7, 0));
      readOffsetsAndSizes.add(new ReadTestData(1, 157, 7));
      readOffsetsAndSizes.add(new ReadTestData(100, 133, 164));
      readOffsetsAndSizes.add(new ReadTestData(0, 17, 297));
      readOffsetsAndSizes.add(new ReadTestData(88, 45, 314));

      return readOffsetsAndSizes;
   }

   /**
    * @see com.github.jmeta.library.media.impl.mediumAccessor.AbstractMediumAccessorTest#getReadTestDataUntilEndOfMedium()
    */
   @Override
   protected ReadTestData getReadTestDataUntilEndOfMedium() {
      return new ReadTestData(0, getExpectedMediumContent().length);
   }

   /**
    * @see AbstractMediumAccessorTest#getImplementationToTest()
    */
   @Override
   protected MediumAccessor<?> createImplementationToTest() {
      return new InputStreamMediumAccessor(getExpectedMedium());
   }

   /**
    * @see com.github.jmeta.library.media.impl.mediumAccessor.AbstractMediumAccessorTest#getExpectedMedium()
    */
   @Override
   protected InputStreamMedium getExpectedMedium() {
      return new InputStreamMedium(testStream, "My_Stream");
   }

   /**
    * @see AbstractMediumAccessorTest#prepareMediumData(byte[])
    */
   @Override
   protected void prepareMediumData(byte[] testFileContents) {

      try {
         testStream = new FileInputStream(MediaTestFiles.FIRST_TEST_FILE_PATH.toFile());
      } catch (FileNotFoundException e) {
         throw new RuntimeException("Could not find test file. Make sure it exists" + "on the hard drive: "
            + MediaTestFiles.FIRST_TEST_FILE_PATH.toAbsolutePath().toString(), e);
      }
   }
}