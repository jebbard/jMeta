/**
 *
 * {@link LibraryJMeta}.java
 *
 * @author Jens
 *
 * @date 20.03.2016
 *
 */
package de.je.jmeta.context.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.je.jmeta.common.ILoggingMessageConstants;
import de.je.jmeta.config.export.IUserConfigAccessor;
import de.je.jmeta.config.export.UserConfigParam;
import de.je.jmeta.context.ILibraryJMeta;
import de.je.jmeta.datablocks.IDataBlockAccessor;
import de.je.jmeta.dataformats.IDataFormatRepository;
import de.je.jmeta.extmanager.export.IExtensionManager;
import de.je.util.javautil.common.config.AbstractConfigParam;
import de.je.util.javautil.common.config.issue.ConfigIssue;
import de.je.util.javautil.common.config.issue.ConfigIssueType;
import de.je.util.javautil.common.extenum.AbstractExtensibleEnum;
import de.je.util.javautil.common.registry.ComponentRegistry;
import de.je.util.javautil.io.stream.NamedInputStream;

/**
 * {@link LibraryJMeta}
 *
 */
public class LibraryJMeta implements ILibraryJMeta {

   /**
    * The official name of the library.
    */
   public final static String LIBRARY_NAME = "jMeta";
   /**
    * Official build number of the current JMeta release.
    */
   public final static String LIBRARY_RELEASE_BUILD = "999";
   /**
    * Official release date of the current JMeta release.
    */
   public final static String LIBRARY_RELEASE_DATE = "2011-01-01";
   /**
    * Official version of the current JMeta release.
    */
   public final static String LIBRARY_RELEASE_VERSION = "0.1.0";
   private final static String EXTENSION_POINTS_CONFIG = "config/ExtensionPoints.xml";
   private static final Logger LOGGER = LoggerFactory.getLogger(LibraryJMeta.class);
   private static final String MSG_CONFIG_LOAD_EXCEPTION = "All configuration parameters will be set to their defaults due to configuration loading exception";
   private static final String MSG_INSTALLATION_CHECK = ILoggingMessageConstants.LINE_SEPARATOR
      + "################################################################################"
      + ILoggingMessageConstants.LINE_SEPARATOR + "  " + LIBRARY_NAME
      + " terminates. Check your installation and contact " + LIBRARY_NAME + " support."
      + ILoggingMessageConstants.LINE_SEPARATOR
      + "################################################################################"
      + ILoggingMessageConstants.LINE_SEPARATOR;
   private static final String MSG_RESET_DEFAULT = " - value has been reset to default";
   private final static String USER_CONFIG_FILE = "." + System.getProperty("file.separator") + "config"
      + System.getProperty("file.separator") + "userConfig.properties";
   // TODO stage2_014: hide this field
   /**
    * Internally used logging trigger.
    */
   public static final String STARTUP_LOGGING_TRIGGER_ID = LIBRARY_NAME + " initial startup";

   /**
    * Helper method that generates a failed task end message.
    *
    * @param taskInfo
    *           The info message for the task.
    * @return a failed task end message for logging.
    */
   private static String failingTask(String taskInfo) {

      return ILoggingMessageConstants.LINE_SEPARATOR + ILoggingMessageConstants.LINE_SEPARATOR
         + ILoggingMessageConstants.PREFIX_TASK_FAILED + ILoggingMessageConstants.PREFIX_CRITICAL_ERROR + taskInfo
         + ILoggingMessageConstants.LINE_SEPARATOR + ILoggingMessageConstants.LINE_SEPARATOR;
   }

   /**
    * Generates a human-readable path list from the given path list by splitting all paths according to the system
    * defined path separator and creating a newline character between each path.
    *
    * @param path
    *           The path to format.
    * @return The human-readable representation of the given path.
    */
   private static String humanReadablePaths(String path) {

      String[] pathSplit = path.split(System.getProperty("path.separator"));

      String returnedPath = ILoggingMessageConstants.LINE_SEPARATOR + "     ";

      for (int i = 0; i < pathSplit.length; i++)
         returnedPath += pathSplit[i] + System.getProperty("path.separator") + ILoggingMessageConstants.LINE_SEPARATOR
            + "     ";

      return returnedPath;
   }

   /**
    * Helper method that generates a neutral task end message.
    *
    * @param taskInfo
    *           The info message for the task.
    * @return a neutral task end message for logging.
    */
   private static String neutralTask(String taskInfo) {

      return ILoggingMessageConstants.LINE_SEPARATOR + ILoggingMessageConstants.LINE_SEPARATOR
         + ILoggingMessageConstants.PREFIX_TASK_DONE_NEUTRAL + taskInfo + ILoggingMessageConstants.LINE_SEPARATOR
         + ILoggingMessageConstants.LINE_SEPARATOR;
   }

