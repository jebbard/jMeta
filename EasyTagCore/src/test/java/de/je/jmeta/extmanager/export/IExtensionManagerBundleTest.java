/**
 *
 * {@link IExtensionManagerBundleTest}.java
 *
 * @author Jens Ebert
 *
 * @date 28.04.2011
 */
package de.je.jmeta.extmanager.export;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import de.je.jmeta.extmanager.impl.ExtensionBundleFileNoBasePathTest;
import de.je.jmeta.testHelpers.basics.JMetaTestBasics;
import de.je.util.javautil.io.stream.NamedInputStream;
import de.je.util.javautil.testUtil.resource.TestResourceHelper;
import de.je.util.javautil.testUtil.setup.TestDataException;
import junit.framework.Assert;

/**
 * {@link IExtensionManagerBundleTest} tests the {@link IExtensionManager} and {@link IExtensionBundle} interfaces.
 */
public abstract class IExtensionManagerBundleTest extends AbstractExtensionManagementTest {

   /**
    * The base folder for test data for testing this component.
    */
   public static File BASE_TEST_DATA_FOLDER = new File(new File(new File("."), "data"), "extmanager");

   /**
    * Sets up the test fixtures.
    */
   @Override
   @Before
   public void setUp() {

      super.setUp();

      final File configFile = TestResourceHelper.resourceToFile(ExtensionBundleFileNoBasePathTest.class,
         getConfigFileResourcePath());
      try {
         BundleLoadExceptions loadExceptions = getTestling().load(NamedInputStream.createFromFile(configFile),
            getFileBasePath());

         Assert.assertTrue(loadExceptions.getExceptions().isEmpty());
      } catch (IOException e) {
         throwExceptionForLoadFailure(configFile, e);
      }
   }

   /**
    * Tears down the test fixtures.
    */
   @After
   public void tearDown() {

      JMetaTestBasics.performGeneralLogCheck(JMetaTestBasics.DEFAULT_LOG_FILE);
   }

   /**
    * Tests {@link IExtensionManager#getAvailableExtensionPoints()}.
    */
   @Test
   public void test_getAvailableExtensionPoints() {

      IExtensionManager manager = getTestling();

      Set<Class<? extends IExtensionPoint>> extensionPointList = manager.getAvailableExtensionPoints();

      Assert.assertNotNull(extensionPointList);
      Assert.assertEquals(getExpectedExtensionPointProviderInterfaces(), extensionPointList);
   }

   /**
    * Tests {@link IExtensionManager#getRegisteredExtensionBundles()} and {@link IExtensionBundle#getDescription()}.
    */
   @Test
   public void test_getRegisteredExtensionBundlesDescription() {

      IExtensionManager manager = getTestling();

      List<IExtensionBundle> bundles = manager.getRegisteredExtensionBundles();

      Assert.assertNotNull(bundles);

      Assert.assertEquals(getExpectedBundleDescriptions().size(), bundles.size());

      for (int i = 0; i < bundles.size(); ++i) {
         IExtensionBundle bundle = bundles.get(i);

         Assert.assertNotNull(bundle);
         Assert.assertNotNull(bundle.getDescription());
         final String extensionBundleId = bundle.getName();
         Assert.assertNotNull(extensionBundleId);

         Assert.assertTrue(getExpectedBundleDescriptions().containsKey(extensionBundleId));
         Assert.assertEquals(getExpectedBundleDescriptions().get(extensionBundleId), bundle.getDescription());
      }
   }

   /**
    * Tests {@link IExtensionBundle#getExtensionsForExtensionPoint(Class)}.
    */
   @Test
   public void test_getExtensionsForExtensionPoint() {

      IExtensionManager manager = getTestling();

      List<IExtensionBundle> bundles = manager.getRegisteredExtensionBundles();

      Assert.assertNotNull(bundles);

      Assert.assertEquals(getExpectedBundleDescriptions().size(), bundles.size());

      for (int i = 0; i < bundles.size(); ++i) {
         IExtensionBundle bundle = bundles.get(i);

         String id = bundle.getName();

         Map<Class<? extends IExtensionPoint>, Integer> expectedProviderCounts = getExpectedExtensionPointProviderCounts()
            .get(id);

         for (Iterator<Class<? extends IExtensionPoint>> iterator = expectedProviderCounts.keySet().iterator(); iterator
            .hasNext();) {
            Class<? extends IExtensionPoint> clazz = iterator.next();
            Integer providerCount = expectedProviderCounts.get(clazz);

            List<? extends IExtensionPoint> extPoints = bundle.getExtensionsForExtensionPoint(clazz);

            Assert.assertNotNull(extPoints);

            Assert.assertEquals(providerCount, new Integer(extPoints.size()));

            for (int j = 0; j < extPoints.size(); ++j) {
               IExtensionPoint extPoint = extPoints.get(j);

               Assert.assertNotNull(extPoint);

               Assert.assertNotNull(extPoint.getExtensionId());
            }
         }
      }
   }

   /**
    * Throws a {@link TestDataException} when loading the extension config file failed.
    * 
    * @param file
    *           The configuration {@link File}.
    * @param e
    *           The exception causing this method call.
    */
   public static void throwExceptionForLoadFailure(final File file, IOException e) {

      throw new TestDataException(
         "Extension points configuration stream could not be loaded: " + file.getAbsolutePath(), e);
   }

   /**
    * Returns the configuration file resource path
    * 
    * @return the configuration file resource path
    */
   protected abstract String getConfigFileResourcePath();

   /**
    * Returns the expected extension point provider interfaces.
    *
    * @return the expected extension point provider interfaces.
    */
   protected abstract Set<Class<? extends IExtensionPoint>> getExpectedExtensionPointProviderInterfaces();

   /**
    * Returns the expected number of the providers for each provider interface in each specific extension bundle.
    *
    * @return the number of the providers for each provider interface in each specific extension bundle.
    */
   protected abstract Map<String, Map<Class<? extends IExtensionPoint>, Integer>> getExpectedExtensionPointProviderCounts();

   /**
    * Returns the expected {@link ExtensionBundleDescription}s for each extension bundle.
    *
    * @return the expected {@link ExtensionBundleDescription}s for each extension bundle.
    */
   protected abstract Map<String, ExtensionBundleDescription> getExpectedBundleDescriptions();

   /**
    * Returns the base path to use for loading extensions
    * 
    * @return the base path to use for loading extensions
    */
   protected abstract File getFileBasePath();
}
