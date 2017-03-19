/**
 * {@link StreamingMediumAccessorTest}.java
 *
 * @author Jens Ebert
 * @date 09.12.10 21:22:53 (December 9, 2010)
 */

package de.je.jmeta.media.impl;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import de.je.jmeta.media.api.MediaTestCaseConstants;
import de.je.jmeta.media.api.datatype.InputStreamMedium;

/**
 * Tests the class {@StreamMediumAccessor}.
 */
public class StreamingMediumAccessorTest extends IMediumAccessorTest {

   private StreamMediumAccessor m_testling;

   private InputStream m_testStream;

   private Map<Integer, Integer> m_readOffsetsAndSizes;

   /**
    * @see IMediumAccessorTest#cleanUpMediumData()
    */
   @Override
   protected void cleanUpMediumData() {

      // Nothing necessary here
   }

   /**
    * @see IMediumAccessorTest#getFileOffsetsToCheckReading()
    */
   @Override
   protected Map<Integer, Integer> getFileOffsetsToCheckReading() {

      if (m_readOffsetsAndSizes == null)
         m_readOffsetsAndSizes = new HashMap<>();

      return m_readOffsetsAndSizes;
   }

   /**
    * @see IMediumAccessorTest#getReadOnlyTestling()
    */
   @Override
   protected IMediumAccessor<?> getReadOnlyTestling() {

      return getTestling();
   }

   /**
    * @see IMediumAccessorTest#getTestling()
    */
   @Override
   protected IMediumAccessor<?> getTestling() {

      if (m_testling == null) {
         m_testling = new StreamMediumAccessor(
            new InputStreamMedium(m_testStream, "My_Stream"));
      }

      return m_testling;
   }

   /**
    * @see IMediumAccessorTest#prepareMediumData()
    */
   @Override
   protected void prepareMediumData() {

      try {
         m_testStream = new FileInputStream(
            MediaTestCaseConstants.STANDARD_TEST_FILE);
      } catch (FileNotFoundException e) {
         throw new RuntimeException(
            "Could not find test file. Make sure it exists"
               + "on the hard drive: "
               + MediaTestCaseConstants.STANDARD_TEST_FILE.getAbsolutePath(),
            e);
      }
   }
}