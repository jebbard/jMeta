/**
 * {@link AbstractWritableRandomAccessMediumAccessorTest}.java
 *
 * @author Jens Ebert
 * @date 08.10.17 21:22:53 (October 8, 2017)
 */

package com.github.jmeta.library.media.impl.mediumAccessor;

import static com.github.jmeta.library.media.api.helper.TestMediumUtility.createReference;

import java.nio.ByteBuffer;

import org.junit.Test;

import com.github.jmeta.library.media.api.helper.TestMediumUtility;
import com.github.jmeta.library.media.api.types.IMedium;
import com.github.jmeta.library.media.api.types.IMediumReference;
import com.github.jmeta.library.media.impl.reference.StandardMediumReference;
import com.github.jmeta.utility.dbc.api.exceptions.PreconditionUnfullfilledException;
import com.github.jmeta.utility.testsetup.api.exceptions.TestDataException;

import junit.framework.Assert;

/**
 * {@link AbstractWritableRandomAccessMediumAccessorTest} is the base class for testing {@link IMediumAccessor}
 * instances that are random-access media and thus can also be modified by writing. All {@link IMediumAccessor}
 * instances returned by {@link #createImplementationToTest()} must be thus enabled for writing.
 * 
 * This class contains all test cases specific to those {@link IMediumAccessor} instances, specifically the tests of
 * {@link IMediumAccessor#write(ByteBuffer)}.
 */
public abstract class AbstractWritableRandomAccessMediumAccessorTest extends AbstractIMediumAccessorTest {

   /**
    * Tests {@link IMediumAccessor#setCurrentPosition(IMediumReference)} and
    * {@link IMediumAccessor#getCurrentPosition()}.
    */
   @Test
   public void setCurrentPosition_offsetBeforeEOM_getCurrentPositionReturnsIt() {

      IMediumAccessor<?> mediumAccessor = getImplementationToTest();

      int newOffsetOne = 20;
      IMediumReference changeReferenceOne = createReference(mediumAccessor.getMedium(), newOffsetOne);

      mediumAccessor.setCurrentPosition(changeReferenceOne);

      Assert.assertEquals(newOffsetOne, mediumAccessor.getCurrentPosition().getAbsoluteMediumOffset());

      int newOffsetTwo = 10;
      IMediumReference changeReferenceTwo = createReference(mediumAccessor.getMedium(), newOffsetTwo);

      mediumAccessor.setCurrentPosition(changeReferenceTwo);

      Assert.assertEquals(newOffsetTwo, mediumAccessor.getCurrentPosition().getAbsoluteMediumOffset());
   }

   /**
    * Tests {@link IMediumAccessor#setCurrentPosition(IMediumReference)}.
    */
   @Test(expected = PreconditionUnfullfilledException.class)
   public void setCurrentPosition_offsetBehindEOM_throwsException() {

      IMediumAccessor<?> mediumAccessor = getImplementationToTest();

      int newOffsetOne = EXPECTED_FILE_CONTENTS.length + 1;
      IMediumReference changeReferenceOne = createReference(mediumAccessor.getMedium(), newOffsetOne);

      mediumAccessor.setCurrentPosition(changeReferenceOne);
   }

   /**
    * Tests {@link IMediumAccessor#setCurrentPosition(IMediumReference)} and
    * {@link IMediumAccessor#getCurrentPosition()}.
    */
   @Test
   public void setCurrentPosition_offsetAtEOM_getCurrentPositionReturnsIt() {

      IMediumAccessor<?> mediumAccessor = getImplementationToTest();

      int newOffsetOne = EXPECTED_FILE_CONTENTS.length;
      IMediumReference changeReferenceOne = createReference(mediumAccessor.getMedium(), newOffsetOne);

      mediumAccessor.setCurrentPosition(changeReferenceOne);

      Assert.assertEquals(newOffsetOne, mediumAccessor.getCurrentPosition().getAbsoluteMediumOffset());
   }

   /**
    * Tests {@link IMediumAccessor#setCurrentPosition(IMediumReference)}.
    */
   @Test(expected = PreconditionUnfullfilledException.class)
   public void setCurrentPosition_onClosedMediumAccessor_throwsException() {

      getImplementationToTest().close();
      getImplementationToTest().setCurrentPosition(createReference(getExpectedMedium(), 0));
   }

