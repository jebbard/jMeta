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

import com.github.jmeta.library.media.api.types.FileMediumTest;
import com.github.jmeta.library.media.api.types.InMemoryMediumTest;
import com.github.jmeta.library.media.api.types.InputStreamMediumTest;
import com.github.jmeta.library.media.api.types.MediumActionEqualityTest;
import com.github.jmeta.library.media.api.types.MediumActionTest;
import com.github.jmeta.library.media.api.types.MediumEqualityTest;
import com.github.jmeta.library.media.api.types.MediumReferenceEqualityTest;
import com.github.jmeta.library.media.api.types.MediumRegionTest;
import com.github.jmeta.library.media.impl.OLD.BlockWiseFileMediumCacheTest;
import com.github.jmeta.library.media.impl.OLD.FileMediumCacheTest;
import com.github.jmeta.library.media.impl.cache.MediumCacheTest;
import com.github.jmeta.library.media.impl.changeManager.MediumChangeManagerCreateFlushPlanTest;
import com.github.jmeta.library.media.impl.changeManager.MediumChangeManagerTest;
import com.github.jmeta.library.media.impl.mediumAccessor.AllMediumAccessorTests;
import com.github.jmeta.library.media.impl.reference.MediumReferenceFactoryTest;
import com.github.jmeta.library.media.impl.reference.StandardMediumReferenceTest;
import com.github.jmeta.library.media.impl.store.AllMediumStoreTests;

/**
 * {@link AllMediaTests} is used for running all test cases of the media component.
 */
@RunWith(Suite.class)
@SuiteClasses({ BlockWiseFileMediumCacheTest.class, FileMediumCacheTest.class, MediumRegionTest.class,
   MediumEqualityTest.class, StandardMediumReferenceTest.class, MediumReferenceEqualityTest.class, FileMediumTest.class,
   InMemoryMediumTest.class, InputStreamMediumTest.class, MediumActionTest.class, MediumActionEqualityTest.class,
   MediumReferenceFactoryTest.class, MediumChangeManagerTest.class, MediumChangeManagerCreateFlushPlanTest.class,
   AllMediumAccessorTests.class, AllMediumStoreTests.class, MediumCacheTest.class, })
public class AllMediaTests {
   // Nothing necessary here
}
