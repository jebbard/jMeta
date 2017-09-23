/**
 * {@link StandardExtensionManager}.java
 *
 * @author Jens Ebert
 * @date 28.04.11 12:50:18 (April 28, 2011)
 */

package de.je.jmeta.extmanager.impl;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.je.jmeta.common.ILoggingMessageConstants;
import de.je.jmeta.extmanager.export.BundleLoadExceptions;
import de.je.jmeta.extmanager.export.CouldNotLoadExtensionsException;
import de.je.jmeta.extmanager.export.ExtLoadExceptionReason;
import de.je.jmeta.extmanager.export.IExtensionBundle;
import de.je.jmeta.extmanager.export.IExtensionManager;
import de.je.jmeta.extmanager.export.IExtensionPoint;
import de.je.jmeta.extmanager.export.InvalidExtensionBundleException;
import de.je.jmeta.extmanager.impl.jaxb.extpoints.ExtensionPointType;
import de.je.util.javautil.common.err.Reject;
import de.je.util.javautil.io.stream.NamedInputStream;
import de.je.util.javautil.xml.jaxbloader.JAXBLoaderException;

/**
 * {@link StandardExtensionManager} is a default implementation of the {@link IExtensionManager} interface that loads
 * several {@link IExtensionBundle}s based on XML configuration files.
 */
public class StandardExtensionManager implements IExtensionManager {

   private static final Logger LOGGER = LoggerFactory.getLogger(StandardExtensionManager.class);

   /**
    * Creates a new {@link StandardExtensionManager}. No parameters must be added as this class may be dynamically
    * instantiated by reflection by a component loader.
    */
   public StandardExtensionManager() {

      m_loader = new ExtensionPointsLoader();
   }

   /**
    * @see de.je.jmeta.extmanager.export.IExtensionManager#getRegisteredExtensionBundles()
    */
   @Override
   public List<IExtensionBundle> getRegisteredExtensionBundles() {

      return m_extensionBundles;
   }

   /**
    * @see de.je.jmeta.extmanager.export.IExtensionManager#getAvailableExtensionPoints()
    */
   @Override
   public Set<Class<? extends IExtensionPoint>> getAvailableExtensionPoints() {

      return new HashSet<>(m_extensionPointIds.values());
   }

   /**
    * @see de.je.jmeta.extmanager.export.IExtensionManager#load(NamedInputStream, File)
    */
   @Override
   public BundleLoadExceptions load(NamedInputStream extensionPointStream, File basePath) {

      Reject.ifNull(extensionPointStream, "extensionPointStream");

      File basePathToUse = basePath;

      if (basePathToUse == null)
         basePathToUse = new File(".");

      m_extensionBundles.clear();
      m_extensionPointIds.clear();

      String streamId = extensionPointStream.getName();

      final String loadingExtensionPoints = "Loading extension point configuration stream with id <" + streamId + ">"
         + ILoggingMessageConstants.SUFFIX_TASK;
      LOGGER.info(ILoggingMessageConstants.PREFIX_TASK_STARTING + loadingExtensionPoints);

      // Load the configuration file and unmarshal to Java classes
      try {
         m_loader.load(extensionPointStream);
      } catch (JAXBLoaderException e) {
         LOGGER.error(ILoggingMessageConstants.PREFIX_TASK_FAILED + ILoggingMessageConstants.PREFIX_CRITICAL_ERROR
            + loadingExtensionPoints);
         LOGGER.error("Exception during loading extension point configuration stream with id <%1$s>.", streamId);
         LOGGER.error("loadConfigurationFile", e);
         throw new CouldNotLoadExtensionsException(
            "Could not load configuration stream due to invalid format: " + streamId, e,
            ExtLoadExceptionReason.STREAM_FORMAT_ERROR);
      }

      // Check version compliance
      final String version = m_loader.getRootObject().getVersion();

      if (!SUPPORTED_FORMAT_VERSIONS.contains(version)) {
         LOGGER.error(ILoggingMessageConstants.PREFIX_TASK_FAILED + ILoggingMessageConstants.PREFIX_CRITICAL_ERROR
            + loadingExtensionPoints);
         final String message = String.format(
            "Could not load extension point configuration stream with id <%1$s> because its format version <%2$s> is unsupported. Supported versions are <%3$s>.",
            streamId, version, SUPPORTED_FORMAT_VERSIONS);
         LOGGER.error(message);
         throw new CouldNotLoadExtensionsException(message, null, ExtLoadExceptionReason.STREAM_INCOMPATIBLE_VERSION);
      }

      // Load extension point classes
      m_extensionPointIds.putAll(getExtensionPointClasses(m_loader.getExtensionPointDescriptions()));

      LOGGER.info(ILoggingMessageConstants.PREFIX_TASK_DONE_SUCCESSFUL + loadingExtensionPoints);
      final String loadingExtBundles = "Loading extension bundles for extension points configuration stream with id <"
         + streamId + ">" + ILoggingMessageConstants.SUFFIX_TASK;
      LOGGER.info(ILoggingMessageConstants.PREFIX_TASK_STARTING + loadingExtBundles);

      // Get the extension bundle search paths
      List<File> extensionBundleSearchPaths = convertExtensionBundleSearchPaths(
         m_loader.getExtensionBundleSearchPaths(), basePath);

      // Get the extension bundles
      List<File> bundleConfigFiles = determineExtensionBundleConfigurations(extensionBundleSearchPaths);

      BundleLoadExceptions loadExceptions = new BundleLoadExceptions();

      m_extensionBundles.addAll(loadExtensionBundles(bundleConfigFiles, m_extensionPointIds, loadExceptions));

      // Determine whether the extension ids are unique
      checkExtensionIdUniqueness(m_extensionBundles, m_extensionPointIds);

      LOGGER.info(ILoggingMessageConstants.PREFIX_TASK_DONE_SUCCESSFUL + loadingExtBundles);

      return loadExceptions;
   }

