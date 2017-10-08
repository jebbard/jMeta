/**
 *
 * {@link AllUtilityTests}.java
 *
 * @author Jens Ebert
 *
 * @date 20.04.2011
 */
package de.je.jmeta.media;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import de.je.jmeta.media.api.datatype.DummyFileMediumTest;
import de.je.jmeta.media.api.datatype.ExistingFileMediumTest;
import de.je.jmeta.media.api.datatype.FileMediumConfigurableTest;
import de.je.jmeta.media.api.datatype.IMediumEqualityTest;
import de.je.jmeta.media.api.datatype.IMediumReferenceEqualityTest;
import de.je.jmeta.media.api.datatype.InMemoryMediumConfigurableTest;
import de.je.jmeta.media.api.datatype.InMemoryMediumTest;
import de.je.jmeta.media.api.datatype.InputStreamMediumConfigurableTest;
import de.je.jmeta.media.api.datatype.InputStreamMediumTest;
import de.je.jmeta.media.api.datatype.MediumActionEqualityTest;
import de.je.jmeta.media.api.datatype.MediumActionTest;
import de.je.jmeta.media.api.datatype.MediumRegionTest;
import de.je.jmeta.media.impl.IMediumReferenceTest;
import de.je.jmeta.media.impl.MediumReferenceFactoryTest;
import de.je.jmeta.media.impl.OLD.BlockWiseFileMediumCacheTest;
import de.je.jmeta.media.impl.OLD.FileMediumCacheTest;
import de.je.jmeta.media.impl.changeManager.MediumChangeManagerCreateFlushPlanTest;
import de.je.jmeta.media.impl.changeManager.MediumChangeManagerTest;
import de.je.jmeta.media.impl.mediumAccessor.ReadOnlyFileMediumAccessorTest;
import de.je.jmeta.media.impl.mediumAccessor.ReadOnlyMemoryMediumAccessorTest;
import de.je.jmeta.media.impl.mediumAccessor.StreamMediumAccessorTest;
import de.je.jmeta.media.impl.mediumAccessor.StreamingMediumAccessorTimeoutTest;
import de.je.jmeta.media.impl.mediumAccessor.WritableFileMediumAccessorTest;
import de.je.jmeta.media.impl.mediumAccessor.WritableMemoryMediumAccessorTest;
import de.je.util.javautil.AllUtilityTests;

/**
 * {@link AllMediaTests} is used for running all test cases of the media component.
 */
@RunWith(Suite.class)
@SuiteClasses({ BlockWiseFileMediumCacheTest.class, FileMediumCacheTest.class, WritableFileMediumAccessorTest.class,
   WritableMemoryMediumAccessorTest.class, StreamMediumAccessorTest.class, MediumRegionTest.class,
   IMediumEqualityTest.class, IMediumReferenceTest.class, IMediumReferenceEqualityTest.class, DummyFileMediumTest.class,
   ExistingFileMediumTest.class, InMemoryMediumTest.class, InputStreamMediumTest.class,
   StreamingMediumAccessorTimeoutTest.class, InputStreamMediumConfigurableTest.class,
   InMemoryMediumConfigurableTest.class, FileMediumConfigurableTest.class, MediumActionTest.class,
   MediumActionEqualityTest.class, MediumReferenceFactoryTest.class, MediumChangeManagerTest.class,
   MediumChangeManagerCreateFlushPlanTest.class, ReadOnlyMemoryMediumAccessorTest.class,
   ReadOnlyFileMediumAccessorTest.class })
public class AllMediaTests {
   // Nothing necessary here
}
