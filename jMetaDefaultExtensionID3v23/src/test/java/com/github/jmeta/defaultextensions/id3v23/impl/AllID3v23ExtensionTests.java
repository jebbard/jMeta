/**
 *
 * {@link AllID3v23ExtensionTests}.java
 *
 * @author Jens Ebert
 *
 * @date 20.04.2011
 */
package com.github.jmeta.defaultextensions.id3v23.impl;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

/**
 * {@link AllID3v23ExtensionTests} is used for running all test cases of the extension management component.
 */
@RunWith(Suite.class)
@SuiteClasses({ ID3v23SingleFile_01_PaddingUTF16TextFrameTest.class, ID3v23SingleFile_02_SinglePaddingByteTest.class,
   ID3v23SingleFile_03_NoPaddingTest.class, ID3v23SingleFile_05_UnknownFrameTest.class, })
public class AllID3v23ExtensionTests {
   // Nothing necessary here
}
