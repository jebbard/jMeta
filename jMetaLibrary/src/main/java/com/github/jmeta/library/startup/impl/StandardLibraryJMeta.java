/**
 *
 * {@link StandardLibraryJMeta}.java
 *
 * @author Jens
 *
 * @date 20.03.2016
 *
 */
package com.github.jmeta.library.startup.impl;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.jmeta.library.datablocks.api.services.DataBlockAccessor;
import com.github.jmeta.library.dataformats.api.services.DataFormatRepository;
import com.github.jmeta.library.startup.api.services.LibraryJMeta;
import com.github.jmeta.utility.compregistry.api.services.ComponentRegistry;
import com.github.jmeta.utility.extmanager.api.services.Extension;
import com.github.jmeta.utility.extmanager.api.services.ExtensionManager;
import com.github.jmeta.utility.logging.api.services.LoggingConstants;

/**
 * {@link StandardLibraryJMeta} is the default implementation of
 * {@link LibraryJMeta}.
 */
public class StandardLibraryJMeta implements LibraryJMeta {

	private static final Logger LOGGER = LoggerFactory.getLogger(StandardLibraryJMeta.class);

	private final static String VERSION_PROPERTIES_FILE_NAME = "jmeta-version.properties";

	private final static String LIBRARY_NAME = "jMeta";

	private final static String PROPERTY_LIBRARY_VERSION = "jmeta.version";
	private final static String PROPERTY_BUILD_NUMBER = "jmeta.build";
	private final static String PROPERTY_BUILD_TIMESTAMP = "jmeta.buildTimestamp";
	private final static String PROPERTY_JAR_NAME = "jmeta.jarname";

	private static final String MSG_INSTALLATION_CHECK =
		// @formatter:off
		"\n###################################################################################\n" + "  FATAL: "
			+ StandardLibraryJMeta.LIBRARY_NAME + " startup aborted. Check your installation and contact "
			+ StandardLibraryJMeta.LIBRARY_NAME + " support.\n"
			+ "###################################################################################\n";
	// @formatter:on

	/**
	 * Helper method that generates a failed task end message.
	 *
	 * @param taskInfo The info message for the task.
	 * @return a failed task end message for logging.
	 */
	private static String failingTask(String taskInfo) {

		return LoggingConstants.PREFIX_TASK_FAILED + LoggingConstants.PREFIX_CRITICAL_ERROR + taskInfo;
	}

	/**
	 * @param N The number of blanks
	 * @return N blanks
	 */
	private static String getNBlanks(int N) {
		String blanks = "";
		for (int i = 0; i < N; i++) {
			blanks += " ";
		}
		return blanks;
	}

	/**
	 * Generates a human-readable path list from the given path list by splitting
	 * all paths according to the system defined path separator and creating a
	 * newline character between each path.
	 *
	 * @param path The path to format.
	 * @return The human-readable representation of the given path.
	 */
	private static String humanReadablePaths(String path) {

		String[] pathSplit = path.split(System.getProperty("path.separator"));

		String returnedPath = LoggingConstants.LINE_SEPARATOR + "     ";

		for (int i = 0; i < pathSplit.length; i++) {
			returnedPath += pathSplit[i] + System.getProperty("path.separator") + LoggingConstants.LINE_SEPARATOR
				+ "     ";
		}

		return returnedPath;
	}

	/**
	 * Helper method that generates a task start message.
	 *
	 * @param taskInfo The info message for the task.
	 * @return a task start message for logging.
	 */
	private static String startingTask(String taskInfo) {

		return LoggingConstants.PREFIX_TASK_STARTING + taskInfo;
	}

	/**
	 * Helper method that generates a successful task end message.
	 *
	 * @param taskInfo The info message for the task.
	 * @return a successful task end message for logging.
	 */
	private static String successfulTask(String taskInfo) {

		return LoggingConstants.PREFIX_TASK_DONE_SUCCESSFUL + taskInfo;
	}

