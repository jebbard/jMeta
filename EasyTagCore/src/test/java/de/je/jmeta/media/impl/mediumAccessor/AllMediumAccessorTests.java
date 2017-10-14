/**
 *
 * {@link AllMediumAccessorTests}.java
 *
 * @author Jens Ebert
 *
 * @date 20.04.2011
 */
package de.je.jmeta.media.impl.mediumAccessor;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

/**
 * {@link AllMediumAccessorTests} is used for running all test cases of the extension management component.
 */
@RunWith(Suite.class)
@SuiteClasses({ 
   StreamMediumAccessorTest.class,
//   StreamingMediumAccessorTimeoutTest.class,
   ReadOnlyFileMediumAccessorTest.class, 
   ReadOnlyMemoryMediumAccessorTest.class, 
   WritableFileMediumAccessorTest.class,
   WritableMemoryMediumAccessorTest.class, 
   })
public class AllMediumAccessorTests {
   // Nothing necessary here
}
