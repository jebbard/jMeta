/**
 *
 * {@link ResultsWriter}.java
 *
 * @author Jens Ebert
 *
 * @date 03.05.2010
 *
 */

package com.github.jmeta.tools.fileaccessperformance.api.services;

import java.io.File;
import java.io.IOException;

import com.github.jmeta.tools.benchmark.api.services.MeasurementSession;
import com.github.jmeta.tools.benchmark.api.types.MeasuredCommand;
import com.github.jmeta.tools.benchmark.api.types.MeasuredCommandExecution;
import com.github.jmeta.utility.charset.api.services.Charsets;
import com.github.jmeta.utility.csv.api.services.CsvWriter;
import com.github.jmeta.utility.namedio.api.services.NamedWriter;

/**
 * {@link ResultsWriter} writes the performance test results to an external file.
 */
public class ResultsWriter {

   private CsvWriter m_csvWriter;

   private static final String OS_PLATFORM_INFO = System.getProperty("os.name") + " " + System.getProperty("os.version")
      + " on " + System.getProperty("os.arch");

   private static final String JAVA_VERSION_INFO = System.getProperty("java.version") + " by "
      + System.getProperty("java.vendor") + " at " + System.getProperty("java.home");

   private static final String CSV_FILE_NAME = "results.csv";

   private static final Object[] FILE_HEADING = new String[] { "Measurement session name", "Session id", "Machine",
      "Java version", "OS and platform", "Time of execution start", "Time of execution end", "Total duration",
      "Duration unit", "Accessor name", "Orig. file path", "Orig. file name", "Orig. file size [byte]",
      "No. of bytes read", "No. of bytes written", "No. of unchanged bytes at EOF" };

   /**
    * Creates a new {@link ResultsWriter}.
    *
    * @param resultDir
    *           the directory the performance test results are stored.
    * @throws IOException
    *            if creation of the result file failed.
    */
   public ResultsWriter(File resultDir) throws IOException {
      final File csvFile = new File(resultDir, CSV_FILE_NAME);

      final boolean appendMode = csvFile.exists();

      if (!appendMode) {
         if (!csvFile.createNewFile())
            throw new RuntimeException("Unable to create CSV file <" + csvFile + ">");
      }

      m_csvWriter = new CsvWriter(FILE_HEADING.length);

      m_csvWriter.setNewResource(NamedWriter.createFromFile(csvFile, Charsets.CHARSET_ASCII, appendMode));

      // Write header only for newly created files
      if (!appendMode)
         m_csvWriter.writeNextRow(FILE_HEADING);
   }

   /**
    * Writes the results of a single measurement to the results file as a data record.
    *
    * @param command
    *           The {@link MeasuredCommand}
    * @param testFile
    *           The test {@link File}
    * @param bytesToRead
    *           The number of bytes read
    * @param bytesToWrite
    *           The number of bytes written
    * @param bytesAtEnd
    *           The number of bytes at EOF
    * @param session
    *           The {@link MeasurementSession}
    * @param execution
    *           TODO
    * @param machineString
    *           The string identifying the machine.
    * @throws IOException
    *            in case writing fails
    */
   public void writeResults(MeasuredCommand command, File testFile, long bytesToRead, long bytesToWrite,
      long bytesAtEnd, MeasurementSession session, MeasuredCommandExecution execution, String machineString)
      throws IOException {

      Object[] record = new Object[] { session.getClass().getSimpleName(), session.getSessionId(), machineString,
         JAVA_VERSION_INFO, OS_PLATFORM_INFO, execution.getStartTime(), execution.getStopTime(),
         execution.getStopTime() - execution.getStartTime(), session.getTimeProvider().getUnit(),
         command.getUniqueName(), testFile.getParentFile().getAbsolutePath(), testFile.getName(), testFile.length(),
         bytesToRead, bytesToWrite, bytesAtEnd, };

      m_csvWriter.writeNextRow(record);
   }

   /**
    * Closes this {@link ResultsWriter}.
    *
    * @throws IOException
    *            if closing was not successful
    */
   public void close() throws IOException {

      m_csvWriter.closeCurrentCsvResource();
   }

   /**
    * Returns the path of the {@link File} the results are written to.
    *
    * @return the path of the {@link File} the results are written to.
    */
   public String getFilePath() {

      return m_csvWriter.getCurrentCsvResource().getName();
   }
}
