/**
 *
 * {@link AllMediumAccessorTests}.java
 *
 * @author Jens Ebert
 *
 * @date 20.04.2011
 */
package com.github.jmeta.library;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import com.github.jmeta.library.dataformats.api.types.BitAddressXXXTest;
import com.github.jmeta.library.dataformats.api.types.FlagSpecificationXXXTest;
import com.github.jmeta.library.dataformats.api.types.Flags1ByteXXXTest;
import com.github.jmeta.library.dataformats.api.types.Flags2ByteXXXTest;
import com.github.jmeta.library.dataformats.api.types.Flags3ByteXXXTest;
import com.github.jmeta.library.dataformats.api.types.Flags4ByteXXXTest;
import com.github.jmeta.library.dataformats.api.types.Flags5ByteXXXTest;
import com.github.jmeta.library.dataformats.api.types.Flags6ByteXXXTest;
import com.github.jmeta.library.dataformats.api.types.Flags7ByteXXXTest;
import com.github.jmeta.library.dataformats.api.types.Flags8ByteXXXTest;
import com.github.jmeta.library.dataformats.api.types.FlagsMultibitTest;
import com.github.jmeta.library.media.AllMediaTests;
import com.github.jmeta.library.media.impl.mediumAccessor.AllMediumAccessorTests;

/**
 * {@link AlljMetaLibraryTests} is used for running all test cases of the extension management component.
 */
@RunWith(Suite.class)
@SuiteClasses({ AllMediaTests.class, BitAddressXXXTest.class, Flags1ByteXXXTest.class, Flags2ByteXXXTest.class,
   Flags3ByteXXXTest.class, Flags4ByteXXXTest.class, Flags5ByteXXXTest.class, Flags6ByteXXXTest.class,
   Flags7ByteXXXTest.class, Flags8ByteXXXTest.class, FlagSpecificationXXXTest.class, FlagsMultibitTest.class, })
public class AlljMetaLibraryTests {
   // Nothing necessary here
}
