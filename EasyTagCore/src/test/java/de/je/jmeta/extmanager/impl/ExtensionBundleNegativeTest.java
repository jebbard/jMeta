/**
 *
 * {@link StandardExtensionManagerNegativeTest}.java
 *
 * @author Jens Ebert
 *
 * @date 01.05.2011
 */

package de.je.jmeta.extmanager.impl;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Test;

import de.je.jmeta.extmanager.export.AbstractExtensionManagementTest;
import de.je.jmeta.extmanager.export.BundleLoadExceptions;
import de.je.jmeta.extmanager.export.ExtLoadExceptionReason;
import de.je.jmeta.extmanager.export.ExtensionBundleDescription;
import de.je.jmeta.extmanager.export.IExtensionBundle;
import de.je.jmeta.extmanager.export.IExtensionManager;
import de.je.jmeta.extmanager.export.IExtensionManagerBundleTest;
import de.je.jmeta.extmanager.export.InvalidExtensionBundleException;
import de.je.util.javautil.io.stream.NamedInputStream;
import de.je.util.javautil.testUtil.resource.TestResourceHelper;
import junit.framework.Assert;

/**
 * {@link ExtensionBundleNegativeTest} tests the {@link StandardExtensionBundle} class for behavior in case of wrong
 * configurations in the extension bundle XML files.
 *
 * Each test case includes several correct extension bundle XML files and a single XML extension bundle file with a
 * single error. Then, the {@link IExtensionManager} implementation must register the correct extension bundles only.
 */
