/**
 *
 * {@link OggSingleFile_01_Test}.java
 *
 * @author Jens Ebert
 *
 * @date 02.06.2011
 */
package com.github.jmeta.defaultextensions.ogg.impl;

import java.nio.file.Path;

import com.github.jmeta.library.datablocks.api.services.AbstractLowLevelAPITest;
import com.github.jmeta.utility.testsetup.api.services.TestResourceHelper;

/**
 * {@link OggSingleFile_02_Test} tests reading a single ogg file.
 */
public class OggSingleFile_02_Test extends AbstractLowLevelAPITest {

	private final static Path THE_FILE = TestResourceHelper.resourceToFile(OggSingleFile_02_Test.class,
		"OGG_FILE_02.txt");

	private final static Path THE_CSV_FILE = TestResourceHelper.resourceToFile(OggSingleFile_02_Test.class,
		"Expected_OGG_FILE_02.csv");

	/**
	 * Creates a new {@link OggSingleFile_02_Test}.
	 */
	public OggSingleFile_02_Test() {
		super(OggSingleFile_02_Test.THE_FILE, OggSingleFile_02_Test.THE_CSV_FILE, new Integer[] { 4 });
	}
}
