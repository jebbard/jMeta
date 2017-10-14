/**
 * {@link StreamMediumAccessorTest}.java
 *
 * @author Jens Ebert
 * @date 09.12.10 21:22:53 (December 9, 2010)
 */

package de.je.jmeta.media.impl.mediumAccessor;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import de.je.jmeta.media.api.datatype.InputStreamMedium;
import de.je.jmeta.media.api.helper.MediaTestCaseConstants;
import de.je.jmeta.media.impl.IMediumAccessor;
import de.je.jmeta.media.impl.StreamMediumAccessor;

/**
 * Tests the class {@StreamMediumAccessor}.
 */
public class StreamMediumAccessorTest extends AbstractReadOnlyMediumAccessorTest {

   private InputStream testStream;

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

      readOffsetsAndSizes.add(new ReadTestData(null, 7, 0));
      readOffsetsAndSizes.add(new ReadTestData(null, 157, 7));
      readOffsetsAndSizes.add(new ReadTestData(null, 133, 164));
      readOffsetsAndSizes.add(new ReadTestData(null, 17, 297));
      readOffsetsAndSizes.add(new ReadTestData(null, 45, 314));

      return readOffsetsAndSizes;
   }

   /**
    * @see de.je.jmeta.media.impl.mediumAccessor.AbstractIMediumAccessorTest#getReadTestDataUntilEndOfMedium()
    */
   @Override
   protected ReadTestData getReadTestDataUntilEndOfMedium() {
      return new ReadTestData(null, EXPECTED_FILE_CONTENTS.length);
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