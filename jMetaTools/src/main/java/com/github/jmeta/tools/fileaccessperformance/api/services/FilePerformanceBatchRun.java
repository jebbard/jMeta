/**
 *
 * {@link FilePerformanceBatchRun}.java
 *
 * @author Jens Ebert
 *
 * @date 03.05.2010
 *
 */

package com.github.jmeta.tools.fileaccessperformance.api.services;

/**
 * {@link FilePerformanceBatchRun} executes a performance test for Java file I/O.
 */
public class FilePerformanceBatchRun {

   private static final String FILE_PATH = "data/performanceTest/files/";

   private static final String MACHINE_INFO = "Windows 7 home premium SP1 @AMD Phenom(tm) II X4 965@3.4GHz@8GB@7200UPM";

   private static final String DELETE_FILES_ON_EXIT = "true";

   private static final String SMALL_BLOCK_SIZE = "100";

   private static final String SMALL_MEDIUM_BLOCK_SIZE = "1000";

   private static final String MEDIUM_BLOCK_SIZE_1 = "10000";

   private static final String MEDIUM_BLOCK_SIZE_2 = "100000";

   private static final String MEDIUM_BLOCK_SIZE_3 = "300000";

   private static final String BIG_BLOCK_SIZE = "1000000";

   private static final String LARGE_BLOCK_SIZE = "3000000";

   private static final String HUGE_BLOCK_SIZE = "25000000";

   private static final String MEGA_BLOCK_SIZE = "125000000";

