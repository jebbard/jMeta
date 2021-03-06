/**
 * {@link AbstractWritableRandomAccessMediumAccessorTest}.java
 *
 * @author Jens Ebert
 * @date 08.10.17 21:22:53 (October 8, 2017)
 */

package com.github.jmeta.library.media.impl.mediumAccessor;

import java.nio.ByteBuffer;

import org.junit.Assert;
import org.junit.Test;

import com.github.jmeta.library.media.api.helper.TestMedia;
import com.github.jmeta.library.media.api.types.Medium;
import com.github.jmeta.library.media.api.types.MediumAccessType;
import com.github.jmeta.library.media.api.types.MediumOffset;
import com.github.jmeta.library.media.impl.offset.StandardMediumOffset;
import com.github.jmeta.utility.dbc.api.exceptions.PreconditionUnfullfilledException;
import com.github.jmeta.utility.testsetup.api.exceptions.InvalidTestDataException;

/**
 * {@link AbstractWritableRandomAccessMediumAccessorTest} is the base class for testing {@link MediumAccessor} instances
 * that are random-access media and thus can also be modified by writing. All {@link MediumAccessor} instances returned
 * by {@link #createImplementationToTest()} must be thus enabled for writing.
 *
 * This class contains all test cases specific to those {@link MediumAccessor} instances, specifically the tests of
 * {@link MediumAccessor#write(ByteBuffer)}.
 */
public abstract class AbstractWritableRandomAccessMediumAccessorTest extends AbstractMediumAccessorTest {

   /**
    * Checks whether of the given {@link ByteBuffer} as written before can be re-read again with exactly the same
    * content.
    * 
    * @param mediumAccessor
    *           The implementation to test
    * @param writeReference
    *           The {@link StandardMediumOffset} where the {@link ByteBuffer} has been written to.
    * @param dataWritten
    *           The {@link ByteBuffer} written before.
    */
   private static void assertSameDataWrittenIsReadAgain(MediumAccessor<?> mediumAccessor, MediumOffset writeReference,
      ByteBuffer dataWritten) {

      ByteBuffer reread = AbstractMediumAccessorTest.performReadNoEOMExpected(mediumAccessor,
         new ReadTestData((int) writeReference.getAbsoluteMediumOffset(), dataWritten.remaining()));

      Assert.assertEquals(dataWritten, reread);
   }

   /**
    * Executes a call to {@link MediumAccessor#write(ByteBuffer)} with hard-coded dummy bytes and returns the dummy
    * bytes written.
    * 
    * It already checks the content of the byte buffer written to have the correct remaining bytes-
    * 
    * @param mediumAccessor
    *           The {@link MediumAccessor} to test
    * @param writeReference
    *           The {@link MediumOffset} to write to
    * @param useReadOnlyBuffer
    *           true to use a read-only buffer when writing, false otherwise
    * @return The written {@link ByteBuffer}.
    */
   private static ByteBuffer performWrite(MediumAccessor<?> mediumAccessor, MediumOffset writeReference,
      boolean useReadOnlyBuffer) {
      ByteBuffer dataToWrite = ByteBuffer
         .wrap(new byte[] { 'T', 'E', 'S', 'T', ' ', 'B', 'U', 'F', ' ', '1', '0', '0', '0', '0', '0', '3' });

      if (useReadOnlyBuffer) {
         dataToWrite = dataToWrite.asReadOnlyBuffer();
      }

      mediumAccessor.setCurrentPosition(writeReference);
      mediumAccessor.write(dataToWrite);

      Assert.assertEquals(0, dataToWrite.position());
      Assert.assertEquals(dataToWrite.capacity(), dataToWrite.limit());
      return dataToWrite;
   }

