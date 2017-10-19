/**
 *
 * {@link AllMediumAccessorTests}.java
 *
 * @author Jens Ebert
 *
 * @date 20.04.2011
 */
package com.github.jmeta.utility;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import com.github.jmeta.utility.extmanager.impl.StandardExtensionManagerTest;

/**
 * {@link AlljMetaUtilityTests} is used for running all test cases of the extension management component.
 */
@RunWith(Suite.class)
@SuiteClasses({ StandardExtensionManagerTest.class, })
public class AlljMetaUtilityTests {
   // Nothing necessary here
}
