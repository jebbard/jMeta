/**
 *
 * {@link AllMediumAccessorTests}.java
 *
 * @author Jens Ebert
 *
 * @date 20.04.2011
 */
package de.je.jmeta;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import de.je.jmeta.config.AllConfigTests;
import de.je.jmeta.extmanager.impl.StandardExtensionManagerTest;
import de.je.jmeta.media.AllMediaTests;
import de.je.jmeta.media.impl.mediumAccessor.AllMediumAccessorTests;

/**
 * {@link AlljMetaTests} is used for running all test cases of the extension management component.
 */
@RunWith(Suite.class)
@SuiteClasses({ StandardExtensionManagerTest.class, AllConfigTests.class, AllMediaTests.class, })
public class AlljMetaTests {
   // Nothing necessary here
}
