/**
 * {@link MemoryMediumAccessorTest}.java
 *
 * @author Jens Ebert
 * @date 09.12.10 21:22:53 (December 9, 2010)
 */

package de.je.jmeta.media.impl;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.HashMap;
import java.util.Map;

import de.je.jmeta.media.api.MediaTestCaseConstants;
import de.je.jmeta.media.api.datatype.InMemoryMedium;

/**
 * Tests the class {@MemoryMediumAccessor}.
 */
public class MemoryMediumAccessorTest extends IMediumAccessorTest {

   private AbstractMediumAccessor<?> m_testling;

   private AbstractMediumAccessor<?> m_readOnlyTestling;

   private byte[] m_memory = new byte[10000];

   private RandomAccessFile m_reader;

   private FileChannel m_channel;

   private Map<Integer, Integer> m_readOffsetsAndSizes;

   /**
    * @see IMediumAccessorTest#cleanUpMediumData()
    */
   @Override
   protected void cleanUpMediumData() {

      if (m_reader != null)
         try {
            if (m_channel != null)
               m_channel.close();

            m_reader.close();

            m_reader = null;
            m_channel = null;
         } catch (IOException e) {
            e.printStackTrace();
         }

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
         m_readOnlyTestling = new MemoryMediumAccessor(
            new InMemoryMedium(m_memory, null, true));
      }

      return m_readOnlyTestling;
   }

   /**
    * @see IMediumAccessorTest#getTestling()
    */
   @Override
   protected IMediumAccessor<?> getTestling() {

      if (m_testling == null) {
         m_testling = new MemoryMediumAccessor(
            new InMemoryMedium(m_memory, null, true));
      }

      return m_testling;
   }

   /**
    * @see IMediumAccessorTest#prepareMediumData()
    */
   @Override
   protected void prepareMediumData() {

      try {
         m_reader = new RandomAccessFile(
            MediaTestCaseConstants.STANDARD_TEST_FILE, "r");

         ByteBuffer bb = ByteBuffer.wrap(m_memory);

         m_channel = m_reader.getChannel();
         m_channel.read(bb);
      }

      catch (FileNotFoundException e) {
         throw new RuntimeException(
            "Could not find test file. Make sure it exists"
               + "on the hard drive: "
               + MediaTestCaseConstants.STANDARD_TEST_FILE.getAbsolutePath(),
            e);
      }

      catch (IOException e) {
         throw new RuntimeException(
            "Could not read from test file: "
               + MediaTestCaseConstants.STANDARD_TEST_FILE.getAbsolutePath(),
            e);
      }
   }
}