	/**
	 * Creates a new {@link StandardLibraryJMeta}.
	 */
	public StandardLibraryJMeta() {
		String jMetaIntro = StandardLibraryJMeta.LIBRARY_NAME + " is about to start...";

		StandardLibraryJMeta.LOGGER.info(jMetaIntro);

		String taskLoadVersionProperties = "Loading library version properties" + LoggingConstants.SUFFIX_TASK;

		StandardLibraryJMeta.LOGGER.info(StandardLibraryJMeta.startingTask(taskLoadVersionProperties));

		Properties versionProperties = getVersionProperties();

		if (versionProperties == null) {
			StandardLibraryJMeta.LOGGER.error("Could not load version properties, reasons see above");
			StandardLibraryJMeta.LOGGER.error(StandardLibraryJMeta.failingTask(taskLoadVersionProperties));
			StandardLibraryJMeta.LOGGER.error(StandardLibraryJMeta.MSG_INSTALLATION_CHECK);

			return;
		} else {
			StandardLibraryJMeta.LOGGER.info(StandardLibraryJMeta.successfulTask(taskLoadVersionProperties));
		}

		logJMetaStartup(versionProperties);

		String taskLoadExtensions = "Load all extensions" + LoggingConstants.SUFFIX_TASK;

		StandardLibraryJMeta.LOGGER.info(StandardLibraryJMeta.startingTask(taskLoadExtensions));

		ExtensionManager extensionManager = ComponentRegistry.lookupService(ExtensionManager.class);

		List<Extension> allExtensions = extensionManager.getAllExtensions();

		StandardLibraryJMeta.LOGGER.info("Found and loaded " + allExtensions.size() + " extensions.");

		allExtensions.forEach(this::logExtensionInfo);

		StandardLibraryJMeta.LOGGER.info(StandardLibraryJMeta.successfulTask(taskLoadExtensions));

		String taskLoadComponents = "Initializing components" + LoggingConstants.SUFFIX_TASK;

		StandardLibraryJMeta.LOGGER.info(StandardLibraryJMeta.startingTask(taskLoadComponents));

		try {
			getDataBlockAccessor();
			getDataFormatRepository();
		}

		catch (Throwable e) {
			StandardLibraryJMeta.LOGGER.error(StandardLibraryJMeta.failingTask(taskLoadComponents));
			StandardLibraryJMeta.LOGGER.error("Runtime exception occurred during initialization of "
				+ StandardLibraryJMeta.LIBRARY_NAME + " components.");
			StandardLibraryJMeta.LOGGER.error(getClass().getSimpleName(), e);
			StandardLibraryJMeta.LOGGER.error(StandardLibraryJMeta.MSG_INSTALLATION_CHECK);
			return;
		}

		StandardLibraryJMeta.LOGGER.info(StandardLibraryJMeta.successfulTask(taskLoadComponents));
		StandardLibraryJMeta.LOGGER
			.info("\n#####################################\n" + " " + StandardLibraryJMeta.LIBRARY_NAME
				+ " startup ended successfully\n" + "#####################################\n");
	}

	/**
	 * @see com.github.jmeta.library.startup.api.services.LibraryJMeta#getDataBlockAccessor()
	 */
	@Override
	public DataBlockAccessor getDataBlockAccessor() {

		return ComponentRegistry.lookupService(DataBlockAccessor.class);
	}

	/**
	 * @see com.github.jmeta.library.startup.api.services.LibraryJMeta#getDataFormatRepository()
	 */
	@Override
	public DataFormatRepository getDataFormatRepository() {

		return ComponentRegistry.lookupService(DataFormatRepository.class);
	}