   /**
    * @see de.je.jmeta.extmanager.export.IExtensionManager#isLoaded()
    */
   @Override
   public boolean isLoaded() {

      return m_loader.isLoaded();
   }

   /**
    * Returns a path that is absolute using the given path in the and - if this one is not already absolute - the given
    * base path.
    *
    * @param path
    *           The path to make absolute.
    * @param basePath
    *           The base path to use when working with relative paths.
    * @return The absolute provider path.
    */
   private static File getAbsolutePath(final String path, File basePath) {

      final File filePath = new File(path);

      File absoluteFilePath = filePath;

      if (!filePath.isAbsolute())
         absoluteFilePath = new File(basePath, path);
      return absoluteFilePath;
   }

   /**
    * Validates and converts the search paths for extension bundles previously loaded from the extensions configuration
    * file to a {@link List} of existing files.
    *
    * @param extensionBundleSearchPaths
    *           The search paths for extension bundles previously loaded from the extensions configuration file.
    * @param basePath
    *           The base path to use when working with relative paths.
    * @return a {@link List} of existing files corresponding to the specified paths.
    */
   private static List<File> convertExtensionBundleSearchPaths(List<String> extensionBundleSearchPaths, File basePath) {

      List<File> convertedSearchPaths = new ArrayList<>();

      for (int i = 0; i < extensionBundleSearchPaths.size(); ++i) {
         String extensionBundlePath = extensionBundleSearchPaths.get(i);

         if (extensionBundlePath == null)
            throw new CouldNotLoadExtensionsException("An extension bundle path must not be emtpy.", null,
               ExtLoadExceptionReason.INVALID_BUNDLE_PATH);

         final File extensionBundlePathFile = getAbsolutePath(extensionBundlePath, basePath);

         if (!extensionBundlePathFile.exists())
            throw new CouldNotLoadExtensionsException(
               "The specified extension bundle path " + extensionBundlePathFile.getAbsolutePath() + " does not exist.",
               null, ExtLoadExceptionReason.INVALID_BUNDLE_PATH);

         if (!extensionBundlePathFile.isDirectory())
            throw new CouldNotLoadExtensionsException("The specified extension bundle path "
               + extensionBundlePathFile.getAbsolutePath() + " must be a directory.", null,
               ExtLoadExceptionReason.INVALID_BUNDLE_PATH);

         convertedSearchPaths.add(extensionBundlePathFile);
      }

      return convertedSearchPaths;
   }

