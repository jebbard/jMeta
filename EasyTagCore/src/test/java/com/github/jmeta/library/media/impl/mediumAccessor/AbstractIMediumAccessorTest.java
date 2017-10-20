/**
 * {@link AbstractIMediumAccessorTest}.java
 *
 * @author Jens Ebert
 * @date 09.12.10 21:22:54 (December 9, 2010)
 */

package com.github.jmeta.library.media.impl.mediumAccessor;

import static com.github.jmeta.library.media.api.helper.TestMediumUtility.createReference;

import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.github.jmeta.library.media.api.exception.EndOfMediumException;
import com.github.jmeta.library.media.api.helper.MediaTestCaseConstants;
import com.github.jmeta.library.media.api.type.IMedium;
import com.github.jmeta.library.media.api.type.IMediumReference;
import com.github.jmeta.utility.dbc.api.exception.PreconditionUnfullfilledException;

import de.je.util.javautil.testUtil.setup.TestDataException;

/**
 * Tests the interface {@IMediumAccessor}. Basic idea is to work on the
 * {@link MediaTestCaseConstants#STANDARD_TEST_FILE}. Its contents is just ASCII bytes that are read once at the
 * beginning of test execution and determined as expected content. Then reading and writing is tested based on this.
 */
public abstract class AbstractIMediumAccessorTest {

   /**
    * {@link ReadTestData} summarizes offset and size of test data for tests of reading.
    */
   protected static class ReadTestData {

      /**
       * This constructor is used for testing random-access implementations, where the offset to read is actually
       * relevant and the bytes read are expected at the same offset.
       * 
       * @param offsetToRead
       *           The offset to read bytes from, null to indicate {@link IMediumAccessor#NEXT_BYTES} for stream media.
       * @param sizeToRead
       *           The number of bytes to read.
       */
      public ReadTestData(int offsetToRead, int sizeToRead) {
         this(offsetToRead, sizeToRead, offsetToRead);
      }

      /**
       * This constructor is used for testing stream implementations, where the offset to read is ignored and no matter
       * what its value is, the bytes are read just sequentially. Thus, the offsetToRead might be arbitrary, while the
       * expected byte offsets are different.
       */
      public ReadTestData(int offsetToRead, int sizeToRead, Integer expectedBytesOffset) {
         this.offsetToRead = offsetToRead;
         this.sizeToRead = sizeToRead;
         this.expectedBytesOffset = expectedBytesOffset;
      }

      private int offsetToRead;
      private int sizeToRead;
      private int expectedBytesOffset;
   }

   private IMediumAccessor<?> mediumAccessor;

   protected static byte[] EXPECTED_FILE_CONTENTS;

   /**
    * Reads the contents of the {@link MediaTestCaseConstants#STANDARD_TEST_FILE} into memory to make it available for
    * expectation testing.
    */
   @BeforeClass
   public static void determineExpectedFileContents() {
      EXPECTED_FILE_CONTENTS = readTestFileContents();
   }

   /**
    * Sets up the test case.
    */
   @Before
   public void setUp() {

      prepareMediumData(EXPECTED_FILE_CONTENTS);

      mediumAccessor = createImplementationToTest();

      if (mediumAccessor == null) {
         throw new TestDataException("The tested object must not be null.", null);
      }

      validateTestMedium(mediumAccessor.getMedium());
   }

   /**
    * Tears the test case down.
    */
   @After
   public void tearDown() {

      if (mediumAccessor.isOpened()) {
         mediumAccessor.close();
      }
   }

   /**
    * Tests {@link IMediumAccessor#isOpened()} and {@link IMediumAccessor#close()}.
    */
   @Test
   public void close_onOpenedMediumAccessor_isOpenedReturnsFalse() {

      mediumAccessor.close();

      Assert.assertFalse(mediumAccessor.isOpened());
   }

   /**
    * Tests {@link IMediumAccessor#close()}.
    */
   @Test(expected = PreconditionUnfullfilledException.class)
   public void close_onClosedMediumAccessor_throwsException() {

      mediumAccessor.close();
      // Close twice
      mediumAccessor.close();
   }