	/**
	 * @param versionProperties The version {@link Properties}
	 * @param stringWidth       The width of the version line string in characters
	 *                          including unix line break
	 * @return The complete versions string lines
	 */
	private String getVersionInfoString(Properties versionProperties, int stringWidth) {
		// @formatter:off
		String versionPrefix = "[        Version         : "
			+ versionProperties.get(StandardLibraryJMeta.PROPERTY_LIBRARY_VERSION);
		String buildPrefix = "[        Build           : "
			+ versionProperties.get(StandardLibraryJMeta.PROPERTY_BUILD_NUMBER);
		String buildTimestampPrefix = "[        Build Timestamp : "
			+ versionProperties.get(StandardLibraryJMeta.PROPERTY_BUILD_TIMESTAMP);
		String jarNamePrefix = "[        Jar Name        : "
			+ versionProperties.get(StandardLibraryJMeta.PROPERTY_JAR_NAME);

		String versionLine = versionPrefix + StandardLibraryJMeta.getNBlanks(stringWidth - versionPrefix.length() - 2)
			+ "]\n";
		String buildLine = buildPrefix + StandardLibraryJMeta.getNBlanks(stringWidth - buildPrefix.length() - 2)
			+ "]\n";
		String buildTimestampLine = buildTimestampPrefix
			+ StandardLibraryJMeta.getNBlanks(stringWidth - buildTimestampPrefix.length() - 2) + "]\n";
		String jarNameLine = jarNamePrefix + StandardLibraryJMeta.getNBlanks(stringWidth - jarNamePrefix.length() - 2)
			+ "]\n";
		// @formatter:on

		return versionLine + buildLine + buildTimestampLine + jarNameLine;
	}

	/**
	 * Loads and returns the version properties. If there are any errors during
	 * this, they are logged and null is returned.
	 * 
	 * @return the version properties or null if they could not be loaded
	 */
	private Properties getVersionProperties() {
		Properties versionProperties = new Properties();
		InputStream resourceAsStream = getClass()
			.getResourceAsStream(StandardLibraryJMeta.VERSION_PROPERTIES_FILE_NAME);

		if (resourceAsStream == null) {
			StandardLibraryJMeta.LOGGER.error(
				"Resource named <" + StandardLibraryJMeta.VERSION_PROPERTIES_FILE_NAME + "> could not be loaded");
			return null;
		}

		try {
			versionProperties.load(resourceAsStream);
		} catch (IOException e) {
			StandardLibraryJMeta.LOGGER.error("I/O exception during loading resource named <"
				+ StandardLibraryJMeta.VERSION_PROPERTIES_FILE_NAME + ">");
			StandardLibraryJMeta.LOGGER.error(getClass().getSimpleName(), e);
			return null;
		}

		List<String> properties = List.of(StandardLibraryJMeta.PROPERTY_LIBRARY_VERSION,
			StandardLibraryJMeta.PROPERTY_BUILD_NUMBER, StandardLibraryJMeta.PROPERTY_BUILD_TIMESTAMP,
			StandardLibraryJMeta.PROPERTY_JAR_NAME);

		for (String propertyName : properties) {
			if (!versionProperties.containsKey(propertyName)) {
				StandardLibraryJMeta.LOGGER.error(
					"Expected property named <" + propertyName + "> in version properties file, but it was not there.");
				return null;
			}
		}

		return versionProperties;
	}

	/**
	 * Logs infos about an {@link Extension}.
	 * 
	 * @param extension The {@link Extension}
	 */
	private void logExtensionInfo(Extension extension) {
		// @formatter:off
		String extensionInfo = "Extension details of extension with id <" + extension.getExtensionId() + ">\n"
			+ "  Extension name: " + extension.getExtensionDescription().getName() + "\n" + "  Extension version: "
			+ extension.getExtensionDescription().getVersion() + "\n" + "  Author(s): "
			+ extension.getExtensionDescription().getAuthors() + "\n" + "  Time of publish: "
			+ extension.getExtensionDescription().getPublishTime() + "\n" + "  Description: "
			+ extension.getExtensionDescription().getDescription() + "\n" + "  Copyright notice: "
			+ extension.getExtensionDescription().getCopyrightNotice() + "\n" + "  License terms: "
			+ extension.getExtensionDescription().getLicenseTerms();

		StandardLibraryJMeta.LOGGER.info(extensionInfo);
		// @formatter:on
	}