   /**
    * @param args
    *           The arguments - none in this case
    */
   public static void main(String[] args) {

      String[][] runs = new String[][] {
         // A: small read and write blocks
         new String[] { MACHINE_INFO, FILE_PATH + "01_SMALL.txt",
            SMALL_BLOCK_SIZE, SMALL_BLOCK_SIZE, SMALL_BLOCK_SIZE,
            DELETE_FILES_ON_EXIT },
         new String[] { MACHINE_INFO, FILE_PATH + "02_SMALL_MEDIUM.txt",
            SMALL_BLOCK_SIZE, SMALL_BLOCK_SIZE, SMALL_BLOCK_SIZE,
            DELETE_FILES_ON_EXIT },
         new String[] { MACHINE_INFO, FILE_PATH + "03_MEDIUM.txt",
            SMALL_BLOCK_SIZE, SMALL_BLOCK_SIZE, SMALL_BLOCK_SIZE,
            DELETE_FILES_ON_EXIT },
         new String[] { MACHINE_INFO, FILE_PATH + "04_BIG.txt",
            SMALL_BLOCK_SIZE, SMALL_BLOCK_SIZE, SMALL_BLOCK_SIZE,
            DELETE_FILES_ON_EXIT },
         new String[] { MACHINE_INFO, FILE_PATH + "05_LARGE.txt",
            SMALL_BLOCK_SIZE, SMALL_BLOCK_SIZE, SMALL_BLOCK_SIZE,
            DELETE_FILES_ON_EXIT },
         new String[] { MACHINE_INFO, FILE_PATH + "06_FUCKING_HUGE.txt",
            SMALL_BLOCK_SIZE, SMALL_BLOCK_SIZE, SMALL_BLOCK_SIZE,
            DELETE_FILES_ON_EXIT },
         // B: small medium read and write blocks
         new String[] { MACHINE_INFO, FILE_PATH + "01_SMALL.txt",
            SMALL_MEDIUM_BLOCK_SIZE, SMALL_MEDIUM_BLOCK_SIZE,
            SMALL_MEDIUM_BLOCK_SIZE, DELETE_FILES_ON_EXIT },
         new String[] { MACHINE_INFO, FILE_PATH + "02_SMALL_MEDIUM.txt",
            SMALL_MEDIUM_BLOCK_SIZE, SMALL_MEDIUM_BLOCK_SIZE,
            SMALL_MEDIUM_BLOCK_SIZE, DELETE_FILES_ON_EXIT },
         new String[] { MACHINE_INFO, FILE_PATH + "03_MEDIUM.txt",
            SMALL_MEDIUM_BLOCK_SIZE, SMALL_MEDIUM_BLOCK_SIZE,
            SMALL_MEDIUM_BLOCK_SIZE, DELETE_FILES_ON_EXIT },
         new String[] { MACHINE_INFO, FILE_PATH + "04_BIG.txt",
            SMALL_MEDIUM_BLOCK_SIZE, SMALL_MEDIUM_BLOCK_SIZE,
            SMALL_MEDIUM_BLOCK_SIZE, DELETE_FILES_ON_EXIT },
         new String[] { MACHINE_INFO, FILE_PATH + "05_LARGE.txt",
            SMALL_MEDIUM_BLOCK_SIZE, SMALL_MEDIUM_BLOCK_SIZE,
            SMALL_MEDIUM_BLOCK_SIZE, DELETE_FILES_ON_EXIT },
         new String[] { MACHINE_INFO, FILE_PATH + "06_FUCKING_HUGE.txt",
            SMALL_MEDIUM_BLOCK_SIZE, SMALL_MEDIUM_BLOCK_SIZE,
            SMALL_MEDIUM_BLOCK_SIZE, DELETE_FILES_ON_EXIT },
         // C: medium read and write blocks (1)
         new String[] { MACHINE_INFO, FILE_PATH + "01_SMALL.txt",
            MEDIUM_BLOCK_SIZE_1, MEDIUM_BLOCK_SIZE_1, MEDIUM_BLOCK_SIZE_1,
            DELETE_FILES_ON_EXIT },
         new String[] { MACHINE_INFO, FILE_PATH + "02_SMALL_MEDIUM.txt",
            MEDIUM_BLOCK_SIZE_1, MEDIUM_BLOCK_SIZE_1, MEDIUM_BLOCK_SIZE_1,
            DELETE_FILES_ON_EXIT },
         new String[] { MACHINE_INFO, FILE_PATH + "03_MEDIUM.txt",
            MEDIUM_BLOCK_SIZE_1, MEDIUM_BLOCK_SIZE_1, MEDIUM_BLOCK_SIZE_1,
            DELETE_FILES_ON_EXIT },
         new String[] { MACHINE_INFO, FILE_PATH + "04_BIG.txt",
            MEDIUM_BLOCK_SIZE_1, MEDIUM_BLOCK_SIZE_1, MEDIUM_BLOCK_SIZE_1,
            DELETE_FILES_ON_EXIT },
         new String[] { MACHINE_INFO, FILE_PATH + "05_LARGE.txt",
            MEDIUM_BLOCK_SIZE_1, MEDIUM_BLOCK_SIZE_1, MEDIUM_BLOCK_SIZE_1,
            DELETE_FILES_ON_EXIT },
         new String[] { MACHINE_INFO, FILE_PATH + "06_FUCKING_HUGE.txt",
            MEDIUM_BLOCK_SIZE_1, MEDIUM_BLOCK_SIZE_1, MEDIUM_BLOCK_SIZE_1,
            DELETE_FILES_ON_EXIT },
         // D: medium read and write blocks (2)
         new String[] { MACHINE_INFO, FILE_PATH + "01_SMALL.txt",
            MEDIUM_BLOCK_SIZE_2, MEDIUM_BLOCK_SIZE_2, MEDIUM_BLOCK_SIZE_2,
            DELETE_FILES_ON_EXIT },
         new String[] { MACHINE_INFO, FILE_PATH + "02_SMALL_MEDIUM.txt",
            MEDIUM_BLOCK_SIZE_2, MEDIUM_BLOCK_SIZE_2, MEDIUM_BLOCK_SIZE_2,
            DELETE_FILES_ON_EXIT },
         new String[] { MACHINE_INFO, FILE_PATH + "03_MEDIUM.txt",
            MEDIUM_BLOCK_SIZE_2, MEDIUM_BLOCK_SIZE_2, MEDIUM_BLOCK_SIZE_2,
            DELETE_FILES_ON_EXIT },
         new String[] { MACHINE_INFO, FILE_PATH + "04_BIG.txt",
            MEDIUM_BLOCK_SIZE_2, MEDIUM_BLOCK_SIZE_2, MEDIUM_BLOCK_SIZE_2,
            DELETE_FILES_ON_EXIT },
         new String[] { MACHINE_INFO, FILE_PATH + "05_LARGE.txt",
            MEDIUM_BLOCK_SIZE_2, MEDIUM_BLOCK_SIZE_2, MEDIUM_BLOCK_SIZE_2,
            DELETE_FILES_ON_EXIT },
         new String[] { MACHINE_INFO, FILE_PATH + "06_FUCKING_HUGE.txt",
            MEDIUM_BLOCK_SIZE_2, MEDIUM_BLOCK_SIZE_2, MEDIUM_BLOCK_SIZE_2,
            DELETE_FILES_ON_EXIT },
         // E: medium read and write blocks (3)
         new String[] { MACHINE_INFO, FILE_PATH + "01_SMALL.txt",
            MEDIUM_BLOCK_SIZE_3, MEDIUM_BLOCK_SIZE_3, MEDIUM_BLOCK_SIZE_3,
            DELETE_FILES_ON_EXIT },
         new String[] { MACHINE_INFO, FILE_PATH + "02_SMALL_MEDIUM.txt",
            MEDIUM_BLOCK_SIZE_3, MEDIUM_BLOCK_SIZE_3, MEDIUM_BLOCK_SIZE_3,
            DELETE_FILES_ON_EXIT },
         new String[] { MACHINE_INFO, FILE_PATH + "03_MEDIUM.txt",
            MEDIUM_BLOCK_SIZE_3, MEDIUM_BLOCK_SIZE_3, MEDIUM_BLOCK_SIZE_3,
            DELETE_FILES_ON_EXIT },
         new String[] { MACHINE_INFO, FILE_PATH + "04_BIG.txt",
            MEDIUM_BLOCK_SIZE_3, MEDIUM_BLOCK_SIZE_3, MEDIUM_BLOCK_SIZE_3,
            DELETE_FILES_ON_EXIT },
         new String[] { MACHINE_INFO, FILE_PATH + "05_LARGE.txt",
            MEDIUM_BLOCK_SIZE_3, MEDIUM_BLOCK_SIZE_3, MEDIUM_BLOCK_SIZE_3,
            DELETE_FILES_ON_EXIT },
         new String[] { MACHINE_INFO, FILE_PATH + "06_FUCKING_HUGE.txt",
            MEDIUM_BLOCK_SIZE_3, MEDIUM_BLOCK_SIZE_3, MEDIUM_BLOCK_SIZE_3,
            DELETE_FILES_ON_EXIT },
         // F: big read and write blocks
         new String[] { MACHINE_INFO, FILE_PATH + "02_SMALL_MEDIUM.txt",
            BIG_BLOCK_SIZE, BIG_BLOCK_SIZE, BIG_BLOCK_SIZE,
            DELETE_FILES_ON_EXIT },
         new String[] { MACHINE_INFO, FILE_PATH + "03_MEDIUM.txt",
            BIG_BLOCK_SIZE, BIG_BLOCK_SIZE, BIG_BLOCK_SIZE,
            DELETE_FILES_ON_EXIT },
         new String[] { MACHINE_INFO, FILE_PATH + "04_BIG.txt", BIG_BLOCK_SIZE,
            BIG_BLOCK_SIZE, BIG_BLOCK_SIZE, DELETE_FILES_ON_EXIT },
         new String[] { MACHINE_INFO, FILE_PATH + "05_LARGE.txt",
            BIG_BLOCK_SIZE, BIG_BLOCK_SIZE, BIG_BLOCK_SIZE,
            DELETE_FILES_ON_EXIT },
         new String[] { MACHINE_INFO, FILE_PATH + "06_FUCKING_HUGE.txt",
            BIG_BLOCK_SIZE, BIG_BLOCK_SIZE, BIG_BLOCK_SIZE,
            DELETE_FILES_ON_EXIT },
         // G: large read and write blocks
         new String[] { MACHINE_INFO, FILE_PATH + "04_BIG.txt",
            LARGE_BLOCK_SIZE, LARGE_BLOCK_SIZE, LARGE_BLOCK_SIZE,
            DELETE_FILES_ON_EXIT },
         new String[] { MACHINE_INFO, FILE_PATH + "05_LARGE.txt",
            LARGE_BLOCK_SIZE, LARGE_BLOCK_SIZE, LARGE_BLOCK_SIZE,
            DELETE_FILES_ON_EXIT },
         new String[] { MACHINE_INFO, FILE_PATH + "06_FUCKING_HUGE.txt",
            LARGE_BLOCK_SIZE, LARGE_BLOCK_SIZE, LARGE_BLOCK_SIZE,
            DELETE_FILES_ON_EXIT },
         // H: Huge read and write blocks
         new String[] { MACHINE_INFO, FILE_PATH + "05_LARGE.txt",
            HUGE_BLOCK_SIZE, HUGE_BLOCK_SIZE, HUGE_BLOCK_SIZE,
            DELETE_FILES_ON_EXIT },
         new String[] { MACHINE_INFO, FILE_PATH + "06_FUCKING_HUGE.txt",
            HUGE_BLOCK_SIZE, HUGE_BLOCK_SIZE, HUGE_BLOCK_SIZE,
            DELETE_FILES_ON_EXIT },
         // I: Mega block size
         new String[] { MACHINE_INFO, FILE_PATH + "06_FUCKING_HUGE.txt",
            MEGA_BLOCK_SIZE, MEGA_BLOCK_SIZE, MEGA_BLOCK_SIZE,
            DELETE_FILES_ON_EXIT }, };

      for (int i = 0; i < runs.length; i++) {
         FilePerformanceTest.main(runs[i]);
      }
   }
}
