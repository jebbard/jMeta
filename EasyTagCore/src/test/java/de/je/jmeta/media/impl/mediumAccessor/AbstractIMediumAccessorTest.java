/**
 * {@link AbstractIMediumAccessorTest}.java
 *
 * @author Jens Ebert
 * @date 09.12.10 21:22:54 (December 9, 2010)
 */

package de.je.jmeta.media.impl.mediumAccessor;

import static de.je.jmeta.media.impl.TestMediumUtility.createReference;

import java.io.File;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.util.List;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import de.je.jmeta.media.api.IMedium;
import de.je.jmeta.media.api.IMediumReference;
import de.je.jmeta.media.api.MediaTestCaseConstants;
import de.je.jmeta.media.api.exception.EndOfMediumException;
import de.je.jmeta.media.impl.IMediumAccessor;
import de.je.util.javautil.common.err.PreconditionUnfullfilledException;
import de.je.util.javautil.testUtil.setup.TestDataException;

/*
 * TODO 1.) Testdaten / Datei-Lesen Lebenszyklus optimieren (nur einmal vor Klasse) 4.) Timeout tests in
 * StreamMediumAccessorTest verschieben 5.) Javadocs der Testfälle aktualisieren 7.) ReadOnly Testklassen für File und
 * InMemoryMedium
 */

/**
 * Tests the interface {@IMediumAccessor}.
 */
public abstract class AbstractIMediumAccessorTest {

   protected static class ReadTestData {

      /**
       * This constructor is used for testing random-access implementations, where the offset to read is actually
       * relevant and the bytes read are expected at the same offset.
       */
      public ReadTestData(int offsetToRead, int sizeToRead) {
         this(offsetToRead, sizeToRead, offsetToRead);
      }

      /**
       * This constructor is used for testing stream implementations, where the offset to read is ignored and no matter
       * what its value is, the bytes are read just sequentially. Thus, the offsetToRead might be arbitrary, while the
       * expected byte offsets are different.
       */
      public ReadTestData(int offsetToRead, int sizeToRead, int expectedBytesOffset) {
         this.offsetToRead = offsetToRead;
         this.sizeToRead = sizeToRead;
         this.expectedBytesOffset = expectedBytesOffset;
      }

      private int offsetToRead;
      private int sizeToRead;
      private int expectedBytesOffset;
   }

   private IMediumAccessor<?> mediumAccessor;

   private byte[] expectedFileContents;