	/**
	 * Logs startup.
	 */
	private void logJMetaStartup(Properties versionProperties) {
		// @formatter:off
		// NOTE: The somehow "deformed" text output is due to the backslashes...

		String bannerDelimiter = "[=================================================================]\n";

		String jMetaGreetings = "\n\n" + bannerDelimiter
			+ "[                   __    ___  _        _                         ]\n"
			+ "[         ##       |__|  /   \\/ | _____| |_ ______     ##         ]\n"
			+ "[        ####   _______ / /| /\\ |/ __|_   _/  _   |   ####        ]\n"
			+ "[       ######  |____  | / | || |  ___|| |_| (_)  |  ######       ]\n"
			+ "[        ####    ____| |/  |_||_|\\___/ |___\\__/\\__|   ####        ]\n"
			+ "[         ##     \\____/                                ##         ]\n"
			+ "[                                                                 ]\n"
			+ "[    _________________________________________________________    ]\n"
			+ "[                                                                 ]\n" + "[       "
			+ StandardLibraryJMeta.LIBRARY_NAME + " - The Metadata and Container Format Library         ]\n"
			+ "[    _________________________________________________________    ]\n"
			+ "[                                                                 ]\n"
			+ "[                \u00A92011-2020 Jens Ebert                            ]\n"
			+ "[                                                                 ]\n"
			+ getVersionInfoString(versionProperties, bannerDelimiter.length())
			+ "[                                                                 ]\n" + bannerDelimiter;

		StandardLibraryJMeta.LOGGER.info(jMetaGreetings);

		String classPath = System.getProperty("java.class.path");
		String libraryPath = System.getProperty("java.library.path");

		libraryPath = StandardLibraryJMeta.humanReadablePaths(libraryPath);
		classPath = StandardLibraryJMeta.humanReadablePaths(classPath);

		String javaEnvironmentInfo = "\n" + "**************************\n" + " Java environmental info: \n"
			+ "**************************\n" + "  Java Runtime Environment version               : "
			+ System.getProperty("java.version") + "\n" + "  Java Runtime Environment vendor                : "
			+ System.getProperty("java.vendor") + "\n" + "  Java Runtime Environment specification version : "
			+ System.getProperty("java.specification.version") + "\n"
			+ "  Java Runtime Environment specification vendor  : " + System.getProperty("java.specification.vendor")
			+ "\n" + "  Java Runtime Environment specification name    : "
			+ System.getProperty("java.specification.name") + "\n"
			+ "  Java vendor URL                                : " + System.getProperty("java.vendor.url") + "\n"
			+ "  Java installation directory                    : " + System.getProperty("java.home") + "\n"
			+ "  Java Virtual Machine specification version     : "
			+ System.getProperty("java.vm.specification.version") + "\n"
			+ "  Java Virtual Machine specification vendor      : " + System.getProperty("java.vm.specification.vendor")
			+ "\n" + "  Java Virtual Machine specification name        : "
			+ System.getProperty("java.vm.specification.name") + "\n"
			+ "  Java Virtual Machine implementation version    : " + System.getProperty("java.vm.version") + "\n"
			+ "  Java Virtual Machine implementation vendor     : " + System.getProperty("java.vm.vendor") + "\n"
			+ "  Java Virtual Machine implementation name       : " + System.getProperty("java.vm.name") + "\n"
			+ "  Java class format version number               : " + System.getProperty("java.class.version") + "\n"
			+ "  Name of JIT compiler to use                    : " + System.getProperty("java.compiler") + "\n"
			+ "  Java class path                                : " + classPath + "\n"
			+ "  List of paths to search when loading libraries : " + libraryPath;

		String osEnvironmentInfo = "\n" + "**************************************" + "\n"
			+ " Operating system environmental info: " + "\n" + "**************************************" + "\n"
			+ "  Operating system name         : " + System.getProperty("os.name") + "\n"
			+ "  Operating system architecture : " + System.getProperty("os.arch") + "\n"
			+ "  Operating system version      : " + System.getProperty("os.version");

		StandardLibraryJMeta.LOGGER.info(javaEnvironmentInfo);
		StandardLibraryJMeta.LOGGER.info(osEnvironmentInfo);
		// @formatter:on
	}
}
