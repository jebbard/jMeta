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

import de.je.util.javautil.common.array.EnhancedArraysXXXTest;
import de.je.util.javautil.common.config.PropertiesConfigLoaderTest;
import de.je.util.javautil.common.config.StandardUserConfigAccessorTest;
import de.je.util.javautil.common.extenum.ConcreteExtensibleEnumTest;
import de.je.util.javautil.common.flags.BitAddressXXXTest;
import de.je.util.javautil.common.flags.FlagSpecificationXXXTest;
import de.je.util.javautil.common.flags.Flags1ByteXXXTest;
import de.je.util.javautil.common.flags.Flags2ByteXXXTest;
import de.je.util.javautil.common.flags.Flags3ByteXXXTest;
import de.je.util.javautil.common.flags.Flags4ByteXXXTest;
import de.je.util.javautil.common.flags.Flags5ByteXXXTest;
import de.je.util.javautil.common.flags.Flags6ByteXXXTest;
import de.je.util.javautil.common.flags.Flags7ByteXXXTest;
import de.je.util.javautil.common.flags.Flags8ByteXXXTest;
import de.je.util.javautil.common.flags.FlagsMultibitTest;
import de.je.util.javautil.common.num.NumericDataTypeHelperTest;
import de.je.util.javautil.common.time.MeasurementDurationTest;
import de.je.util.javautil.common.time.MeasurementSessionTest;
import de.je.util.javautil.io.csv.EmptyCsvFileTest;
import de.je.util.javautil.io.csv.NineColumnCsvFileTest;
import de.je.util.javautil.io.csv.OneColumnCsvFileTest;
import de.je.util.javautil.io.csv.TwelveColumnCsvFileTest;
import de.je.util.javautil.io.csv.TwoColumnCsvFileTest;

/**
 * {@link AllUtilityTests} is used for running all test cases of the extension management component.
 */
@RunWith(Suite.class)
@SuiteClasses({ EnhancedArraysXXXTest.class, BitAddressXXXTest.class, Flags1ByteXXXTest.class, Flags2ByteXXXTest.class,
   Flags3ByteXXXTest.class, Flags4ByteXXXTest.class, Flags5ByteXXXTest.class, Flags6ByteXXXTest.class,
   Flags7ByteXXXTest.class, Flags8ByteXXXTest.class, FlagSpecificationXXXTest.class, FlagsMultibitTest.class,
   TwelveColumnCsvFileTest.class, NineColumnCsvFileTest.class, TwoColumnCsvFileTest.class, OneColumnCsvFileTest.class,
   EmptyCsvFileTest.class, NumericDataTypeHelperTest.class, PropertiesConfigLoaderTest.class,
   MeasurementDurationTest.class, MeasurementSessionTest.class, ConcreteExtensibleEnumTest.class,
   StandardUserConfigAccessorTest.class })
public class AllUtilityTests {
   // Nothing necessary here
}
