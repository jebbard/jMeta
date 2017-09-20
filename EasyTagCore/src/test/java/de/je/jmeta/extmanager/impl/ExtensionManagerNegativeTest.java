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
import java.util.ArrayList;
import java.util.List;

import junit.framework.Assert;

import org.junit.Test;

import de.je.jmeta.extmanager.export.AbstractExtensionManagementTest;
import de.je.jmeta.extmanager.export.CouldNotLoadExtensionsException;
import de.je.jmeta.extmanager.export.ExtLoadExceptionReason;
import de.je.jmeta.extmanager.export.IExtensionManager;
import de.je.jmeta.extmanager.export.IExtensionPoint;
import de.je.util.javautil.io.stream.NamedInputStream;
import de.je.util.javautil.testUtil.resource.TestResourceHelper;

/**
 * {@link ExtensionManagerNegativeTest} tests the {@link StandardExtensionManager} class for behavior in case of wrong
 * configurations.
 */
public class ExtensionManagerNegativeTest
   extends AbstractExtensionManagementTest {

   /**
    * Tests the {@link StandardExtensionManager} when loading extension point files with XML schema errors (e.g.
    * non-regular XML, missing element etc.). The implementation must throw an {@link CouldNotLoadExtensionsException}
    * in that case.
    */
   @Test
   public void test_negativeInvalidXML() {

      checkExpectedExceptionForConfigFiles(INVALID_XML_EXT_POINT_FILES,
         ExtLoadExceptionReason.STREAM_FORMAT_ERROR);
   }

   /**
    * Tests the {@link StandardExtensionManager} when loading an extension point file with invalid extension point ids.
    */
   @Test
   public void test_negativeInvalidId() {

      checkExpectedExceptionForConfigFiles(INVALID_EXT_POINT_IDS_FILES,
         ExtLoadExceptionReason.STREAM_FORMAT_ERROR);
   }

   /**
    * Tests the {@link StandardExtensionManager} when loading an extension point file with invalid paths to extension
    * bundles.
    */
   @Test
   public void test_negativeInvalidExtensionBundlePath() {

      checkExpectedExceptionForConfigFiles(INVALID_EXT_BUNDLE_PATHS_FILES,
         ExtLoadExceptionReason.INVALID_BUNDLE_PATH);
   }

   /**
    * Tests the {@link StandardExtensionManager} when loading an extension point file with empty (and this invalid)
    * paths to extension bundles.
    */
   @Test
   public void test_negativeInvalidEmptyExtensionBundlePath() {

      checkExpectedExceptionForConfigFiles(INVALID_EMPTY_EXT_BUNDLE_PATHS_FILES,
         ExtLoadExceptionReason.INVALID_BUNDLE_PATH);
   }

   /**
    * Tests the {@link StandardExtensionManager} when loading an extension point file with an extension point interface
    * that does not exist.
    */
   @Test
   public void test_negativeInvalidExtensionPointInterface_nonExisting() {

      checkExpectedExceptionForConfigFiles(
         INVALID_EXT_POINT_NON_EXISTING_IF_FILES,
         ExtLoadExceptionReason.INVALID_EXTENSION_POINT_INTERFACE);
   }

   /**
    * Tests the {@link StandardExtensionManager} when loading an extension point file with an extension point interface
    * that is no interface.
    */
   @Test
   public void test_negativeInvalidExtensionPointInterface_noInterface() {

      checkExpectedExceptionForConfigFiles(INVALID_EXT_POINT_NON_IF_FILES,
         ExtLoadExceptionReason.INVALID_EXTENSION_POINT_INTERFACE);
   }

   /**
    * Tests the {@link StandardExtensionManager} when loading an extension point file with an extension point interface
    * that is not extending the expected interface {@link IExtensionPoint} .
    */
   @Test
   public void test_negativeInvalidExtensionPointInterface_notExtendingExpectedInterface() {

      checkExpectedExceptionForConfigFiles(
         INVALID_EXT_POINT_NON_EXTENDING_IF_FILES,
         ExtLoadExceptionReason.INVALID_EXTENSION_POINT_INTERFACE);
   }

   /**
    * Tests the {@link StandardExtensionManager} when loading an extension point file with invalid version.
    */
   @Test
   public void test_negativeInvalidVersion() {

      checkExpectedExceptionForConfigFiles(INVALID_VERSION_FILES,
         ExtLoadExceptionReason.STREAM_INCOMPATIBLE_VERSION);
   }

   /**
    * Makes the {@link Assert} checks for testing a {@link CouldNotLoadExtensionsException}.
    *
    * @param configFileList
    *           The {@link List} of configuration file relative paths to be used for testing.
    * @param expectedReason
    *           The expected reason of the exception.
    */
   private void checkExpectedExceptionForConfigFiles(List<File> configFileList,
      ExtLoadExceptionReason expectedReason) {

      for (int i = 0; i < configFileList.size(); ++i) {
         File configFile = configFileList.get(i);

         try {
            IExtensionManager manager = getTestling();

            manager.load(NamedInputStream.createFromFile(configFile), null);

            Assert.fail("Expected CouldNotLoadExtensionsException");
         }

         catch (CouldNotLoadExtensionsException e) {
            Assert.assertEquals(expectedReason, e.getReason());
         }

         catch (IOException e) {
            Assert
               .fail("Expected CouldNotLoadExtensionsException, instead: " + e);
         }
      }
   }

   private final static List<File> INVALID_XML_EXT_POINT_FILES = new ArrayList<>();

   static {
      // Not well-formed
      INVALID_XML_EXT_POINT_FILES.add(
         TestResourceHelper.resourceToFile(ExtensionManagerNegativeTest.class,
            "negativeManager/ExtensionPoints_InvalidXML_1.xml"));
      // Required tag missing
      INVALID_XML_EXT_POINT_FILES.add(
         TestResourceHelper.resourceToFile(ExtensionManagerNegativeTest.class,
            "negativeManager/ExtensionPoints_InvalidXML_2.xml"));
   }

   private final static List<File> INVALID_EXT_POINT_IDS_FILES = new ArrayList<>();

   static {
      // Duplicate id
      INVALID_EXT_POINT_IDS_FILES.add(
         TestResourceHelper.resourceToFile(ExtensionManagerNegativeTest.class,
            "negativeManager/ExtensionPoints_Invalid_id_1.xml"));
      // Missing id attribute
      INVALID_EXT_POINT_IDS_FILES.add(
         TestResourceHelper.resourceToFile(ExtensionManagerNegativeTest.class,
            "negativeManager/ExtensionPoints_Invalid_id_2.xml"));
      // non-allowed characters in id
      INVALID_EXT_POINT_IDS_FILES.add(
         TestResourceHelper.resourceToFile(ExtensionManagerNegativeTest.class,
            "negativeManager/ExtensionPoints_Invalid_id_3.xml"));
   }

   private final static List<File> INVALID_EXT_BUNDLE_PATHS_FILES = new ArrayList<>();

   static {
      // Non existing paths
      INVALID_EXT_BUNDLE_PATHS_FILES.add(
         TestResourceHelper.resourceToFile(ExtensionManagerNegativeTest.class,
            "negativeManager/ExtensionPoints_InvalidBundlePaths_1.xml"));
   }

   private final static List<File> INVALID_EMPTY_EXT_BUNDLE_PATHS_FILES = new ArrayList<>();

   static {
      // Empty path
      INVALID_EMPTY_EXT_BUNDLE_PATHS_FILES.add(
         TestResourceHelper.resourceToFile(ExtensionManagerNegativeTest.class,
            "negativeManager/ExtensionPoints_InvalidBundlePaths_2.xml"));
   }

   private final static List<File> INVALID_EXT_POINT_NON_EXISTING_IF_FILES = new ArrayList<>();

   static {
      // A non existing interface
      INVALID_EXT_POINT_NON_EXISTING_IF_FILES.add(
         TestResourceHelper.resourceToFile(ExtensionManagerNegativeTest.class,
            "negativeManager/ExtensionPoints_Invalid_IF_1.xml"));
   }

   private final static List<File> INVALID_EXT_POINT_NON_IF_FILES = new ArrayList<>();

   static {
      // Extension point if is not an interface but a class
      INVALID_EXT_POINT_NON_IF_FILES.add(
         TestResourceHelper.resourceToFile(ExtensionManagerNegativeTest.class,
            "negativeManager/ExtensionPoints_Invalid_IF_2.xml"));
   }

   private final static List<File> INVALID_EXT_POINT_NON_EXTENDING_IF_FILES = new ArrayList<>();

   static {
      // Extension point does not extend required interface
      INVALID_EXT_POINT_NON_EXTENDING_IF_FILES.add(
         TestResourceHelper.resourceToFile(ExtensionManagerNegativeTest.class,
            "negativeManager/ExtensionPoints_Invalid_IF_3.xml"));
   }

   private final static List<File> INVALID_VERSION_FILES = new ArrayList<>();

   static {
      INVALID_VERSION_FILES.add(
         TestResourceHelper.resourceToFile(ExtensionManagerNegativeTest.class,
            "negativeManager/ExtensionPoints_Invalid_Version_1.xml"));
   }
}
