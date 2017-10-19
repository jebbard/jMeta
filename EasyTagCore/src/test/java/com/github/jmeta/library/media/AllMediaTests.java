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

import com.github.jmeta.library.media.api.type.DummyFileMediumTest;
import com.github.jmeta.library.media.api.type.ExistingFileMediumTest;
import com.github.jmeta.library.media.api.type.FileMediumConfigurableTest;
import com.github.jmeta.library.media.api.type.IMediumEqualityTest;
import com.github.jmeta.library.media.api.type.IMediumReferenceEqualityTest;
import com.github.jmeta.library.media.api.type.InMemoryMediumConfigurableTest;
import com.github.jmeta.library.media.api.type.InMemoryMediumTest;
import com.github.jmeta.library.media.api.type.InputStreamMediumConfigurableTest;
import com.github.jmeta.library.media.api.type.InputStreamMediumTest;
import com.github.jmeta.library.media.api.type.MediumActionEqualityTest;
import com.github.jmeta.library.media.api.type.MediumActionTest;
import com.github.jmeta.library.media.api.type.MediumRegionTest;
import com.github.jmeta.library.media.impl.OLD.BlockWiseFileMediumCacheTest;
import com.github.jmeta.library.media.impl.OLD.FileMediumCacheTest;
import com.github.jmeta.library.media.impl.changeManager.MediumChangeManagerCreateFlushPlanTest;
import com.github.jmeta.library.media.impl.changeManager.MediumChangeManagerTest;
import com.github.jmeta.library.media.impl.mediumAccessor.AllMediumAccessorTests;
import com.github.jmeta.library.media.impl.reference.MediumReferenceFactoryTest;
import com.github.jmeta.library.media.impl.reference.StandardMediumReferenceTest;

import de.je.util.javautil.AllUtilityTests;

/**
 * {@link AllMediaTests} is used for running all test cases of the media component.
 */
@RunWith(Suite.class)
@SuiteClasses({ BlockWiseFileMediumCacheTest.class, FileMediumCacheTest.class, MediumRegionTest.class,
   IMediumEqualityTest.class, StandardMediumReferenceTest.class, IMediumReferenceEqualityTest.class, DummyFileMediumTest.class,
   ExistingFileMediumTest.class, InMemoryMediumTest.class, InputStreamMediumTest.class,
   InputStreamMediumConfigurableTest.class, InMemoryMediumConfigurableTest.class, FileMediumConfigurableTest.class,
   MediumActionTest.class, MediumActionEqualityTest.class, MediumReferenceFactoryTest.class,
   MediumChangeManagerTest.class, MediumChangeManagerCreateFlushPlanTest.class, AllMediumAccessorTests.class, })
public class AllMediaTests {
   // Nothing necessary here
}
