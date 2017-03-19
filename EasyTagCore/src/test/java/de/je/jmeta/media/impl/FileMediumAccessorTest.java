/**
 * {@link FileMediumAccessorTest}.java
 *
 * @author Jens Ebert
 * @date 09.12.10 21:22:53 (December 9, 2010)
 */

package de.je.jmeta.media.impl;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import de.je.jmeta.media.api.MediaTestCaseConstants;
import de.je.jmeta.media.api.datatype.FileMedium;
import de.je.jmeta.media.api.exception.MediumAccessException;
import de.je.util.javautil.io.file.FileUtility;

/**
 * Tests the class {@FileMediumAccessor}.
 */
public class FileMediumAccessorTest extends IMediumAccessorTest {

   private static final String TEMP_FOLDER_NAME = "temp/";

   private static int TEST_FILE_COUNTER = 0;

   private File m_copiedTestFile = new File(MediaTestCaseConstants.TEST_FILE_OUTPUT_DIR + TEMP_FOLDER_NAME
      + MediaTestCaseConstants.STANDARD_TEST_FILE_RESOURCE);

   private Map<Integer, Integer> m_readOffsetsAndSizes;

   private FileMediumAccessor m_testling;

   private FileMediumAccessor m_readOnlyTestling;

   /**
    * @see IMediumAccessorTest#cleanUpMediumData()
    */
   @Override
   protected void cleanUpMediumData() {

      // Do nothing as deletion is done only when starting a new test run
      // This ensures that the written data can be looked at after test
      // execution
   }

   /**
    * @see IMediumAccessorTest#getFileOffsetsToCheckReading()
    */
   @Override
   protected Map<Integer, Integer> getFileOffsetsToCheckReading() {

      if (m_readOffsetsAndSizes == null) {
         m_readOffsetsAndSizes = new HashMap<>();

         m_readOffsetsAndSizes.put(16, 7);
         m_readOffsetsAndSizes.put(93, 157);
         m_readOffsetsAndSizes.put(610, 133);
         m_readOffsetsAndSizes.put(0, 17);
         m_readOffsetsAndSizes.put(211, 45);
      }

      return m_readOffsetsAndSizes;
   }

   /**
    * @see IMediumAccessorTest#getReadOnlyTestling()
    */
   @Override
   protected IMediumAccessor<?> getReadOnlyTestling() {

      if (m_readOnlyTestling == null) {
         m_readOnlyTestling = new FileMediumAccessor(new FileMedium(MediaTestCaseConstants.STANDARD_TEST_FILE, true));
      }

      return m_readOnlyTestling;
   }

   /**
    * @see IMediumAccessorTest#getTestling()
    */
   @Override
   protected IMediumAccessor<?> getTestling() {

      if (m_testling == null) {
         m_testling = new FileMediumAccessor(new FileMedium(m_copiedTestFile, false));
      }

      return m_testling;
   }

   /**
    * @see IMediumAccessorTest#prepareMediumData()
    */
   @Override
   protected void prepareMediumData() {

      m_copiedTestFile = new File(MediaTestCaseConstants.TEST_FILE_OUTPUT_DIR,
         TEMP_FOLDER_NAME + TEST_FILE_COUNTER + '_' + MediaTestCaseConstants.STANDARD_TEST_FILE_RESOURCE);

      TEST_FILE_COUNTER++;

      // If the copied file still exists (from a recent test run), delete it
      if (m_copiedTestFile.exists())
         if (!m_copiedTestFile.delete())
            throw new RuntimeException(
               "Could not delete copied test file " + m_copiedTestFile + ". Manuel deletion is necessary.");

      final String message = "Could not copy the test file " + MediaTestCaseConstants.STANDARD_TEST_FILE_RESOURCE
         + " to " + m_copiedTestFile;
      try {
         if (!FileUtility.copyFile(MediaTestCaseConstants.STANDARD_TEST_FILE, m_copiedTestFile))
            throw new RuntimeException(message);
      } catch (IOException e) {
         throw new RuntimeException(message, e);
      }
   }

   /**
    * Tests whether the creation of a new {@link FileMediumAccessor} on an already locked medium throws an exception.
    * 
    * Will probably only succeed on Windows platforms.
    */
   @SuppressWarnings("unused")
   @Test(expected = MediumAccessException.class)
   public void test_lockAlreadyLockedMedium() {

      // Make sure the first instance is created
      getTestling();

      // Create second instance on the same medium
      new FileMediumAccessor(new FileMedium(m_copiedTestFile, false));
   }

   /**
    * Tests {@link FileMediumAccessor#read} and {@link FileMediumAccessor#write} for reacting with an exception to a
    * concurrent change by another (faked) process.
    */
   @Test
   public void test_manipulationByOtherProcess() {

      /*
       * TODO media004: Run this test case on Linux or MAC, make it generic so it can always be uncommented, e.g. using
       * system property os.name. Test case cannot be implemented on Windows as setLastModified will always return false
       * and therefore fail to change the last modified date. This is maybe because the file is locked by the
       * IMediumAccessor.
       */

      // IMediumAccessor accessor = getTestling();
      //
      // ByteBuffer readBuffer = ByteBuffer.allocate(20);
      //
      // try {
      // final AbstractMedium<?> medium = accessor.getMedium();
      // accessor.read(new StandardMediumReference(medium, 10), readBuffer);
      //
      // File theFile = (File) medium.getMedium();
      //
      // // Sleep to make sure the newly set last change time stamp differs slightly
      // Thread.sleep(200);
      //
      // // Simulate access to the file by another process
      // theFile.setLastModified(System.currentTimeMillis());
      //
      // // The same read works, although the changed time has been modified
      // accessor.read(new StandardMediumReference(medium, 10), readBuffer);
      //
      // // Sleep to make sure the newly set last change time stamp differs slightly
      // Thread.sleep(200);
      //
      // // Simulate access to the file by another process
      // theFile.setLastModified(System.currentTimeMillis());
      //
      // // First write works
      // accessor.write(new StandardMediumReference(medium, 10), readBuffer);
      // }
      //
      // catch (EndOfMediumException e) {
      // Assert.fail("Unexpected EndOfMedium");
      // } catch (InterruptedException e) {
      // Assert.fail("Unexpected thread interruption");
      // }
   }

}