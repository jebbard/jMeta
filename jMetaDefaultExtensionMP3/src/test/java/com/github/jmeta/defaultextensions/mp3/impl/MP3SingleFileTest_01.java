/**
 *
 * {@link OggSingleFileTest_01}.java
 *
 * @author Jens Ebert
 *
 * @date 02.06.2011
 */
package com.github.jmeta.defaultextensions.mp3.impl;

import java.nio.file.Path;

import com.github.jmeta.library.datablocks.api.services.AbstractDataBlockAccessorTest;
import com.github.jmeta.utility.testsetup.api.services.TestResourceHelper;

/**
 * {@link MP3SingleFileTest_01} tests reading a single MP3 file.
 */
// TODO mp3: Add test cases for CRC and padding
public class MP3SingleFileTest_01 extends AbstractDataBlockAccessorTest {

   /**
    * Creates a new {@link MP3SingleFileTest_01}.
    */
   public MP3SingleFileTest_01() {
      super(THE_FILE, THE_CSV_FILE, new Integer[] { 4 });
   }

   private final static Path THE_FILE = TestResourceHelper.resourceToFile(MP3SingleFileTest_01.class,
      "MP3_FILE_01.txt");

   private final static Path THE_CSV_FILE = TestResourceHelper.resourceToFile(MP3SingleFileTest_01.class,
      "Expected_MP3_FILE_01.csv");
}
