/**
 *
 * {@link ID3v23SingleFileTest_05_UnknownFrame}.java
 *
 * @author Jens Ebert
 *
 * @date 02.06.2011
 */
package de.je.jmeta.defext.datablocks.impl.id3v23;

import java.nio.file.Path;

import de.je.jmeta.defext.datablocks.impl.AbstractDataBlockAccessorDefaultExtensionTest;
import de.je.util.javautil.testUtil.resource.TestResourceHelper;

/**
 * {@link ID3v23SingleFileTest_05_UnknownFrame} tests reading a single ID3v23 tag with an unspecified ID3v23 frame.
 */
public class ID3v23SingleFileTest_05_UnknownFrame extends AbstractDataBlockAccessorDefaultExtensionTest {

   /**
    * Creates a new {@link ID3v23SingleFileTest_05_UnknownFrame}.
    */
   public ID3v23SingleFileTest_05_UnknownFrame() {
      super(THE_FILE, THE_CSV_FILE, new Integer[] { 15, 30 });
   }

   private final static Path THE_FILE = TestResourceHelper
      .resourceToFile(AbstractDataBlockAccessorDefaultExtensionTest.class, "id3v23/ID3v23_FILE_05_UnknownFrame.txt");

   private final static Path THE_CSV_FILE = TestResourceHelper.resourceToFile(
      AbstractDataBlockAccessorDefaultExtensionTest.class, "id3v23/Expected_ID3v23_FILE_05_UnknownFrame.csv");
}
