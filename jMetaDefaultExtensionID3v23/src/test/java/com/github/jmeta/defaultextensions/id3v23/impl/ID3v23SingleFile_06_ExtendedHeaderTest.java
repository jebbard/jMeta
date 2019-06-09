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
 * {@link ID3v23SingleFile_06_ExtendedHeaderTest} tests reading a single ID3v23
 * tag that contains a usual amount of padding bytes and an extended header.
 */
public class ID3v23SingleFile_06_ExtendedHeaderTest extends AbstractDataBlockAccessorTest {

	private final static Path THE_FILE = TestResourceHelper.resourceToFile(ID3v23SingleFile_06_ExtendedHeaderTest.class,
		"ID3v23_FILE_06_ExtendedHeader.txt");

	private final static Path THE_CSV_FILE = TestResourceHelper
		.resourceToFile(ID3v23SingleFile_06_ExtendedHeaderTest.class, "Expected_ID3v23_FILE_06_ExtendedHeader.csv");

	/**
	 * Creates a new {@link ID3v23SingleFile_06_ExtendedHeaderTest}.
	 */
	public ID3v23SingleFile_06_ExtendedHeaderTest() {
		super(ID3v23SingleFile_06_ExtendedHeaderTest.THE_FILE, ID3v23SingleFile_06_ExtendedHeaderTest.THE_CSV_FILE,
			new Integer[] { 15, 30 });
	}
}
