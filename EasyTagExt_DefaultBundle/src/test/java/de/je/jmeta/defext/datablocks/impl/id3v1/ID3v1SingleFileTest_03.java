/**
 *
 * {@link ID3v1SingleFileTest_03}.java
 *
 * @author Jens Ebert
 *
 * @date 02.06.2011
 */
package de.je.jmeta.defext.datablocks.impl.id3v1;

import java.nio.file.Path;

import de.je.jmeta.defext.datablocks.impl.AbstractDataBlockAccessorDefaultExtensionTest;
import de.je.util.javautil.testUtil.resource.TestResourceHelper;

/**
 * {@link ID3v1SingleFileTest_03} tests reading a single ID3v1 tag.
 */
public class ID3v1SingleFileTest_03
   extends AbstractDataBlockAccessorDefaultExtensionTest {

   /**
    * Creates a new {@link ID3v1SingleFileTest_03}.
    */
   public ID3v1SingleFileTest_03() {
      super(THE_FILE, THE_CSV_FILE, new Integer[] { 4 });
   }

   private final static Path THE_FILE = TestResourceHelper.resourceToFile(
      AbstractDataBlockAccessorDefaultExtensionTest.class,
      "id3v1/ID3v1_FILE_03.txt");

   private final static Path THE_CSV_FILE = TestResourceHelper.resourceToFile(
      AbstractDataBlockAccessorDefaultExtensionTest.class,
      "id3v1/Expected_ID3v1_FILE_03.csv");
}