   /**
    * Tests {@link IMediumAccessor#isOpened()}.
    */
   @Test
   public void isOpened_forNewMediumAccessor_returnsTrue() {

      Assert.assertTrue("IMediumAccessor must be opened", mediumAccessor.isOpened());
   }

   /**
    * Tests {@link IMediumAccessor#getMedium()}.
    */
   @Test
   public void getMedium_onOpenedMediumAccessor_returnsExpectedMedium() {

      Assert.assertEquals(getExpectedMedium(), getImplementationToTest().getMedium());
   }

   /**
    * Tests {@link IMediumAccessor#getMedium()}.
    */
   @Test
   public void getMedium_onClosedMediumAccessor_returnsExpectedMedium() {

      IMediumAccessor<?> mediumAccessor = getImplementationToTest();

      mediumAccessor.close();

      Assert.assertEquals(getExpectedMedium(), mediumAccessor.getMedium());
   }

   /**
    * Tests {@link IMediumAccessor#getCurrentPosition()}.
    */
   @Test
   public void getCurrentPosition_forNewlyOpenedMediumAccessor_returnsZero() {

      IMediumAccessor<?> mediumAccessor = getImplementationToTest();

      Assert.assertEquals(mediumAccessor.getMedium(), mediumAccessor.getCurrentPosition().getMedium());
      Assert.assertEquals(0, mediumAccessor.getCurrentPosition().getAbsoluteMediumOffset());
   }

   /**
    * Tests {@link IMediumAccessor#getCurrentPosition()}.
    */
   @Test
   public void getCurrentPosition_afterReadWithoutEOM_changedByNumberOfReadBytes() {

      IMediumAccessor<?> mediumAccessor = getImplementationToTest();

      int sizeToRead = 10;
      performReadNoEOMExpected(mediumAccessor, new ReadTestData(0, sizeToRead));

      Assert.assertEquals(mediumAccessor.getMedium(), mediumAccessor.getCurrentPosition().getMedium());
      Assert.assertEquals(sizeToRead, mediumAccessor.getCurrentPosition().getAbsoluteMediumOffset());
   }

   /**
    * Tests {@link IMediumAccessor#getCurrentPosition()}.
    */
   @Test
   public void getCurrentPosition_afterReadUntilEOM_changedByNumberOfReadBytesUntilEOM() {

      IMediumAccessor<?> mediumAccessor = getImplementationToTest();

      ReadTestData readTestData = getReadTestDataUntilEndOfMedium();

      int sizeToRead = readTestData.sizeToRead + 10;

      IMediumReference initialPosition = createReference(mediumAccessor.getMedium(), readTestData.offsetToRead);

      mediumAccessor.setCurrentPosition(initialPosition);

      ByteBuffer readContent = ByteBuffer.allocate(sizeToRead);
      try {
         mediumAccessor.read(readContent);

         Assert.fail("Expected end of medium exception, but it did not occur!");
      }

      catch (EndOfMediumException e) {
         Assert.assertEquals(mediumAccessor.getMedium(), mediumAccessor.getCurrentPosition().getMedium());
         Assert.assertEquals(initialPosition.advance(e.getBytesReallyRead()), mediumAccessor.getCurrentPosition());
      }
   }

   /**
    * Tests {@link IMediumAccessor#read(ByteBuffer)}.
    */
   @Test
   public void read_forAnyOffsetAndSize_returnsExpectedBytes() {

      final List<ReadTestData> readTestData = getReadTestDataToUse();

      IMediumAccessor<?> mediumAccessor = getImplementationToTest();
      IMedium<?> medium = mediumAccessor.getMedium();

      long mediumSizeBeforeRead = medium.getCurrentLength();

      for (ReadTestData readTestDataRecord : readTestData) {
         ByteBuffer readContent = performReadNoEOMExpected(mediumAccessor, readTestDataRecord);

         // Reads the correct contents
         assertEqualsFileContent(readContent, readTestDataRecord.expectedBytesOffset);
      }

      // Size did not change after read operations
      Assert.assertEquals(mediumSizeBeforeRead, medium.getCurrentLength());
   }