   /**
    * Tests {@link MediumAccessor#getCurrentPosition()}.
    */
   @Test
   public void getCurrentPosition_afterSuccessfulWriteBeforeEOM_changedByNumberOfWrittenBytes() {

      MediumAccessor<?> mediumAccessor = getImplementationToTest();

      mediumAccessor.open();

      MediumOffset writeReference = TestMedia.at(mediumAccessor.getMedium(),
         AbstractMediumAccessorTest.getExpectedMediumContent().length / 2);

      ByteBuffer dataWritten = AbstractWritableRandomAccessMediumAccessorTest.performWrite(mediumAccessor,
         writeReference, false);

      Assert.assertEquals(writeReference.advance(dataWritten.capacity()), mediumAccessor.getCurrentPosition());
   }

   /**
    * Tests {@link MediumAccessor#getCurrentPosition()}.
    */
   @Test
   public void getCurrentPosition_afterSuccessfulWriteOverlappingEOM_changedByNumberOfWrittenBytes() {
      MediumAccessor<?> mediumAccessor = getImplementationToTest();

      mediumAccessor.open();

      int startBeforeEndOfMedium = 5;
      MediumOffset writeReference = TestMedia.at(mediumAccessor.getMedium(),
         AbstractMediumAccessorTest.getExpectedMediumContent().length - startBeforeEndOfMedium);

      ByteBuffer dataWritten = AbstractWritableRandomAccessMediumAccessorTest.performWrite(mediumAccessor,
         writeReference, false);

      Assert.assertEquals(writeReference.advance(dataWritten.capacity()), mediumAccessor.getCurrentPosition());
   }

   /**
    * Tests {@link MediumAccessor#isAtEndOfMedium()}.
    */
   @Test
   public void isAtEndOfMedium_forRandomAccessWithoutPriorReadIfAtEndOfMedium_returnsTrue() {

      MediumAccessor<?> mediumAccessor = getImplementationToTest();

      mediumAccessor.open();

      int readOffset = AbstractMediumAccessorTest.getExpectedMediumContent().length;

      MediumOffset readReference = TestMedia.at(mediumAccessor.getMedium(), readOffset);

      // Each call is checked twice to ensure it is repeatable (especially for
      // streams!)
      mediumAccessor.setCurrentPosition(readReference);
      Assert.assertEquals(true, mediumAccessor.isAtEndOfMedium());
      mediumAccessor.setCurrentPosition(readReference);
      Assert.assertEquals(true, mediumAccessor.isAtEndOfMedium());
   }

   /**
    * Tests {@link MediumAccessor#setCurrentPosition(MediumOffset)}.
    */
   @Test(expected = PreconditionUnfullfilledException.class)
   public void setCurrentPosition_forInvalidMediumReference_throwsException() {
      MediumAccessor<?> mediumAccessor = getImplementationToTest();

      mediumAccessor.setCurrentPosition(TestMedia.at(TestMedia.OTHER_MEDIUM, 0));
   }

   /**
    * Tests {@link MediumAccessor#setCurrentPosition(MediumOffset)} and {@link MediumAccessor#getCurrentPosition()}.
    */
   @Test
   public void setCurrentPosition_offsetAtEOM_getCurrentPositionReturnsIt() {

      MediumAccessor<?> mediumAccessor = getImplementationToTest();

      mediumAccessor.open();

      int newOffsetOne = AbstractMediumAccessorTest.getExpectedMediumContent().length;
      MediumOffset changeReferenceOne = TestMedia.at(mediumAccessor.getMedium(), newOffsetOne);

      mediumAccessor.setCurrentPosition(changeReferenceOne);

      Assert.assertEquals(newOffsetOne, mediumAccessor.getCurrentPosition().getAbsoluteMediumOffset());
   }

