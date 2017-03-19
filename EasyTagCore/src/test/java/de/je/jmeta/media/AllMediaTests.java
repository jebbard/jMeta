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
import de.je.jmeta.media.api.datatype.InMemoryMediumConfigurableTest;
import de.je.jmeta.media.api.datatype.InMemoryMediumTest;
import de.je.jmeta.media.api.datatype.InputStreamMediumConfigurableTest;
import de.je.jmeta.media.api.datatype.InputStreamMediumTest;
import de.je.jmeta.media.api.datatype.MediumActionEqualityTest;
import de.je.jmeta.media.api.datatype.MediumActionTest;
import de.je.jmeta.media.impl.BlockWiseFileMediumCacheTest;
import de.je.jmeta.media.impl.FileMediumAccessorTest;
import de.je.jmeta.media.impl.FileMediumCacheTest;
import de.je.jmeta.media.impl.IMediumReferenceEqualityTest;
import de.je.jmeta.media.impl.IMediumReferenceTest;
import de.je.jmeta.media.impl.MediumChangeManagerTest;
import de.je.jmeta.media.impl.MediumReferenceFactoryTest;
import de.je.jmeta.media.impl.MediumRegionTest;
import de.je.jmeta.media.impl.MemoryMediumAccessorTest;
import de.je.jmeta.media.impl.StreamingMediumAccessorTest;
import de.je.jmeta.media.impl.timeout.StreamingMediumAccessorTimeoutTest;

/**
 * {@link AllMediaTests} is used for running all test cases of the media component.
 */
@RunWith(Suite.class)
@SuiteClasses({ BlockWiseFileMediumCacheTest.class, FileMediumCacheTest.class,
   FileMediumAccessorTest.class, MemoryMediumAccessorTest.class,
   StreamingMediumAccessorTest.class, MediumRegionTest.class,
   IMediumEqualityTest.class, IMediumReferenceTest.class,
   IMediumReferenceEqualityTest.class, DummyFileMediumTest.class,
   ExistingFileMediumTest.class, InMemoryMediumTest.class,
   InputStreamMediumTest.class, StreamingMediumAccessorTimeoutTest.class,
   InputStreamMediumConfigurableTest.class,
   InMemoryMediumConfigurableTest.class, FileMediumConfigurableTest.class,
   MediumActionTest.class, MediumActionEqualityTest.class,
   MediumReferenceFactoryTest.class, MediumChangeManagerTest.class })
public class AllMediaTests {
   // Nothing necessary here
}
