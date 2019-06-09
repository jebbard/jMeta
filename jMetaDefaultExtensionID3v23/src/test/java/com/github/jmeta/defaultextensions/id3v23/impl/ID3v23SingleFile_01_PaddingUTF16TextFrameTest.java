/**
 *
 * {@link ID3v23SingleFile_01_PaddingUTF16TextFrameTest}.java
 *
 * @author Jens Ebert
 *
 * @date 02.06.2011
 */
package com.github.jmeta.defaultextensions.id3v23.impl;

import java.nio.file.Path;

import com.github.jmeta.library.datablocks.api.services.AbstractDataBlockAccessorTest;
import com.github.jmeta.utility.testsetup.api.services.TestResourceHelper;

/**
 * {@link ID3v23SingleFile_01_PaddingUTF16TextFrameTest} tests reading a single
 * ID3v23 tag that contains a usual amount of padding bytes and UTF-16 encoded
 * text frames.
 */
public class ID3v23SingleFile_01_PaddingUTF16TextFrameTest extends AbstractDataBlockAccessorTest {

	private final static Path THE_FILE = TestResourceHelper
		.resourceToFile(ID3v23SingleFile_01_PaddingUTF16TextFrameTest.class, "ID3v23_FILE_01_PaddingUTF16Chars.txt");

	private final static Path THE_CSV_FILE = TestResourceHelper.resourceToFile(
		ID3v23SingleFile_01_PaddingUTF16TextFrameTest.class, "Expected_ID3v23_FILE_01_PaddingUTF16Chars.csv");

	/**
	 * Creates a new {@link ID3v23SingleFile_01_PaddingUTF16TextFrameTest}.
	 */
	public ID3v23SingleFile_01_PaddingUTF16TextFrameTest() {
		super(ID3v23SingleFile_01_PaddingUTF16TextFrameTest.THE_FILE,
			ID3v23SingleFile_01_PaddingUTF16TextFrameTest.THE_CSV_FILE, new Integer[] { 15, 30 });
	}
}