   /**
    * Sets up the test case.
    */
   @Before
   public void setUp() {

      expectedFileContents = readTestFileContents();

      prepareMediumData(expectedFileContents);

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

      cleanUpMediumData();
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
    * Tests the {@link IMediumAccessor#read(IMediumReference, ByteBuffer)}.
    */
   @Test
   public void read_forGivenOffsetAndSize_returnsExpectedBytes() {

      final List<ReadTestData> readTestData = getReadTestDataToUse();

      IMediumAccessor<?> mediumAccessor = getImplementationToTest();
      IMedium<?> medium = mediumAccessor.getMedium();

      long mediumSizeBeforeRead = medium.getCurrentLength();

      for (ReadTestData readTestDataRecord : readTestData) {
         ByteBuffer readContent = performReadNoEOFExpected(mediumAccessor, readTestDataRecord);

         // Reads the correct contents
         assertEqualsFileContent(readContent, readTestDataRecord.expectedBytesOffset);
      }

      // Size did not change after read operations
      Assert.assertEquals(mediumSizeBeforeRead, medium.getCurrentLength());
   }

   /**
    * Tests the {@link IMediumAccessor#read(IMediumReference, ByteBuffer)}.
    */
   @Test
   public void read_untilEndOfMedium_throwsEndOfMediumException() {

      ReadTestData readOverEndOfMedium = getReadTestDataUntilEndOfMedium();

      IMediumAccessor<?> mediumAccessor = getImplementationToTest();
      IMedium<?> medium = mediumAccessor.getMedium();

      int readOffset = readOverEndOfMedium.offsetToRead;
      int readSize = readOverEndOfMedium.sizeToRead + 20;

      ByteBuffer readContent = ByteBuffer.allocate(readSize);

      IMediumReference readReference = createReference(medium, readOffset);

      try {
         mediumAccessor.read(readReference, readContent);

         Assert.fail("Expected end of medium exception, but it did not occur!");
      }

      catch (EndOfMediumException e) {
         Assert.assertEquals(readReference, e.getMediumReference());
         Assert.assertEquals(readSize, e.getByteCountTriedToRead());
         Assert.assertEquals(getExpectedFileContents().length - readOffset, e.getBytesReallyRead());
      }
   }

   /**
    * Tests the {@link IMediumAccessor#read(IMediumReference, ByteBuffer)}.
    */
   @Test(expected = PreconditionUnfullfilledException.class)
   public void read_onClosedMediumAccessor_throwsException() {
      IMediumAccessor<?> mediumAccessor = getImplementationToTest();

      mediumAccessor.close();

      performReadNoEOFExpected(mediumAccessor, new ReadTestData(0, 5));
   }

   /**
    * Tests the {@link IMediumAccessor#isAtEndOfMedium(IMediumReference)}.
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
      Assert.assertEquals(false, mediumAccessor.isAtEndOfMedium(readReferenceOne));
      Assert.assertEquals(false, mediumAccessor.isAtEndOfMedium(readReferenceTwo));
      Assert.assertEquals(false, mediumAccessor.isAtEndOfMedium(readReferenceOne));
      Assert.assertEquals(false, mediumAccessor.isAtEndOfMedium(readReferenceTwo));
   }

   /**
    * Tests the {@link IMediumAccessor#isAtEndOfMedium(IMediumReference)}.
    */
   @Test
   public void isAtEndOfMedium_ifAtEndOfMedium_returnsTrue() {

      IMediumAccessor<?> mediumAccessor = getImplementationToTest();

      ReadTestData readOverEndOfMedium = getReadTestDataUntilEndOfMedium();

      // The explicit read is only really necessary for stream media, see a similar test case without read for
      // random-access media
      performReadNoEOFExpected(mediumAccessor, readOverEndOfMedium);

      IMediumReference endOfMediumReference = createReference(mediumAccessor.getMedium(),
         readOverEndOfMedium.offsetToRead + readOverEndOfMedium.sizeToRead);

      // Each call is checked twice to ensure it is repeatable (especially for streams!)
      Assert.assertEquals(true, mediumAccessor.isAtEndOfMedium(endOfMediumReference));
      Assert.assertEquals(true, mediumAccessor.isAtEndOfMedium(endOfMediumReference));
   }

   /**
    * Tests the {@link IMediumAccessor#isAtEndOfMedium(IMediumReference)}.
    */
   @Test(expected = PreconditionUnfullfilledException.class)
   public void isAtEndOfMedium_onClosedMediumAccessor_throwsException() {
      IMediumAccessor<?> mediumAccessor = getImplementationToTest();

      mediumAccessor.close();

      mediumAccessor.isAtEndOfMedium(createReference(mediumAccessor.getMedium(), 0));
   }

   protected static ByteBuffer performReadNoEOFExpected(IMediumAccessor<?> mediumAccessor,
      ReadTestData readOverEndOfMedium) {
      ByteBuffer readContent = ByteBuffer.allocate(readOverEndOfMedium.sizeToRead);

      try {
         mediumAccessor.read(createReference(mediumAccessor.getMedium(), (long) readOverEndOfMedium.offsetToRead),
            readContent);
      }

      catch (EndOfMediumException e) {
         Assert.fail("Unexpected end of medium detected! Exception: " + e);
      }

      return readContent;
   }

   protected IMediumAccessor<?> getImplementationToTest() {
      return mediumAccessor;
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

   protected abstract IMedium<?> getExpectedMedium();

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
}