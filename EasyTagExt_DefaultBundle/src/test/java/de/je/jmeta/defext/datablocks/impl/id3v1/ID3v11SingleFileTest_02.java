/**
 *
 * {@link ID3v11SingleFileTest_02}.java
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
 * {@link ID3v11SingleFileTest_02} tests reading a single ID3v1 tag.
 */
public class ID3v11SingleFileTest_02
   extends AbstractDataBlockAccessorDefaultExtensionTest {

   /**
    * Creates a new {@link ID3v11SingleFileTest_02}.
    */
   public ID3v11SingleFileTest_02() {
      super(THE_FILE, THE_CSV_FILE, new Integer[] { 4 });
   }

   private final static Path THE_FILE = TestResourceHelper.resourceToFile(
      AbstractDataBlockAccessorDefaultExtensionTest.class,
      "id3v1/ID3v11_FILE_02.txt");

   private final static Path THE_CSV_FILE = TestResourceHelper.resourceToFile(
      AbstractDataBlockAccessorDefaultExtensionTest.class,
      "id3v1/Expected_ID3v11_FILE_02.csv");
}
