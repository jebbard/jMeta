/**
 *
 * {@link ID3v23SingleFileTest_03_NoPadding}.java
 *
 * @author Jens Ebert
 *
 * @date 02.06.2011
 */
package de.je.jmeta.defext.datablocks.impl.id3v23;

import java.io.File;

import de.je.jmeta.defext.datablocks.impl.AbstractDataBlockAccessorDefaultExtensionTest;
import de.je.util.javautil.testUtil.resource.TestResourceHelper;

/**
 * {@link ID3v23SingleFileTest_03_NoPadding} tests reading a single ID3v23 tag with a single padding byte.
 */
public class ID3v23SingleFileTest_03_NoPadding
   extends AbstractDataBlockAccessorDefaultExtensionTest {

   /**
    * Creates a new {@link ID3v23SingleFileTest_03_NoPadding}.
    */
   public ID3v23SingleFileTest_03_NoPadding() {
      super(THE_FILE, THE_CSV_FILE, new Integer[] { 15, 30 });
   }

   private final static File THE_FILE = TestResourceHelper.resourceToFile(
      AbstractDataBlockAccessorDefaultExtensionTest.class,
      "id3v23/ID3v23_FILE_03_NoPadding.txt");

   private final static File THE_CSV_FILE = TestResourceHelper.resourceToFile(
      AbstractDataBlockAccessorDefaultExtensionTest.class,
      "id3v23/Expected_ID3v23_FILE_03_NoPadding.csv");
}