   /**
    * Tests {@link IMediumAccessor#read(ByteBuffer)}.
    */
   @Test
   public void read_untilEndOfMedium_throwsEndOfMediumException() {

      ReadTestData readOverEndOfMedium = getReadTestDataUntilEndOfMedium();

      IMediumAccessor<?> mediumAccessor = getImplementationToTest();
      IMedium<?> medium = mediumAccessor.getMedium();

      Integer readOffset = readOverEndOfMedium.offsetToRead;
      int readSize = readOverEndOfMedium.sizeToRead + 20;

      ByteBuffer readContent = ByteBuffer.allocate(readSize);

      IMediumReference readReference = createReference(medium, readOffset);

      mediumAccessor.setCurrentPosition(readReference);

      try {
         mediumAccessor.read(readContent);

         Assert.fail("Expected end of medium exception, but it did not occur!");
      }

      catch (EndOfMediumException e) {
         Assert.assertEquals(readReference, e.getMediumReference());
         Assert.assertEquals(readSize, e.getByteCountTriedToRead());
         Assert.assertEquals(EXPECTED_FILE_CONTENTS.length - readOffset, e.getBytesReallyRead());
      }
   }

   /**
    * Tests {@link IMediumAccessor#read(ByteBuffer)}.
    */
   @Test(expected = PreconditionUnfullfilledException.class)
   public void read_onClosedMediumAccessor_throwsException() {
      IMediumAccessor<?> mediumAccessor = getImplementationToTest();

      mediumAccessor.close();

      performReadNoEOMExpected(mediumAccessor, new ReadTestData(0, 5));
   }

   /**
    * Tests {@link IMediumAccessor#isAtEndOfMedium()}.
    */
   @Test
   public void isAtEndOfMedium_ifNotAtEndOfMedium_returnsFalse() {

      ReadTestData readOverEndOfMedium = getReadTestDataUntilEndOfMedium();

      IMediumAccessor<?> mediumAccessor = getImplementationToTest();
      IMedium<?> medium = mediumAccessor.getMedium();

      int readOffset = readOverEndOfMedium.offsetToRead;

      IMediumReference readReferenceOne = createReference(medium, 0);
      IMediumReference readReferenceTwo = createReference(medium, readOffset);

      // Each call is checked twice to ensure it is repeatable (especially for streams!)
      mediumAccessor.setCurrentPosition(readReferenceOne);
      Assert.assertEquals(false, mediumAccessor.isAtEndOfMedium());
      mediumAccessor.setCurrentPosition(readReferenceTwo);
      Assert.assertEquals(false, mediumAccessor.isAtEndOfMedium());
      mediumAccessor.setCurrentPosition(readReferenceOne);
      Assert.assertEquals(false, mediumAccessor.isAtEndOfMedium());
      mediumAccessor.setCurrentPosition(readReferenceTwo);
      Assert.assertEquals(false, mediumAccessor.isAtEndOfMedium());
   }

   /**
    * Tests {@link IMediumAccessor#isAtEndOfMedium()}.
    */
   @Test
   public void isAtEndOfMedium_ifAtEndOfMedium_returnsTrue() {

      IMediumAccessor<?> mediumAccessor = getImplementationToTest();

      ReadTestData readOverEndOfMedium = getReadTestDataUntilEndOfMedium();

      // The explicit read is only really necessary for stream media, see a similar test case without read for
      // random-access media
      performReadNoEOMExpected(mediumAccessor, readOverEndOfMedium);

      // Each call is checked twice to ensure it is repeatable (especially for streams!)
      Assert.assertEquals(true, mediumAccessor.isAtEndOfMedium());
      Assert.assertEquals(true, mediumAccessor.isAtEndOfMedium());
   }

   /**
    * Tests {@link IMediumAccessor#isAtEndOfMedium()}.
    */
   @Test(expected = PreconditionUnfullfilledException.class)
   public void isAtEndOfMedium_onClosedMediumAccessor_throwsException() {
      IMediumAccessor<?> mediumAccessor = getImplementationToTest();

      mediumAccessor.close();

      mediumAccessor.isAtEndOfMedium();
   }

