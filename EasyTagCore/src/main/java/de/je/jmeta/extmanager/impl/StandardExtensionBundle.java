/**
 * {@link StandardExtensionBundle}.java
 *
 * @author Jens Ebert
 * @date 28.04.11 12:50:18 (April 28, 2011)
 */

package de.je.jmeta.extmanager.impl;

import java.io.File;
import java.io.FileInputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.je.jmeta.extmanager.export.ExtLoadExceptionReason;
import de.je.jmeta.extmanager.export.ExtensionBundleDescription;
import de.je.jmeta.extmanager.export.IExtensionBundle;
import de.je.jmeta.extmanager.export.IExtensionPoint;
import de.je.jmeta.extmanager.export.InvalidExtensionBundleException;
import de.je.jmeta.extmanager.export.UnknownExtensionPointException;
import de.je.jmeta.extmanager.impl.jaxb.extbundles.BundleJarType;
import de.je.jmeta.extmanager.impl.jaxb.extbundles.ExtensionType;
import de.je.util.javautil.common.err.Reject;

/**
 * {@link StandardExtensionBundle} loads and provides all extensions from a single extension bundle. If the extension
 * bundle or a single extension is erroneous, the {@link StandardExtensionBundle} cannot be created.
 */
public class StandardExtensionBundle implements IExtensionBundle {

   /**
    * Creates a new {@link StandardExtensionBundle}.
    *
    * @param configFile
    *           The bundle configuration {@link File} to load. This must be a valid extension bundle XML {@link File}
    *           that can be loaded using JAXB.
    * @param availableExtensionPoints
    *           The available {@link IExtensionPoint} classes an extension in the new {@link StandardExtensionBundle}
    *           may implement. Any extension provider configured in the given extension bundle configuration
    *           {@link File} must implement at least one of theses interfaces.
    * @param logging
    *           The {@link ILogging} instance for logging purposes.
    * @throws InvalidExtensionBundleException
    *            If loading the extension bundle configuration {@link File} or instantiation of any of the extensions
    *            failed.
    *
    * @pre configFile.exists()
    * @pre configFile.isFile()
    */
   public StandardExtensionBundle(File configFile,
      Map<String, Class<? extends IExtensionPoint>> availableExtensionPoints) throws InvalidExtensionBundleException {
      Reject.ifNull(configFile, "configFile");
      Reject.ifNull(availableExtensionPoints, "availableExtensionPoints");
      Reject.ifFalse(configFile.exists(), "configFile.exists()");
      Reject.ifFalse(configFile.isFile(), "configFile.isFile()");

      m_loader = new ExtensionBundleLoader();

      // Load the configuration file
      try (FileInputStream configStream = new FileInputStream(configFile)) {
         m_loader.load(configStream);
      } catch (Exception e) {
         throw new InvalidExtensionBundleException(
            "Could not load configuration file due to invalid format: " + configFile.getAbsolutePath(), e, null,
            ExtLoadExceptionReason.STREAM_FORMAT_ERROR);
      }

      // Check the version of the file
      final String version = m_loader.getRootObject().getVersion();

      // Load description and name
      m_description = m_loader.getBundleDescription();

      if (!SUPPORTED_FORMAT_VERSIONS.contains(version))
         throw new InvalidExtensionBundleException(
            "Could not load configuration file because its format version " + version
               + " is unsupported. Supported versions are " + SUPPORTED_FORMAT_VERSIONS,
            null, m_description, ExtLoadExceptionReason.STREAM_INCOMPATIBLE_VERSION);

      m_name = m_loader.getBundleName();

      // Determine the class path of the extension bundle
      final BundleJarType bundleJar = m_loader.getRootObject().getBundleJar();

      File classPath = determineBundleClassPath(configFile, bundleJar);

      // Actually load the extensions themselves
      m_extensions.putAll(loadExtensions(classPath, availableExtensionPoints, bundleJar != null,
         m_loader.getExtensionDescriptions(), m_description));
   }

   /**
    * @see de.je.jmeta.extmanager.export.IExtensionBundle#getName()
    */
   @Override
   public String getName() {

      return m_name;
   }

   /**
    * @see de.je.jmeta.extmanager.export.IExtensionBundle#getDescription()
    */
   @Override
   public ExtensionBundleDescription getDescription() {

      return m_description;
   }

   /**
    * @see de.je.jmeta.extmanager.export.IExtensionBundle#getExtensionsForExtensionPoint(java.lang.Class)
    */
   @Override
   @SuppressWarnings("unchecked")
   public <T extends IExtensionPoint> List<T> getExtensionsForExtensionPoint(Class<T> type) {

      Reject.ifNull(type, "type");

      if (!m_extensions.containsKey(type))
         throw new UnknownExtensionPointException("The given extension point type " + type
            + " is none of the available types of extension points. Available types are: " + m_extensions.keySet());

      return (List<T>) m_extensions.get(type);
   }

   /**
    * @see java.lang.Object#toString()
    */
   @Override
   public String toString() {

      return "StandardExtensionBundle [m_description=" + m_description + ", m_name=" + m_name + "]";
   }

