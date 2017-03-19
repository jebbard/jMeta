/**
 *
 * {@link OggSingleFileTest_01}.java
 *
 * @author Jens Ebert
 *
 * @date 02.06.2011
 */
package de.je.jmeta.defext.datablocks.impl.ogg;

import java.io.File;

import de.je.jmeta.defext.datablocks.impl.AbstractDataBlockAccessorDefaultExtensionTest;
import de.je.util.javautil.testUtil.resource.TestResourceHelper;

/**
 * {@link OggSingleFileTest_02} tests reading a single ogg file.
 */
public class OggSingleFileTest_02
   extends AbstractDataBlockAccessorDefaultExtensionTest {

   /**
    * Creates a new {@link OggSingleFileTest_02}.
    */
   public OggSingleFileTest_02() {
      super(THE_FILE, THE_CSV_FILE, new Integer[] { 4 });
   }

   private final static File THE_FILE = TestResourceHelper.resourceToFile(
      AbstractDataBlockAccessorDefaultExtensionTest.class,
      "ogg/OGG_FILE_02.txt");

   private final static File THE_CSV_FILE = TestResourceHelper.resourceToFile(
      AbstractDataBlockAccessorDefaultExtensionTest.class,
      "ogg/Expected_OGG_FILE_02.csv");
}
