/**
 *
 * {@link MultiFileTest_01_TypicalMP3}.java
 *
 * @author Jens Ebert
 *
 * @date 02.06.2011
 */
package de.je.jmeta.defext.datablocks.impl.lyrics3v2;

import java.nio.file.Path;

import de.je.jmeta.defext.datablocks.impl.AbstractDataBlockAccessorDefaultExtensionTest;
import de.je.jmeta.defext.datablocks.impl.multi.MultiFileTest_01_TypicalMP3;
import de.je.util.javautil.testUtil.resource.TestResourceHelper;

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

   private final static Path THE_FILE = TestResourceHelper
      .resourceToFile(AbstractDataBlockAccessorDefaultExtensionTest.class, "lyrics3v2/Lyrics3v2_FILE_01.txt");

   private final static Path THE_CSV_FILE = TestResourceHelper
      .resourceToFile(AbstractDataBlockAccessorDefaultExtensionTest.class, "lyrics3v2/Expected_Lyrics3v2_FILE_01.csv");
}
