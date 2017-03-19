/**
 *
 * {@link FilePerformanceTest}.java
 *
 * @author Jens Ebert
 *
 * @date 02.05.2010
 *
 */

package de.et.perf.file;

import java.io.File;

/**
 * {@link FilePerformanceTest} examines different ways to access files and tries to find out the best way for
 * performance.
 * 
 * See {@link FilePerformanceRunner} for details.
 */
public final class FilePerformanceTest {

   /**
    * Start the performance test from command line.
    * 
    * @param args
    *           The arguments. See {@link #usage()}.
    */
   public static void main(String[] args) {

      if (args.length != ARG_COUNT) {
         System.out.println("[ERROR] Invalid argument count!");
         System.out.println();
         usage();
         System.exit(-1);
      }

      String machineName = args[0];
      File testFile = new File(args[1]);
      int bytesToRead = Integer.parseInt(args[2]);
      int bytesToWrite = Integer.parseInt(args[3]);
      int bytesAtEnd = Integer.parseInt(args[4]);
      boolean deleteTempFiles = Boolean.parseBoolean(args[5]);

      FilePerformanceRunner runner = new FilePerformanceRunner(testFile,
         bytesToRead, bytesToWrite, bytesAtEnd, deleteTempFiles, machineName);

      runner.runPerformanceTest();
   }

   /**
    * Prints a usage description.
    */
   public static void usage() {

      System.out.println("Usage:");
      System.out.println(
         "FilePerformanceTest <machine> <test file> <bytes to read> <bytes to write> <delete files>"
            + "<bytes at end>");
      System.out.println(
         "\t<machine>       : String identifying the HW of the current machine."
            + "Must have a size greater than <bytes to read> + <bytes at end>");
      System.out
         .println("\t<test file>       : File to be taken for performance test."
            + "Must have a size greater than <bytes to read> + <bytes at end>");
      System.out.println(
         "\t<bytes to read>  : The number of bytes to read up to the point "
            + "where <bytes at end> bytes are still remaining within the file");
      System.out
         .println("\t<bytes to write> : The number of bytes to write after the "
            + "<bytes to read> and the bytes at end");
      System.out.println(
         "\t<bytes at end>   : The number of bytes remaining untouched "
            + "before the end of file");
      System.out.println(
         "\t<delete files>   : true to delete all produced temporary files, false to preserve them");
   }

   private static final int ARG_COUNT = 6;
}
