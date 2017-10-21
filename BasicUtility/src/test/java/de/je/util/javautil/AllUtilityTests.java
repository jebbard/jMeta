/**
 *
 * {@link AllUtilityTests}.java
 *
 * @author Jens Ebert
 *
 * @date 20.04.2011
 */
package de.je.util.javautil;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import de.je.util.javautil.common.config.PropertiesConfigLoaderTest;
import de.je.util.javautil.common.config.StandardUserConfigAccessorTest;
import de.je.util.javautil.common.extenum.ConcreteExtensibleEnumTest;
import de.je.util.javautil.common.time.MeasurementDurationTest;
import de.je.util.javautil.common.time.MeasurementSessionTest;

/**
 * {@link AllUtilityTests} is used for running all test cases of the extension management component.
 */
@RunWith(Suite.class)
@SuiteClasses({ PropertiesConfigLoaderTest.class, MeasurementDurationTest.class, MeasurementSessionTest.class,
   ConcreteExtensibleEnumTest.class, StandardUserConfigAccessorTest.class })
public class AllUtilityTests {
   // Nothing necessary here
}
