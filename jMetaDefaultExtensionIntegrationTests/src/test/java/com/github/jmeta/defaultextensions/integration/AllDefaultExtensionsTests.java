/**
 *
 * {@link AllDefaultExtensionsTests}.java
 *
 * @author Jens Ebert
 *
 * @date 20.04.2011
 */
package com.github.jmeta.defaultextensions.integration;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import com.github.jmeta.defaultextensions.apev2.impl.AllAPEv2ExtensionTests;
import com.github.jmeta.defaultextensions.id3v1.impl.AllID3v1ExtensionTests;
import com.github.jmeta.defaultextensions.id3v23.impl.AllID3v23ExtensionTests;
import com.github.jmeta.defaultextensions.integration.impl.MultiFile_01_TypicalMP3Test;
import com.github.jmeta.defaultextensions.lyrics3v2.impl.AllLyrics3v2ExtensionTests;
import com.github.jmeta.defaultextensions.mp3.impl.AllMP3ExtensionTests;
import com.github.jmeta.defaultextensions.ogg.impl.AllOggExtensionTests;

/**
 * {@link AllDefaultExtensionsTests} is used for running all test cases of the extension management component.
 */
@RunWith(Suite.class)
@SuiteClasses({ MultiFile_01_TypicalMP3Test.class, AllAPEv2ExtensionTests.class, AllID3v1ExtensionTests.class,
   AllID3v23ExtensionTests.class, AllLyrics3v2ExtensionTests.class, AllMP3ExtensionTests.class,
   AllOggExtensionTests.class })
public class AllDefaultExtensionsTests {
   // Nothing necessary here
}
