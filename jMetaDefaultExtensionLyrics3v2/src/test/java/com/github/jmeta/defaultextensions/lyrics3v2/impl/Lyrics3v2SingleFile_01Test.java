/**
 *
 * {@link MultiFileTest_01_TypicalMP3}.java
 *
 * @author Jens Ebert
 *
 * @date 02.06.2011
 */
package com.github.jmeta.defaultextensions.lyrics3v2.impl;

import java.nio.file.Path;

import com.github.jmeta.library.datablocks.api.services.AbstractDataBlockAccessorTest;
import com.github.jmeta.utility.testsetup.api.services.TestResourceHelper;

/**
 * {@link Lyrics3v2SingleFile_01Test} tests reading a single Lyrics3v2 tag with header and footer.
 */
public class Lyrics3v2SingleFile_01Test extends AbstractDataBlockAccessorTest {

   /**
    * Creates a new {@link Lyrics3v2SingleFile_01Test}.
    */
   public Lyrics3v2SingleFile_01Test() {
      super(THE_FILE, THE_CSV_FILE, new Integer[] { 15, 30 });
   }

   private final static Path THE_FILE = TestResourceHelper.resourceToFile(Lyrics3v2SingleFile_01Test.class,
      "Lyrics3v2_FILE_01.txt");

   private final static Path THE_CSV_FILE = TestResourceHelper.resourceToFile(Lyrics3v2SingleFile_01Test.class,
      "Expected_Lyrics3v2_FILE_01.csv");
}
