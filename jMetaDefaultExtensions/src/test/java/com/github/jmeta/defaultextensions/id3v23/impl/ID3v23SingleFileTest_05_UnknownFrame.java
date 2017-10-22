/**
 *
 * {@link ID3v23SingleFileTest_05_UnknownFrame}.java
 *
 * @author Jens Ebert
 *
 * @date 02.06.2011
 */
package com.github.jmeta.defaultextensions.id3v23.impl;

import java.nio.file.Path;

import com.github.jmeta.defaultextensions.AbstractDataBlockAccessorDefaultExtensionTest;
import com.github.jmeta.utility.testsetup.api.services.TestResourceHelper;

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

   private final static Path THE_FILE = TestResourceHelper.resourceToFile(ID3v23SingleFileTest_05_UnknownFrame.class,
      "ID3v23_FILE_05_UnknownFrame.txt");

   private final static Path THE_CSV_FILE = TestResourceHelper
      .resourceToFile(ID3v23SingleFileTest_05_UnknownFrame.class, "Expected_ID3v23_FILE_05_UnknownFrame.csv");
}