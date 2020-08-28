/**
 *
 * {@link ID3v23SingleFile_02_SinglePaddingByteTest}.java
 *
 * @author Jens Ebert
 *
 * @date 02.06.2011
 */
package com.github.jmeta.defaultextensions.id3v23.impl;

import java.nio.file.Path;

import com.github.jmeta.library.datablocks.api.services.AbstractLowLevelAPITest;
import com.github.jmeta.utility.testsetup.api.services.TestResourceHelper;

/**
 * {@link ID3v23SingleFile_02_SinglePaddingByteTest} tests reading a single
 * ID3v23 tag with a single padding byte.
 */
public class ID3v23SingleFile_02_SinglePaddingByteTest extends AbstractLowLevelAPITest {

	private final static Path THE_FILE = TestResourceHelper
		.resourceToFile(ID3v23SingleFile_02_SinglePaddingByteTest.class, "ID3v23_FILE_02_SinglePaddingByte.txt");

	private final static Path THE_CSV_FILE = TestResourceHelper.resourceToFile(
		ID3v23SingleFile_02_SinglePaddingByteTest.class, "Expected_ID3v23_FILE_02_SinglePaddingByte.csv");

	/**
	 * Creates a new {@link ID3v23SingleFile_02_SinglePaddingByteTest}.
	 */
	public ID3v23SingleFile_02_SinglePaddingByteTest() {
		super(ID3v23SingleFile_02_SinglePaddingByteTest.THE_FILE,
			ID3v23SingleFile_02_SinglePaddingByteTest.THE_CSV_FILE, new Integer[] { 15, 30 });
	}
}
