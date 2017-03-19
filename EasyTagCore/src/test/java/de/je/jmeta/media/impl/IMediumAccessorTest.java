/**
 * {@link IMediumAccessorTest}.java
 *
 * @author Jens Ebert
 * @date 09.12.10 21:22:54 (December 9, 2010)
 */

package de.je.jmeta.media.impl;

import java.io.File;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.util.Iterator;
import java.util.Map;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import de.je.jmeta.media.api.IMediumReference;
import de.je.jmeta.media.api.MediaTestCaseConstants;
import de.je.jmeta.media.api.exception.EndOfMediumException;
import de.je.jmeta.media.api.exception.ReadOnlyMediumException;

// TODO media001: Test media component on Windows XP (slow), Windows 7, Linux, Mac

/**
 * Tests the interface {@IMediumAccessor}.
 */
public abstract class IMediumAccessorTest {

   private static final String EXPECTED_END_OF_MEDIUM_EXCEPTION = "Expected EndOfMediumException!";

   private static final String UNEXPECTED_EOM = "Unexpected end of medium detected! Exception: ";

   private final static ByteBuffer[] TEST_BUFFERS_FOR_WRITING = new ByteBuffer[] {
      ByteBuffer.wrap(new byte[] { 'T', 'E', 'S', 'T', ' ', 'B', 'U', 'F', ' ',
         '0', '0', '0', '0', '0', '0', '0', '0', '1' }),
      ByteBuffer.wrap(new byte[] { 'T', 'E', 'S', 'T', ' ', 'B', 'U', 'F', ' ',
         '0', '0', '0', '0', '0', '0', '0', '2' }),
      ByteBuffer.wrap(new byte[] { 'T', 'E', 'S', 'T', ' ', 'B', 'U', 'F', ' ',
         '0', '0', '0', '0', '0', '0', '3' }),
      ByteBuffer.wrap(new byte[] { 'T', 'E', 'S', 'T', ' ', 'B', 'U', 'F', ' ',
         '0', '0', '0', '0', '0', '4' }),
      ByteBuffer.wrap(new byte[] { 'T', 'E', 'S', 'T', ' ', 'B', 'U', 'F', ' ',
         '0', '0', '0', '0', '5' }),
      ByteBuffer.wrap(new byte[] { 'T', 'E', 'S', 'T', ' ', 'B', 'U', 'F', ' ',
         '0', '0', '0', '6' }),
      ByteBuffer.wrap(new byte[] { 'T', 'E', 'S', 'T', ' ', 'B', 'U', 'F', ' ',
         '0', '0', '7' }),
      ByteBuffer.wrap(new byte[] { 'T', 'E', 'S', 'T', ' ', 'B', 'U', 'F', ' ',
         '0', '8' }), };

   private IMediumAccessor<?> mediumAccessor;

   private IMediumAccessor<?> m_readOnlyTestling;

   private byte[] m_expectedFileContents;

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
            int readReturn = raf.read(bytesReadBuffer, bytesRead,
               size - bytesRead);

            if (readReturn == -1)
               throw new RuntimeException("Unexpected EOF");

