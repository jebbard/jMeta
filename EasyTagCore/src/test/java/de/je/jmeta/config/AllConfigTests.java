/**
 *
 * {@link AllUtilityTests}.java
 *
 * @author Jens Ebert
 *
 * @date 20.04.2011
 */
package de.je.jmeta.config;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import de.je.jmeta.config.impl.StandardUserConfigAccessorTest;
import de.je.util.javautil.AllUtilityTests;

/**
 * {@link AllConfigTests} is used for running all test cases of the extension management component.
 */
@RunWith(Suite.class)
@SuiteClasses({ StandardUserConfigAccessorTest.class, })
public class AllConfigTests {
   // Nothing necessary here
}
