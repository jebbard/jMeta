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

import com.github.jmeta.utility.csv.api.services.EmptyCsvFileTest;
import com.github.jmeta.utility.csv.api.services.NineColumnCsvFileTest;
import com.github.jmeta.utility.csv.api.services.OneColumnCsvFileTest;
import com.github.jmeta.utility.csv.api.services.TwelveColumnCsvFileTest;
import com.github.jmeta.utility.csv.api.services.TwoColumnCsvFileTest;

/**
 * {@link AlljMetaTestUtilityTests} is used for running all test cases of the extension management component.
 */
@RunWith(Suite.class)
@SuiteClasses({ TwelveColumnCsvFileTest.class, NineColumnCsvFileTest.class, TwoColumnCsvFileTest.class,
   OneColumnCsvFileTest.class, EmptyCsvFileTest.class, })
public class AlljMetaTestUtilityTests {
   // Nothing necessary here
}
