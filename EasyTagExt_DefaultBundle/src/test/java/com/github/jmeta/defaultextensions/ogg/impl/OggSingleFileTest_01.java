/**
 *
 * {@link OggSingleFileTest_01}.java
 *
 * @author Jens Ebert
 *
 * @date 02.06.2011
 */
package com.github.jmeta.defaultextensions.ogg.impl;

import java.nio.file.Path;

import com.github.jmeta.defaultextensions.AbstractDataBlockAccessorDefaultExtensionTest;
import com.github.jmeta.utility.testsetup.api.services.TestResourceHelper;

/**
 * {@link OggSingleFileTest_01} tests reading a single ogg file.
 */
public class OggSingleFileTest_01 extends AbstractDataBlockAccessorDefaultExtensionTest {

   /**
    * Creates a new {@link OggSingleFileTest_01}.
    */
   public OggSingleFileTest_01() {
      super(THE_FILE, THE_CSV_FILE, new Integer[] { 4 });
   }

   private final static Path THE_FILE = TestResourceHelper.resourceToFile(OggSingleFileTest_01.class,
      "OGG_FILE_01.txt");

   private final static Path THE_CSV_FILE = TestResourceHelper.resourceToFile(OggSingleFileTest_01.class,
      "Expected_OGG_FILE_01.csv");
}