   /**
    * Determines the class path of the extension bundle which is either the path to the single JAR file specified in the
    * configuration file (if present) or the parent folder of the configuration file.
    *
    * @param configFile
    *           The extension bundle configuration {@link File}.
    * @param bundleJar
    *           The {@link BundleJarType} containing the optional information about a referenced JAR file.
    * @return the class path of the extension bundle which is either the path to the single JAR file specified in the
    *         configuration file (if present) or the parent folder of the configuration file.
    */
   private static File determineBundleClassPath(File configFile, final BundleJarType bundleJar) {

      File classPath = null;

      // The extension is contained in a JAR file
      if (bundleJar != null) {
         String jarPath = bundleJar.getJarRelPath();

         classPath = new File(configFile.getParentFile(), jarPath);
      }

      // The extension is contained as classes in the same folder as the configuration
      // file
      else
         classPath = configFile.getParentFile();

      return classPath;
   }

   /**
    * Actually loads all extensions contained in this extension bundle by instantiating each one.
    *
    * @param bundleClassPath
    *           The base class path for the extensions to be loaded.
    * @param availableExtensionPoints
    *           The available extension points.
    * @param jarClassPath
    *           Whether or not the class path must consider that a single JAR is loaded. In that case, the relative
    *           paths for each provider are ignored.
    * @param extensionDescs
    *           The detailed descriptions of each extension as loaded from the configuration file.
    * @param bundleDescription
    *           The {@link ExtensionBundleDescription}.
    * @param logging
    *           The {@link ILogging} instance for logging purposes.
    * @return All extensions as a {@link Map} per {@link IExtensionPoint} class.
    *
    * @throws InvalidExtensionBundleException
    *            If the extensions from the bundle could not be loaded due to technical errors or invalid configuration
    *            file content.
    *
    */
   private static Map<Class<? extends IExtensionPoint>, List<IExtensionPoint>> loadExtensions(File bundleClassPath,
      Map<String, Class<? extends IExtensionPoint>> availableExtensionPoints, boolean jarClassPath,
      List<ExtensionType> extensionDescs, ExtensionBundleDescription bundleDescription)
         throws InvalidExtensionBundleException {

      Map<Class<? extends IExtensionPoint>, List<IExtensionPoint>> returnedExtensions = new HashMap<>();

      // Prepare the lists for each extension
      for (Iterator<Class<? extends IExtensionPoint>> iterator = availableExtensionPoints.values().iterator(); iterator
         .hasNext();) {
         returnedExtensions.put(iterator.next(), new ArrayList<IExtensionPoint>());
      }

      if (!bundleClassPath.exists())
         throw new InvalidExtensionBundleException(
            "Bundle class path " + bundleClassPath.getAbsolutePath() + " does not exist.", null, bundleDescription,
            ExtLoadExceptionReason.INVALID_PROVIDER_PATH);

      Set<File> classPathEntries = new HashSet<>();

      classPathEntries.add(bundleClassPath);

      // Verify each extension and build up class loader paths
      for (int i = 0; i < extensionDescs.size(); ++i) {
         ExtensionType extension = extensionDescs.get(i);

         // Check the basic properties of the extension
         checkExtensionBasicProperties(extension, bundleDescription, jarClassPath);

         // The extension point of the extension is unknown
         if (!availableExtensionPoints.containsKey(extension.getExtensionPointId()))
            throw new InvalidExtensionBundleException(
               "The extension " + extension.getId() + " specifies an unknown extension point id: "
                  + extension.getExtensionPointId(),
               null, bundleDescription, ExtLoadExceptionReason.UNKNOWN_EXTENSION_POINT);

         // Only if no JAR file has been specified in the configuration file, the provider
         // class path is extended by its relative path in the configuration file
         // (which is otherwise ignored)
         if (!jarClassPath) {
            File extensionClassPath = new File(bundleClassPath, extension.getRelativePath());

            if (!extensionClassPath.exists())
               throw new InvalidExtensionBundleException(
                  "Class path " + extensionClassPath.getAbsolutePath() + " does not exist.", null, bundleDescription,
                  ExtLoadExceptionReason.INVALID_PROVIDER_PATH);

            classPathEntries.add(extensionClassPath);
         }
      }

      ClassLoader classLoader = createClassLoader(classPathEntries, bundleDescription);

      // Load all the extensions
      for (int i = 0; i < extensionDescs.size(); ++i) {
         ExtensionType extension = extensionDescs.get(i);

         Class<? extends IExtensionPoint> extensionPointInterface = availableExtensionPoints
            .get(extension.getExtensionPointId());

         // Actually load and add the provider
         returnedExtensions.get(extensionPointInterface)
            .add(loadExtensionProvider(bundleDescription, extension, extensionPointInterface, classLoader));
      }

      return returnedExtensions;
   }

