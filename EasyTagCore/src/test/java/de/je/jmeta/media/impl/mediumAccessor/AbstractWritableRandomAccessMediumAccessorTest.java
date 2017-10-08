/**
 * {@link AbstractWritableRandomAccessMediumAccessorTest}.java
 *
 * @author Jens Ebert
 * @date 08.10.17 21:22:53 (October 8, 2017)
 */

package de.je.jmeta.media.impl.mediumAccessor;

import static de.je.jmeta.media.api.helper.TestMediumUtility.createReference;

import java.nio.ByteBuffer;

import org.junit.Test;

import de.je.jmeta.media.api.IMedium;
import de.je.jmeta.media.api.IMediumReference;
import de.je.jmeta.media.impl.IMediumAccessor;
import de.je.jmeta.media.impl.StandardMediumReference;
import de.je.util.javautil.common.err.PreconditionUnfullfilledException;
import de.je.util.javautil.testUtil.setup.TestDataException;
import junit.framework.Assert;

/**
 * {@link AbstractWritableRandomAccessMediumAccessorTest} is the base class for testing {@link IMediumAccessor}
 * instances that are random-access media and thus can also be modified by writing. All {@link IMediumAccessor}
 * instances returned by {@link #createImplementationToTest()} must be thus enabled for writing.
 * 
 * This class contains all test cases specific to those {@link IMediumAccessor} instances, specifically the tests of
 * {@link IMediumAccessor#write(IMediumReference, ByteBuffer)}.
 */
public abstract class AbstractWritableRandomAccessMediumAccessorTest extends AbstractIMediumAccessorTest {

   /**
    * Tests the {@link IMediumAccessor#isAtEndOfMedium(IMediumReference)}.
    */
   @Test
   public void isAtEndOfMedium_forRandomAccessWithoutPriorReadIfAtEndOfMedium_returnsTrue() {

      IMediumAccessor<?> mediumAccessor = getImplementationToTest();
      int readOffset = EXPECTED_FILE_CONTENTS.length;

      IMediumReference readReference = createReference(mediumAccessor.getMedium(), readOffset);

      // Each call is checked twice to ensure it is repeatable (especially for streams!)
      Assert.assertEquals(true, mediumAccessor.isAtEndOfMedium(readReference));
      Assert.assertEquals(true, mediumAccessor.isAtEndOfMedium(readReference));
   }

   /**
    * Tests the {@link IMediumAccessor#write(IMediumReference, ByteBuffer)}.
    */
   @Test
   public void write_endOfWriteBeforeEndOfFile_overwritesWithExpectedBytesAndLeavesOtherBytesUnchanged() {
      IMediumAccessor<?> mediumAccessor = getImplementationToTest();

      IMediumReference writeReference = createReference(mediumAccessor.getMedium(), EXPECTED_FILE_CONTENTS.length / 2);

      ByteBuffer dataToWrite = ByteBuffer
         .wrap(new byte[] { 'T', 'E', 'S', 'T', ' ', 'B', 'U', 'F', ' ', '0', '0', '0', '0', '0', '0', '3' });

      long mediumLengthBeforeWrite = mediumAccessor.getMedium().getCurrentLength();

      mediumAccessor.write(writeReference, dataToWrite);

      Assert.assertEquals(0, dataToWrite.remaining());
      Assert.assertEquals(mediumLengthBeforeWrite, mediumAccessor.getMedium().getCurrentLength());

      assertSameDataWrittenIsReadAgain(mediumAccessor, writeReference, dataToWrite);
      // Ensure the medium did not change before the written range
      assertMediumDidNotChangeInRange(mediumAccessor, createReference(mediumAccessor.getMedium(), 0), writeReference);
      // Ensure the medium did not change after the written range
      assertMediumDidNotChangeInRange(mediumAccessor, writeReference.advance(dataToWrite.capacity()),
         createReference(mediumAccessor.getMedium(), mediumAccessor.getMedium().getCurrentLength()));
   }

   /**
    * Tests the {@link IMediumAccessor#write(IMediumReference, ByteBuffer)}.
    */
   @Test
   public void write_endOfWriteAfterEndOfFile_overwritesWithExpectedBytesExtendsFileAndLeavesOtherBytesUnchanged() {
      IMediumAccessor<?> mediumAccessor = getImplementationToTest();

      int startBeforeEndOfMedium = 5;
      IMediumReference writeReference = createReference(mediumAccessor.getMedium(),
         EXPECTED_FILE_CONTENTS.length - startBeforeEndOfMedium);

      ByteBuffer dataToWrite = ByteBuffer
         .wrap(new byte[] { 'T', 'E', 'S', 'T', ' ', 'B', 'U', 'F', ' ', '1', '0', '0', '0', '0', '0', '3' });

      long extendedLength = dataToWrite.capacity() - startBeforeEndOfMedium;

      long mediumLengthBeforeWrite = mediumAccessor.getMedium().getCurrentLength();

      mediumAccessor.write(writeReference, dataToWrite);

      Assert.assertEquals(0, dataToWrite.remaining());

      Assert.assertEquals(mediumLengthBeforeWrite + extendedLength, mediumAccessor.getMedium().getCurrentLength());

      assertSameDataWrittenIsReadAgain(mediumAccessor, writeReference, dataToWrite);
      // Ensure the medium did not change before the written range
      assertMediumDidNotChangeInRange(mediumAccessor, createReference(mediumAccessor.getMedium(), 0), writeReference);
   }

   /**
    * Tests the {@link IMediumAccessor#write(IMediumReference, ByteBuffer)}.
    */
   @Test(expected = PreconditionUnfullfilledException.class)
   public void write_onClosedMediumAccessor_throwsException() {
      IMediumAccessor<?> mediumAccessor = getImplementationToTest();

      mediumAccessor.close();

      mediumAccessor.write(createReference(mediumAccessor.getMedium(), 0), ByteBuffer.allocate(5));
   }

   /**
    * @see de.je.jmeta.media.impl.mediumAccessor.AbstractIMediumAccessorTest#validateTestMedium(de.je.jmeta.media.api.IMedium)
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

      ByteBuffer reread = performReadNoEOFExpected(mediumAccessor,
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

      ByteBuffer bytesRead = performReadNoEOFExpected(mediumAccessor,
         new ReadTestData((int) rangeStartReference.getAbsoluteMediumOffset(), sizeToRead));

      Assert.assertEquals(bytesExpected, bytesRead);
   }
}