   /**
    * Helper method that generates a task start message.
    *
    * @param taskInfo
    *           The info message for the task.
    * @return a task start message for logging.
    */
   private static String startingTask(String taskInfo) {

      return ILoggingMessageConstants.LINE_SEPARATOR + ILoggingMessageConstants.LINE_SEPARATOR
         + ILoggingMessageConstants.PREFIX_TASK_STARTING + taskInfo + ILoggingMessageConstants.LINE_SEPARATOR
         + ILoggingMessageConstants.LINE_SEPARATOR;
   }

   /**
    * Helper method that generates a successful task end message.
    *
    * @param taskInfo
    *           The info message for the task.
    * @return a successful task end message for logging.
    */
   private static String successfulTask(String taskInfo) {

      return ILoggingMessageConstants.LINE_SEPARATOR + ILoggingMessageConstants.LINE_SEPARATOR
         + ILoggingMessageConstants.PREFIX_TASK_DONE_SUCCESSFUL + taskInfo + ILoggingMessageConstants.LINE_SEPARATOR
         + ILoggingMessageConstants.LINE_SEPARATOR;
   }

   /**
    * Creates a new {@link LibraryJMeta}.
    */
   public LibraryJMeta() {
      String loadComponents = "Loading components initially" + ILoggingMessageConstants.SUFFIX_TASK;

      IUserConfigAccessor userConfigAccessor = null;

      String jMetaIntro = ILoggingMessageConstants.LINE_SEPARATOR + LIBRARY_NAME + " is about to start..."
         + ILoggingMessageConstants.LINE_SEPARATOR;

      File jMetaHomeDir = null;

      //////////
      // ##### (1.) Log jMeta startup
      //////////
      try {
         LOGGER.info(jMetaIntro);

         userConfigAccessor = ComponentRegistry.lookupService(IUserConfigAccessor.class);

         logJMetaStartup();
      }

      catch (Throwable e) {
         LOGGER.error(failingTask(LIBRARY_NAME + " startup"));
         LOGGER.error("Could not startup " + LIBRARY_NAME + " due to exception.");
         LOGGER.error(getClass().getSimpleName(), e);

         LOGGER.error(MSG_INSTALLATION_CHECK);

         return;
      }

      //////////
      // ##### (2.) Load and validate user configuration
      //////////
      String loadConfig = "Loading configuration" + ILoggingMessageConstants.SUFFIX_TASK;

      LOGGER.info(startingTask(loadConfig));

      final File userConfigFile = new File(jMetaHomeDir, USER_CONFIG_FILE);

      try (InputStream configStream = new FileInputStream(userConfigFile)) {
         // Load configuration parameters
         List<ConfigIssue> userConfigIssues = userConfigAccessor.load(configStream);

         checkConfigIssues(userConfigIssues, userConfigFile.getAbsolutePath());

         // Log user configuration parameter values
         LOGGER.info(ILoggingMessageConstants.LINE_SEPARATOR + LIBRARY_NAME
            + " has loaded the following user configuration parameter values: "
            + ILoggingMessageConstants.LINE_SEPARATOR);

         // It seems that the explicit specification of generic concrete types <AbstractConfigParam, UserConfigParam>
         // in the following line is necessary for javac to compile, which is not for Eclipse java compiler...
         @SuppressWarnings({ "unchecked", "rawtypes" })
         Set<AbstractConfigParam> allUserInstances = AbstractExtensibleEnum
            .<AbstractConfigParam, UserConfigParam> values(UserConfigParam.class);

         for (@SuppressWarnings("rawtypes")
         Iterator<AbstractConfigParam> iterator = allUserInstances.iterator(); iterator.hasNext();) {
            UserConfigParam<?> configParam = (UserConfigParam<?>) iterator.next();

            if (userConfigAccessor.hasUserConfigParam(configParam))
               LOGGER.info(String.format("Parameter: <%1$s> = '%2$s'", configParam.getId(),
                  userConfigAccessor.getUserConfigParam(configParam)));
         }
      }

      catch (Throwable e) {
         LOGGER.warn("Exception during validation of configuration parameters - see below");
         LOGGER.error(getClass().getSimpleName(), e);
         LOGGER.warn(MSG_CONFIG_LOAD_EXCEPTION);
      }

      LOGGER.info(neutralTask(loadConfig));

      //////////
      // ##### (3.) Load all extensions
      //////////
      String taskLoadExtensions = "Load all extensions" + ILoggingMessageConstants.SUFFIX_TASK;

      LOGGER.info(startingTask(taskLoadExtensions));

      try (NamedInputStream extensionPointsStream = NamedInputStream.createFromResource(LibraryJMeta.class,
         EXTENSION_POINTS_CONFIG)) {
         IExtensionManager extManager = ComponentRegistry.lookupService(IExtensionManager.class);

         extManager.load(extensionPointsStream, jMetaHomeDir);

         LOGGER.info(successfulTask(taskLoadExtensions));
      } catch (Throwable e) {
         LOGGER.error(failingTask(taskLoadExtensions));
         LOGGER.error("Could not load extensions due to exception.");
         LOGGER.error(getClass().getSimpleName(), e);
         LOGGER.error(MSG_INSTALLATION_CHECK);
         return;
      }

      //////////
      // ##### (4.) Load all components initially
      //////////
      LOGGER.info(startingTask(loadComponents));

      try {
         ComponentRegistry.lookupService(IDataBlockAccessor.class);
         ComponentRegistry.lookupService(IDataFormatRepository.class);
      }

      catch (Throwable e) {
         LOGGER.error(failingTask(loadComponents));
         LOGGER.error("Runtime exception occurred during instantiation of " + LIBRARY_NAME + " components.");
         LOGGER.error(getClass().getSimpleName(), e);
         LOGGER.error(MSG_INSTALLATION_CHECK);
         return;
      }

      LOGGER.info(successfulTask(loadComponents));
      LOGGER.info(ILoggingMessageConstants.LINE_SEPARATOR + "#################################################"
         + ILoggingMessageConstants.LINE_SEPARATOR + " " + LIBRARY_NAME + " startup ended successfully"
         + ILoggingMessageConstants.LINE_SEPARATOR + "#################################################"
         + ILoggingMessageConstants.LINE_SEPARATOR);
   }

