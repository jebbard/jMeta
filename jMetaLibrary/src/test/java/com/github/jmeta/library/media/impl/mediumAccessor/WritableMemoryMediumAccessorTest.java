/**
 * {@link WritableMemoryMediumAccessorTest}.java
 *
 * @author Jens Ebert
 * @date 09.12.10 21:22:53 (December 9, 2010)
 */

package com.github.jmeta.library.media.impl.mediumAccessor;

import java.util.ArrayList;
import java.util.List;

import com.github.jmeta.library.media.api.types.InMemoryMedium;
import com.github.jmeta.library.media.api.types.MediumAccessType;

/**
 * Tests the class {@link InMemoryMediumAccessor} for a writable medium.
 */
public class WritableMemoryMediumAccessorTest extends AbstractWritableRandomAccessMediumAccessorTest {

   private byte[] memory;

   /**
    * @see AbstractMediumAccessorTest#getImplementationToTest()
    */
   @Override
   protected MediumAccessor<?> createImplementationToTest() {
      return new InMemoryMediumAccessor(getExpectedMedium());
   }

   /**
    * @see com.github.jmeta.library.media.impl.mediumAccessor.AbstractMediumAccessorTest#getExpectedMedium()
    */
   @Override
   protected InMemoryMedium getExpectedMedium() {
      return new InMemoryMedium(memory, null, MediumAccessType.READ_WRITE);
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
    * @see com.github.jmeta.library.media.impl.mediumAccessor.AbstractMediumAccessorTest#getReadTestDataUntilEndOfMedium()
    */
   @Override
   protected ReadTestData getReadTestDataUntilEndOfMedium() {
      return new ReadTestData(550, AbstractMediumAccessorTest.getExpectedMediumContent().length - 550);
   }

   /**
    * @see AbstractMediumAccessorTest#prepareMediumData(byte[])
    */
   @Override
   protected void prepareMediumData(byte[] testFileContents) {

      memory = new byte[testFileContents.length];

      System.arraycopy(testFileContents, 0, memory, 0, testFileContents.length);
   }
}