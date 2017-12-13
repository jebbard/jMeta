/**
 *
 * {@link AllID3v1ExtensionTests}.java
 *
 * @author Jens Ebert
 *
 * @date 20.04.2011
 */
package com.github.jmeta.defaultextensions.id3v1.impl;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

/**
 * {@link AllID3v1ExtensionTests} is used for running all test cases of the extension management component.
 */
@RunWith(Suite.class)
@SuiteClasses({ ID3v11SingleFile_01_Test.class, ID3v11SingleFile_02_Test.class, ID3v1SingleFile_01_Test.class,
   ID3v1SingleFile_02_Test.class, ID3v1SingleFile_03_Test.class, })
public class AllID3v1ExtensionTests {
   // Nothing necessary here
}
