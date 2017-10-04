/**
 * {@link AbstractIMediumAccessorTest}.java
 *
 * @author Jens Ebert
 * @date 09.12.10 21:22:54 (December 9, 2010)
 */

package de.je.jmeta.media.impl;

import java.io.File;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.util.Map;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import de.je.jmeta.media.api.IMediumReference;
import de.je.jmeta.media.api.MediaTestCaseConstants;
import de.je.jmeta.media.api.exception.ReadOnlyMediumException;

/**
 * Tests the interface {@IMediumAccessor}.
 */
public abstract class AbstractIMediumAccessorTest {

   protected static final String EXPECTED_END_OF_MEDIUM_EXCEPTION = "Expected EndOfMediumException!";

   protected static final String UNEXPECTED_EOM = "Unexpected end of medium detected! Exception: ";

   protected final static ByteBuffer[] TEST_BUFFERS_FOR_WRITING = new ByteBuffer[] {
      ByteBuffer
         .wrap(new byte[] { 'T', 'E', 'S', 'T', ' ', 'B', 'U', 'F', ' ', '0', '0', '0', '0', '0', '0', '0', '0', '1' }),
      ByteBuffer
         .wrap(new byte[] { 'T', 'E', 'S', 'T', ' ', 'B', 'U', 'F', ' ', '0', '0', '0', '0', '0', '0', '0', '2' }),
      ByteBuffer.wrap(new byte[] { 'T', 'E', 'S', 'T', ' ', 'B', 'U', 'F', ' ', '0', '0', '0', '0', '0', '0', '3' }),
      ByteBuffer.wrap(new byte[] { 'T', 'E', 'S', 'T', ' ', 'B', 'U', 'F', ' ', '0', '0', '0', '0', '0', '4' }),
      ByteBuffer.wrap(new byte[] { 'T', 'E', 'S', 'T', ' ', 'B', 'U', 'F', ' ', '0', '0', '0', '0', '5' }),
      ByteBuffer.wrap(new byte[] { 'T', 'E', 'S', 'T', ' ', 'B', 'U', 'F', ' ', '0', '0', '0', '6' }),
      ByteBuffer.wrap(new byte[] { 'T', 'E', 'S', 'T', ' ', 'B', 'U', 'F', ' ', '0', '0', '7' }),
      ByteBuffer.wrap(new byte[] { 'T', 'E', 'S', 'T', ' ', 'B', 'U', 'F', ' ', '0', '8' }), };

   private IMediumAccessor<?> mediumAccessor;

   private IMediumAccessor<?> readOnlyMediumAccessor;

   private byte[] expectedFileContents;

   /**
    * Sets up the test case.
    */
   @Before
   public void setUp() {

      expectedFileContents = readTestFileContents();

      prepareMediumData(expectedFileContents);

      mediumAccessor = createMediumAccessorImplementationToTest();
      readOnlyMediumAccessor = createReadOnlyMediumAccessorImplementationToTest();

      checkTestData();
   }

   /**
    * Tears the test case down.
    */
   @After
   public void tearDown() {

      if (mediumAccessor.isOpened())
         mediumAccessor.close();

      if (readOnlyMediumAccessor.isOpened())
         readOnlyMediumAccessor.close();

      cleanUpMediumData();
   }

   /**
    * Tests {@link IMediumAccessor#isOpened()}.
    */
   @Test
   public void isOpened_forNewMediumAccessor_returnsTrue() {

      Assert.assertTrue("IMediumAccessor must be opened", mediumAccessor.isOpened());
   }

   /**
    * Tests {@link IMediumAccessor#isOpened()} and {@link IMediumAccessor#close()}.
    */
   @Test
   public void close_onOpenedMediumAccessor_isOpenedReturnsFalse() {

      mediumAccessor.close();

      Assert.assertFalse("IMediumAccessor must be closed after calling close", mediumAccessor.isOpened());
   }

   /**
    * Checks whether a read-only medium cannot be accessed writing.
    */
   @Test(expected = ReadOnlyMediumException.class)
   public void write_onReadOnlyMedium_throwsException() {

      final IMediumReference startReference = new StandardMediumReference(readOnlyMediumAccessor.getMedium(), 0);
      readOnlyMediumAccessor.write(startReference, TEST_BUFFERS_FOR_WRITING[0]);
   }

   protected IMediumAccessor<?> getMediumAccessorImplementationToTest() {
      return mediumAccessor;
   }

   protected IMediumAccessor<?> getReadOnlyMediumAccessorImplementationToTest() {
      return readOnlyMediumAccessor;
   }

   protected byte[] getExpectedFileContents() {
      return expectedFileContents;
   }