   /**
    * @see de.je.jmeta.context.IJMetaContext#getDataBlockAccessor()
    */
   @Override
   public IDataBlockAccessor getDataBlockAccessor() {

      return ComponentRegistry.lookupService(IDataBlockAccessor.class);
   }

   /**
    * @see de.je.jmeta.context.IJMetaContext#getDataFormatRepository()
    */
   @Override
   public IDataFormatRepository getDataFormatRepository() {

      return ComponentRegistry.lookupService(IDataFormatRepository.class);
   }

   /**
    * Checks the given {@link List} of {@link ConfigIssue}s and logs appropriate messages when problems are detected.
    *
    * @param issues
    *           The {@link List} of {@link ConfigIssue}s.
    * @param configStreamName
    *           The name of the configuration {@link InputStream} loaded, for files this could be the file's absolute
    *           path.
    */
   private void checkConfigIssues(List<ConfigIssue> issues, String configStreamName) {

      for (int i = 0; i < issues.size(); ++i) {
         ConfigIssue issue = issues.get(i);

         String prefix = "Validate config param <%1$s> - ";

         if (issue.getType().equals(ConfigIssueType.EMPTY_PARAMETER))
            LOGGER.warn(String.format(prefix + "Value was empty" + MSG_RESET_DEFAULT, issue.getParam()));

         else if (issue.getType().equals(ConfigIssueType.CONFIG_FILE_LOADING)) {
            LOGGER.warn(String.format("Failed to load user configuration <%1$s> due to exception - see below.",
               configStreamName));
            LOGGER.warn(MSG_CONFIG_LOAD_EXCEPTION);
         }

         else if (issue.getType().equals(ConfigIssueType.MISSING_PARAMETER))
            LOGGER.warn(String.format(prefix + "Parameter not configured" + MSG_RESET_DEFAULT, issue.getParam()));

         else if (issue.getType().equals(ConfigIssueType.NON_ENUMERATED_PARAMETER_VALUE))
            LOGGER.warn(String.format(
               prefix + "Parameter value <%2$s> is not allowed. Allowed values: <%3$s>" + MSG_RESET_DEFAULT,
               issue.getParam(), issue.getParamStringValue(), issue.getParam().getEnumValues()));

         else if (issue.getType().equals(ConfigIssueType.PARAMETER_VALUE_CONVERSION_FAILED))
            LOGGER.warn(String.format(prefix + "Parameter value <%2$s> could not be converted", issue.getParam(),
               issue.getParamStringValue()));

         else if (issue.getType().equals(ConfigIssueType.PARAMETER_VALUE_OUT_OF_BOUNDS))
            LOGGER.warn(String.format(
               prefix + "Parameter value <%2$s> out of bounds: [min=<%3$s>, max=<%4$s>]" + MSG_RESET_DEFAULT,
               issue.getParam(), issue.getParamStringValue(), issue.getParam().getMinimumValue(),
               issue.getParam().getMaximumValue()));

         else if (issue.getType().equals(ConfigIssueType.UNKNOWN_PARAMETER))
            LOGGER.warn(
               String.format(prefix + "Parameter with id <%2$s> is unknown.", issue.getParam(), issue.getParamId()));

         if (issue.getException() != null)
            LOGGER.error(getClass().getSimpleName(), issue.getException());
      }
   }

