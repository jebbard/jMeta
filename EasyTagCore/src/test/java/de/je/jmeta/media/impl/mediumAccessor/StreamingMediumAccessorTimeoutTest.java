/**
 *
 * {@link StreamingMediumAccessorTimeoutTest}.java
 *
 * @author Jens Ebert
 *
 * @date 25.04.2011
 */
package de.je.jmeta.media.impl.mediumAccessor;

import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.junit.Test;

import de.je.jmeta.media.api.datatype.InputStreamMedium;
import de.je.jmeta.media.api.exception.EndOfMediumException;
import de.je.jmeta.media.api.exception.ReadTimedOutException;
import de.je.jmeta.media.impl.StandardMediumReference;
import de.je.jmeta.media.impl.StreamMediumAccessor;
import de.je.jmeta.media.impl.mediumAccessor.BlockedInputStreamSimulator.WriteMode;
import junit.framework.Assert;

/**
 * {@link StreamingMediumAccessorTimeoutTest} tests the {@link StreamMediumAccessor} class regarding timeout aspects.
 * 
 * As this test case is based on a multithreaded helper class, some test cases might occassionally fail due to lack of
 * timeout accuracy, e.g. due to currently high processor load or a slow computer used. Then the accuracy should be
 * adapted in the variable {@link #TIMEOUT_TOLERANCE_MILLIS}.
 */
public class StreamingMediumAccessorTimeoutTest {

   /**
    * Tolerance of the measured timeout within the test case (+/-). If the difference to the specified timeout is
    * bigger, then the test cases will fail.
    */
   public static final int TIMEOUT_TOLERANCE_MILLIS = 45;

   private final static Map<Integer, byte[]> THE_TIMEOUTS = new HashMap<>();

   static {
      final byte[] bytes = new byte[] { 'T', 'h', 'e', ' ', 'W', 'i', 't', 'c', 'h', 'e', 'r', ' ', '2', ' ', '-', ' ',
         'n', 'e', 'x', 't', ' ', 'g', 'a', 'm', 'e', ' ', 'e', 'x', 'p', 'e', 'r', 'i', 'e', 'n', 'c', 'e' };
      THE_TIMEOUTS.put(1, bytes);
      THE_TIMEOUTS.put(100, bytes);
      THE_TIMEOUTS.put(200, bytes);
      THE_TIMEOUTS.put(1000, bytes);
      THE_TIMEOUTS.put(2000, bytes);
      THE_TIMEOUTS.put(10000, bytes);
   }

   /**
    * Checks the accuracy of an expired timeout (with a given tolerance).
    * 
    * @param timeoutMillis
    *           The expected timeout to have expired.
    * @param startTime
    *           The system time immediately before the read call.
    * @param endTime
    *           The system time immediately after the read call.
    */
   private void checkTimeoutAccuracy(Integer timeoutMillis, long startTime, long endTime) {

      final long diffTime = endTime - startTime;

      System.out.println("Calculated difference time in milliseconds before and after read call " + diffTime);
      // The passed time should closely match the specified timeout
      Assert.assertTrue(diffTime + TIMEOUT_TOLERANCE_MILLIS > timeoutMillis);
      Assert.assertTrue(diffTime - TIMEOUT_TOLERANCE_MILLIS < timeoutMillis);
   }

