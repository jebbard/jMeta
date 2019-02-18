/**
 *
 * {@link AllMediumStoreTests}.java
 *
 * @author Jens Ebert
 *
 * @date 20.04.2011
 */
package com.github.jmeta.library;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import com.github.jmeta.library.datablocks.AllDataBlocksTests;
import com.github.jmeta.library.dataformats.AllDataFormatsTests;
import com.github.jmeta.library.media.AllMediaTests;
import com.github.jmeta.library.startup.AllStartupTests;

/**
 * {@link AlljMetaLibraryTests} is used for running all test cases of the extension management component.
 */
@RunWith(Suite.class)
@SuiteClasses({ AllStartupTests.class, AllMediaTests.class, AllDataFormatsTests.class, AllDataBlocksTests.class, })
public class AlljMetaLibraryTests {
   // Nothing necessary here
}
