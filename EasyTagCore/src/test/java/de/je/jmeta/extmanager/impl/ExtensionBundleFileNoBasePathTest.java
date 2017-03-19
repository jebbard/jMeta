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
 * {@link ExtensionBundleFileNoBasePathTest} tests the {@link StandardExtensionManager} and
 * {@link StandardExtensionBundle} implementations, without specification of a base path. The extension bundles used do
 * not point to a JAR files. All extensions reside in file system.
 *
 * All extension point interfaces used are stored in the sub-package "testExtensionPoints".
 *
 * The test cases load four bundles, each with 0 up to 3 valid implementations for every extension point.
 *
 * The bundles are scattered throughout two search paths (see the extension point configuration file used).
 * 
 * Uses valid bundles 01 to 04.
 */
public class ExtensionBundleFileNoBasePathTest
   extends IExtensionManagerBundleTest {

   private static final String AUTHOR = "J. E.";

   /**
    * Bundle
    */
   static final String BUNDLE_01 = "bundle01";

   /**
    * Bundle
    */
   static final String BUNDLE_02 = "bundle02";

   /**
    * Bundle
    */
   static final String BUNDLE_03 = "bundle03";

   /**
    * Bundle
    */
   static final String BUNDLE_04 = "bundle04";

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

         m_expectedExtensionBundleDescriptions.put(BUNDLE_01,
            new ExtensionBundleDescription("My second bundle", AUTHOR, "0.2",
               new GregorianCalendar(2011, 0, 3).getTime(),
               "My second description", BundleType.CUSTOM));

         m_expectedExtensionBundleDescriptions.put(BUNDLE_02,
            new ExtensionBundleDescription("My first bundle", AUTHOR, "0.1",
               new GregorianCalendar(2011, 0, 1).getTime(), "My description",
               BundleType.DEFAULT));

         m_expectedExtensionBundleDescriptions.put(BUNDLE_03,
            new ExtensionBundleDescription("My third bundle", AUTHOR, "0.3",
               new GregorianCalendar(2011, 0, 4).getTime(),
               "My third description", BundleType.DEFAULT));

         m_expectedExtensionBundleDescriptions.put(BUNDLE_04,
            new ExtensionBundleDescription("My FOURTH bundle", AUTHOR,
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

         m_extensionPointProviderCounts.put(BUNDLE_01, providersBundle1);

         Map<Class<? extends IExtensionPoint>, Integer> providersBundle2 = new HashMap<>();

         providersBundle2.put(TestExtensionPointCompA1.class, 1);
         providersBundle2.put(TestExtensionPointCompA2.class, 0);
         providersBundle2.put(TestExtensionPointCompB1.class, 1);
         providersBundle2.put(TestExtensionPointCompC1.class, 0);
         providersBundle2.put(ExtensionPointXYZ.class, 1);

         m_extensionPointProviderCounts.put(BUNDLE_02, providersBundle2);

         Map<Class<? extends IExtensionPoint>, Integer> providersBundle3 = new HashMap<>();

         providersBundle3.put(TestExtensionPointCompA1.class, 0);
         providersBundle3.put(TestExtensionPointCompA2.class, 0);
         providersBundle3.put(TestExtensionPointCompB1.class, 1);
         providersBundle3.put(TestExtensionPointCompC1.class, 0);
         providersBundle3.put(ExtensionPointXYZ.class, 3);

         m_extensionPointProviderCounts.put(BUNDLE_03, providersBundle3);

         Map<Class<? extends IExtensionPoint>, Integer> providersBundle4 = new HashMap<>();

         providersBundle4.put(TestExtensionPointCompA1.class, 1);
         providersBundle4.put(TestExtensionPointCompA2.class, 1);
         providersBundle4.put(TestExtensionPointCompB1.class, 0);
         providersBundle4.put(TestExtensionPointCompC1.class, 1);
         providersBundle4.put(ExtensionPointXYZ.class, 1);

         m_extensionPointProviderCounts.put(BUNDLE_04, providersBundle4);
      }

      return m_extensionPointProviderCounts;
   }

   /**
    * @see de.je.jmeta.extmanager.export.IExtensionManagerBundleTest#getConfigFileResourcePath()
    */
   @Override
   protected String getConfigFileResourcePath() {

      return "positive/ExtensionBundleFileNoBasePathTest.xml";
   }

   /**
    * @see de.je.jmeta.extmanager.export.IExtensionManagerBundleTest#getFileBasePath()
    */
   @Override
   protected File getFileBasePath() {

      return null;
   }

   private Set<Class<? extends IExtensionPoint>> m_expectedExtensionPoints = null;

   private Map<String, ExtensionBundleDescription> m_expectedExtensionBundleDescriptions = null;

   private Map<String, Map<Class<? extends IExtensionPoint>, Integer>> m_extensionPointProviderCounts = null;
}
