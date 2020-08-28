/**
 *
 * {@link OggSingleFileTest_01}.java
 *
 * @author Jens Ebert
 *
 * @date 02.06.2011
 */
package com.github.jmeta.defaultextensions.mp3.impl;

import java.nio.file.Path;

import com.github.jmeta.library.datablocks.api.services.AbstractLowLevelAPITest;
import com.github.jmeta.utility.testsetup.api.services.TestResourceHelper;

/**
 * {@link MP3SingleFile_01Test} tests reading a single MP3 file.
 */
// TODO mp3: Add test cases for CRC and padding
public class MP3SingleFile_01Test extends AbstractLowLevelAPITest {

	private final static Path THE_FILE = TestResourceHelper.resourceToFile(MP3SingleFile_01Test.class,
		"MP3_FILE_01.txt");

	private final static Path THE_CSV_FILE = TestResourceHelper.resourceToFile(MP3SingleFile_01Test.class,
		"Expected_MP3_FILE_01.csv");

	/**
	 * Creates a new {@link MP3SingleFile_01Test}.
	 */
	public MP3SingleFile_01Test() {
		super(MP3SingleFile_01Test.THE_FILE, MP3SingleFile_01Test.THE_CSV_FILE, new Integer[] { 4 });
	}
}
