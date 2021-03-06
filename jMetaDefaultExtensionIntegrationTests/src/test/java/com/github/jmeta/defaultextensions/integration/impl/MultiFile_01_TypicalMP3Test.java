/**
 *
 * {@link MultiFile_01_TypicalMP3Test}.java
 *
 * @author Jens Ebert
 *
 * @date 02.06.2011
 */
package com.github.jmeta.defaultextensions.integration.impl;

import java.nio.file.Path;

import com.github.jmeta.library.datablocks.api.services.AbstractLowLevelAPITest;
import com.github.jmeta.utility.testsetup.api.services.TestResourceHelper;

/**
 * {@link MultiFile_01_TypicalMP3Test} tests reading a typical MP3 file with
 * ID3v2.3 tag at the beginning, some MP3 frames in the middle and APEv2,
 * Lyrics3v2 and ID3v1 tags at the end.
 */
public class MultiFile_01_TypicalMP3Test extends AbstractLowLevelAPITest {

	private final static Path THE_FILE = TestResourceHelper.resourceToFile(MultiFile_01_TypicalMP3Test.class,
		"Multi_FILE_01_TypicalMP3.txt");

	private final static Path THE_CSV_FILE = TestResourceHelper.resourceToFile(MultiFile_01_TypicalMP3Test.class,
		"Expected_Multi_FILE_01_TypicalMP3.csv");

	/**
	 * Creates a new {@link MultiFile_01_TypicalMP3Test}.
	 */
	public MultiFile_01_TypicalMP3Test() {
		super(MultiFile_01_TypicalMP3Test.THE_FILE, MultiFile_01_TypicalMP3Test.THE_CSV_FILE, new Integer[] { 15, 30 });
	}
}
