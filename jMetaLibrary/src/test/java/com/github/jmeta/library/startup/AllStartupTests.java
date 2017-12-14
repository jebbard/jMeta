/**
 *
 * {@link AllUtilityTests}.java
 *
 * @author Jens Ebert
 *
 * @date 20.04.2011
 */
package com.github.jmeta.library.startup;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import com.github.jmeta.library.startup.api.services.LibraryJMetaTest;

/**
 * {@link AllStartupTests} is used for running all test cases of the media component.
 */
@RunWith(Suite.class)
@SuiteClasses({ LibraryJMetaTest.class, })
public class AllStartupTests {
   // Nothing necessary here
}
