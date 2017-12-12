/**
 *
 * {@link AllUtilityTests}.java
 *
 * @author Jens Ebert
 *
 * @date 20.04.2011
 */
package com.github.jmeta.library.media;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import com.github.jmeta.library.media.api.types.AllMediaAPITypesTests;
import com.github.jmeta.library.media.impl.AllServiceImplementationTests;
import com.github.jmeta.library.media.impl.mediumAccessor.AllMediumAccessorTests;
import com.github.jmeta.library.media.impl.store.AllMediumStoreTests;

/**
 * {@link AllMediaTests} is used for running all test cases of the media component.
 */
@RunWith(Suite.class)
@SuiteClasses({ AllMediaAPITypesTests.class, AllMediumAccessorTests.class, AllMediumStoreTests.class,
   AllServiceImplementationTests.class, })
public class AllMediaTests {
   // Nothing necessary here
}