   /**
    * Returns a Map of offsets in the {@link MediaTestCaseConstants#STANDARD_TEST_FILE} that are checked using
    * {@link IMediumAccessor#read}. It is checked that the bytes read from that offset match the expected bytes from the
    * {@link MediaTestCaseConstants#STANDARD_TEST_FILE}. The given size to read is mapped to the offset.
    * 
    * @return a Map of offsets in the {@link MediaTestCaseConstants#STANDARD_TEST_FILE} that are checked using
    *         {@link IMediumAccessor#read}.
    */
   protected abstract Map<Integer, Integer> getFileOffsetsToCheckReading();

   /**
    * Returns a concrete implementation of {@IMediumAccessor} to test, but a read-only version that works on a copy of
    * the {@IMediumAccessor } the testling returned by {@link #createMediumAccessorImplementationToTest()} processes.
    * 
    * @return a concrete implementation of {@IMediumAccessor} to test, but a read-only version that works on a copy of
    *         the {@IMediumAccessor } the testling returned by {@link #createMediumAccessorImplementationToTest()}
    *         processes.
    */
   protected abstract IMediumAccessor<?> createReadOnlyMediumAccessorImplementationToTest();

   /**
    * Returns the concrete implementation of {@IMediumAccessor } to test. It is used for reading and writing from a
    * {@IMediumAccessor}.
    * 
    * @return the concrete implementation of {@IMediumAccessor } to test.
    */
   protected abstract IMediumAccessor<?> createMediumAccessorImplementationToTest();

   /**
    * This method is called during {@link #setUp()} to prepare the medium data to be tested in a sufficient way. E.g. in
    * case of a file a prototypical test file might first be copied before doing the tests.
    * 
    * @param testFileContents
    *           The contents of the test file
    */
   protected abstract void prepareMediumData(byte[] testFileContents);

   /**
    * This method is called during {@link #tearDown()} to dispose the medium data to be tested in a sufficient way. E.g.
    * in case of a file a prototypical test file copied on {@link #setUp()} might be deleted again.
    */
   protected void cleanUpMediumData() {
      // Default implementation: empty - to be overridden only if necessary
   }

   /**
    * Asserts whether the given bytes read previously in a test case at the given zero based test file offset equal the
    * test file contents at that offset.
    * 
    * @param bytesRead
    *           The bytes previously read
    * @param fileOffset
    *           The zero-based file offset expected to contain the bytes matching the given bytes read
    */
   protected void assertEqualsFileContent(ByteBuffer bytesRead, int fileOffset) {

      bytesRead.mark();

      int index = 0;

      Assert.assertTrue(bytesRead.remaining() + fileOffset <= expectedFileContents.length);

      while (bytesRead.hasRemaining()) {
         Assert.assertEquals(expectedFileContents[fileOffset + index], bytesRead.get());

         index++;
      }

      bytesRead.reset();
   }

   /**
    * Reads the {@link MediaTestCaseConstants#STANDARD_TEST_FILE} and returns its contents.
    * 
    * @return contents of the {@link MediaTestCaseConstants#STANDARD_TEST_FILE} as byte array.
    */
   private static byte[] readTestFileContents() {

      File testFile = MediaTestCaseConstants.STANDARD_TEST_FILE;

      int size = (int) testFile.length();
      byte[] bytesReadBuffer = new byte[size];

      try (RandomAccessFile raf = new RandomAccessFile(testFile, "r")) {
         int bytesRead = 0;
         while (bytesRead < size) {
            int readReturn = raf.read(bytesReadBuffer, bytesRead, size - bytesRead);

            if (readReturn == -1)
               throw new RuntimeException("Unexpected EOF");

            bytesRead += readReturn;
         }
      } catch (Exception e) {
         throw new RuntimeException("Unexpected exception during reading of test file", e);
      }

      return bytesReadBuffer;
   }

   /**
    * Checks the correctness of test data.
    * 
    * @throws IllegalArgumentException
    *            with corresponding message if test data is incorrect.
    */
   private void checkTestData() {

      if (mediumAccessor == null || readOnlyMediumAccessor == null)
         throw new IllegalArgumentException("The tested object must not be null.");

      if (!readOnlyMediumAccessor.getMedium().isReadOnly())
         throw new IllegalArgumentException("The read-only testling must be in read-only state.");

      // Check if the two mediums really have the same properties except
      // read-only-ness
      if (readOnlyMediumAccessor.getMedium().isRandomAccess() && !mediumAccessor.getMedium().isRandomAccess()
         || (readOnlyMediumAccessor.getMedium().isRandomAccess() && (readOnlyMediumAccessor.getMedium()
            .getCurrentLength() != mediumAccessor.getMedium().getCurrentLength())))
         throw new IllegalArgumentException("The mediums of the testlings have different properties");
   }
}