   /**
    * Tests {@link IMediumAccessor#setCurrentPosition(IMediumReference)}.
    */
   @Test(expected = PreconditionUnfullfilledException.class)
   public void setCurrentPosition_forInvalidMediumReference_throwsException() {
      IMediumAccessor<?> mediumAccessor = getImplementationToTest();

      mediumAccessor.setCurrentPosition(createReference(TestMediumUtility.DUMMY_UNRELATED_MEDIUM, 0));
   }

   /**
    * Tests {@link IMediumAccessor#getCurrentPosition()}.
    */
   @Test
   public void getCurrentPosition_afterSuccessfulWriteBeforeEOM_changedByNumberOfWrittenBytes() {

      IMediumAccessor<?> mediumAccessor = getImplementationToTest();

      IMediumReference writeReference = createReference(mediumAccessor.getMedium(), EXPECTED_FILE_CONTENTS.length / 2);

      ByteBuffer dataWritten = performWrite(mediumAccessor, writeReference);

      Assert.assertEquals(writeReference.advance(dataWritten.capacity()), mediumAccessor.getCurrentPosition());
   }

   /**
    * Tests {@link IMediumAccessor#getCurrentPosition()}.
    */
   @Test
   public void getCurrentPosition_afterSuccessfulWriteOverlappingEOM_changedByNumberOfWrittenBytes() {
      IMediumAccessor<?> mediumAccessor = getImplementationToTest();

      int startBeforeEndOfMedium = 5;
      IMediumReference writeReference = createReference(mediumAccessor.getMedium(),
         EXPECTED_FILE_CONTENTS.length - startBeforeEndOfMedium);

      ByteBuffer dataWritten = performWrite(mediumAccessor, writeReference);

      Assert.assertEquals(writeReference.advance(dataWritten.capacity()), mediumAccessor.getCurrentPosition());
   }

   /**
    * Tests {@link IMediumAccessor#isAtEndOfMedium(IMediumReference)}.
    */
   @Test
   public void isAtEndOfMedium_forRandomAccessWithoutPriorReadIfAtEndOfMedium_returnsTrue() {

      IMediumAccessor<?> mediumAccessor = getImplementationToTest();
      int readOffset = EXPECTED_FILE_CONTENTS.length;

      IMediumReference readReference = createReference(mediumAccessor.getMedium(), readOffset);

      // Each call is checked twice to ensure it is repeatable (especially for streams!)
      mediumAccessor.setCurrentPosition(readReference);
      Assert.assertEquals(true, mediumAccessor.isAtEndOfMedium());
      mediumAccessor.setCurrentPosition(readReference);
      Assert.assertEquals(true, mediumAccessor.isAtEndOfMedium());
   }

   /**
    * Tests {@link IMediumAccessor#write(ByteBuffer)}.
    */
   @Test
   public void write_endOfWriteBeforeEndOfFile_overwritesWithExpectedBytesAndLeavesOtherBytesUnchanged() {
      IMediumAccessor<?> mediumAccessor = getImplementationToTest();

      IMediumReference writeReference = createReference(mediumAccessor.getMedium(), EXPECTED_FILE_CONTENTS.length / 2);

      long mediumLengthBeforeWrite = mediumAccessor.getMedium().getCurrentLength();

      ByteBuffer dataWritten = performWrite(mediumAccessor, writeReference);

      Assert.assertEquals(mediumLengthBeforeWrite, mediumAccessor.getMedium().getCurrentLength());

      assertSameDataWrittenIsReadAgain(mediumAccessor, writeReference, dataWritten);
      // Ensure the medium did not change before the written range
      assertMediumDidNotChangeInRange(mediumAccessor, createReference(mediumAccessor.getMedium(), 0), writeReference);
      // Ensure the medium did not change after the written range
      assertMediumDidNotChangeInRange(mediumAccessor, writeReference.advance(dataWritten.capacity()),
         createReference(mediumAccessor.getMedium(), mediumAccessor.getMedium().getCurrentLength()));
   }

