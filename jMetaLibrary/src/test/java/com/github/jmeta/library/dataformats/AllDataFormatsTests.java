/**
 *
 * {@link AllMediumStoreTests}.java
 *
 * @author Jens Ebert
 *
 * @date 20.04.2011
 */
package com.github.jmeta.library.dataformats;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import com.github.jmeta.library.dataformats.api.types.BitAddressEqualityTest;
import com.github.jmeta.library.dataformats.api.types.FlagSpecificationTest;
import com.github.jmeta.library.dataformats.api.types.Flags1ByteTest;
import com.github.jmeta.library.dataformats.api.types.Flags2ByteTest;
import com.github.jmeta.library.dataformats.api.types.Flags3ByteTest;
import com.github.jmeta.library.dataformats.api.types.Flags4ByteTest;
import com.github.jmeta.library.dataformats.api.types.Flags5ByteTest;
import com.github.jmeta.library.dataformats.api.types.Flags6ByteTest;
import com.github.jmeta.library.dataformats.api.types.Flags7ByteTest;
import com.github.jmeta.library.dataformats.api.types.Flags8ByteTest;
import com.github.jmeta.library.dataformats.api.types.FlagsMultibitTest;

/**
 * {@link AllDataFormatsTests} is used for running all test cases of the
 * extension management component.
 */
@RunWith(Suite.class)
@SuiteClasses({ BitAddressEqualityTest.class, Flags1ByteTest.class, Flags2ByteTest.class, Flags3ByteTest.class,
	Flags4ByteTest.class, Flags5ByteTest.class, Flags6ByteTest.class, Flags7ByteTest.class, Flags8ByteTest.class,
	FlagSpecificationTest.class, FlagsMultibitTest.class, })
public class AllDataFormatsTests {
	// Nothing necessary here
}
