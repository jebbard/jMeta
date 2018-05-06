/**
 *
 * {@link AllMediumStoreTests}.java
 *
 * @author Jens Ebert
 *
 * @date 20.04.2011
 */
package com.github.jmeta.library.media.impl.mediumAccessor;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import com.github.jmeta.library.media.impl.store.AllMediumStoreTests;

/**
 * {@link AllMediumAccessorTests} is used for running all test cases of the extension management component.
 */
@RunWith(Suite.class)
@SuiteClasses({ StreamMediumAccessorTest.class, ReadOnlyFileMediumAccessorTest.class,
   ReadOnlyMemoryMediumAccessorTest.class, WritableFileMediumAccessorTest.class,
   WritableMemoryMediumAccessorTest.class, })
public class AllMediumAccessorTests {
   // Nothing necessary here
}
