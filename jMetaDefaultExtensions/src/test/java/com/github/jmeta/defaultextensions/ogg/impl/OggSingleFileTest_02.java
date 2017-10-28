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

import com.github.jmeta.library.datablocks.api.services.AbstractDataBlockAccessorTest;
import com.github.jmeta.utility.testsetup.api.services.TestResourceHelper;

/**
 * {@link OggSingleFileTest_02} tests reading a single ogg file.
 */
public class OggSingleFileTest_02 extends AbstractDataBlockAccessorTest {

   /**
    * Creates a new {@link OggSingleFileTest_02}.
    */
   public OggSingleFileTest_02() {
      super(THE_FILE, THE_CSV_FILE, new Integer[] { 4 });
   }

   private final static Path THE_FILE = TestResourceHelper.resourceToFile(OggSingleFileTest_02.class,
      "OGG_FILE_02.txt");

   private final static Path THE_CSV_FILE = TestResourceHelper.resourceToFile(OggSingleFileTest_02.class,
      "Expected_OGG_FILE_02.csv");
}
