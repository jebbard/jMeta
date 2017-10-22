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

import com.github.jmeta.defaultextensions.multi.impl.MultiFileTest_01_TypicalMP3;

/**
 * {@link AllMultiTopLevelDataBlocksTests} is used for running all test cases of the extension management component.
 */
@RunWith(Suite.class)
@SuiteClasses({ MultiFileTest_01_TypicalMP3.class, })
public class AllMultiTopLevelDataBlocksTests {
   // Nothing necessary here
}
