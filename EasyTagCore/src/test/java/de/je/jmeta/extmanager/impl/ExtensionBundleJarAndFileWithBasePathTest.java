/**
 *
 * {@link StandardExtensionManagerBundleTest}.java
 *
 * @author Jens Ebert
 *
 * @date 28.04.2011
 */
package de.je.jmeta.extmanager.impl;

import java.io.File;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import de.je.jmeta.extmanager.export.BundleType;
import de.je.jmeta.extmanager.export.ExtensionBundleDescription;
import de.je.jmeta.extmanager.export.IExtensionManagerBundleTest;
import de.je.jmeta.extmanager.export.IExtensionPoint;
import de.je.jmeta.extmanager.testExtensionPoints.a.TestExtensionPointCompA1;
import de.je.jmeta.extmanager.testExtensionPoints.a.TestExtensionPointCompA2;
import de.je.jmeta.extmanager.testExtensionPoints.b.TestExtensionPointCompB1;
import de.je.jmeta.extmanager.testExtensionPoints.c.TestExtensionPointCompC1;
import de.je.jmeta.extmanager.testExtensionPoints.d.ExtensionPointXYZ;

/**
 * {@link ExtensionBundleJarAndFileWithBasePathTest} tests the {@link StandardExtensionManager} and
 * {@link StandardExtensionBundle} implementations in positive case with a specified base path. One extension bundle
 * resides in a JAR file, the other one in the file system.
 *
 * All extension point interfaces used are stored in the sub-package "testExtensionPoints".
 *
 * The test cases load two bundles, each with 0 up to 1 valid implementations for every extension point.
 *
 * The bundles are found within a single search path (see the extension point configuration file used).
 */
public class ExtensionBundleJarAndFileWithBasePathTest
   extends IExtensionManagerBundleTest {

   /**
    * @see de.je.jmeta.extmanager.export.IExtensionManagerBundleTest#getExpectedExtensionPointProviderInterfaces()
    */
   @Override
   protected Set<Class<? extends IExtensionPoint>> getExpectedExtensionPointProviderInterfaces() {

      if (m_expectedExtensionPoints == null) {
         m_expectedExtensionPoints = new HashSet<>();

         m_expectedExtensionPoints.add(TestExtensionPointCompA1.class);
         m_expectedExtensionPoints.add(TestExtensionPointCompA2.class);
         m_expectedExtensionPoints.add(TestExtensionPointCompB1.class);
         m_expectedExtensionPoints.add(TestExtensionPointCompC1.class);
         m_expectedExtensionPoints.add(ExtensionPointXYZ.class);
      }

      return m_expectedExtensionPoints;
   }

   /**
    * @see de.je.jmeta.extmanager.export.IExtensionManagerBundleTest#getExpectedBundleDescriptions()
    */
   @Override
   protected Map<String, ExtensionBundleDescription> getExpectedBundleDescriptions() {

      if (m_expectedExtensionBundleDescriptions == null) {
         m_expectedExtensionBundleDescriptions = new HashMap<>();

         m_expectedExtensionBundleDescriptions.put(BUNDLE_08,
            new ExtensionBundleDescription("My bundle for base paths", "J. E.",
               "12er124efqwe", new GregorianCalendar(2011, 0, 5).getTime(),
               "My fourth description", BundleType.CUSTOM));

         m_expectedExtensionBundleDescriptions.put(BUNDLE_09,
            new ExtensionBundleDescription("My bundle for base paths", "J. E.",
               "12er124efqwe", new GregorianCalendar(2011, 0, 5).getTime(),
               "My fourth description", BundleType.CUSTOM));
      }

      return m_expectedExtensionBundleDescriptions;
   }

   /**
    * @see de.je.jmeta.extmanager.export.IExtensionManagerBundleTest#getExpectedExtensionPointProviderCounts()
    */
   @Override
   protected Map<String, Map<Class<? extends IExtensionPoint>, Integer>> getExpectedExtensionPointProviderCounts() {

      if (m_extensionPointProviderCounts == null) {
         m_extensionPointProviderCounts = new HashMap<>();

         Map<Class<? extends IExtensionPoint>, Integer> providersBundle1 = new HashMap<>();

         providersBundle1.put(TestExtensionPointCompA1.class, 1);
         providersBundle1.put(TestExtensionPointCompA2.class, 1);
         providersBundle1.put(TestExtensionPointCompB1.class, 0);
         providersBundle1.put(TestExtensionPointCompC1.class, 1);
         providersBundle1.put(ExtensionPointXYZ.class, 1);

         m_extensionPointProviderCounts.put(BUNDLE_08, providersBundle1);

         Map<Class<? extends IExtensionPoint>, Integer> providersBundle2 = new HashMap<>();

         providersBundle2.put(TestExtensionPointCompA1.class, 1);
         providersBundle2.put(TestExtensionPointCompA2.class, 1);
         providersBundle2.put(TestExtensionPointCompB1.class, 0);
         providersBundle2.put(TestExtensionPointCompC1.class, 1);
         providersBundle2.put(ExtensionPointXYZ.class, 1);

         m_extensionPointProviderCounts.put(BUNDLE_09, providersBundle2);
      }

      return m_extensionPointProviderCounts;
   }

   /**
    * @see de.je.jmeta.extmanager.export.IExtensionManagerBundleTest#getConfigFileResourcePath()
    */
   @Override
   protected String getConfigFileResourcePath() {

      return "positive/ExtensionBundleJarAndFileWithBasePathTest.xml";
   }

   /**
    * @see de.je.jmeta.extmanager.export.IExtensionManagerBundleTest#getFileBasePath()
    */
   @Override
   protected File getFileBasePath() {

      return new File(
         "../ExtManager_Test_Impl/testBundles/valid/03_jarAndFileWithBasePath");
   }

   private static final String BUNDLE_08 = "bundle08";

   private static final String BUNDLE_09 = "bundle09";

   private Set<Class<? extends IExtensionPoint>> m_expectedExtensionPoints = null;

   private Map<String, ExtensionBundleDescription> m_expectedExtensionBundleDescriptions = null;

   private Map<String, Map<Class<? extends IExtensionPoint>, Integer>> m_extensionPointProviderCounts = null;
}
