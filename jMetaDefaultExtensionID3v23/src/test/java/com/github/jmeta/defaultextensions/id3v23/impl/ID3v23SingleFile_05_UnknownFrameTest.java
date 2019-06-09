/**
 *
 * {@link ID3v23SingleFile_05_UnknownFrameTest}.java
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
 * {@link ID3v23SingleFile_05_UnknownFrameTest} tests reading a single ID3v23
 * tag with an unspecified ID3v23 frame.
 */
public class ID3v23SingleFile_05_UnknownFrameTest extends AbstractDataBlockAccessorTest {

	private final static Path THE_FILE = TestResourceHelper.resourceToFile(ID3v23SingleFile_05_UnknownFrameTest.class,
		"ID3v23_FILE_05_UnknownFrame.txt");

	private final static Path THE_CSV_FILE = TestResourceHelper
		.resourceToFile(ID3v23SingleFile_05_UnknownFrameTest.class, "Expected_ID3v23_FILE_05_UnknownFrame.csv");

	/**
	 * Creates a new {@link ID3v23SingleFile_05_UnknownFrameTest}.
	 */
	public ID3v23SingleFile_05_UnknownFrameTest() {
		super(ID3v23SingleFile_05_UnknownFrameTest.THE_FILE, ID3v23SingleFile_05_UnknownFrameTest.THE_CSV_FILE,
			new Integer[] { 15, 30 });
	}
}
