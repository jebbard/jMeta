/**
 * {@link UserConfigParam}.java
 *
 * @author Jens Ebert
 * @date 28.06.11 15:26:21 (June 28, 2011)
 */

package de.je.util.javautil.common.configparams;

import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;

import de.je.util.javautil.common.config.AbstractConfigParam;
import de.je.util.javautil.common.config.handler.IConfigParamValueHandler;
import de.je.util.javautil.common.config.handler.LogLevelParamHandler;
import de.je.util.javautil.common.config.handler.StringParamHandler;

/**
 * {@link UserConfigParam} represents a user configuration parameter.
 * 
 * @param <T>
 *           The concrete data type of the parameter's value.
 */
public final class UserConfigParam<T> extends AbstractConfigParam<T> {

   /**
    * Prefix of each configuration parameter id used by all {@link UserConfigParam} instances.
    */
   public static final String CONFIG_PREFIX = UserConfigParam.class.getPackage().getName();

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
    * Absolute or relative path to the central log file.
    */
   public static final UserConfigParam<String> LOG_FILE_PATH = new UserConfigParam<>(CONFIG_PREFIX + ".logFilePath",
      null, null, "./logs", null, StringParamHandler.DEFAULT_INSTANCE);

   /**
    * Log level to use when logging to the central or any of its child log files.
    */
   public static final UserConfigParam<Level> LOG_LEVEL = new UserConfigParam<>(CONFIG_PREFIX + ".logLevel", null, null,
      Level.FINEST, LEVEL_ENUM_VALUES, new LogLevelParamHandler());

   /**
    * Creates a new {@link UserConfigParam}.
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
   private UserConfigParam(String id, T minimumValue, T maximumValue, T defaultValue, Set<T> enumValues,
      IConfigParamValueHandler<T> handler) {
      super(id, minimumValue, maximumValue, defaultValue, enumValues, handler);
   }
}