            bytesRead += readReturn;
         }
      } catch (Exception e) {
         throw new RuntimeException(
            "Unexpected exception during reading of test file", e);
      }

      return bytesReadBuffer;
   }

   /**
    * Asserts whether the given bytes read previously in a test case at the given zero based test file offset equal the
    * test file contents at that offset.
    * 
    * @param bytesRead
    *           The bytes previously read.
    * @param fileOffset
    *           The zero-based file offset.
    */
   private void checkEqualsFileContent(ByteBuffer bytesRead, int fileOffset) {

      bytesRead.mark();

      int index = 0;

      Assert.assertTrue(
         bytesRead.remaining() + fileOffset <= m_expectedFileContents.length);

      while (bytesRead.hasRemaining()) {
         Assert.assertEquals(m_expectedFileContents[fileOffset + index],
            bytesRead.get());

         index++;
      }

      bytesRead.reset();
   }

   /**
    * Checks whether offset and write have changed as expected after a write operation for a random-access
    * {@link IMediumAccessor}.
    * 
    * @param mediumEndReference
    *           {@link StandardMediumReference} pointing to the offset behind the last medium byte. This must be the
    *           reference determined before the write call.
    * @param writeEndReference
    *           The end offset of the previous write operation.
    */
   private void checkLengthAfterWrite(IMediumReference mediumEndReference,
      IMediumReference writeEndReference) {

      long lengthAfter = mediumAccessor.getMedium().getCurrentLength();

      // Length must not have changed, as write overwrote something
      if (mediumEndReference.behindOrEqual(writeEndReference))
         Assert.assertEquals(mediumEndReference.getAbsoluteMediumOffset(),
            lengthAfter);

      // Length must have changed by the size the buffer extends the medium size
      else
         Assert.assertEquals(writeEndReference.getAbsoluteMediumOffset(),
            lengthAfter);
   }

   /**
    * Checks whether of the given {@link ByteBuffer} as written before can be re-read again with exactly the same
    * content.
    * 
    * @param dataWritten
    *           The {@link ByteBuffer} written before.
    * @param writeStartReference
    *           The {@link StandardMediumReference} where the {@link ByteBuffer} has been written to.
    */
   private void checkReRead(ByteBuffer dataWritten,
      IMediumReference writeStartReference) {

      ByteBuffer reread = ByteBuffer.allocate(dataWritten.capacity());

      try {
         mediumAccessor.read(writeStartReference, reread);
      }

      catch (EndOfMediumException e) {
         Assert.fail(UNEXPECTED_EOM + e);
      }

      // Reset position to zero
      dataWritten.rewind();

      // The content recently written is re-read again as is
      Assert.assertEquals(dataWritten, reread);
   }

   /**
    * Checks the correctness of test data.
    * 
    * @throws IllegalArgumentException
    *            with corresponding message if test data is incorrect.
    */
   private void checkTestData() {

      if (mediumAccessor == null || m_readOnlyTestling == null)
         throw new IllegalArgumentException(
            "The tested object must not be null.");

      if (!m_readOnlyTestling.getMedium().isReadOnly())
         throw new IllegalArgumentException(
            "The read-only testling must be in read-only state.");

      // Check if the two mediums really have the same properties except
      // read-only-ness
      if (m_readOnlyTestling.getMedium().isRandomAccess()
         && !mediumAccessor.getMedium().isRandomAccess()
         || (m_readOnlyTestling.getMedium().isRandomAccess()
            && (m_readOnlyTestling.getMedium()
               .getCurrentLength() != mediumAccessor.getMedium()
                  .getCurrentLength())))
         throw new IllegalArgumentException(
            "The mediums of the testlings have different properties");
   }

   /**
    * Checks whether the bytes between two non-overlapping or non-directly-sibling write operations are unchanged
    * compared to the read-only {@link IMediumAccessor}.
    * 
    * @param previousWriteEndReference
    *           The {@link StandardMediumReference} pointing to the byte behind the previous write operation.
    * @param currentWriteStartReference
    *           The {@link StandardMediumReference} pointing to the start byte of the current write operation.
    */
   private void checkUnchangedGaps(IMediumReference previousWriteEndReference,
      IMediumReference currentWriteStartReference) {

      // Check if content in between two writes has not changed
      if (previousWriteEndReference.before(currentWriteStartReference)) {
         final int size = (int) (currentWriteStartReference
            .distanceTo(previousWriteEndReference));

         ByteBuffer inBetween = ByteBuffer.allocate(size);

         ByteBuffer inBetweenExpected = ByteBuffer.allocate(size);

         try {
            mediumAccessor.read(previousWriteEndReference, inBetween);
            m_readOnlyTestling.read(
               new StandardMediumReference(m_readOnlyTestling.getMedium(),
                  previousWriteEndReference.getAbsoluteMediumOffset()),
               inBetweenExpected);
         }

         catch (EndOfMediumException e) {
            Assert.fail(UNEXPECTED_EOM + e);
         }

         Assert.assertEquals(inBetweenExpected, inBetween);
      }
   }

   /**
    * This method is called during {@link #tearDown()} to dispose the medium data to be tested in a sufficient way. E.g.
    * in case of a file a prototypical test file copied on {@link #setUp()} might be deleted again.
    */
   protected abstract void cleanUpMediumData();

   /**
    * Determines an appropriate offset for a write operation.
    * 
    * The write offsets are chosen such that there are gaps between subsequent writes.
    * 
    * @param testling
    *           The {@link IMediumAccessor} to test.
    * @param previousWriteEndReference
    *           The reference to the end of the previous write call.
    * @return An appropriate write offset.
    */
   private IMediumReference determineWriteReference(IMediumAccessor<?> testling,
      IMediumReference previousWriteEndReference) {

      final long newWriteOffset = previousWriteEndReference
         .getAbsoluteMediumOffset()
         + (testling.getMedium().getCurrentLength()
            - previousWriteEndReference.getAbsoluteMediumOffset()) / 2;

      if (newWriteOffset >= testling.getMedium().getCurrentLength())
         return new StandardMediumReference(testling.getMedium(),
            testling.getMedium().getCurrentLength() - 1);

      return new StandardMediumReference(testling.getMedium(), newWriteOffset);
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
    * the {@IMediumAccessor } the testling returned by {@link #getTestling()} processes.
    * 
    * @return a concrete implementation of {@IMediumAccessor} to test, but a read-only version that works on a copy of
    *         the {@IMediumAccessor } the testling returned by {@link #getTestling()} processes.
    */
   protected abstract IMediumAccessor<?> getReadOnlyTestling();

   /**
    * Returns the concrete implementation of {@IMediumAccessor } to test. It is used for reading and writing from a
    * {@IMediumAccessor}.
    * 
    * @return the concrete implementation of {@IMediumAccessor } to test.
    */
   protected abstract IMediumAccessor<?> getTestling();

   /**
    * This method is called during {@link #setUp()} to prepare the medium data to be tested in a sufficient way. E.g. in
    * case of a file a prototypical test file might first be copied before doing the tests.
    */
   protected abstract void prepareMediumData();

   /**
    * Sets up the test case.
    */
   @Before
   public void setUp() {

      prepareMediumData();

      mediumAccessor = getTestling();
      m_readOnlyTestling = getReadOnlyTestling();

      m_expectedFileContents = readTestFileContents();

      checkTestData();
   }

   /**
    * Tears the test case down.
    */
   @After
   public void tearDown() {

      if (mediumAccessor.isOpened())
         mediumAccessor.close();

      if (m_readOnlyTestling.isOpened())
         m_readOnlyTestling.close();

      cleanUpMediumData();
   }

   /**
    * Tests {@link IMediumAccessor#isOpened()}.
    */
   @Test
   public void isOpened_returnsTrue_forNewMediumAccessor() {

      Assert.assertTrue("IMediumAccessor must be opened",
         mediumAccessor.isOpened());
   }

   /**
    * Tests {@link IMediumAccessor#isOpened()} and {@link IMediumAccessor#close()}.
    */
   @Test
   public void isOpened_returnsFalse_whenClosingMediumAccessor() {

      mediumAccessor.close();

      Assert.assertFalse("IMediumAccessor must be closed after calling close",
         mediumAccessor.isOpened());
   }

   /**
    * Checks whether a read-only medium cannot be accessed writing.
    */
   @Test(expected = ReadOnlyMediumException.class)
   public void write_throwsException_whenInovekdOnReadOnlyMedium() {

      final IMediumReference startReference = new StandardMediumReference(
         m_readOnlyTestling.getMedium(), 0);
      m_readOnlyTestling.write(startReference, TEST_BUFFERS_FOR_WRITING[0]);
   }

   /**
    * Tests end of medium conditions for random access media. The test case reads 10 + X (X >0) bytes starting at the
    * offset 10 bytes before end of medium to cause an end of medium condition.
    * 
    * Tested method: {@link IMediumAccessor#read(IMediumReference, ByteBuffer)}
    */
   @Test
   public void testEndOfMedium_randomAccess() {

      if (mediumAccessor.getMedium().isRandomAccess()) {
         // Try count of 11 tests the boundary case that read goes only 1 byte
         // beyond
         // end of medium
         final int bytesToEOM = 10;
         final int[] tryRead = new int[] { 100, bytesToEOM + 1 };

         for (int i = 0; i < tryRead.length; i++) {
            final IMediumReference mediumEndReference = new StandardMediumReference(
               getTestling().getMedium(),
               getTestling().getMedium().getCurrentLength());
            Assert
               .assertTrue(getTestling().isAtEndOfMedium(mediumEndReference));
            Assert.assertTrue(
               getTestling().isAtEndOfMedium(mediumEndReference.advance(1)));
            Assert.assertTrue(
               getTestling().isAtEndOfMedium(mediumEndReference.advance(2)));

            ByteBuffer bbForRead = ByteBuffer.allocate(tryRead[i]);

            final IMediumReference referenceForReading = new StandardMediumReference(
               getTestling().getMedium(),
               getTestling().getMedium().getCurrentLength() - bytesToEOM);

            try {
               mediumAccessor.read(referenceForReading, bbForRead);

               Assert.fail(EXPECTED_END_OF_MEDIUM_EXCEPTION);
            } catch (EndOfMediumException e) {
               Assert.assertEquals(tryRead[i], e.getByteCountTriedToRead());
               Assert.assertEquals(bytesToEOM, e.getBytesReallyRead());
               Assert.assertEquals(bytesToEOM, bbForRead.remaining());
               Assert.assertEquals(referenceForReading, e.getMediumReference());
            }
         }
      }
   }

   /**
    * Tests end of medium conditions for stream-based media. Here, the medium is fully read from start to back until end
    * of medium condition occurs.
    * 
    * Tested method: {@link IMediumAccessor#read(IMediumReference, ByteBuffer)}
    */
   @Test
   public void testEndOfMedium_streamBased() {

      if (!mediumAccessor.getMedium().isRandomAccess()) {
         // For 100 bytes read, there are 43 really remaining up to end of
         // medium when
         // last trying to read 100
         final int[] tryRead = new int[] { 100, 10 };
         final int[] bytesToEOM = new int[] {
            m_expectedFileContents.length % tryRead[0],
            m_expectedFileContents.length % tryRead[1] };

         int currentOffset = 0;

         for (int i = 0; i < tryRead.length; i++) {
            ByteBuffer bbForRead = ByteBuffer.allocate(tryRead[i]);

            final IMediumReference nullReference = new StandardMediumReference(
               getTestling().getMedium(), 0);

            // Read from START of medium up to end of medium is reached
            while (!getTestling().isAtEndOfMedium(nullReference)) {
               try {
                  getTestling().read(nullReference, bbForRead);
                  checkEqualsFileContent(bbForRead, currentOffset);

                  currentOffset += tryRead[i];
               } catch (EndOfMediumException e) {
                  System.out.println("End of medium exception as expected!");

                  Assert.assertEquals(bytesToEOM[i], e.getBytesReallyRead());
                  Assert.assertEquals(bytesToEOM[i], bbForRead.remaining());
                  Assert.assertEquals(tryRead[i], e.getByteCountTriedToRead());
                  Assert.assertEquals(nullReference, e.getMediumReference());
                  checkEqualsFileContent(bbForRead, currentOffset);
               }

               // Set position = 0 and limit = capacity for the next read
               bbForRead.clear();
            }

            Assert.assertTrue(getTestling().isAtEndOfMedium(nullReference));

            // Read again at end of medium
            try {
               getTestling().read(nullReference, bbForRead);

               Assert.fail(EXPECTED_END_OF_MEDIUM_EXCEPTION);
            } catch (EndOfMediumException e) {
               Assert.assertEquals(tryRead[i], e.getByteCountTriedToRead());
               // No bytes read at all here
               Assert.assertEquals(0, e.getBytesReallyRead());
               Assert.assertEquals(0, bbForRead.remaining());
               Assert.assertEquals(nullReference, e.getMediumReference());
            }

            Assert.assertTrue(getTestling().isAtEndOfMedium(nullReference));
         }
      }
   }

   /**
    * Tests the {@link IMediumAccessor#read(IMediumReference, ByteBuffer)} method for random-access media.
    */
   @Test
   public void testRead_randomAccess() {

      final Map<Integer, Integer> expectedContentsForRead = getFileOffsetsToCheckReading();

      if (mediumAccessor.getMedium().isRandomAccess()) {
         long sizeBeforeRead = mediumAccessor.getMedium().getCurrentLength();

         for (Iterator<Integer> offsetIterator = expectedContentsForRead
            .keySet().iterator(); offsetIterator.hasNext();) {
            Integer offset = offsetIterator.next();
            Integer size = expectedContentsForRead.get(offset);

            ByteBuffer readContent = ByteBuffer.allocate(size);

            try {
               mediumAccessor.read(new StandardMediumReference(
                  getTestling().getMedium(), offset), readContent);
            }

            catch (EndOfMediumException e) {
               Assert.fail(UNEXPECTED_EOM + e);
            }

            // Reads the correct contents
            checkEqualsFileContent(readContent, offset);
         }

         // Size did not change after read operations
         Assert.assertEquals(sizeBeforeRead,
            mediumAccessor.getMedium().getCurrentLength());
      }
   }

   /**
    * Tests the {@link IMediumAccessor#read(IMediumReference, ByteBuffer)} method for stream-based media.
    */
   @Test
   public void testRead_streamBased() {

      if (!mediumAccessor.getMedium().isRandomAccess()) {
         final int byteCountToRead = 100;
         int currentOffset = 0;

         // It is not read up to end of test file (just 600 bytes)
         for (int i = 0; i < 6; i++) {
            ByteBuffer readContent = ByteBuffer.allocate(byteCountToRead);

            final IMediumReference nullReference = new StandardMediumReference(
               getTestling().getMedium(), 0);
            try {
               mediumAccessor.read(nullReference, readContent);
            }

            catch (EndOfMediumException e) {
               Assert.fail(UNEXPECTED_EOM + e);
            }

            // Reads the correct contents
            checkEqualsFileContent(readContent, currentOffset);

            currentOffset += byteCountToRead;
         }
      }
   }

   /**
    * Tests the {@link IMediumAccessor#write(IMediumReference, ByteBuffer)} method for random-access media.
    */
   @Test
   public void testWrite_randomAccess() {

      if (!mediumAccessor.getMedium().isReadOnly()) {
         if (mediumAccessor.getMedium().isRandomAccess()) {
            IMediumReference previousWriteEndReference = new StandardMediumReference(
               getTestling().getMedium(), 0);

            for (int i = 0; i < TEST_BUFFERS_FOR_WRITING.length; i++) {
               ByteBuffer dataToWrite = TEST_BUFFERS_FOR_WRITING[i];

               // Get a randomly generated offset in the medium for writing
               IMediumReference writeStartReference = determineWriteReference(
                  mediumAccessor, previousWriteEndReference);

               // Reference pointing to the offset behind the last medium byte
               IMediumReference mediumEndReference = new StandardMediumReference(
                  getTestling().getMedium(),
                  getTestling().getMedium().getCurrentLength());

               // Reference pointing to the offset behind the last byte written
               IMediumReference writeEndReference = writeStartReference
                  .advance(dataToWrite.capacity());

               // Write the data
               mediumAccessor.write(writeStartReference, dataToWrite);

               Assert.assertEquals(0, dataToWrite.remaining());

               // Length of the medium has changed as expected
               checkLengthAfterWrite(mediumEndReference, writeEndReference);
               // The data written can be reread without changes
               checkReRead(dataToWrite, writeStartReference);

               // The data between the previous write call and the current write
               // call
               // did not change by this write call
               checkUnchangedGaps(previousWriteEndReference,
                  writeStartReference);

               previousWriteEndReference = writeEndReference;
            }
         }
      }
   }

   /**
    * Tests the {@link IMediumAccessor#write(IMediumReference, ByteBuffer)} method for stream-based media.
    */
   @Test
   public void testWrite_streamBased() {

      if (!mediumAccessor.getMedium().isReadOnly()) {
         if (!mediumAccessor.getMedium().isRandomAccess()) {
            for (int i = 0; i < TEST_BUFFERS_FOR_WRITING.length; i++) {
               ByteBuffer dataToWrite = TEST_BUFFERS_FOR_WRITING[i];

               mediumAccessor.write(
                  new StandardMediumReference(getTestling().getMedium(), 0),
                  dataToWrite);

               Assert.assertEquals(dataToWrite.remaining(), 0);

               // INFO: No further tests, as there is no way to prove that the
               // bytes have
               // really been written in non-random-access mediums.
            }
         }
      }
   }
}