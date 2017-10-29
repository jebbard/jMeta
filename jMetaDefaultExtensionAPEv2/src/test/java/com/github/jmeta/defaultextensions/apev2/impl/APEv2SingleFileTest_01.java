/**
 *
 * {@link APEv2SingleFileTest_01}.java
 *
 * @author Jens Ebert
 *
 * @date 02.06.2011
 */
package com.github.jmeta.defaultextensions.apev2.impl;

import java.nio.file.Path;

import com.github.jmeta.library.datablocks.api.services.AbstractDataBlockAccessorTest;
import com.github.jmeta.utility.testsetup.api.services.TestResourceHelper;

/**
 * {@link APEv2SingleFileTest_01} tests reading a single APEv2 tag with header and footer.
 */
public class APEv2SingleFileTest_01 extends AbstractDataBlockAccessorTest {

   /**
    * Creates a new {@link APEv2SingleFileTest_01}.
    */
   public APEv2SingleFileTest_01() {
      super(THE_FILE, THE_CSV_FILE, new Integer[] { 15, 30 });
   }

   private final static Path THE_FILE = TestResourceHelper.resourceToFile(APEv2SingleFileTest_01.class,
      "APEv2_FILE_01.txt");

   private final static Path THE_CSV_FILE = TestResourceHelper.resourceToFile(APEv2SingleFileTest_01.class,
      "Expected_APEv2_FILE_01.csv");
}
