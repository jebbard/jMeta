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

import com.github.jmeta.utility.byteutils.api.services.ByteArrayUtilsTest;
import com.github.jmeta.utility.byteutils.api.services.ByteBufferUtilsTest;
import com.github.jmeta.utility.csv.api.services.EmptyCsvFileTest;
import com.github.jmeta.utility.csv.api.services.NineColumnCsvFileTest;
import com.github.jmeta.utility.csv.api.services.OneColumnCsvFileTest;
import com.github.jmeta.utility.csv.api.services.TwelveColumnCsvFileTest;
import com.github.jmeta.utility.csv.api.services.TwoColumnCsvFileTest;
import com.github.jmeta.utility.extmanager.impl.StandardExtensionManagerTest;
import com.github.jmeta.utility.numericutils.api.services.NumericDataTypeHelperTest;

/**
 * {@link AlljMetaUtilityTests} is used for running all test cases of the extension management component.
 */
@RunWith(Suite.class)
@SuiteClasses({ StandardExtensionManagerTest.class, ByteArrayUtilsTest.class, NumericDataTypeHelperTest.class,
   TwelveColumnCsvFileTest.class, NineColumnCsvFileTest.class, TwoColumnCsvFileTest.class, OneColumnCsvFileTest.class,
   EmptyCsvFileTest.class, ByteBufferUtilsTest.class })
public class AlljMetaUtilityTests {
   // Nothing necessary here
}
