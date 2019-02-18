/**
 *
 * {@link AllMediumStoreTests}.java
 *
 * @author Jens Ebert
 *
 * @date 20.04.2011
 */
package com.github.jmeta.library.media.impl.store;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

/**
 * {@link AllMediumStoreTests} is used for running all test cases of the extension management component.
 */
@RunWith(Suite.class)
@SuiteClasses({ ReadOnlyFileMediumStoreTest.class, ReadOnlyInMemoryMediumStoreTest.class,
   ReadOnlyStreamMediumStoreTest.class, StreamMediumStoreTest.class, WritableFileMediumStoreTest.class,
   WritableInMemoryMediumStoreTest.class, })
public class AllMediumStoreTests {
   // Nothing necessary here
}