   /**
    * Tests {@link StreamMediumAccessor#read} with several timeout values and for the case that the given timeout does
    * not expire, because read does provide all the data in time.
    * 
    * Data is written to the {@link InputStream} at once, and not piece-wise (white-box knowledge).
    */
   @Test
   public void test_readAllDataWithinTimeout_atOnce() {

      System.out.println();
      System.out.println("######################################");
      System.out.println(" test_readAllDataWithinTimeout_atOnce ");
      System.out.println("######################################");

      for (Iterator<Integer> iterator = THE_TIMEOUTS.keySet().iterator(); iterator.hasNext();) {
         Integer timeoutMillis = iterator.next();
         byte[] bytesExpectedToBeRead = THE_TIMEOUTS.get(timeoutMillis);

         System.out.println("Next iteration with timeout " + timeoutMillis);

         // Create simulator and schedule thread for starting the simulation
         // soon
         // There are no bytes written, so the timeout is expected to expire
         BlockedInputStreamSimulator simulator = new BlockedInputStreamSimulator(bytesExpectedToBeRead, timeoutMillis,
            WriteMode.WM_AT_ONCE);

         // Prepare testling
         InputStreamMedium medium = new InputStreamMedium(simulator.getInputStream(), null);
         StreamMediumAccessor testling = new StreamMediumAccessor(medium);

         medium.setReadTimeout(timeoutMillis);

         // The actual test
         StandardMediumReference nullReference = new StandardMediumReference(testling.getMedium(), 0);

         ByteBuffer readBuffer = ByteBuffer.allocate(bytesExpectedToBeRead.length);

         try {
            testling.read(readBuffer);

            // Data has been read correctly
            org.junit.Assert.assertArrayEquals(bytesExpectedToBeRead, readBuffer.array());
         } catch (EndOfMediumException e) {
            throw new RuntimeException("Unexpected end of medium", e);
         }
         // This exception is NOT expected!
         catch (ReadTimedOutException e) {
            Assert.fail("read was expected to NOT throw a ReadTimedOutException, but threw: " + e);
         }
      }
   }

   /**
    * Tests {@link StreamMediumAccessor#read} with several timeout values and for the case that the given timeout does
    * not expire, because read does provide all the data in time.
    * 
    * Data is written to the {@link InputStream} piece-wise with short sleep intervals (white-box knowledge).
    */
   @Test
   public void test_readAllDataWithinTimeout_pieceWise() {

      System.out.println();
      System.out.println("#########################################");
      System.out.println(" test_readAllDataWithinTimeout_pieceWise ");
      System.out.println("#########################################");

      for (Iterator<Integer> iterator = THE_TIMEOUTS.keySet().iterator(); iterator.hasNext();) {
         Integer timeoutMillis = iterator.next();
         byte[] bytesExpectedToBeRead = THE_TIMEOUTS.get(timeoutMillis);

         System.out.println("Next iteration with timeout " + timeoutMillis);

         // Create simulator and schedule thread for starting the simulation
         // soon
         // There are no bytes written, so the timeout is expected to expire
         BlockedInputStreamSimulator simulator = new BlockedInputStreamSimulator(bytesExpectedToBeRead, timeoutMillis,
            WriteMode.WM_BEFORE_TIMEOUT);

         // Prepare testling
         InputStreamMedium medium = new InputStreamMedium(simulator.getInputStream(), null);
         StreamMediumAccessor testling = new StreamMediumAccessor(medium);

         medium.setReadTimeout(timeoutMillis);

         // The actual test
         StandardMediumReference nullReference = new StandardMediumReference(testling.getMedium(), 0);

         ByteBuffer readBuffer = ByteBuffer.allocate(bytesExpectedToBeRead.length);

         try {
            testling.read(readBuffer);

            // Data has been read correctly
            org.junit.Assert.assertArrayEquals(bytesExpectedToBeRead, readBuffer.array());
         } catch (EndOfMediumException e) {
            throw new RuntimeException("Unexpected end of medium", e);
         }
         // This exception is NOT expected!
         catch (ReadTimedOutException e) {
            Assert.fail("read was expected to NOT throw a ReadTimedOutException, but threw: " + e);
         }
      }
   }

