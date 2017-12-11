/**
 *
 * {@link AllOggExtensionTests}.java
 *
 * @author Jens Ebert
 *
 * @date 20.04.2011
 */
package com.github.jmeta.defaultextensions.ogg.impl;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

/**
 * {@link AllOggExtensionTests} is used for running all test cases of the extension management component.
 */
@RunWith(Suite.class)
@SuiteClasses({ OggSingleFile_01_Test.class, OggSingleFile_02_Test.class, })
public class AllOggExtensionTests {
   // Nothing necessary here
}