   /**
    * Logs startup.
    */
   private void logJMetaStartup() {
      /*
       * "[=================================================================]"
       * "[                   __    ___  __        _                        ]" + ILoggingStringConstants.LINE_SEPARATOR
       * + "[        ##        |__|  /   \/  | _____| |_ _____     ##         ]" +
       * ILoggingStringConstants.LINE_SEPARATOR + "[       ####    _______ / /| / \ |/ __|_   _/  _  |   ####        ]"
       * + ILoggingStringConstants.LINE_SEPARATOR +
       * "[      ######   |____  | / | | | |  ___|| |_| (_) |  ######       ]" + ILoggingStringConstants.LINE_SEPARATOR
       * + "[       ####     ____| |/  |_| |_|\___/ |___\__/\_|   ####        ]" +
       * ILoggingStringConstants.LINE_SEPARATOR + "[        ##      \____/                                ##         ]"
       * + ILoggingStringConstants.LINE_SEPARATOR +
       * "[                                                                 ]" + ILoggingStringConstants.LINE_SEPARATOR
       * +
       */

      String jMetaGreetings = ILoggingMessageConstants.LINE_SEPARATOR + ILoggingMessageConstants.LINE_SEPARATOR
         + "[=================================================================]"
         + ILoggingMessageConstants.LINE_SEPARATOR
         + "[                   __    ___  __        _                        ]"
         + ILoggingMessageConstants.LINE_SEPARATOR
         + "[         ##       |__|  /   \\/  | _____| |_ _____     ##         ]"
         + ILoggingMessageConstants.LINE_SEPARATOR
         + "[        ####   _______ / /| / \\ |/ __|_   _/  _  |   ####        ]"
         + ILoggingMessageConstants.LINE_SEPARATOR
         + "[       ######  |____  | / | | | |  ___|| |_| (_) |  ######       ]"
         + ILoggingMessageConstants.LINE_SEPARATOR
         + "[        ####    ____| |/  |_| |_|\\___/ |___\\__/\\_|   ####        ]"
         + ILoggingMessageConstants.LINE_SEPARATOR
         + "[         ##     \\____/                                ##         ]"
         + ILoggingMessageConstants.LINE_SEPARATOR
         + "[                                                                 ]"
         + ILoggingMessageConstants.LINE_SEPARATOR
         + "[    _________________________________________________________    ]"
         + ILoggingMessageConstants.LINE_SEPARATOR
         + "[                                                                 ]"
         + ILoggingMessageConstants.LINE_SEPARATOR + "[       " + LIBRARY_NAME
         + " - The Metadata and Container Format Library         ]" + ILoggingMessageConstants.LINE_SEPARATOR
         + "[    _________________________________________________________    ]"
         + ILoggingMessageConstants.LINE_SEPARATOR
         + "[                                                                 ]"
         + ILoggingMessageConstants.LINE_SEPARATOR
         + "[                © 2011-2020 Jens Ebert                           ]"
         + ILoggingMessageConstants.LINE_SEPARATOR
         + "[                                                                 ]"
         + ILoggingMessageConstants.LINE_SEPARATOR + "[               Version: " + LIBRARY_RELEASE_VERSION
         + "                                    ]" + ILoggingMessageConstants.LINE_SEPARATOR + "[               Build: "
         + LIBRARY_RELEASE_BUILD + "                                        ]" + ILoggingMessageConstants.LINE_SEPARATOR
         + "[               Release Date: " + LIBRARY_RELEASE_DATE + "                          ]"
         + ILoggingMessageConstants.LINE_SEPARATOR
         + "[                                                                 ]"
         + ILoggingMessageConstants.LINE_SEPARATOR
         + "[=================================================================]"
         + ILoggingMessageConstants.LINE_SEPARATOR + ILoggingMessageConstants.LINE_SEPARATOR;

      LOGGER.info(jMetaGreetings);

      String classPath = System.getProperty("java.class.path");
      String libraryPath = System.getProperty("java.library.path");
      String extPath = System.getProperty("java.ext.dirs");

      libraryPath = humanReadablePaths(libraryPath);
      classPath = humanReadablePaths(classPath);
      extPath = humanReadablePaths(extPath);

      String javaEnvironmentInfo = ILoggingMessageConstants.LINE_SEPARATOR + ILoggingMessageConstants.LINE_SEPARATOR
         + "**************************" + ILoggingMessageConstants.LINE_SEPARATOR + " Java environmental info: "
         + ILoggingMessageConstants.LINE_SEPARATOR + "**************************"
         + ILoggingMessageConstants.LINE_SEPARATOR + "  Java Runtime Environment version               : "
         + System.getProperty("java.version") + ILoggingMessageConstants.LINE_SEPARATOR
         + "  Java Runtime Environment vendor                : " + System.getProperty("java.vendor")
         + ILoggingMessageConstants.LINE_SEPARATOR + "  Java Runtime Environment specification version : "
         + System.getProperty("java.specification.version") + ILoggingMessageConstants.LINE_SEPARATOR
         + "  Java Runtime Environment specification vendor  : " + System.getProperty("java.specification.vendor")
         + ILoggingMessageConstants.LINE_SEPARATOR + "  Java Runtime Environment specification name    : "
         + System.getProperty("java.specification.name") + ILoggingMessageConstants.LINE_SEPARATOR
         + "  Java vendor URL                                : " + System.getProperty("java.vendor.url")
         + ILoggingMessageConstants.LINE_SEPARATOR + "  Java installation directory                    : "
         + System.getProperty("java.home") + ILoggingMessageConstants.LINE_SEPARATOR
         + "  Java Virtual Machine specification version     : " + System.getProperty("java.vm.specification.version")
         + ILoggingMessageConstants.LINE_SEPARATOR + "  Java Virtual Machine specification vendor      : "
         + System.getProperty("java.vm.specification.vendor") + ILoggingMessageConstants.LINE_SEPARATOR
         + "  Java Virtual Machine specification name        : " + System.getProperty("java.vm.specification.name")
         + ILoggingMessageConstants.LINE_SEPARATOR + "  Java Virtual Machine implementation version    : "
         + System.getProperty("java.vm.version") + ILoggingMessageConstants.LINE_SEPARATOR
         + "  Java Virtual Machine implementation vendor     : " + System.getProperty("java.vm.vendor")
         + ILoggingMessageConstants.LINE_SEPARATOR + "  Java Virtual Machine implementation name       : "
         + System.getProperty("java.vm.name") + ILoggingMessageConstants.LINE_SEPARATOR
         + "  Java class format version number               : " + System.getProperty("java.class.version")
         + ILoggingMessageConstants.LINE_SEPARATOR + "  Name of JIT compiler to use                    : "
         + System.getProperty("java.compiler") + ILoggingMessageConstants.LINE_SEPARATOR
         + "  Java class path                                : " + classPath + ILoggingMessageConstants.LINE_SEPARATOR
         + "  List of paths to search when loading libraries : " + libraryPath + ILoggingMessageConstants.LINE_SEPARATOR
         + "  Path of extension directory or directories     : " + extPath + ILoggingMessageConstants.LINE_SEPARATOR
         + ILoggingMessageConstants.LINE_SEPARATOR;

      String osEnvironmentInfo = ILoggingMessageConstants.LINE_SEPARATOR + ILoggingMessageConstants.LINE_SEPARATOR
         + "**************************************" + ILoggingMessageConstants.LINE_SEPARATOR
         + " Operating system environmental info: " + ILoggingMessageConstants.LINE_SEPARATOR
         + "**************************************" + ILoggingMessageConstants.LINE_SEPARATOR
         + "  Operating system name         : " + System.getProperty("os.name")
         + ILoggingMessageConstants.LINE_SEPARATOR + "  Operating system architecture : "
         + System.getProperty("os.arch") + ILoggingMessageConstants.LINE_SEPARATOR
         + "  Operating system version      : " + System.getProperty("os.version")
         + ILoggingMessageConstants.LINE_SEPARATOR + ILoggingMessageConstants.LINE_SEPARATOR;

      LOGGER.info(javaEnvironmentInfo);
      LOGGER.info(osEnvironmentInfo);
   }
}