   /**
    * Tests {@link MediumAccessor#setCurrentPosition(MediumOffset)} and {@link MediumAccessor#getCurrentPosition()}.
    */
   @Test
   public void setCurrentPosition_offsetBeforeEOM_getCurrentPositionReturnsIt() {

      MediumAccessor<?> mediumAccessor = getImplementationToTest();

      mediumAccessor.open();

      int newOffsetOne = 20;
      MediumOffset changeReferenceOne = TestMedia.at(mediumAccessor.getMedium(), newOffsetOne);

      mediumAccessor.setCurrentPosition(changeReferenceOne);

      Assert.assertEquals(newOffsetOne, mediumAccessor.getCurrentPosition().getAbsoluteMediumOffset());

      int newOffsetTwo = 10;
      MediumOffset changeReferenceTwo = TestMedia.at(mediumAccessor.getMedium(), newOffsetTwo);

      mediumAccessor.setCurrentPosition(changeReferenceTwo);

      Assert.assertEquals(newOffsetTwo, mediumAccessor.getCurrentPosition().getAbsoluteMediumOffset());
   }

   /**
    * Tests {@link MediumAccessor#setCurrentPosition(MediumOffset)}.
    */
   @Test
   public void setCurrentPosition_offsetBehindEOM_getCurrentPositionReturnsIt() {

      MediumAccessor<?> mediumAccessor = getImplementationToTest();

      mediumAccessor.open();

      int newOffsetOne = AbstractMediumAccessorTest.getExpectedMediumContent().length + 10;
      MediumOffset changeReferenceOne = TestMedia.at(mediumAccessor.getMedium(), newOffsetOne);

      mediumAccessor.setCurrentPosition(changeReferenceOne);

      Assert.assertEquals(newOffsetOne, mediumAccessor.getCurrentPosition().getAbsoluteMediumOffset());
   }

   /**
    * Tests {@link MediumAccessor#setCurrentPosition(MediumOffset)}.
    */
   @Test(expected = PreconditionUnfullfilledException.class)
   public void setCurrentPosition_onClosedMediumAccessor_throwsException() {

      getImplementationToTest().close();
      getImplementationToTest().setCurrentPosition(TestMedia.at(getExpectedMedium(), 0));
   }

   /**
    * Tests {@link MediumAccessor#truncate()}.
    */
   @Test
   public void truncate_atEOM_doesNotChangeMedium() {
      MediumAccessor<?> mediumAccessor = getImplementationToTest();

      mediumAccessor.open();

      int newExpectedLength = AbstractMediumAccessorTest.getExpectedMediumContent().length;
      testTruncate_mediumSizeChangesAndBytesBeforeAreUnchanged(mediumAccessor,
         TestMedia.at(mediumAccessor.getMedium(), newExpectedLength), newExpectedLength);
   }

   /**
    * Tests {@link MediumAccessor#truncate()}.
    */
   @Test(expected = PreconditionUnfullfilledException.class)
   public void truncate_onClosedMediumAccessor_throwsException() {
      MediumAccessor<?> mediumAccessor = getImplementationToTest();

      mediumAccessor.open();

      mediumAccessor.close();

      mediumAccessor.truncate();
   }

   /**
    * Tests {@link MediumAccessor#truncate()}.
    */
   @Test
   public void truncate_toExactlyCurrentLength_doesNotChangeTheMedium() {
      MediumAccessor<?> mediumAccessor = getImplementationToTest();

      mediumAccessor.open();

      int newExpectedLength = AbstractMediumAccessorTest.getExpectedMediumContent().length;
      testTruncate_mediumSizeChangesAndBytesBeforeAreUnchanged(mediumAccessor,
         TestMedia.at(mediumAccessor.getMedium(), newExpectedLength), newExpectedLength);
   }

   /**
    * Tests the {@link MediumAccessor#truncate()}.
    */
   @Test
   public void truncate_toOneByteSmallerLength_mediumBytesBehindReferenceAreGone() {
      MediumAccessor<?> mediumAccessor = getImplementationToTest();

      mediumAccessor.open();

      int newExpectedLength = AbstractMediumAccessorTest.getExpectedMediumContent().length - 1;
      testTruncate_mediumSizeChangesAndBytesBeforeAreUnchanged(mediumAccessor,
         TestMedia.at(mediumAccessor.getMedium(), newExpectedLength), newExpectedLength);
   }

