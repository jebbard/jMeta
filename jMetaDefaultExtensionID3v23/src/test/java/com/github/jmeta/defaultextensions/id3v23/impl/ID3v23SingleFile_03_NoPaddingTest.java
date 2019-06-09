/**
 *
 * {@link ID3v23SingleFile_03_NoPaddingTest}.java
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
 * {@link ID3v23SingleFile_03_NoPaddingTest} tests reading a single ID3v23 tag
 * with a single padding byte.
 */
public class ID3v23SingleFile_03_NoPaddingTest extends AbstractDataBlockAccessorTest {

	private final static Path THE_FILE = TestResourceHelper.resourceToFile(ID3v23SingleFile_03_NoPaddingTest.class,
		"ID3v23_FILE_03_NoPadding.txt");

	private final static Path THE_CSV_FILE = TestResourceHelper.resourceToFile(ID3v23SingleFile_03_NoPaddingTest.class,
		"Expected_ID3v23_FILE_03_NoPadding.csv");

	/**
	 * Creates a new {@link ID3v23SingleFile_03_NoPaddingTest}.
	 */
	public ID3v23SingleFile_03_NoPaddingTest() {
		super(ID3v23SingleFile_03_NoPaddingTest.THE_FILE, ID3v23SingleFile_03_NoPaddingTest.THE_CSV_FILE,
			new Integer[] { 15, 30 });
	}
}
