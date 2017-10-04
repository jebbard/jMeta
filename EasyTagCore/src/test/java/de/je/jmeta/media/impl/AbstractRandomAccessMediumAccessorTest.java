package de.je.jmeta.media.impl;

import java.nio.ByteBuffer;
import java.util.Iterator;
import java.util.Map;

import org.junit.Test;

import de.je.jmeta.media.api.IMediumReference;
import de.je.jmeta.media.api.exception.EndOfMediumException;
import junit.framework.Assert;

public abstract class AbstractRandomAccessMediumAccessorTest extends AbstractIMediumAccessorTest {

   /**
    * Tests end of medium conditions for random access media. The test case reads 10 + X (X >0) bytes starting at the
    * offset 10 bytes before end of medium to cause an end of medium condition.
    * 
    * Tested method: {@link IMediumAccessor#read(IMediumReference, ByteBuffer)}
    */
   @Test
   public void testEndOfMedium_randomAccess() {

      if (getMediumAccessorImplementationToTest().getMedium().isRandomAccess()) {
         // Try count of 11 tests the boundary case that read goes only 1 byte
         // beyond
         // end of medium
         final int bytesToEOM = 10;
         final int[] tryRead = new int[] { 100, bytesToEOM + 1 };

         for (int i = 0; i < tryRead.length; i++) {
            final IMediumReference mediumEndReference = new StandardMediumReference(
               getMediumAccessorImplementationToTest().getMedium(),
               getMediumAccessorImplementationToTest().getMedium().getCurrentLength());
            Assert.assertTrue(getMediumAccessorImplementationToTest().isAtEndOfMedium(mediumEndReference));
            Assert.assertTrue(getMediumAccessorImplementationToTest().isAtEndOfMedium(mediumEndReference.advance(1)));
            Assert.assertTrue(getMediumAccessorImplementationToTest().isAtEndOfMedium(mediumEndReference.advance(2)));

            ByteBuffer bbForRead = ByteBuffer.allocate(tryRead[i]);

            final IMediumReference referenceForReading = new StandardMediumReference(
               getMediumAccessorImplementationToTest().getMedium(),
               getMediumAccessorImplementationToTest().getMedium().getCurrentLength() - bytesToEOM);

            try {
               getMediumAccessorImplementationToTest().read(referenceForReading, bbForRead);

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
    * Tests the {@link IMediumAccessor#read(IMediumReference, ByteBuffer)} method for random-access media.
    */
   @Test
   public void testRead_randomAccess() {

      final Map<Integer, Integer> expectedContentsForRead = getFileOffsetsToCheckReading();

      if (getMediumAccessorImplementationToTest().getMedium().isRandomAccess()) {
         long sizeBeforeRead = getMediumAccessorImplementationToTest().getMedium().getCurrentLength();

         for (Iterator<Integer> offsetIterator = expectedContentsForRead.keySet().iterator(); offsetIterator
            .hasNext();) {
            Integer offset = offsetIterator.next();
            Integer size = expectedContentsForRead.get(offset);

            ByteBuffer readContent = ByteBuffer.allocate(size);

            try {
               getMediumAccessorImplementationToTest().read(
                  new StandardMediumReference(getMediumAccessorImplementationToTest().getMedium(), offset),
                  readContent);
            }

            catch (EndOfMediumException e) {
               Assert.fail(UNEXPECTED_EOM + e);
            }

            // Reads the correct contents
            assertEqualsFileContent(readContent, offset);
         }

         // Size did not change after read operations
         Assert.assertEquals(sizeBeforeRead, getMediumAccessorImplementationToTest().getMedium().getCurrentLength());
      }
   }

   /**
    * Tests the {@link IMediumAccessor#write(IMediumReference, ByteBuffer)} method for random-access media.
    */
   @Test
   public void testWrite_randomAccess() {

      if (!getMediumAccessorImplementationToTest().getMedium().isReadOnly()) {
         if (getMediumAccessorImplementationToTest().getMedium().isRandomAccess()) {
            IMediumReference previousWriteEndReference = new StandardMediumReference(
               getMediumAccessorImplementationToTest().getMedium(), 0);

            for (int i = 0; i < TEST_BUFFERS_FOR_WRITING.length; i++) {
               ByteBuffer dataToWrite = TEST_BUFFERS_FOR_WRITING[i];

               // Get a randomly generated offset in the medium for writing
               IMediumReference writeStartReference = determineWriteReference(getMediumAccessorImplementationToTest(),
                  previousWriteEndReference);

               // Reference pointing to the offset behind the last medium byte
               IMediumReference mediumEndReference = new StandardMediumReference(
                  getMediumAccessorImplementationToTest().getMedium(),
                  getMediumAccessorImplementationToTest().getMedium().getCurrentLength());

               // Reference pointing to the offset behind the last byte written
               IMediumReference writeEndReference = writeStartReference.advance(dataToWrite.capacity());

               // Write the data
               getMediumAccessorImplementationToTest().write(writeStartReference, dataToWrite);

               Assert.assertEquals(0, dataToWrite.remaining());

               // Length of the medium has changed as expected
               checkLengthAfterWrite(mediumEndReference, writeEndReference);
               // The data written can be reread without changes
               checkReRead(dataToWrite, writeStartReference);

               // The data between the previous write call and the current write
               // call
               // did not change by this write call
               checkUnchangedGaps(previousWriteEndReference, writeStartReference);

               previousWriteEndReference = writeEndReference;
            }
         }
      }
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
   private void checkLengthAfterWrite(IMediumReference mediumEndReference, IMediumReference writeEndReference) {

      long lengthAfter = getMediumAccessorImplementationToTest().getMedium().getCurrentLength();

      // Length must not have changed, as write overwrote something
      if (mediumEndReference.behindOrEqual(writeEndReference))
         Assert.assertEquals(mediumEndReference.getAbsoluteMediumOffset(), lengthAfter);

      // Length must have changed by the size the buffer extends the medium size
      else
         Assert.assertEquals(writeEndReference.getAbsoluteMediumOffset(), lengthAfter);
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
   private void checkReRead(ByteBuffer dataWritten, IMediumReference writeStartReference) {

      ByteBuffer reread = ByteBuffer.allocate(dataWritten.capacity());

      try {
         getMediumAccessorImplementationToTest().read(writeStartReference, reread);
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
         final int size = (int) (currentWriteStartReference.distanceTo(previousWriteEndReference));

         ByteBuffer inBetween = ByteBuffer.allocate(size);

         ByteBuffer inBetweenExpected = ByteBuffer.allocate(size);

         try {
            getMediumAccessorImplementationToTest().read(previousWriteEndReference, inBetween);
            getReadOnlyMediumAccessorImplementationToTest()
               .read(new StandardMediumReference(getReadOnlyMediumAccessorImplementationToTest().getMedium(),
                  previousWriteEndReference.getAbsoluteMediumOffset()), inBetweenExpected);
         }

         catch (EndOfMediumException e) {
            Assert.fail(UNEXPECTED_EOM + e);
         }

         Assert.assertEquals(inBetweenExpected, inBetween);
      }
   }

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

      final long newWriteOffset = previousWriteEndReference.getAbsoluteMediumOffset()
         + (testling.getMedium().getCurrentLength() - previousWriteEndReference.getAbsoluteMediumOffset()) / 2;

      if (newWriteOffset >= testling.getMedium().getCurrentLength())
         return new StandardMediumReference(testling.getMedium(), testling.getMedium().getCurrentLength() - 1);

      return new StandardMediumReference(testling.getMedium(), newWriteOffset);
   }
}