   /**
    * Determines a {@link List} of extension bundle configuration files found in the given search paths.
    *
    * @param extensionBundlePaths
    *           The paths for searching for extension bundle configuration files.
    * @return a {@link List} of extension bundle configuration files found in the given search paths.
    */
   @SuppressWarnings("synthetic-access")
   private static List<File> determineExtensionBundleConfigurations(List<File> extensionBundlePaths) {

      List<File> extensionBundleConfigurations = new ArrayList<>();

      for (int i = 0; i < extensionBundlePaths.size(); ++i) {
         File nextPath = extensionBundlePaths.get(i);

         if (nextPath.isDirectory()) {
            final File[] filteredFiles = nextPath.listFiles(THE_FILTER);
            extensionBundleConfigurations.addAll(Arrays.asList(filteredFiles));
         }
      }

      return extensionBundleConfigurations;
   }

   /**
    * Actually loads the extension point classes.
    * 
    * @param extensionPointTypes
    *           The information about all the supported extension points as loaded from the extensions configuration
    *           file.
    *
    * @return All extension point classes loaded, mapped to the extension point id.
    */
   @SuppressWarnings("unchecked")
   private Map<String, Class<? extends IExtensionPoint>> getExtensionPointClasses(
      List<ExtensionPointType> extensionPointTypes) {

      Map<String, Class<? extends IExtensionPoint>> extensionPoints = new HashMap<>();

      for (int i = 0; i < extensionPointTypes.size(); ++i) {
         ExtensionPointType extensionPoint = extensionPointTypes.get(i);

         String interfaceName = extensionPoint.getExtensionInterface();

         if (interfaceName == null)
            throw new CouldNotLoadExtensionsException(
               "Extension point " + extensionPoint.getId() + " must specify exactly one interface name.", null,
               ExtLoadExceptionReason.INVALID_EXTENSION_POINT_INTERFACE);

         try {
            Class<?> interfaceClass = Thread.currentThread().getContextClassLoader().loadClass(interfaceName);

            if (!interfaceClass.isInterface())
               throw new CouldNotLoadExtensionsException("The extension point " + interfaceName
                  + " specified by extension point " + extensionPoint.getId() + " is not a Java interface", null,
                  ExtLoadExceptionReason.INVALID_EXTENSION_POINT_INTERFACE);

            if (!IExtensionPoint.class.isAssignableFrom(interfaceClass))
               throw new CouldNotLoadExtensionsException(
                  "The extension point " + interfaceName + " specified by extension point " + extensionPoint.getId()
                     + " must extend the interface " + IExtensionPoint.class.getCanonicalName(),
                  null, ExtLoadExceptionReason.INVALID_EXTENSION_POINT_INTERFACE);

            if (extensionPoints.values().contains(interfaceClass))
               LOGGER.warn(
                  "Extension point interface <%1$s> is configured multiple times in configuration file for extension point <%2$s>.",
                  interfaceClass, extensionPoint.getId());

            else {
               if (extensionPoints.containsKey(extensionPoint.getId()))
                  LOGGER.warn(
                     "Extension point <%1$s> with extension interface <%2$s> is ignored because its id is used multiple times.",
                     extensionPoint.getId(), interfaceClass);

               else {
                  final Class<? extends IExtensionPoint> interfaceClazz = (Class<? extends IExtensionPoint>) interfaceClass;
                  extensionPoints.put(extensionPoint.getId(), interfaceClazz);
               }
            }
         }

         catch (ClassNotFoundException e) {
            throw new CouldNotLoadExtensionsException("Could not find class with name " + interfaceName
               + " specified by extension point " + extensionPoint.getId(), e,
               ExtLoadExceptionReason.INVALID_EXTENSION_POINT_INTERFACE);
         }
      }

      return extensionPoints;
   }

