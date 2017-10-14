/**
 * {@link StreamMediumAccessorTest}.java
 *
 * @author Jens Ebert
 * @date 09.12.10 21:22:53 (December 9, 2010)
 */

package de.je.jmeta.media.impl.mediumAccessor;

import static de.je.jmeta.media.api.helper.TestMediumUtility.createReference;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import de.je.jmeta.media.api.IMediumReference;
import de.je.jmeta.media.api.datatype.InputStreamMedium;
import de.je.jmeta.media.api.helper.MediaTestCaseConstants;
import de.je.jmeta.media.impl.IMediumAccessor;
import de.je.jmeta.media.impl.StreamMediumAccessor;
import junit.framework.Assert;

/**
 * Tests the class {@StreamMediumAccessor}.
 */
public class StreamMediumAccessorTest extends AbstractReadOnlyMediumAccessorTest {

   private InputStream testStream;

   /**
    * Tests {@link IMediumAccessor#setCurrentPosition(IMediumReference)} and
    * {@link IMediumAccessor#getCurrentPosition()}.
    */
   @Test
   public void setCurrentPosition_onStreamMedium_doesNotChangeCurrentPosition() {

      IMediumAccessor<?> mediumAccessor = getImplementationToTest();

      int newOffsetOne = 20;
      IMediumReference changeReferenceOne = createReference(mediumAccessor.getMedium(), newOffsetOne);

      mediumAccessor.setCurrentPosition(changeReferenceOne);

      Assert.assertEquals(0, mediumAccessor.getCurrentPosition().getAbsoluteMediumOffset());

      int newOffsetTwo = 10;
      IMediumReference changeReferenceTwo = createReference(mediumAccessor.getMedium(), newOffsetTwo);

      mediumAccessor.setCurrentPosition(changeReferenceTwo);

      Assert.assertEquals(0, mediumAccessor.getCurrentPosition().getAbsoluteMediumOffset());
   }

   @Test
   public void read_beforeEOMWithTimeoutIfFinishedBeforeTimeout_returnsAllDataAsExpected() {
      // TODO implement
   }

   @Test
   public void read_overEndOfMediumWithTimeoutIfFinishedBeforeTimeout_throwsEndOfMediumException() {
      // TODO implement
   }

   @Test
   public void read_beforeEndOfMediumWithTimeoutTimeOverButOnlyPartialDataRead_throwsExceptionTerminatesThreadAndReturnsPartialData() {
      // TODO implement
   }

   @Test
   public void read_beforeEndOfMediumWithTimeoutTimeOverNoDataRead_throwsExceptionTerminatesThreadAndReturnsNoData() {
      // TODO implement
   }

   /**
    * @see AbstractIMediumAccessorTest#getReadTestDataToUse()
    */
   @Override
   protected List<ReadTestData> getReadTestDataToUse() {

      List<ReadTestData> readOffsetsAndSizes = new ArrayList<>();

      readOffsetsAndSizes.add(new ReadTestData(5, 7, 0));
      readOffsetsAndSizes.add(new ReadTestData(1, 157, 7));
      readOffsetsAndSizes.add(new ReadTestData(100, 133, 164));
      readOffsetsAndSizes.add(new ReadTestData(0, 17, 297));
      readOffsetsAndSizes.add(new ReadTestData(88, 45, 314));

      return readOffsetsAndSizes;
   }

   /**
    * @see de.je.jmeta.media.impl.mediumAccessor.AbstractIMediumAccessorTest#getReadTestDataUntilEndOfMedium()
    */
   @Override
   protected ReadTestData getReadTestDataUntilEndOfMedium() {
      return new ReadTestData(0, EXPECTED_FILE_CONTENTS.length);
   }

   /**
    * @see AbstractIMediumAccessorTest#getImplementationToTest()
    */
   @Override
   protected IMediumAccessor<?> createImplementationToTest() {
      return new StreamMediumAccessor(getExpectedMedium());
   }

   /**
    * @see de.je.jmeta.media.impl.mediumAccessor.AbstractIMediumAccessorTest#getExpectedMedium()
    */
   @Override
   protected InputStreamMedium getExpectedMedium() {
      return new InputStreamMedium(testStream, "My_Stream");
   }

   /**
    * @see AbstractIMediumAccessorTest#prepareMediumData(byte[])
    */
   @Override
   protected void prepareMediumData(byte[] testFileContents) {

      try {
         testStream = new FileInputStream(MediaTestCaseConstants.STANDARD_TEST_FILE.toFile());
      } catch (FileNotFoundException e) {
         throw new RuntimeException("Could not find test file. Make sure it exists" + "on the hard drive: "
            + MediaTestCaseConstants.STANDARD_TEST_FILE.toAbsolutePath().toString(), e);
      }
   }
}