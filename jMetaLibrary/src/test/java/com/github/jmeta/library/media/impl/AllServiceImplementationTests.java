/**
 *
 * {@link AllUtilityTests}.java
 *
 * @author Jens Ebert
 *
 * @date 20.04.2011
 */
package com.github.jmeta.library.media.impl;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import com.github.jmeta.library.media.impl.cache.MediumCacheTest;
import com.github.jmeta.library.media.impl.changeManager.MediumChangeManagerCreateFlushPlanTest;
import com.github.jmeta.library.media.impl.changeManager.MediumChangeManagerTest;
import com.github.jmeta.library.media.impl.offset.MediumOffsetFactoryTest;

/**
 * {@link AllServiceImplementationTests} is used for running all test cases of the media component.
 */
@RunWith(Suite.class)
@SuiteClasses({ MediumOffsetFactoryTest.class, MediumChangeManagerTest.class,
   MediumChangeManagerCreateFlushPlanTest.class, MediumCacheTest.class, StandardMediaAPITest.class, })
public class AllServiceImplementationTests {
   // Nothing necessary here
}
