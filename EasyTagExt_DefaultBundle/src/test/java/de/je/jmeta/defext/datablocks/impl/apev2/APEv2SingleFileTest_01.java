/**
 *
 * {@link APEv2SingleFileTest_01}.java
 *
 * @author Jens Ebert
 *
 * @date 02.06.2011
 */
package de.je.jmeta.defext.datablocks.impl.apev2;

import java.nio.file.Path;

import de.je.jmeta.defext.datablocks.impl.AbstractDataBlockAccessorDefaultExtensionTest;
import de.je.util.javautil.testUtil.resource.TestResourceHelper;

/**
 * {@link APEv2SingleFileTest_01} tests reading a single APEv2 tag with header and footer.
 */
public class APEv2SingleFileTest_01 extends AbstractDataBlockAccessorDefaultExtensionTest {

   /**
    * Creates a new {@link APEv2SingleFileTest_01}.
    */
   public APEv2SingleFileTest_01() {
      super(THE_FILE, THE_CSV_FILE, new Integer[] { 15, 30 });
   }

   private final static Path THE_FILE = TestResourceHelper
      .resourceToFile(AbstractDataBlockAccessorDefaultExtensionTest.class, "apev2/APEv2_FILE_01.txt");

   private final static Path THE_CSV_FILE = TestResourceHelper
      .resourceToFile(AbstractDataBlockAccessorDefaultExtensionTest.class, "apev2/Expected_APEv2_FILE_01.csv");
}
