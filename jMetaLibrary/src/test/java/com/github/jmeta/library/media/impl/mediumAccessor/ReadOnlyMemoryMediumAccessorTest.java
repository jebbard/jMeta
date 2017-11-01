/**
 *
 * {@link ReadOnlyMemoryMediumAccessorTest}.java
 *
 * @author Jens Ebert
 *
 * @date 08.10.2017
 *
 */
package com.github.jmeta.library.media.impl.mediumAccessor;

import java.util.ArrayList;
import java.util.List;

import com.github.jmeta.library.media.api.types.InMemoryMedium;

/**
 * {@link ReadOnlyMemoryMediumAccessorTest} tests a {@link MemoryMediumAccessor} working on a read-only medium.
 */
public class ReadOnlyMemoryMediumAccessorTest extends AbstractReadOnlyMediumAccessorTest {

   private byte[] memory;

   /**
    * @see com.github.jmeta.library.media.impl.mediumAccessor.AbstractMediumAccessorTest#getExpectedMedium()
    */
   @Override
   protected InMemoryMedium getExpectedMedium() {
      return new InMemoryMedium(memory, null, true);
   }

   /**
    * @see com.github.jmeta.library.media.impl.mediumAccessor.AbstractMediumAccessorTest#getReadTestDataToUse()
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
      return new ReadTestData(550, getExpectedMediumContent().length - 550);
   }

   /**
    * @see com.github.jmeta.library.media.impl.mediumAccessor.AbstractMediumAccessorTest#createImplementationToTest()
    */
   @Override
   protected MediumAccessor<?> createImplementationToTest() {
      return new MemoryMediumAccessor(getExpectedMedium());
   }

   /**
    * @see com.github.jmeta.library.media.impl.mediumAccessor.AbstractMediumAccessorTest#prepareMediumData(byte[])
    */
   @Override
   protected void prepareMediumData(byte[] testFileContents) {
      memory = new byte[testFileContents.length];

      System.arraycopy(testFileContents, 0, memory, 0, testFileContents.length);
   }

}