   /**
    * Tests {@link IMediumAccessor#write(ByteBuffer)}.
    */
   @Test
   public void write_endOfWriteAfterEndOfFile_overwritesWithExpectedBytesExtendsFileAndLeavesOtherBytesUnchanged() {
      IMediumAccessor<?> mediumAccessor = getImplementationToTest();

      int startBeforeEndOfMedium = 5;
      IMediumReference writeReference = createReference(mediumAccessor.getMedium(),
         EXPECTED_FILE_CONTENTS.length - startBeforeEndOfMedium);

      long mediumLengthBeforeWrite = mediumAccessor.getMedium().getCurrentLength();

      ByteBuffer dataWritten = performWrite(mediumAccessor, writeReference);

      long extendedLength = dataWritten.capacity() - startBeforeEndOfMedium;

      Assert.assertEquals(mediumLengthBeforeWrite + extendedLength, mediumAccessor.getMedium().getCurrentLength());

      assertSameDataWrittenIsReadAgain(mediumAccessor, writeReference, dataWritten);
      // Ensure the medium did not change before the written range
      assertMediumDidNotChangeInRange(mediumAccessor, createReference(mediumAccessor.getMedium(), 0), writeReference);
   }

   /**
    * Tests {@link IMediumAccessor#write(ByteBuffer)}.
    */
   @Test(expected = PreconditionUnfullfilledException.class)
   public void write_onClosedMediumAccessor_throwsException() {
      IMediumAccessor<?> mediumAccessor = getImplementationToTest();

      mediumAccessor.close();

      mediumAccessor.write(ByteBuffer.allocate(5));
   }

   /**
    * Tests {@link IMediumAccessor#truncate(IMediumReference)}.
    */
   @Test
   public void truncate_toStrictlyPositiveLength_mediumBytesBehindReferenceAreGone() {
      IMediumAccessor<?> mediumAccessor = getImplementationToTest();

      int newExpectedLength = 200;
      testTruncate_mediumSizeChangesAndBytesBeforeAreUnchanged(mediumAccessor,
         createReference(mediumAccessor.getMedium(), newExpectedLength), newExpectedLength);
   }

   /**
    * Tests {@link IMediumAccessor#truncate(IMediumReference)}.
    */
   @Test
   public void truncate_atEOM_doesNotChangeMedium() {
      IMediumAccessor<?> mediumAccessor = getImplementationToTest();

      int newExpectedLength = EXPECTED_FILE_CONTENTS.length;
      testTruncate_mediumSizeChangesAndBytesBeforeAreUnchanged(mediumAccessor,
         createReference(mediumAccessor.getMedium(), newExpectedLength), newExpectedLength);
   }

   /**
    * Tests the {@link IMediumAccessor#truncate(IMediumReference)}.
    */
   @Test
   public void truncate_toOneByteSmallerLength_mediumBytesBehindReferenceAreGone() {
      IMediumAccessor<?> mediumAccessor = getImplementationToTest();

      int newExpectedLength = EXPECTED_FILE_CONTENTS.length - 1;
      testTruncate_mediumSizeChangesAndBytesBeforeAreUnchanged(mediumAccessor,
         createReference(mediumAccessor.getMedium(), newExpectedLength), newExpectedLength);
   }

   /**
    * Tests {@link IMediumAccessor#truncate(IMediumReference)}.
    */
   @Test
   public void truncate_toZeroLength_noMediumBytesAnymore() {
      IMediumAccessor<?> mediumAccessor = getImplementationToTest();

      int newExpectedLength = 0;
      testTruncate_mediumSizeChangesAndBytesBeforeAreUnchanged(mediumAccessor,
         createReference(mediumAccessor.getMedium(), newExpectedLength), newExpectedLength);
   }

   /**
    * Tests {@link IMediumAccessor#truncate(IMediumReference)}.
    */
   @Test
   public void truncate_toExactlyCurrentLength_doesNotChangeTheMedium() {
      IMediumAccessor<?> mediumAccessor = getImplementationToTest();

      int newExpectedLength = EXPECTED_FILE_CONTENTS.length;
      testTruncate_mediumSizeChangesAndBytesBeforeAreUnchanged(mediumAccessor,
         createReference(mediumAccessor.getMedium(), newExpectedLength), newExpectedLength);
   }

   /**
    * Tests {@link IMediumAccessor#truncate(IMediumReference)}.
    */
   @Test(expected = PreconditionUnfullfilledException.class)
   public void truncate_onClosedMediumAccessor_throwsException() {
      IMediumAccessor<?> mediumAccessor = getImplementationToTest();

      mediumAccessor.close();

      mediumAccessor.truncate();
   }

   /**
    * @see com.github.jmeta.library.media.impl.mediumAccessor.AbstractIMediumAccessorTest#validateTestMedium(com.github.jmeta.library.media.api.types.IMedium)
    */
   @Override
   protected void validateTestMedium(IMedium<?> theMedium) {
      if (!theMedium.isRandomAccess()) {
         throw new TestDataException("The test IMedium must be a random access medium", null);
      }

      if (theMedium.isReadOnly()) {
         throw new TestDataException("The test IMedium must not be read-only", null);
      }
   }

