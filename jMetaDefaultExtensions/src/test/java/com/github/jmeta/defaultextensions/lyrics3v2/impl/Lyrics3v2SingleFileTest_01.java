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

import com.github.jmeta.defaultextensions.AbstractDataBlockAccessorDefaultExtensionTest;
import com.github.jmeta.defaultextensions.multi.impl.MultiFileTest_01_TypicalMP3;
import com.github.jmeta.utility.testsetup.api.services.TestResourceHelper;

/**
 * {@link Lyrics3v2SingleFileTest_01} tests reading a single Lyrics3v2 tag with header and footer.
 */
public class Lyrics3v2SingleFileTest_01 extends AbstractDataBlockAccessorDefaultExtensionTest {

   /**
    * Creates a new {@link Lyrics3v2SingleFileTest_01}.
    */
   public Lyrics3v2SingleFileTest_01() {
      super(THE_FILE, THE_CSV_FILE, new Integer[] { 15, 30 });
   }

   private final static Path THE_FILE = TestResourceHelper.resourceToFile(Lyrics3v2SingleFileTest_01.class,
      "Lyrics3v2_FILE_01.txt");

   private final static Path THE_CSV_FILE = TestResourceHelper.resourceToFile(Lyrics3v2SingleFileTest_01.class,
      "Expected_Lyrics3v2_FILE_01.csv");
}
