/**
 *
 * {@link ID3v1SingleFileTest_02}.java
 *
 * @author Jens Ebert
 *
 * @date 02.06.2011
 */
package com.github.jmeta.defaultextensions.id3v1.impl;

import java.nio.file.Path;

import com.github.jmeta.library.datablocks.api.services.AbstractDataBlockAccessorTest;
import com.github.jmeta.utility.testsetup.api.services.TestResourceHelper;

/**
 * {@link ID3v1SingleFileTest_02} tests reading a single ID3v1 tag.
 */
public class ID3v1SingleFileTest_02 extends AbstractDataBlockAccessorTest {

   /**
    * Creates a new {@link ID3v1SingleFileTest_02}.
    */
   public ID3v1SingleFileTest_02() {
      super(THE_FILE, THE_CSV_FILE, new Integer[] { 4 });
   }

   private final static Path THE_FILE = TestResourceHelper.resourceToFile(ID3v1SingleFileTest_02.class,
      "ID3v1_FILE_02.txt");

   private final static Path THE_CSV_FILE = TestResourceHelper.resourceToFile(ID3v1SingleFileTest_02.class,
      "Expected_ID3v1_FILE_02.csv");
}