   private static ClassLoader createClassLoader(Set<File> classPathEntries,
      ExtensionBundleDescription bundleDescription) throws InvalidExtensionBundleException {

      URL[] urls = new URL[classPathEntries.size()];

      int i = 0;

      try {
         for (Iterator<File> classPathIterator = classPathEntries.iterator(); classPathIterator.hasNext(); ++i) {
            File nextEntry = classPathIterator.next();
            urls[i] = nextEntry.toURI().toURL();
         }
      } catch (MalformedURLException e) {
         throw new InvalidExtensionBundleException("Malformed URL for class path entries" + classPathEntries, e,
            bundleDescription, ExtLoadExceptionReason.INVALID_EXTENSION_PROVIDER);
      }

      return new URLClassLoader(urls, Thread.currentThread().getContextClassLoader());
   }

   /**
    * Checks various basic properties of the extension to be loaded. Basic checks ensure that mandatory fields cannot be
    * empty.
    *
    * @param extension
    *           The extension description as loaded from the bundle configuration file.
    * @param bundleDescription
    *           The {@link ExtensionBundleDescription} of the bundle.
    * @param jarClassPath
    *           Whether or not the class path must consider that a single JAR is loaded. In that case, the relative
    *           paths for each provider are ignored.
    * @throws InvalidExtensionBundleException
    *            If the basic checks failed.
    */
   private static void checkExtensionBasicProperties(ExtensionType extension,
      ExtensionBundleDescription bundleDescription, boolean jarClassPath) throws InvalidExtensionBundleException {

      final String extId = extension.getId();

      if (extId == null)
         throw new InvalidExtensionBundleException("The extension must specify an id.", null, bundleDescription,
            ExtLoadExceptionReason.INVALID_EXTENSION_ID);

      if (extension.getProvider() == null)
         throw new InvalidExtensionBundleException(
            "The provider specified for extension " + extId + " must not be null.", null, bundleDescription,
            ExtLoadExceptionReason.INVALID_EXTENSION_PROVIDER);

      if (extension.getExtensionPointId() == null)
         throw new InvalidExtensionBundleException(
            "The extension point id specified for extension " + extId + " must not be null.", null, bundleDescription,
            ExtLoadExceptionReason.UNKNOWN_EXTENSION_POINT);

      if (extension.getRelativePath() == null && !jarClassPath)
         throw new InvalidExtensionBundleException(
            "The relative provider path for extension " + extId + " is null, but no jar is specified.", null,
            bundleDescription, ExtLoadExceptionReason.INVALID_PROVIDER_PATH);
   }

   /**
    * Actually loads a single {@link IExtensionPoint} (i.e. the provider of the extension).
    *
    * @param bundleDescription
    *           The {@link ExtensionBundleDescription}.
    * @param extension
    *           The description of the extension as loaded from the configuration file.
    * @param extensionPointInterface
    *           The {@link IExtensionPoint} derived class that the extension provider implements.
    * @param logging
    *           The {@link ILogging} instance for logging purposes.
    * @return The loaded provider, i.e. an instance of {@link IExtensionPoint}.
    * @throws InvalidExtensionBundleException
    *            If loading fails due to several Java reflection loading errors.
    */
   @SuppressWarnings("unchecked")
   private static IExtensionPoint loadExtensionProvider(ExtensionBundleDescription bundleDescription,
      ExtensionType extension, Class<? extends IExtensionPoint> extensionPointInterface, ClassLoader classLoader)
         throws InvalidExtensionBundleException {

      Class<? extends IExtensionPoint> providerClass = null;

      IExtensionPoint instantiatedProvider = null;
      try {
         providerClass = (Class<? extends IExtensionPoint>) classLoader.loadClass(extension.getProvider());

         if (!extensionPointInterface.isAssignableFrom(providerClass))
            throw new InvalidExtensionBundleException(
               "The extension " + extension.getId() + " specifies a provider class " + providerClass
                  + " that is not implementing the interface class " + extensionPointInterface
                  + " defined for extension point with id " + extension.getExtensionPointId() + ".",
               null, bundleDescription, ExtLoadExceptionReason.INVALID_EXTENSION_PROVIDER);

         instantiatedProvider = providerClass.newInstance();
      } catch (ClassNotFoundException e) {
         throw new InvalidExtensionBundleException(
            "Provider class for extension " + extension.getId() + " could not be loaded: " + extension.getProvider(), e,
            bundleDescription, ExtLoadExceptionReason.INVALID_EXTENSION_PROVIDER);
      } catch (InstantiationException e) {
         throw new InvalidExtensionBundleException(
            "Provider class " + providerClass + " could not be instantiated as it is an interface or abstract class.",
            e, bundleDescription, ExtLoadExceptionReason.INVALID_EXTENSION_PROVIDER);
      } catch (IllegalAccessException e) {
         throw new InvalidExtensionBundleException(
            "Provider class " + providerClass + " or its constructor is not accessible.", e, bundleDescription,
            ExtLoadExceptionReason.INVALID_EXTENSION_PROVIDER);
      }
      return instantiatedProvider;
   }

   private ExtensionBundleLoader m_loader;

   private final Map<Class<? extends IExtensionPoint>, List<IExtensionPoint>> m_extensions = new HashMap<>();

   private final ExtensionBundleDescription m_description;

   private final String m_name;

   private final static Set<String> SUPPORTED_FORMAT_VERSIONS = new HashSet<>();

   static {
      SUPPORTED_FORMAT_VERSIONS.add("1.0");
   }
}
