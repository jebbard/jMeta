package de.je.jmeta.media.impl.timeout;

import java.io.IOException;
import java.io.InputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;

import de.je.jmeta.media.impl.IMediumAccessor;
import de.je.util.javautil.common.err.Reject;

/**
 * {@link BlockedInputStreamSimulator} simulates a blocked {@link InputStream} using {@link PipedInputStream} and
 * {@link PipedOutputStream}. This class is used for testing the {@link IMediumAccessor} timed-out read facility.
 */
public class BlockedInputStreamSimulator {

   private static final String UNEXPECTED_THREAD_INTERRUPTION = "Unexpected thread interruption";

   private static final String UNEXPECTED_IO_EXCEPTION = "Unexpected IO exception";

   private final PipedInputStream m_inputStream;

   /**
    * {@link WriterThread} is an inner class that runs the thread that writes the data.
    */
   private class WriterThread implements Runnable, AutoCloseable {

      /**
       * @see java.lang.AutoCloseable#close()
       */
      @Override
      public void close() throws Exception {

         m_outputStream.close();
      }

      private static final int WRITE_COUNT = 5;

      private final byte[] m_data;

      private final int m_sleepTime;

      private final int m_byteCountWrittenAtOnce;

      private final PipedOutputStream m_outputStream;

      /**
       * Creates a new {@WriterThread}.
       * 
       * @param outputStream
       *           The {@link PipedOutputStream} to use for writing.
       * @param data
       *           The bytes to be written to the output stream, they get piped to the input stream.
       * @param timeoutMillis
       *           The timeout value used in the test in milliseconds.
       * @param writeMode
       *           The {@link WriteMode} to use.
       */
      public WriterThread(PipedOutputStream outputStream, byte[] data,
         int timeoutMillis, WriteMode writeMode) {

         Reject.ifNull(data, "data");
         Reject.ifNull(writeMode, "writeMode");
         Reject.ifNull(outputStream, "outputStream");
         Reject.ifNegativeOrZero(timeoutMillis, "timeoutMillis");

         m_data = data;
         m_outputStream = outputStream;

         switch (writeMode) {
            // Data is written at once, no timeout
            case WM_AT_ONCE:
               m_sleepTime = 0;
               m_byteCountWrittenAtOnce = data.length;
               break;

            // Data is written piecewise with the calculated timeout between
            // each write.
            // The -(timeoutMillis / 5) ensures that the timeout is not fully
            // reached, so the write completes
            // before the timeout expires
            case WM_BEFORE_TIMEOUT:
               m_sleepTime = (timeoutMillis - (timeoutMillis / 5))
                  / WRITE_COUNT;
               m_byteCountWrittenAtOnce = data.length / WRITE_COUNT;
               break;

            // Data is written piecewise with the calculated timeout between
            // each write.
            // The -(timeoutMillis / 5) ensures that the timeout is not fully
            // reached, so the write completes
            // before the timeout expires
            case WM_EXPIRE_TIMEOUT:
               m_sleepTime = (timeoutMillis / WRITE_COUNT) + 300;
               m_byteCountWrittenAtOnce = data.length / WRITE_COUNT;
               break;

            default:
               m_sleepTime = 0;
               m_byteCountWrittenAtOnce = data.length;
               break;
         }
      }

      /**
       * @see java.lang.Runnable#run()
       */
      @Override
      public void run() {

         int bytesWritten = 0;

         while (bytesWritten < m_data.length) {
            try {
               final int bytesLeftToBeWritten = m_data.length - bytesWritten;
               m_outputStream.write(m_data, bytesWritten,
                  (m_byteCountWrittenAtOnce < bytesLeftToBeWritten
                     ? m_byteCountWrittenAtOnce : bytesLeftToBeWritten));

               bytesWritten += m_byteCountWrittenAtOnce;
            } catch (IOException e) {
               throw new RuntimeException(UNEXPECTED_IO_EXCEPTION, e);
            }

            try {
               Thread.sleep(m_sleepTime);
            } catch (InterruptedException e) {
               throw new RuntimeException(UNEXPECTED_THREAD_INTERRUPTION, e);
            }
         }
      }
   }

   /**
    * Creates a new {@BlockedInputStreamSimulator} .
    * 
    * @param data
    *           The bytes to be written to the output stream, they get piped to the input stream.
    * @param timeoutMillis
    *           The timeout value used in the test in milliseconds.
    * @param writeMode
    *           The {@link WriteMode} to use.
    */
   @SuppressWarnings("resource")
   public BlockedInputStreamSimulator(byte[] data, int timeoutMillis,
      WriteMode writeMode) {

      Reject.ifNull(data, "data");
      Reject.ifNegativeOrZero(timeoutMillis, "timeoutMillis");

      m_inputStream = new PipedInputStream();

      try {

         WriterThread thread = new WriterThread(
            new PipedOutputStream(m_inputStream), data, timeoutMillis,
            writeMode);

         Thread runner = new Thread(thread);

         runner.start();

         // Make it very possible that the thread really starts after invocation
         // of this
         // method
         try {
            Thread.sleep(250);
         } catch (InterruptedException e) {
            throw new RuntimeException(UNEXPECTED_THREAD_INTERRUPTION, e);
         }
      } catch (Exception e) {
         throw new RuntimeException(UNEXPECTED_IO_EXCEPTION, e);
      }
   }

   /**
    * Returns the {@link InputStream} for testing.
    * 
    * @return the {@link InputStream} for testing.
    */
   public InputStream getInputStream() {

      return m_inputStream;
   }
}
