/**
 *
 * {@link AllUtilityTests}.java
 *
 * @author Jens Ebert
 *
 * @date 20.04.2011
 */
package de.je.jmeta.defext.datablocks;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

/**
 * {@link AllDefaultExtensionDataBlocksTests} is used for running all test cases of the extension management component.
 */
@RunWith(Suite.class)
@SuiteClasses({ AllSingleTopLevelDataBlockTests.class,
   AllMultiTopLevelDataBlocksTests.class, })
public class AllDefaultExtensionDataBlocksTests {
   // Nothing necessary here
}