   /**
    * Tests {@link StreamMediumAccessor#read} with several timeout values and for the case that the timeout expires
    * before all data requested in the read call could be read from the {@link InputStream}, i.e. it is only partly read
    * up to the expired timeout.
    * 
    * Data is written to the {@link InputStream} piece-wise with short sleep intervals (white-box knowledge).
    */
   @Test
   public void test_timedOutReadWithData_pieceWise() {

      System.out.println();
      System.out.println("#####################################");
      System.out.println(" test_timedOutReadWithData_pieceWise ");
      System.out.println("#####################################");

      for (Iterator<Integer> iterator = THE_TIMEOUTS.keySet().iterator(); iterator.hasNext();) {
         Integer timeoutMillis = iterator.next();
         byte[] bytesExpectedToBeRead = THE_TIMEOUTS.get(timeoutMillis);

         System.out.println("Next iteration with timeout " + timeoutMillis);

         // Create simulator and schedule thread for starting the simulation
         // soon
         // There are no bytes written, so the timeout is expected to expire
         BlockedInputStreamSimulator simulator = new BlockedInputStreamSimulator(bytesExpectedToBeRead, timeoutMillis,
            WriteMode.WM_EXPIRE_TIMEOUT);

         // Prepare testling
         InputStreamMedium medium = new InputStreamMedium(simulator.getInputStream(), null);
         StreamMediumAccessor testling = new StreamMediumAccessor(medium);

         medium.setReadTimeout(timeoutMillis);

         // The actual test
         StandardMediumReference nullReference = new StandardMediumReference(testling.getMedium(), 0);

         ByteBuffer readBuffer = ByteBuffer.allocate(bytesExpectedToBeRead.length);

         long startTime = System.currentTimeMillis();

         try {
            testling.read(readBuffer);

            Assert.fail("read was expected to throw a ReadTimedOutException");
         } catch (EndOfMediumException e) {
            throw new RuntimeException("Unexpected end of medium", e);
         }
         // This exception is expected!
         catch (ReadTimedOutException e) {
            Assert.assertEquals(timeoutMillis.intValue(), e.getTimeoutValue());

            Assert.assertEquals(bytesExpectedToBeRead.length, e.getByteCountTriedToRead());

            // Nevertheless some bytes were read
            Assert.assertTrue(e.getBytesReallyRead() > 0);
            Assert.assertTrue(readBuffer.remaining() > 0);

            int index = 0;
            while (readBuffer.hasRemaining()) {
               Assert.assertEquals(bytesExpectedToBeRead[index++], readBuffer.get());
            }
         }

         long endTime = System.currentTimeMillis();

         checkTimeoutAccuracy(timeoutMillis, startTime, endTime);
      }
   }

   /**
    * Tests {@link StreamMediumAccessor#read} with several timeout values and for the case that the timeout expires and
    * no data at all is read from the {@link InputStream}.
    */
   @Test
   public void test_timedOutReadWithoutData() {

      System.out.println();
      System.out.println("##############################");
      System.out.println(" test_timedOutReadWithoutData ");
      System.out.println("##############################");

      for (Iterator<Integer> iterator = THE_TIMEOUTS.keySet().iterator(); iterator.hasNext();) {
         Integer timeoutMillis = iterator.next();
         byte[] bytesExpectedToBeRead = THE_TIMEOUTS.get(timeoutMillis);

         System.out.println("Next iteration with timeout " + timeoutMillis);

         // Create simulator and schedule thread for starting the simulation
         // soon
         // There are no bytes written, so the timeout is expected to expire
         BlockedInputStreamSimulator simulator = new BlockedInputStreamSimulator(new byte[] {}, timeoutMillis,
            WriteMode.WM_EXPIRE_TIMEOUT);

         // Prepare testling
         InputStreamMedium medium = new InputStreamMedium(simulator.getInputStream(), null);
         StreamMediumAccessor testling = new StreamMediumAccessor(medium);

         medium.setReadTimeout(timeoutMillis);

         // The actual test
         StandardMediumReference nullReference = new StandardMediumReference(testling.getMedium(), 0);

         ByteBuffer readBuffer = ByteBuffer.allocate(bytesExpectedToBeRead.length);

         long startTime = System.currentTimeMillis();

         try {
            testling.read(readBuffer);

            Assert.fail("read was expected to throw a ReadTimedOutException");
         } catch (EndOfMediumException e) {
            throw new RuntimeException("Unexpected end of medium", e);
         }
         // This exception is expected!
         catch (ReadTimedOutException e) {
            Assert.assertEquals(timeoutMillis.intValue(), e.getTimeoutValue());
            Assert.assertEquals(bytesExpectedToBeRead.length, e.getByteCountTriedToRead());
            Assert.assertEquals(0, e.getBytesReallyRead());

            // Nothing has been read
            Assert.assertEquals(0, readBuffer.remaining());
         }

         long endTime = System.currentTimeMillis();

         checkTimeoutAccuracy(timeoutMillis, startTime, endTime);
      }
   }
}
