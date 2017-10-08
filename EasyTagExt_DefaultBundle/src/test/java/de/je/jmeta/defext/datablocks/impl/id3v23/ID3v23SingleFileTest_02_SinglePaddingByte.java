/**
 *
 * {@link ID3v23SingleFileTest_02_SinglePaddingByte}.java
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
 * {@link ID3v23SingleFileTest_02_SinglePaddingByte} tests reading a single ID3v23 tag with a single padding byte.
 */
public class ID3v23SingleFileTest_02_SinglePaddingByte extends AbstractDataBlockAccessorDefaultExtensionTest {

   /**
    * Creates a new {@link ID3v23SingleFileTest_02_SinglePaddingByte}.
    */
   public ID3v23SingleFileTest_02_SinglePaddingByte() {
      super(THE_FILE, THE_CSV_FILE, new Integer[] { 15, 30 });
   }

   private final static Path THE_FILE = TestResourceHelper.resourceToFile(
      AbstractDataBlockAccessorDefaultExtensionTest.class, "id3v23/ID3v23_FILE_02_SinglePaddingByte.txt");

   private final static Path THE_CSV_FILE = TestResourceHelper.resourceToFile(
      AbstractDataBlockAccessorDefaultExtensionTest.class, "id3v23/Expected_ID3v23_FILE_02_SinglePaddingByte.csv");
}