   /**
    * Tests {@link MediumAccessor#truncate()}.
    */
   @Test
   public void truncate_toStrictlyPositiveLength_mediumBytesBehindReferenceAreGone() {
      MediumAccessor<?> mediumAccessor = getImplementationToTest();

      mediumAccessor.open();

      int newExpectedLength = 200;
      testTruncate_mediumSizeChangesAndBytesBeforeAreUnchanged(mediumAccessor,
         TestMedia.at(mediumAccessor.getMedium(), newExpectedLength), newExpectedLength);
   }

   /**
    * Tests {@link MediumAccessor#truncate()}.
    */
   @Test
   public void truncate_toZeroLength_noMediumBytesAnymore() {
      MediumAccessor<?> mediumAccessor = getImplementationToTest();

      mediumAccessor.open();

      int newExpectedLength = 0;
      testTruncate_mediumSizeChangesAndBytesBeforeAreUnchanged(mediumAccessor,
         TestMedia.at(mediumAccessor.getMedium(), newExpectedLength), newExpectedLength);
   }

   /**
    * Tests {@link MediumAccessor#write(ByteBuffer)}.
    */
   @Test
   public void write_endOfWriteAfterEndOfFile_overwritesWithExpectedBytesExtendsFileAndLeavesOtherBytesUnchanged() {
      MediumAccessor<?> mediumAccessor = getImplementationToTest();

      mediumAccessor.open();

      int startBeforeEndOfMedium = 5;
      MediumOffset writeReference = TestMedia.at(mediumAccessor.getMedium(),
         AbstractMediumAccessorTest.getExpectedMediumContent().length - startBeforeEndOfMedium);

      long mediumLengthBeforeWrite = mediumAccessor.getMedium().getCurrentLength();

      ByteBuffer dataWritten = AbstractWritableRandomAccessMediumAccessorTest.performWrite(mediumAccessor,
         writeReference, false);

      long extendedLength = dataWritten.capacity() - startBeforeEndOfMedium;

      Assert.assertEquals(mediumLengthBeforeWrite + extendedLength, mediumAccessor.getMedium().getCurrentLength());

      AbstractWritableRandomAccessMediumAccessorTest.assertSameDataWrittenIsReadAgain(mediumAccessor, writeReference,
         dataWritten);
      // Ensure the medium did not change before the written range
      assertMediumDidNotChangeInRange(mediumAccessor, TestMedia.at(mediumAccessor.getMedium(), 0), writeReference);
   }

   /**
    * Tests {@link MediumAccessor#write(ByteBuffer)}.
    */
   @Test
   public void write_endOfWriteBeforeEndOfFile_overwritesWithExpectedBytesAndLeavesOtherBytesUnchanged() {
      MediumAccessor<?> mediumAccessor = getImplementationToTest();

      mediumAccessor.open();

      MediumOffset writeReference = TestMedia.at(mediumAccessor.getMedium(),
         AbstractMediumAccessorTest.getExpectedMediumContent().length / 2);

      long mediumLengthBeforeWrite = mediumAccessor.getMedium().getCurrentLength();

      ByteBuffer dataWritten = AbstractWritableRandomAccessMediumAccessorTest.performWrite(mediumAccessor,
         writeReference, false);

      Assert.assertEquals(mediumLengthBeforeWrite, mediumAccessor.getMedium().getCurrentLength());

      AbstractWritableRandomAccessMediumAccessorTest.assertSameDataWrittenIsReadAgain(mediumAccessor, writeReference,
         dataWritten);
      // Ensure the medium did not change before the written range
      assertMediumDidNotChangeInRange(mediumAccessor, TestMedia.at(mediumAccessor.getMedium(), 0), writeReference);
      // Ensure the medium did not change after the written range
      assertMediumDidNotChangeInRange(mediumAccessor, writeReference.advance(dataWritten.capacity()),
         TestMedia.at(mediumAccessor.getMedium(), mediumAccessor.getMedium().getCurrentLength()));
   }