   /**
    * Loads each single extension bundle corresponding to each previously found extension bundle file. If a single
    * bundle could not be loaded, this is logged as a warning only, and the next bundle is tried to be loaded.
    *
    * @param bundleConfigurations
    *           The {@link List} of extension bundle configuration files. For each of them it is tried to load an
    *           extension bundle.
    * @param extensionPointIds
    *           All supported extension point interfaces.
    * @param loadExceptions
    *           An object to store any exceptions that occured during extension bundle loading.
    * @return All extension bundles that could be successfully loaded.
    */
   private List<IExtensionBundle> loadExtensionBundles(List<File> bundleConfigurations,
      Map<String, Class<? extends IExtensionPoint>> extensionPointIds, final BundleLoadExceptions loadExceptions) {

      List<IExtensionBundle> extensionBundles = new ArrayList<>();

      for (int i = 0; i < bundleConfigurations.size(); ++i) {
         File bundleConfigFile = bundleConfigurations.get(i);

         try {
            final String message = "Trying to load bundle config file <%1$s>";
            LOGGER.info(ILoggingMessageConstants.PREFIX_TASK_STARTING + message + ILoggingMessageConstants.SUFFIX_TASK,
               bundleConfigFile.getAbsolutePath());
            extensionBundles.add(new StandardExtensionBundle(bundleConfigFile, extensionPointIds));
            LOGGER.info(
               ILoggingMessageConstants.PREFIX_TASK_DONE_SUCCESSFUL + message + ILoggingMessageConstants.SUFFIX_TASK,
               bundleConfigFile.getAbsolutePath());
         } catch (InvalidExtensionBundleException e) {
            loadExceptions.addExtensionLoadingException(bundleConfigFile, e);

            LOGGER.warn("Could not load extension bundle from config file <%1$s> due to exception (see below).",
               bundleConfigFile.getAbsolutePath());

            LOGGER.error("loadExtensionBundles", e);
         }
      }

      return extensionBundles;
   }

   /**
    * Checks whether each extension has a unique id amongst all of the successfully loaded extensions. If this is not
    * the case, this method currently only logs a warning.
    *
    * @param bundles
    *           The {@link IExtensionBundle}s previously loaded.
    * @param extensionPointIds
    *           All supported extension point interfaces.
    *
    */
   private void checkExtensionIdUniqueness(List<IExtensionBundle> bundles,
      Map<String, Class<? extends IExtensionPoint>> extensionPointIds) {

      // Set for checking uniqueness of ids.
      Set<String> extensionIds = new HashSet<>();

      for (int i = 0; i < bundles.size(); ++i) {
         IExtensionBundle bundle = bundles.get(i);

         for (Iterator<String> iterator = extensionPointIds.keySet().iterator(); iterator.hasNext();) {
            String extensionPointId = iterator.next();

            Class<? extends IExtensionPoint> extensionPointClass = extensionPointIds.get(extensionPointId);

            for (int j = 0; j < bundle.getExtensionsForExtensionPoint(extensionPointClass).size(); ++j) {
               IExtensionPoint extensionPoint = bundle.getExtensionsForExtensionPoint(extensionPointClass).get(j);

               if (extensionIds.contains(extensionPoint.getExtensionId()))
                  LOGGER.warn(
                     "Extension id <%1$s> in bundle <%2$s> is already used by another loaded extension. This may cause problems when trying to identify a single extension.",
                     extensionPoint.getExtensionId(), bundle.getName());
               else
                  extensionIds.add(extensionPoint.getExtensionId());
            }
         }
      }
   }

   /**
    * {@link ExtensionBundleFileNameFilter} filters a folder for extension bundle configuration, i.e. files with the
    * name {@link #EXTENSION_BUNDLE_FILE_NAME}
    */
   private static final class ExtensionBundleFileNameFilter implements FilenameFilter {

      /**
       * @see java.io.FilenameFilter#accept(java.io.File, java.lang.String)
       */
      @Override
      public boolean accept(File dir, String name) {

         Reject.ifNull(name, "name");
         Reject.ifNull(dir, "dir");

         return name.equals(EXTENSION_BUNDLE_FILE_NAME);
      }
   }

   private final List<IExtensionBundle> m_extensionBundles = new ArrayList<>();

   private final Map<String, Class<? extends IExtensionPoint>> m_extensionPointIds = new HashMap<>();

   private final ExtensionPointsLoader m_loader;

   private final static ExtensionBundleFileNameFilter THE_FILTER = new ExtensionBundleFileNameFilter();

   private final static String EXTENSION_BUNDLE_FILE_NAME = "ExtensionBundle.xml";

   private final static Set<String> SUPPORTED_FORMAT_VERSIONS = new HashSet<>();

   static {
      SUPPORTED_FORMAT_VERSIONS.add("1.0");
   }
}
