/**
 *
 * {@link AllUtilityTests}.java
 *
 * @author Jens Ebert
 *
 * @date 20.04.2011
 */
package com.github.jmeta.library.media.api.types;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import com.github.jmeta.library.media.impl.offset.StandardMediumOffsetTest;

/**
 * {@link AllMediaAPITypesTests} is used for running all test cases of the media
 * component.
 */
@RunWith(Suite.class)
@SuiteClasses({ MediumEqualityTest.class, FileMediumTest.class, InMemoryMediumTest.class, InputStreamMediumTest.class,
	MediumRegionTest.class, StandardMediumOffsetTest.class, MediumOffsetEqualityTest.class, MediumActionTest.class,
	MediumActionEqualityTest.class, })
public class AllMediaAPITypesTests {
	// Nothing necessary here
}
