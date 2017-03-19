/**
 *
 * {@link ID3v1SingleFileTest_01}.java
 *
 * @author Jens Ebert
 *
 * @date 02.06.2011
 */
package de.je.jmeta.defext.datablocks.impl.id3v1;

import java.io.File;

import de.je.jmeta.defext.datablocks.impl.AbstractDataBlockAccessorDefaultExtensionTest;
import de.je.util.javautil.testUtil.resource.TestResourceHelper;

/**
 * {@link ID3v1SingleFileTest_01} tests reading a single ID3v1 tag.
 */
public class ID3v1SingleFileTest_01
   extends AbstractDataBlockAccessorDefaultExtensionTest {

   /**
    * Creates a new {@link ID3v1SingleFileTest_01}.
    */
   public ID3v1SingleFileTest_01() {
      super(THE_FILE, THE_CSV_FILE, new Integer[] { 4 });
   }

   private final static File THE_FILE = TestResourceHelper.resourceToFile(
      AbstractDataBlockAccessorDefaultExtensionTest.class,
      "id3v1/ID3v1_FILE_01.txt");

   private final static File THE_CSV_FILE = TestResourceHelper.resourceToFile(
      AbstractDataBlockAccessorDefaultExtensionTest.class,
      "id3v1/Expected_ID3v1_FILE_01.csv");
}
