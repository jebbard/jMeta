/**
 *
 * {@link AllUtilityTests}.java
 *
 * @author Jens Ebert
 *
 * @date 20.04.2011
 */
package de.je.jmeta.defext;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import de.je.jmeta.defext.datablocks.AllDefaultExtensionDataBlocksTests;

/**
 * {@link AllDefaultExtensionTests} is used for running all test cases of the media component.
 */
@RunWith(Suite.class)
@SuiteClasses({ AllDefaultExtensionDataBlocksTests.class, })
public class AllDefaultExtensionTests {
   // Nothing necessary here
}
