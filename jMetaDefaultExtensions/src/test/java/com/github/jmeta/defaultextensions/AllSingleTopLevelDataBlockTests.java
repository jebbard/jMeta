/**
 *
 * {@link AllUtilityTests}.java
 *
 * @author Jens Ebert
 *
 * @date 20.04.2011
 */
package com.github.jmeta.defaultextensions;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import com.github.jmeta.defaultextensions.apev2.impl.APEv2SingleFileTest_01;
import com.github.jmeta.defaultextensions.id3v1.impl.ID3v11SingleFileTest_01;
import com.github.jmeta.defaultextensions.id3v1.impl.ID3v11SingleFileTest_02;
import com.github.jmeta.defaultextensions.id3v1.impl.ID3v1SingleFileTest_01;
import com.github.jmeta.defaultextensions.id3v1.impl.ID3v1SingleFileTest_02;
import com.github.jmeta.defaultextensions.id3v1.impl.ID3v1SingleFileTest_03;
import com.github.jmeta.defaultextensions.id3v23.impl.ID3v23SingleFileTest_01_PaddingUTF16TextFrame;
import com.github.jmeta.defaultextensions.id3v23.impl.ID3v23SingleFileTest_02_SinglePaddingByte;
import com.github.jmeta.defaultextensions.id3v23.impl.ID3v23SingleFileTest_03_NoPadding;
import com.github.jmeta.defaultextensions.id3v23.impl.ID3v23SingleFileTest_05_UnknownFrame;
import com.github.jmeta.defaultextensions.lyrics3v2.impl.Lyrics3v2SingleFileTest_01;
import com.github.jmeta.defaultextensions.mp3.impl.MP3SingleFileTest_01;
import com.github.jmeta.defaultextensions.ogg.impl.OggSingleFileTest_01;
import com.github.jmeta.defaultextensions.ogg.impl.OggSingleFileTest_02;

/**
 * {@link AllSingleTopLevelDataBlockTests} is used for running all test cases of the extension management component.
 */
@RunWith(Suite.class)
@SuiteClasses({ ID3v1SingleFileTest_01.class, ID3v1SingleFileTest_02.class,
   ID3v1SingleFileTest_03.class, ID3v11SingleFileTest_01.class,
   ID3v11SingleFileTest_02.class,
   ID3v23SingleFileTest_01_PaddingUTF16TextFrame.class,
   ID3v23SingleFileTest_02_SinglePaddingByte.class,
   ID3v23SingleFileTest_03_NoPadding.class,
   // TODO stage2_012: Include test case as soon as correct handling
   // of unknown fields is implemented
   // ID3v23SingleFileTest_04_WrongCharsetEnumVal.class,
   ID3v23SingleFileTest_05_UnknownFrame.class, MP3SingleFileTest_01.class,
   APEv2SingleFileTest_01.class, Lyrics3v2SingleFileTest_01.class,
   OggSingleFileTest_01.class, OggSingleFileTest_02.class, })
public class AllSingleTopLevelDataBlockTests {
   // Nothing necessary here
}