   /**
    * Encapsulates test calls to {@link IMediumAccessor#read(ByteBuffer)}, without expecting an end of medium during
    * read, i.e. if it occurs, a test failure is generated.
    * 
    * @param mediumAccessor
    *           The {@link IMediumAccessor} to use.
    * @param readTestData
    *           The {@link ReadTestData} to use.
    * @return The {@link ByteBuffer} of data read, returned by {@link IMediumAccessor#read(ByteBuffer)}
    */
   protected static ByteBuffer performReadNoEOMExpected(IMediumAccessor<?> mediumAccessor, ReadTestData readTestData) {
      ByteBuffer readContent = ByteBuffer.allocate(readTestData.sizeToRead);

      mediumAccessor.setCurrentPosition(createReference(mediumAccessor.getMedium(), readTestData.offsetToRead));

      try {
         mediumAccessor.read(readContent);
      }

      catch (EndOfMediumException e) {
         Assert.fail("Unexpected end of medium detected! Exception: " + e);
      }

      return readContent;
   }

   /**
    * @return The concrete {@link IMediumAccessor} currently tested.
    */
   protected IMediumAccessor<?> getImplementationToTest() {
      return mediumAccessor;
   }

   /**
    * Returns a Map of offsets in the {@link MediaTestCaseConstants#STANDARD_TEST_FILE} that are checked using
    * {@link IMediumAccessor#read}. It is checked that the bytes read from that offset match the expected bytes from the
    * {@link MediaTestCaseConstants#STANDARD_TEST_FILE}. The given size to read is mapped to the offset.
    * 
    * @return a Map of offsets in the {@link MediaTestCaseConstants#STANDARD_TEST_FILE} that are checked using
    *         {@link IMediumAccessor#read}.
    */
   protected abstract List<ReadTestData> getReadTestDataToUse();

   /**
    * Returns a {@link ReadTestData} instance ranging from a specific offset until exactly the last byte of the medium.
    * 
    * @return a {@link ReadTestData} instance ranging from a specific offset until exactly the last byte of the medium.
    */
   protected abstract ReadTestData getReadTestDataUntilEndOfMedium();

   /**
    * Returns the concrete implementation of {@IMediumAccessor } to test. It is used for reading and writing from a
    * {@IMediumAccessor}.
    * 
    * @return the concrete implementation of {@IMediumAccessor } to test.
    */
   protected abstract IMediumAccessor<?> createImplementationToTest();

   /**
    * This method is called during {@link #setUp()} to prepare the medium data to be tested in a sufficient way. E.g. in
    * case of a file a prototypical test file might first be copied before doing the tests.
    * 
    * @param testFileContents
    *           The contents of the test file
    */
   protected abstract void prepareMediumData(byte[] testFileContents);

   /**
    * @return the {@link IMedium} of the current {@link IMediumAccessor} tested
    */
   protected abstract IMedium<?> getExpectedMedium();

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

      Assert.assertTrue(bytesRead.remaining() + fileOffset <= EXPECTED_FILE_CONTENTS.length);

      while (bytesRead.hasRemaining()) {
         Assert.assertEquals(EXPECTED_FILE_CONTENTS[fileOffset + index], bytesRead.get());

         index++;
      }

      bytesRead.reset();
   }

   /**
    * Checks the test {@link IMedium} to fulfill any preconditions for the tests.
    * 
    * @param theMedium
    *           The {@link IMedium} to test.
    */
   protected abstract void validateTestMedium(IMedium<?> theMedium);

   /**
    * Reads the {@link MediaTestCaseConstants#STANDARD_TEST_FILE} and returns its contents.
    * 
    * @return contents of the {@link MediaTestCaseConstants#STANDARD_TEST_FILE} as byte array.
    */
   private static byte[] readTestFileContents() {

      Path testFile = MediaTestCaseConstants.STANDARD_TEST_FILE;

      try (RandomAccessFile raf = new RandomAccessFile(testFile.toFile(), "r")) {
         int size = (int) Files.size(testFile);
         byte[] bytesReadBuffer = new byte[size];

         int bytesRead = 0;
         while (bytesRead < size) {
            int readReturn = raf.read(bytesReadBuffer, bytesRead, size - bytesRead);

            if (readReturn == -1)
               throw new RuntimeException("Unexpected EOF");

            bytesRead += readReturn;
         }

         return bytesReadBuffer;
      } catch (Exception e) {
         throw new RuntimeException("Unexpected exception during reading of test file", e);
      }
   }
}