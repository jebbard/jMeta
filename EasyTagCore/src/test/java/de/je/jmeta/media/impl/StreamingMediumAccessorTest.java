/**
 * {@link StreamingMediumAccessorTest}.java
 *
 * @author Jens Ebert
 * @date 09.12.10 21:22:53 (December 9, 2010)
 */

package de.je.jmeta.media.impl;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import de.je.jmeta.media.api.IMediumReference;
import de.je.jmeta.media.api.MediaTestCaseConstants;
import de.je.jmeta.media.api.datatype.InputStreamMedium;
import de.je.jmeta.media.api.exception.EndOfMediumException;
import junit.framework.Assert;

/**
 * Tests the class {@StreamMediumAccessor}.
 */
public class StreamingMediumAccessorTest extends AbstractIMediumAccessorTest {

   private InputStream testStream;

   /**
    * Tests end of medium conditions for stream-based media. Here, the medium is fully read from start to back until end
    * of medium condition occurs.
    * 
    * Tested method: {@link IMediumAccessor#read(IMediumReference, ByteBuffer)}
    */
   @Test
   public void testEndOfMedium_streamBased() {
   
      if (!getMediumAccessorImplementationToTest().getMedium().isRandomAccess()) {
         // For 100 bytes read, there are 43 really remaining up to end of
         // medium when
         // last trying to read 100
         final int[] tryRead = new int[] { 100, 10 };
         final int[] bytesToEOM = new int[] { getExpectedFileContents().length % tryRead[0],
            getExpectedFileContents().length % tryRead[1] };
   
         int currentOffset = 0;
   
         for (int i = 0; i < tryRead.length; i++) {
            ByteBuffer bbForRead = ByteBuffer.allocate(tryRead[i]);
   
            final IMediumReference nullReference = new StandardMediumReference(
               getMediumAccessorImplementationToTest().getMedium(), 0);
   
            // Read from START of medium up to end of medium is reached
            while (!getMediumAccessorImplementationToTest().isAtEndOfMedium(nullReference)) {
               try {
                  getMediumAccessorImplementationToTest().read(nullReference, bbForRead);
                  assertEqualsFileContent(bbForRead, currentOffset);
   
                  currentOffset += tryRead[i];
               } catch (EndOfMediumException e) {
                  System.out.println("End of medium exception as expected!");
   
                  Assert.assertEquals(bytesToEOM[i], e.getBytesReallyRead());
                  Assert.assertEquals(bytesToEOM[i], bbForRead.remaining());
                  Assert.assertEquals(tryRead[i], e.getByteCountTriedToRead());
                  Assert.assertEquals(nullReference, e.getMediumReference());
                  assertEqualsFileContent(bbForRead, currentOffset);
               }
   
               // Set position = 0 and limit = capacity for the next read
               bbForRead.clear();
            }
   
            Assert.assertTrue(getMediumAccessorImplementationToTest().isAtEndOfMedium(nullReference));
   
            // Read again at end of medium
            try {
               getMediumAccessorImplementationToTest().read(nullReference, bbForRead);
   
               Assert.fail(EXPECTED_END_OF_MEDIUM_EXCEPTION);
            } catch (EndOfMediumException e) {
               Assert.assertEquals(tryRead[i], e.getByteCountTriedToRead());
               // No bytes read at all here
               Assert.assertEquals(0, e.getBytesReallyRead());
               Assert.assertEquals(0, bbForRead.remaining());
               Assert.assertEquals(nullReference, e.getMediumReference());
            }
   
            Assert.assertTrue(getMediumAccessorImplementationToTest().isAtEndOfMedium(nullReference));
         }
      }
   }

   /**
    * Tests the {@link IMediumAccessor#read(IMediumReference, ByteBuffer)} method for stream-based media.
    */
   @Test
   public void testRead_streamBased() {
   
      if (!getMediumAccessorImplementationToTest().getMedium().isRandomAccess()) {
         final int byteCountToRead = 100;
         int currentOffset = 0;
   
         // It is not read up to end of test file (just 600 bytes)
         for (int i = 0; i < 6; i++) {
            ByteBuffer readContent = ByteBuffer.allocate(byteCountToRead);
   
            final IMediumReference nullReference = new StandardMediumReference(
               getMediumAccessorImplementationToTest().getMedium(), 0);
            try {
               getMediumAccessorImplementationToTest().read(nullReference, readContent);
            }
   
            catch (EndOfMediumException e) {
               Assert.fail(UNEXPECTED_EOM + e);
            }
   
            // Reads the correct contents
            assertEqualsFileContent(readContent, currentOffset);
   
            currentOffset += byteCountToRead;
         }
      }
   }

   /**
    * @see AbstractIMediumAccessorTest#getFileOffsetsToCheckReading()
    */
   @Override
   protected Map<Integer, Integer> getFileOffsetsToCheckReading() {
      return new HashMap<>();
   }

   /**
    * @see AbstractIMediumAccessorTest#createReadOnlyMediumAccessorImplementationToTest()
    */
   @Override
   protected IMediumAccessor<?> createReadOnlyMediumAccessorImplementationToTest() {

      return getMediumAccessorImplementationToTest();
   }

   /**
    * @see AbstractIMediumAccessorTest#getMediumAccessorImplementationToTest()
    */
   @Override
   protected IMediumAccessor<?> createMediumAccessorImplementationToTest() {
      return new StreamMediumAccessor(new InputStreamMedium(testStream, "My_Stream"));
   }

   /**
    * @see AbstractIMediumAccessorTest#prepareMediumData(byte[])
    */
   @Override
   protected void prepareMediumData(byte[] testFileContents) {

      try {
         testStream = new FileInputStream(MediaTestCaseConstants.STANDARD_TEST_FILE);
      } catch (FileNotFoundException e) {
         throw new RuntimeException("Could not find test file. Make sure it exists" + "on the hard drive: "
            + MediaTestCaseConstants.STANDARD_TEST_FILE.getAbsolutePath(), e);
      }
   }
}