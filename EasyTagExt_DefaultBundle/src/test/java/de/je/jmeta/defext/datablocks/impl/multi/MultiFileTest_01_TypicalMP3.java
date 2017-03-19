/**
 *
 * {@link MultiFileTest_01_TypicalMP3}.java
 *
 * @author Jens Ebert
 *
 * @date 02.06.2011
 */
package de.je.jmeta.defext.datablocks.impl.multi;

import java.io.File;

import de.je.jmeta.defext.datablocks.impl.AbstractDataBlockAccessorDefaultExtensionTest;
import de.je.util.javautil.testUtil.resource.TestResourceHelper;

/**
 * {@link MultiFileTest_01_TypicalMP3} tests reading a typical MP3 file with ID3v2.3 tag at the beginning, some MP3
 * frames in the middle and APEv2, Lyrics3v2 and ID3v1 tags at the end.
 */
public class MultiFileTest_01_TypicalMP3
   extends AbstractDataBlockAccessorDefaultExtensionTest {

   /**
    * Creates a new {@link MultiFileTest_01_TypicalMP3}.
    */
   public MultiFileTest_01_TypicalMP3() {
      super(THE_FILE, THE_CSV_FILE, new Integer[] { 15, 30 });
   }

   private final static File THE_FILE = TestResourceHelper.resourceToFile(
      AbstractDataBlockAccessorDefaultExtensionTest.class,
      "multi/Multi_FILE_01_TypicalMP3.txt");

   private final static File THE_CSV_FILE = TestResourceHelper.resourceToFile(
      AbstractDataBlockAccessorDefaultExtensionTest.class,
      "multi/Expected_Multi_FILE_01_TypicalMP3.csv");
}
