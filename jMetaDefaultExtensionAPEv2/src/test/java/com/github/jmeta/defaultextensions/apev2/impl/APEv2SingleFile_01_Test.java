/**
 *
 * {@link APEv2SingleFile_01_Test}.java
 *
 * @author Jens Ebert
 *
 * @date 02.06.2011
 */
package com.github.jmeta.defaultextensions.apev2.impl;

import java.nio.file.Path;

import com.github.jmeta.library.datablocks.api.services.AbstractLowLevelAPITest;
import com.github.jmeta.utility.testsetup.api.services.TestResourceHelper;

/**
 * {@link APEv2SingleFile_01_Test} tests reading a single APEv2 tag with header
 * and footer.
 */
public class APEv2SingleFile_01_Test extends AbstractLowLevelAPITest {

	private final static Path THE_FILE = TestResourceHelper.resourceToFile(APEv2SingleFile_01_Test.class,
		"APEv2_FILE_01.txt");

	private final static Path THE_CSV_FILE = TestResourceHelper.resourceToFile(APEv2SingleFile_01_Test.class,
		"Expected_APEv2_FILE_01.csv");

	/**
	 * Creates a new {@link APEv2SingleFile_01_Test}.
	 */
	public APEv2SingleFile_01_Test() {
		super(APEv2SingleFile_01_Test.THE_FILE, APEv2SingleFile_01_Test.THE_CSV_FILE, new Integer[] { 15, 30 });
	}
}
