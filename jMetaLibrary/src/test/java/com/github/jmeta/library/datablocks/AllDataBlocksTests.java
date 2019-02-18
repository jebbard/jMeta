/**
 *
 * {@link AllMediumStoreTests}.java
 *
 * @author Jens Ebert
 *
 * @date 20.04.2011
 */
package com.github.jmeta.library.datablocks;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import com.github.jmeta.library.datablocks.impl.FieldTerminationFinderTest;

/**
 * {@link AllDataBlocksTests} is used for running all test cases of the extension management component.
 */
@RunWith(Suite.class)
@SuiteClasses({ FieldTerminationFinderTest.class, })
public class AllDataBlocksTests {
   // Nothing necessary here
}