   /**
    * Tests {@link MediumAccessor#write(ByteBuffer)}.
    */
   @Test
   public void write_endOfWriteBeforeEndOfFile_withByteBufferLimitAndPositionInMiddle_overwritesWithExpectedBytesAndLeavesOtherBytesUnchanged() {
      MediumAccessor<?> mediumAccessor = getImplementationToTest();

      mediumAccessor.open();

      MediumOffset writeReference = TestMedia.at(mediumAccessor.getMedium(),
         AbstractMediumAccessorTest.getExpectedMediumContent().length / 2);

      long mediumLengthBeforeWrite = mediumAccessor.getMedium().getCurrentLength();

      ByteBuffer dataToWrite = ByteBuffer
         .wrap(new byte[] { 'T', 'E', 'S', 'T', ' ', 'B', 'U', 'F', ' ', '1', '0', '0', '0', '0', '0', '3' });

      int startPosition = 2;
      int endLimit = 10;
      dataToWrite.position(startPosition);
      dataToWrite.limit(endLimit);

      ByteBuffer expectedDataToBeReRead = dataToWrite.asReadOnlyBuffer();

      mediumAccessor.setCurrentPosition(writeReference);
      mediumAccessor.write(dataToWrite);

      Assert.assertEquals(startPosition, dataToWrite.position());
      Assert.assertEquals(endLimit, dataToWrite.limit());

      Assert.assertEquals(mediumLengthBeforeWrite, mediumAccessor.getMedium().getCurrentLength());

      AbstractWritableRandomAccessMediumAccessorTest.assertSameDataWrittenIsReadAgain(mediumAccessor, writeReference,
         expectedDataToBeReRead);
      // Ensure the medium did not change before the written range
      assertMediumDidNotChangeInRange(mediumAccessor, TestMedia.at(mediumAccessor.getMedium(), 0), writeReference);
      // Ensure the medium did not change after the written range
      assertMediumDidNotChangeInRange(mediumAccessor, writeReference.advance(endLimit - startPosition),
         TestMedia.at(mediumAccessor.getMedium(), mediumAccessor.getMedium().getCurrentLength()));
   }

   /**
    * Tests {@link MediumAccessor#write(ByteBuffer)}.
    */
   @Test(expected = PreconditionUnfullfilledException.class)
   public void write_onClosedMediumAccessor_throwsException() {
      MediumAccessor<?> mediumAccessor = getImplementationToTest();

      mediumAccessor.open();

      mediumAccessor.close();

      mediumAccessor.write(ByteBuffer.allocate(5));
   }

   /**
    * Tests {@link MediumAccessor#write(ByteBuffer)}.
    */
   @Test
   public void write_startOfWriteAfterEndOfFileWithReadOnlyBuffer_overwritesWithExpectedBytesExtendsFileAndLeavesOtherBytesUnchanged() {
      MediumAccessor<?> mediumAccessor = getImplementationToTest();

      mediumAccessor.open();

      int distFromEOM = 5;
      long startOffset = AbstractMediumAccessorTest.getExpectedMediumContent().length + distFromEOM;
      MediumOffset writeReference = TestMedia.at(mediumAccessor.getMedium(), startOffset);

      long mediumLengthBeforeWrite = mediumAccessor.getMedium().getCurrentLength();

      ByteBuffer dataWritten = AbstractWritableRandomAccessMediumAccessorTest.performWrite(mediumAccessor,
         writeReference, true);

      long extendedLength = dataWritten.capacity() + distFromEOM;

      Assert.assertEquals(mediumLengthBeforeWrite + extendedLength, mediumAccessor.getMedium().getCurrentLength());

      AbstractWritableRandomAccessMediumAccessorTest.assertSameDataWrittenIsReadAgain(mediumAccessor, writeReference,
         dataWritten);
      // Ensure the medium did not change before the written range
      MediumOffset mediumStartOfs = TestMedia.at(mediumAccessor.getMedium(), 0);
      assertMediumDidNotChangeInRange(mediumAccessor, mediumStartOfs,
         mediumStartOfs.advance(AbstractMediumAccessorTest.getExpectedMediumContent().length));
   }