   /**
    * Tests the {@link IMediumAccessor#truncate(IMediumReference)} method on the given {@link IMediumAccessor} and with
    * the given truncate {@link IMediumReference}.
    * 
    * @param mediumAccessor
    *           The {@link IMediumAccessor} to test
    * @param truncateReference
    *           The truncate {@link IMediumReference}
    * @param expectedNewLength
    *           The expected new {@link IMedium} length
    */
   private void testTruncate_mediumSizeChangesAndBytesBeforeAreUnchanged(IMediumAccessor<?> mediumAccessor,
      IMediumReference truncateReference, long expectedNewLength) {

      mediumAccessor.setCurrentPosition(truncateReference);
      mediumAccessor.truncate();

      Assert.assertEquals(expectedNewLength, mediumAccessor.getMedium().getCurrentLength());
      Assert.assertEquals(mediumAccessor.getCurrentPosition(), truncateReference);
      Assert.assertTrue(mediumAccessor.isAtEndOfMedium());

      IMediumReference mediumStartReference = createReference(mediumAccessor.getMedium(), 0);
      IMediumReference mediumEndReference = createReference(mediumAccessor.getMedium(),
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
    * Executes a call to {@link IMediumAccessor#write(ByteBuffer)} with hard-coded dummy bytes and returns the dummy
    * bytes written.
    * 
    * It already checks the content of the byte buffer written to have the correct remaining bytes-
    * 
    * @param mediumAccessor
    *           The {@link IMediumAccessor} to test
    * @param writeReference
    *           The {@link IMediumReference} to write to
    * @return The written {@link ByteBuffer}.
    */
   private static ByteBuffer performWrite(IMediumAccessor<?> mediumAccessor, IMediumReference writeReference) {
      ByteBuffer dataToWrite = ByteBuffer
         .wrap(new byte[] { 'T', 'E', 'S', 'T', ' ', 'B', 'U', 'F', ' ', '1', '0', '0', '0', '0', '0', '3' });

      mediumAccessor.setCurrentPosition(writeReference);
      mediumAccessor.write(dataToWrite);

      Assert.assertEquals(0, dataToWrite.remaining());
      return dataToWrite;
   }

   /**
    * Checks whether of the given {@link ByteBuffer} as written before can be re-read again with exactly the same
    * content.
    * 
    * @param mediumAccessor
    *           The implementation to test
    * @param writeReference
    *           The {@link StandardMediumReference} where the {@link ByteBuffer} has been written to.
    * @param dataWritten
    *           The {@link ByteBuffer} written before.
    */
   private static void assertSameDataWrittenIsReadAgain(IMediumAccessor<?> mediumAccessor,
      IMediumReference writeReference, ByteBuffer dataWritten) {

      ByteBuffer reread = performReadNoEOMExpected(mediumAccessor,
         new ReadTestData((int) writeReference.getAbsoluteMediumOffset(), dataWritten.capacity()));

      // Reset position to zero
      dataWritten.rewind();

      Assert.assertEquals(dataWritten, reread);
   }

   /**
    * Checks whether the bytes between two {@link IMediumReference} instances is unchanged after a write operation.
    * 
    * @param mediumAccessor
    *           The implementation to test
    * @param rangeStartReference
    *           The {@link IMediumReference} pointing to the start of the range to check.
    * @param rangeEndReference
    *           The {@link IMediumReference} pointing to the end of the range to check, i.e. the first offset not
    *           checked.
    */
   private void assertMediumDidNotChangeInRange(IMediumAccessor<?> mediumAccessor, IMediumReference rangeStartReference,
      IMediumReference rangeEndReference) {

      int sizeToRead = (int) (rangeEndReference.distanceTo(rangeStartReference));

      byte[] expectedBytes = new byte[sizeToRead];

      System.arraycopy(EXPECTED_FILE_CONTENTS, (int) rangeStartReference.getAbsoluteMediumOffset(), expectedBytes, 0,
         sizeToRead);

      ByteBuffer bytesExpected = ByteBuffer.wrap(expectedBytes);

      ByteBuffer bytesRead = performReadNoEOMExpected(mediumAccessor,
         new ReadTestData((int) rangeStartReference.getAbsoluteMediumOffset(), sizeToRead));

      Assert.assertEquals(bytesExpected, bytesRead);
   }
}
