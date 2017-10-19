/**
 *
 * {@link MyTestConfigParams1}.java
 *
 * @author Jens Ebert
 *
 * @date 02.07.2011
 */
package de.je.util.javautil.common.config;

import java.io.File;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;

import de.je.util.javautil.common.config.handler.IConfigParamValueHandler;
import de.je.util.javautil.common.config.handler.IntegerParamHandler;
import de.je.util.javautil.common.config.handler.LogLevelParamHandler;
import de.je.util.javautil.common.config.handler.StringParamHandler;

/**
 * {@link TestConfigParams} defines some test parameters.
 *
 * @param <T>
 *           The concrete type of parameter value.
 */
public class TestConfigParams<T> extends AbstractConfigParam<T> {

   /**
    * Creates a new {@link TestConfigParams}.
    * 
    * @param id
    *           The id of the enumeration value. Must be unique.
    * @param minimumValue
    *           The minimum value (including) or null if there is none.
    * @param maximumValue
    *           The maximum value (including) or null if there is none.
    * @param defaultValue
    *           The default value. Must not be null.
    * @param enumValues
    *           An enumeration of allowed values or null if there is none.
    * @param handler
    *           The {@link IConfigParamValueHandler} that checks and converts {@link AbstractConfigParam}s.
    */
   private TestConfigParams(String id, T minimumValue, T maximumValue, T defaultValue, Set<T> enumValues,
      IConfigParamValueHandler<T> handler) {
      super(id, minimumValue, maximumValue, defaultValue, enumValues, handler);
   }

   private static final String CONFIG_PREFIX = TestConfigParams.class.getPackage().getName();
   private static final HashSet<Level> LEVEL_ENUM_VALUES = new HashSet<>();

   static {
      LEVEL_ENUM_VALUES.add(Level.INFO);
      LEVEL_ENUM_VALUES.add(Level.CONFIG);
      LEVEL_ENUM_VALUES.add(Level.WARNING);
      LEVEL_ENUM_VALUES.add(Level.FINE);
      LEVEL_ENUM_VALUES.add(Level.FINER);
      LEVEL_ENUM_VALUES.add(Level.FINEST);
      LEVEL_ENUM_VALUES.add(Level.SEVERE);
   }

   /**
    * A log file path.
    */
   public static final TestConfigParams<File> LOG_FILE_PATH = new TestConfigParams<>(CONFIG_PREFIX + ".logFilePath",
      null, null, new File("./logs"), null, new TestFileParamHandler());

   /**
    * A log level.
    */
   public static final TestConfigParams<Level> LOG_LEVEL = new TestConfigParams<>(CONFIG_PREFIX + ".logLevel", null,
      null, Level.SEVERE, LEVEL_ENUM_VALUES, new LogLevelParamHandler());

   /**
    * A timeout.
    */
   public static final TestConfigParams<Integer> TIMEOUT_BLOCK = new TestConfigParams<>(CONFIG_PREFIX + ".timeoutBlock",
      1000, Integer.MAX_VALUE, 5000, null, IntegerParamHandler.DEFAULT_INSTANCE);

   /**
    * Another timeout.
    */
   public static final TestConfigParams<Integer> TIMEOUT_IDENTIFY = new TestConfigParams<>(
      CONFIG_PREFIX + ".timeoutIdentify", 1000, Integer.MAX_VALUE, 5000, null, IntegerParamHandler.DEFAULT_INSTANCE);

   /**
    * A maximum cache region size.
    */
   public static final TestConfigParams<Integer> MAX_CACHE_REGION_SIZE = new TestConfigParams<>(
      CONFIG_PREFIX + ".maxCacheRegionSize", 1000, Integer.MAX_VALUE, Integer.MAX_VALUE, null,
      IntegerParamHandler.DEFAULT_INSTANCE);

   /**
    * A maximum binary value region size.
    */
   public static final TestConfigParams<Integer> MAX_BINARY_VALUE_FRAGMENT_SIZE = new TestConfigParams<>(
      CONFIG_PREFIX + ".maxBinaryValueFragmentSize", 1000, 9999999, 9999999, null,
      IntegerParamHandler.DEFAULT_INSTANCE);

   /**
    * A path to a configuration file.
    */
   public static final TestConfigParams<String> COMPONENT_CONFIG_PATH = new TestConfigParams<>(
      CONFIG_PREFIX + ".componentConfigPath", null, null, "./config/ComponentConfiguration.xml", null,
      StringParamHandler.DEFAULT_INSTANCE);

   /**
    * A path to an extension file.
    */
   public static final TestConfigParams<String> EXTENSIONS_CONFIG_PATH = new TestConfigParams<>(
      CONFIG_PREFIX + ".extensionsConfigPath", null, null, "./config/AllExtensionPoints.xml", null,
      StringParamHandler.DEFAULT_INSTANCE);
}