   /**
    * Checks whether the bytes between two {@link MediumOffset} instances is unchanged after a write operation.
    * 
    * @param mediumAccessor
    *           The implementation to test
    * @param rangeStartReference
    *           The {@link MediumOffset} pointing to the start of the range to check.
    * @param rangeEndReference
    *           The {@link MediumOffset} pointing to the end of the range to check, i.e. the first offset not checked.
    */
   private void assertMediumDidNotChangeInRange(MediumAccessor<?> mediumAccessor, MediumOffset rangeStartReference,
      MediumOffset rangeEndReference) {

      int sizeToRead = (int) rangeEndReference.distanceTo(rangeStartReference);

      byte[] expectedBytes = new byte[sizeToRead];

      System.arraycopy(AbstractMediumAccessorTest.getExpectedMediumContent(),
         (int) rangeStartReference.getAbsoluteMediumOffset(), expectedBytes, 0, sizeToRead);

      ByteBuffer bytesExpected = ByteBuffer.wrap(expectedBytes);

      ByteBuffer bytesRead = AbstractMediumAccessorTest.performReadNoEOMExpected(mediumAccessor,
         new ReadTestData((int) rangeStartReference.getAbsoluteMediumOffset(), sizeToRead));

      Assert.assertEquals(bytesExpected, bytesRead);
   }

   /**
    * Tests the {@link MediumAccessor#truncate()} method on the given {@link MediumAccessor} and with the given truncate
    * {@link MediumOffset}.
    * 
    * @param mediumAccessor
    *           The {@link MediumAccessor} to test
    * @param truncateReference
    *           The truncate {@link MediumOffset}
    * @param expectedNewLength
    *           The expected new {@link Medium} length
    */
   private void testTruncate_mediumSizeChangesAndBytesBeforeAreUnchanged(MediumAccessor<?> mediumAccessor,
      MediumOffset truncateReference, long expectedNewLength) {

      mediumAccessor.setCurrentPosition(truncateReference);
      mediumAccessor.truncate();

      Assert.assertEquals(expectedNewLength, mediumAccessor.getMedium().getCurrentLength());
      Assert.assertEquals(mediumAccessor.getCurrentPosition(), truncateReference);
      Assert.assertTrue(mediumAccessor.isAtEndOfMedium());

      MediumOffset mediumStartReference = TestMedia.at(mediumAccessor.getMedium(), 0);
      MediumOffset mediumEndReference = TestMedia.at(mediumAccessor.getMedium(),
         mediumAccessor.getMedium().getCurrentLength());

      if (expectedNewLength > 0) {
         if (mediumEndReference.before(truncateReference)) {
            assertMediumDidNotChangeInRange(mediumAccessor, mediumStartReference, mediumEndReference);
         } else {
            assertMediumDidNotChangeInRange(mediumAccessor, mediumStartReference, truncateReference);
         }
      }
   }

   /**
    * @see com.github.jmeta.library.media.impl.mediumAccessor.AbstractMediumAccessorTest#validateTestMedium(com.github.jmeta.library.media.api.types.Medium)
    */
   @Override
   protected void validateTestMedium(Medium<?> theMedium) {
      if (!theMedium.isRandomAccess()) {
         throw new InvalidTestDataException("The test IMedium must be a random access medium", null);
      }

      if (theMedium.getMediumAccessType() == MediumAccessType.READ_ONLY) {
         throw new InvalidTestDataException("The test IMedium must not be read-only", null);
      }
   }
}
