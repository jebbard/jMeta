/**
 *
 * {@link AllUtilityTests}.java
 *
 * @author Jens Ebert
 *
 * @date 20.04.2011
 */
package de.je.jmeta.extmanager;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import de.je.jmeta.extmanager.impl.ExtensionBundleFileNoBasePathTest;
import de.je.jmeta.extmanager.impl.ExtensionBundleJarAndFileWithBasePathTest;
import de.je.jmeta.extmanager.impl.ExtensionBundleJarNoBasePathTest;
import de.je.jmeta.extmanager.impl.ExtensionBundleNegativeTest;
import de.je.jmeta.extmanager.impl.ExtensionManagerNegativeTest;

/**
 * {@link AllExtensionManagmentTests} is used for running all test cases of the extension management component.
 */
@RunWith(Suite.class)
@SuiteClasses({ ExtensionBundleFileNoBasePathTest.class,
   ExtensionBundleJarNoBasePathTest.class, ExtensionBundleNegativeTest.class,
   ExtensionManagerNegativeTest.class,
   ExtensionBundleJarAndFileWithBasePathTest.class })
public class AllExtensionManagmentTests {
   // Nothing to be done here
}
