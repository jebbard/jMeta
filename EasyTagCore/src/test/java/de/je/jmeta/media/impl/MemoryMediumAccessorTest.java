/**
 * {@link MemoryMediumAccessorTest}.java
 *
 * @author Jens Ebert
 * @date 09.12.10 21:22:53 (December 9, 2010)
 */

package de.je.jmeta.media.impl;

import java.util.HashMap;
import java.util.Map;

import de.je.jmeta.media.api.datatype.InMemoryMedium;

/**
 * Tests the class {@MemoryMediumAccessor}.
 */
public class MemoryMediumAccessorTest extends AbstractRandomAccessMediumAccessorTest {

   private byte[] memory;

   /**
    * @see AbstractIMediumAccessorTest#getFileOffsetsToCheckReading()
    */
   @Override
   protected Map<Integer, Integer> getFileOffsetsToCheckReading() {

      Map<Integer, Integer> readOffsetsAndSizes = new HashMap<>();

      readOffsetsAndSizes.put(16, 7);
      readOffsetsAndSizes.put(93, 157);
      readOffsetsAndSizes.put(610, 133);
      readOffsetsAndSizes.put(0, 17);
      readOffsetsAndSizes.put(211, 45);

      return readOffsetsAndSizes;
   }

   /**
    * @see AbstractIMediumAccessorTest#createReadOnlyMediumAccessorImplementationToTest()
    */
   @Override
   protected IMediumAccessor<?> createReadOnlyMediumAccessorImplementationToTest() {
      return new MemoryMediumAccessor(new InMemoryMedium(memory, null, true));
   }

   /**
    * @see AbstractIMediumAccessorTest#getMediumAccessorImplementationToTest()
    */
   @Override
   protected IMediumAccessor<?> createMediumAccessorImplementationToTest() {
      return new MemoryMediumAccessor(new InMemoryMedium(memory, null, true));
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