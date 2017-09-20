/**
 *
 * {@link OggSingleFileTest_01}.java
 *
 * @author Jens Ebert
 *
 * @date 02.06.2011
 */
package de.je.jmeta.defext.datablocks.impl.mp3;

import java.io.File;

import de.je.jmeta.defext.datablocks.impl.AbstractDataBlockAccessorDefaultExtensionTest;
import de.je.jmeta.defext.datablocks.impl.ogg.OggSingleFileTest_01;
import de.je.util.javautil.testUtil.resource.TestResourceHelper;

/**
 * {@link MP3SingleFileTest_01} tests reading a single MP3 file.
 */
// TODO mp3: Add test cases for CRC and padding
public class MP3SingleFileTest_01
   extends AbstractDataBlockAccessorDefaultExtensionTest {

   /**
    * Creates a new {@link MP3SingleFileTest_01}.
    */
   public MP3SingleFileTest_01() {
      super(THE_FILE, THE_CSV_FILE, new Integer[] { 4 });
   }

   private final static File THE_FILE = TestResourceHelper.resourceToFile(
      AbstractDataBlockAccessorDefaultExtensionTest.class,
      "mp3/MP3_FILE_01.txt");

   private final static File THE_CSV_FILE = TestResourceHelper.resourceToFile(
      AbstractDataBlockAccessorDefaultExtensionTest.class,
      "mp3/Expected_MP3_FILE_01.csv");
}