public class ExtensionBundleNegativeTest
   extends AbstractExtensionManagementTest {

   /**
    * Tests the {@link StandardExtensionBundle} when loading extension bundle files with invalid bundle jar file.
    * 
    * Involved bundles are: - bundle001 to bundle004 and - invalidBundle001 to invalidBundle002
    */
   @Test
   public void test_negativeInvalidBundleJar() {

      Map<String, ExtLoadExceptionReason> expectedReasons = new HashMap<>();
      expectedReasons.put("invalidBundle001",
         ExtLoadExceptionReason.INVALID_PROVIDER_PATH);
      expectedReasons.put("invalidBundle002",
         ExtLoadExceptionReason.INVALID_PROVIDER_PATH);

      checkRegisteredExtensionBundles(CORRECT_BUNDLE_IDS,
         TestResourceHelper.resourceToFile(ExtensionBundleNegativeTest.class,
            "negativeBundles/ExtensionPointsInvalidBundleJAR.xml"),
         expectedReasons, false);
   }

   /**
    * Tests the {@link StandardExtensionBundle} when loading extension bundle files with invalid bundle type.
    * 
    * Involved bundles are: - bundle001 to bundle004 and - invalidBundle003 to invalidBundle005
    */
   @Test
   public void test_negativeInvalidBundleType() {

      Map<String, ExtLoadExceptionReason> expectedReasons = new HashMap<>();
      expectedReasons.put("invalidBundle003",
         ExtLoadExceptionReason.STREAM_FORMAT_ERROR);
      expectedReasons.put("invalidBundle004",
         ExtLoadExceptionReason.STREAM_FORMAT_ERROR);
      expectedReasons.put("invalidBundle005",
         ExtLoadExceptionReason.STREAM_FORMAT_ERROR);

      checkRegisteredExtensionBundles(CORRECT_BUNDLE_IDS,
         TestResourceHelper.resourceToFile(ExtensionBundleNegativeTest.class,
            "negativeBundles/ExtensionPointsInvalidBundleType.xml"),
         expectedReasons, true);
   }

   /**
    * Tests the {@link StandardExtensionBundle} when loading extension bundle files with invalid version.
    * 
    * Involved bundles are: - bundle001 to bundle004 and - invalidBundle006
    */
   @Test
   public void test_negativeInvalidVersion() {

      Map<String, ExtLoadExceptionReason> expectedReasons = new HashMap<>();
      expectedReasons.put("invalidBundle006",
         ExtLoadExceptionReason.STREAM_INCOMPATIBLE_VERSION);

      checkRegisteredExtensionBundles(CORRECT_BUNDLE_IDS,
         TestResourceHelper.resourceToFile(ExtensionBundleNegativeTest.class,
            "negativeBundles/ExtensionPointsInvalidBundleVersion.xml"),
         expectedReasons, false);
   }

   /**
    * Tests the {@link StandardExtensionBundle} when loading extension bundle files with invalid extension point ids
    * referred to by extensions. These bundles must not get registered.
    * 
    * Involved bundles are: - bundle001 to bundle004 and - invalidBundle007 to invalidBundle008
    */
   @Test
   public void test_negativeInvalidPointIds() {

      Map<String, ExtLoadExceptionReason> expectedReasons = new HashMap<>();
      expectedReasons.put("invalidBundle007",
         ExtLoadExceptionReason.UNKNOWN_EXTENSION_POINT);
      expectedReasons.put("invalidBundle008",
         ExtLoadExceptionReason.UNKNOWN_EXTENSION_POINT);

      checkRegisteredExtensionBundles(CORRECT_BUNDLE_IDS,
         TestResourceHelper.resourceToFile(ExtensionBundleNegativeTest.class,
            "negativeBundles/ExtensionPointsInvalidBundleExtPointIds.xml"),
         expectedReasons, false);
   }

   /**
    * Tests the {@link StandardExtensionBundle} when loading extension bundle files with invalid provider relative
    * paths. These bundles must not get registered.
    * 
    * Involved bundles are: - bundle001 to bundle004 and - invalidBundle009 to invalidBundle014
    */
   @Test
   public void test_negativeInvalidProvider() {

      Map<String, ExtLoadExceptionReason> expectedReasons = new HashMap<>();
      expectedReasons.put("invalidBundle009",
         ExtLoadExceptionReason.INVALID_EXTENSION_PROVIDER);
      expectedReasons.put("invalidBundle010",
         ExtLoadExceptionReason.INVALID_EXTENSION_PROVIDER);
      expectedReasons.put("invalidBundle011",
         ExtLoadExceptionReason.INVALID_EXTENSION_PROVIDER);
      expectedReasons.put("invalidBundle012",
         ExtLoadExceptionReason.INVALID_EXTENSION_PROVIDER);
      expectedReasons.put("invalidBundle013",
         ExtLoadExceptionReason.INVALID_EXTENSION_PROVIDER);
      expectedReasons.put("invalidBundle014",
         ExtLoadExceptionReason.INVALID_EXTENSION_PROVIDER);

      checkRegisteredExtensionBundles(CORRECT_BUNDLE_IDS,
         TestResourceHelper.resourceToFile(ExtensionBundleNegativeTest.class,
            "negativeBundles/ExtensionPointsInvalidBundleProvider.xml"),
         expectedReasons, false);
   }

   /**
    * Tests the {@link StandardExtensionBundle} when loading extension bundle files with invalid providers (e.g. no
    * class, no default constructor, not implementing the extension point interface, abstract class etc.). These bundles
    * must not get registered.
    * 
    * Involved bundles are: - bundle001 to bundle004 and - invalidBundle016 to invalidBundle017
    */
   @Test
   public void test_negativeInvalidProviderPath() {

      Map<String, ExtLoadExceptionReason> expectedReasons = new HashMap<>();
      expectedReasons.put("invalidBundle016",
         ExtLoadExceptionReason.INVALID_PROVIDER_PATH);
      expectedReasons.put("invalidBundle017",
         ExtLoadExceptionReason.INVALID_PROVIDER_PATH);

      checkRegisteredExtensionBundles(CORRECT_BUNDLE_IDS,
         TestResourceHelper.resourceToFile(ExtensionBundleNegativeTest.class,
            "negativeBundles/ExtensionPointsInvalidBundleProviderPath.xml"),
         expectedReasons, false);
   }

   /**
    * Tests the {@link StandardExtensionBundle} when loading extension bundle files with invalid XML content. These
    * bundles must not get registered.
    * 
    * Involved bundles are: - bundle001 to bundle004 and - invalidBundle018 to invalidBundle020
    */
   @Test
   public void test_negativeInvalidXML() {

      Map<String, ExtLoadExceptionReason> expectedReasons = new HashMap<>();
      expectedReasons.put("invalidBundle018",
         ExtLoadExceptionReason.STREAM_FORMAT_ERROR);
      expectedReasons.put("invalidBundle019",
         ExtLoadExceptionReason.STREAM_FORMAT_ERROR);
      expectedReasons.put("invalidBundle020",
         ExtLoadExceptionReason.STREAM_FORMAT_ERROR);

      checkRegisteredExtensionBundles(CORRECT_BUNDLE_IDS,
         TestResourceHelper.resourceToFile(ExtensionBundleNegativeTest.class,
            "negativeBundles/ExtensionPointsInvalidBundleXML.xml"),
         expectedReasons, true);
   }

   /**
    * Checks for the expected number and kind of extension bundles loaded.
    *
    * @param expectedBundleIds
    *           The ids of the expected extension bundles loaded.
    * @param configFile
    *           The path to the configuration file to test.
    * @param expectedExceptionLoadReasons
    *           The expected reasons for load failure for each bundle.
    * @param expectingOnlyStreamFormatErrors
    *           Determines whether the call is only expecting stream format errors (=true) or only other errors
    *           (=false).
    */
   private void checkRegisteredExtensionBundles(Set<String> expectedBundleIds,
      File configFile,
      Map<String, ExtLoadExceptionReason> expectedExceptionLoadReasons,
      boolean expectingOnlyStreamFormatErrors) {

      IExtensionManager manager = getTestling();

      try {
         BundleLoadExceptions loadExceptions = manager
            .load(NamedInputStream.createFromFile(configFile), null);

         Map<File, InvalidExtensionBundleException> exceptions = loadExceptions
            .getExceptions();

         Assert.assertEquals(expectedExceptionLoadReasons.size(),
            exceptions.size());

         for (Iterator<File> iterator = exceptions.keySet().iterator(); iterator
            .hasNext();) {
            File nextFile = iterator.next();
            InvalidExtensionBundleException exception = exceptions
               .get(nextFile);
            ExtensionBundleDescription nextDesc = exception.getBundleDesc();

            if (expectingOnlyStreamFormatErrors) {
               Assert.assertEquals(ExtLoadExceptionReason.STREAM_FORMAT_ERROR,
                  exception.getReason());
            }

            else {
               Assert.assertNotNull(nextDesc);

               ExtLoadExceptionReason expectedReason = expectedExceptionLoadReasons
                  .get(nextDesc.getName());
               Assert.assertTrue(
                  expectedExceptionLoadReasons.containsKey(nextDesc.getName()));
               Assert.assertEquals(expectedReason, exception.getReason());
            }
         }
      } catch (IOException e) {
         IExtensionManagerBundleTest.throwExceptionForLoadFailure(configFile,
            e);
      }

      Assert.assertTrue(manager.isLoaded());

      List<IExtensionBundle> bundles = manager.getRegisteredExtensionBundles();

      Assert.assertEquals(expectedBundleIds.size(), bundles.size());

      for (int i = 0; i < bundles.size(); ++i) {
         IExtensionBundle bundle = bundles.get(i);

         Assert.assertTrue(expectedBundleIds.contains(bundle.getName()));
      }
   }

   private final static Set<String> CORRECT_BUNDLE_IDS = new HashSet<>();

   static {
      CORRECT_BUNDLE_IDS.add(ExtensionBundleFileNoBasePathTest.BUNDLE_01);
      CORRECT_BUNDLE_IDS.add(ExtensionBundleFileNoBasePathTest.BUNDLE_02);
      CORRECT_BUNDLE_IDS.add(ExtensionBundleFileNoBasePathTest.BUNDLE_03);
      CORRECT_BUNDLE_IDS.add(ExtensionBundleFileNoBasePathTest.BUNDLE_04);
   }
}
