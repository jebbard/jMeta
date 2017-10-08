/**
 *
 * {@link ReadOnlyMemoryMediumAccessorTest}.java
 *
 * @author Jens Ebert
 *
 * @date 08.10.2017
 *
 */
package de.je.jmeta.media.impl.mediumAccessor;

import java.util.ArrayList;
import java.util.List;

import de.je.jmeta.media.api.datatype.InMemoryMedium;
import de.je.jmeta.media.impl.IMediumAccessor;
import de.je.jmeta.media.impl.MemoryMediumAccessor;

/**
 * {@link ReadOnlyMemoryMediumAccessorTest} tests a {@link MemoryMediumAccessor} working on a read-only medium.
 */
public class ReadOnlyMemoryMediumAccessorTest extends AbstractReadOnlyMediumAccessorTest {

   private byte[] memory;

   /**
    * @see de.je.jmeta.media.impl.mediumAccessor.AbstractIMediumAccessorTest#getExpectedMedium()
    */
   @Override
   protected InMemoryMedium getExpectedMedium() {
      return new InMemoryMedium(memory, null, true);
   }

   /**
    * @see de.je.jmeta.media.impl.mediumAccessor.AbstractIMediumAccessorTest#getReadTestDataToUse()
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
      return new ReadTestData(550, EXPECTED_FILE_CONTENTS.length - 550);
   }

   /**
    * @see de.je.jmeta.media.impl.mediumAccessor.AbstractIMediumAccessorTest#createImplementationToTest()
    */
   @Override
   protected IMediumAccessor<?> createImplementationToTest() {
      return new MemoryMediumAccessor(getExpectedMedium());
   }

   /**
    * @see de.je.jmeta.media.impl.mediumAccessor.AbstractIMediumAccessorTest#prepareMediumData(byte[])
    */
   @Override
   protected void prepareMediumData(byte[] testFileContents) {
      memory = new byte[testFileContents.length];

      System.arraycopy(testFileContents, 0, memory, 0, testFileContents.length);
   }

}
