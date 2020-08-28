/**
 *
 * {@link ID3v1SingleFile_01_Test}.java
 *
 * @author Jens Ebert
 *
 * @date 02.06.2011
 */
package com.github.jmeta.defaultextensions.id3v1.impl;

import java.nio.file.Path;

import com.github.jmeta.library.datablocks.api.services.AbstractLowLevelAPITest;
import com.github.jmeta.utility.testsetup.api.services.TestResourceHelper;

/**
 * {@link ID3v1SingleFile_01_Test} tests reading a single ID3v1 tag.
 */
public class ID3v1SingleFile_01_Test extends AbstractLowLevelAPITest {

	private final static Path THE_FILE = TestResourceHelper.resourceToFile(ID3v1SingleFile_01_Test.class,
		"ID3v1_FILE_01.txt");

	private final static Path THE_CSV_FILE = TestResourceHelper.resourceToFile(ID3v1SingleFile_01_Test.class,
		"Expected_ID3v1_FILE_01.csv");

	/**
	 * Creates a new {@link ID3v1SingleFile_01_Test}.
	 */
	public ID3v1SingleFile_01_Test() {
		super(ID3v1SingleFile_01_Test.THE_FILE, ID3v1SingleFile_01_Test.THE_CSV_FILE, new Integer[] { 4 });
	}
}
