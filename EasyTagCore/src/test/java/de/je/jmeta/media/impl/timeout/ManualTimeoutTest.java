/**
 *
 * {@link ManualTimeoutTest}.java
 *
 * @author Jens Ebert
 *
 * @date 24.04.2011
 */
package de.je.jmeta.media.impl.timeout;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;

import de.je.jmeta.media.api.datatype.InputStreamMedium;
import de.je.jmeta.media.api.exception.EndOfMediumException;
import de.je.jmeta.media.api.exception.ReadTimedOutException;
import de.je.jmeta.media.impl.IMediumAccessor;
import de.je.jmeta.media.impl.StandardMediumReference;
import de.je.jmeta.media.impl.StreamMediumAccessor;
import de.je.util.javautil.common.err.Reject;

/**
 * {@link ManualTimeoutTest} is a manual test for proofing that the timeout facility works, based on {@link System#in}.
 * After this class has been started, the timeout value in milliseconds can be specified. Then, the test as such starts,
 * querying further input from the user (e.g. 10 bytes) until the timeout has expired. If the user types the requested
 * number of bytes in {@link System#in} before the timeout expires, the user input is printed again, and the loop with
 * the timed out read starts again. Otherwise, the loop is terminated, printing the message that the timeout has expired
 * before the user has provided the requested number of bytes.
 */
public class ManualTimeoutTest {

   private final IMediumAccessor<?> m_theAccessor;

   private final int m_timeoutMillis;

   private ManualTimeoutTest(InputStream stream, int timeoutMillis,
      int byteCountToProvide) {

	  Reject.ifNegativeOrZero(timeoutMillis, "timeoutMillis");
	  Reject.ifNegativeOrZero(byteCountToProvide,
         "byteCountToProvide");
      Reject.ifFalse(byteCountToProvide <= 50,
         "byteCountToProvide <= 50");

      m_timeoutMillis = timeoutMillis;

      InputStreamMedium medium = new InputStreamMedium(stream, null);
      m_theAccessor = new StreamMediumAccessor(medium);

      medium.setReadTimeout(m_timeoutMillis);
   }

   /**
    * Starts the test program.
    * 
    * @param args
    *           No arguments
    */
   public static void main(String[] args) {

      System.out
         .println("########################################################");
      System.out
         .println(" Timeout manual test for IMediumAccessor implementation ");
      System.out
         .println("########################################################");

      ByteBuffer bytesRead = null;

      byte[] streamBuffer = new byte[50];

      for (;;) {
         long startTime = 0;

         try {
            System.out.println(
               "Specify the number of milliseconds for the timeout (must be > 0): ");

            int timeoutMillis = readIntFromInputStream(System.in, streamBuffer);
            System.out.println(
               "Specify the number of bytes to be read by the read call (must be in interval 0 <= x <= 50): ");

            int byteCountToProvide = readIntFromInputStream(System.in,
               streamBuffer);

            ManualTimeoutTest test = new ManualTimeoutTest(System.in,
               timeoutMillis, byteCountToProvide);

            startTime = System.currentTimeMillis();

            System.out.println(
               "Type your input now. Either you provide " + byteCountToProvide
                  + " characters (includes end of line characters) or you let expire the timeout of "
                  + timeoutMillis / 1000.0f + " seconds:");

            bytesRead = ByteBuffer.allocate(byteCountToProvide);

            test.readInput(bytesRead);

            System.out.println(byteCountToProvide
               + " characters have been entered before the timeout expired.");
         } catch (IOException e) {
            throw new RuntimeException("Unexpected IO exception", e);
         } catch (EndOfMediumException e) {
            throw new RuntimeException("Unexpected end of medium", e);
         } catch (ReadTimedOutException e) {
            throw new RuntimeException(
               "Read timed out before enough characters had been specified.",
               e);
         }

         try {
            long endTime = System.currentTimeMillis();

            System.out.println("This loop took "
               + (endTime - startTime) / 1000.0f + " seconds.");

            final byte[] byteArray = new byte[bytesRead.remaining()];

            bytesRead.get(byteArray);

            System.out.println("The following bytes have been entered: "
               + new String(byteArray, "ASCII"));

            System.out.println("Shall the program be terminated (y/n)?");

            if (System.in.read() == 'y')
               break;
         } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("Unexpected exception", e);
         } catch (IOException e) {
            throw new RuntimeException("Unexpected IO exception", e);
         }
      }

      System.out.println("#########################");
      System.out.println(" Test program shuts down ");
      System.out.println("#########################");
   }

   private static int readIntFromInputStream(InputStream is, byte[] buffer)
      throws IOException {

      int byteCount = is.read(buffer);

      // A string without whitespace
      String theString = new String(buffer, 0, byteCount, "ASCII").trim();

      return Integer.parseInt(theString);
   }

   private void readInput(ByteBuffer buffer) throws EndOfMediumException {

      m_theAccessor.read(
         new StandardMediumReference(m_theAccessor.getMedium(), 0), buffer);
   }
}
