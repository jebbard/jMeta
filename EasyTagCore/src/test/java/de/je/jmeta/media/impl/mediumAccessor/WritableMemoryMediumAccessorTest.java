/**
 * {@link WritableMemoryMediumAccessorTest}.java
 *
 * @author Jens Ebert
 * @date 09.12.10 21:22:53 (December 9, 2010)
 */

package de.je.jmeta.media.impl.mediumAccessor;

import java.util.ArrayList;
import java.util.List;

import de.je.jmeta.media.api.datatype.InMemoryMedium;
import de.je.jmeta.media.impl.IMediumAccessor;
import de.je.jmeta.media.impl.MemoryMediumAccessor;

/**
 * Tests the class {@MemoryMediumAccessor}.
 */
public class WritableMemoryMediumAccessorTest extends AbstractWritableRandomAccessMediumAccessorTest {

   @Override
   protected InMemoryMedium getExpectedMedium() {
      return new InMemoryMedium(memory, null, false);
   }

   private byte[] memory;

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
      return new MemoryMediumAccessor(getExpectedMedium());
   }

   /**
    * @see AbstractIMediumAccessorTest#prepareMediumData(byte[])
    */
   @Override
   protected void prepareMediumData(byte[] testFileContents) {

      memory = new byte[testFileContents.length];

      System.arraycopy(testFileContents, 0, memory, 0, testFileContents.length);
   }
}