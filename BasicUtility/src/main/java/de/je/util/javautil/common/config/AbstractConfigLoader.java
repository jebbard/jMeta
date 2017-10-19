package de.je.util.javautil.common.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.je.util.javautil.common.config.handler.IConfigParamValueHandler;
import de.je.util.javautil.common.config.issue.ConfigIssue;
import de.je.util.javautil.common.config.issue.ConfigIssueType;
import de.je.util.javautil.common.err.Reject;

/**
 * {@link AbstractConfigLoader} is a class that loads a fixed set of configuration parameters from an external source
 * which can be an arbitrary {@link InputStream}. Each parameter's value is initially preset with its defined default
 * value. Thus is not necessary to load any external {@link InputStream}.
 *
 * When loading using {@link #load(InputStream)}, the class validates the externally stored parameter values and returns
 * corresponding problems as {@link ConfigIssue}s. Valid parameters have been set to their stored values, while invalid
 * ones retain their default value.
 *
 * This class can also be used for manipulating configuration parameter values during runtime using
 * {@link #setConfigParamValue(AbstractConfigParam, Object)}. These manipulations <i>do not</i> affect the external
 * {@link InputStream}, they only override already loaded values.
 */
public abstract class AbstractConfigLoader {

   /**
    * Creates a new {@link AbstractConfigLoader}.
    * 
    * @param configParameterSet
    *           The set of {@link AbstractConfigParam}s that can be loaded, queried and set using this
    *           {@link AbstractConfigLoader}. Must not be empty.
    */
   public AbstractConfigLoader(Set<AbstractConfigParam<?>> configParameterSet) {
      Reject.ifNull(configParameterSet, "configParameterSet");
      Reject.ifTrue(configParameterSet.isEmpty(), "configParameterSet.isEmpty()");

      // Initialize parameter values with defaults
      for (Iterator<AbstractConfigParam<?>> iterator = configParameterSet.iterator(); iterator.hasNext();) {
         AbstractConfigParam<?> nextParam = iterator.next();

         m_paramValues.put(nextParam, nextParam);
      }

      // Reset all config parameters to their defaults
      reset();
   }

   /**
    * Loads configuration parameters from the given {@link InputStream}. All configuration parameter values present and
    * valid are taken over while the values for missing configurations parameters or such with invalid value in the
    * stream are set to their defined default values.
    *
    * The method returns a {@link List} of {@link ConfigIssue}s reporting the problems when reading from the stream:
    * <ul>
    * <li>Configuration parameters not present in the stream are reported with {@link ConfigIssueType#MISSING_PARAMETER}
    * </li>
    * <li>Parameters with an empty value in the stream are reported with {@link ConfigIssueType#EMPTY_PARAMETER}</li>
    * <li>Parameters with a value in the stream that is not contained in the enumeration of allowed values for the
    * parameter are reported with {@link ConfigIssueType#NON_ENUMERATED_PARAMETER_VALUE}</li>
    * <li>Parameters in the stream that are unknown to this {@link AbstractConfigLoader} are reported with
    * {@link ConfigIssueType#UNKNOWN_PARAMETER}</li>
    * <li>Parameters with a value in the stream that cannot be converted to its interpreted representation are reported
    * with {@link ConfigIssueType#PARAMETER_VALUE_CONVERSION_FAILED}</li>
    * <li>Parameters with a value in the stream that is not within the defined bounds (see {@link AbstractConfigParam})
    * are reported with {@link ConfigIssueType#PARAMETER_VALUE_OUT_OF_BOUNDS}</li>
    * </ul>
    *
    * For all {@link AbstractConfigParam}s for which a {@link ConfigIssue} is present in the returned {@link List}, it
    * is guaranteed that they are set to their default value.
    *
    * @param stream
    *           The {@link InputStream}.
    * @return A {@link List} of {@link ConfigIssue} instances. May be empty. See method description for details.
    * @throws Exception
    *            If loading configuration parameters from the {@link InputStream} failed. This exception is only thrown
    *            due to technical reasons (medium access errors or data format errors). If parameters are missing or
    *            have invalid values, a {@link List} with {@link ConfigIssue}s is returned instead.
    */
   @SuppressWarnings({ "rawtypes", "unchecked" })
   public List<ConfigIssue> load(InputStream stream) throws Exception {
      Reject.ifNull(stream, "stream");

      // Reset all config parameters to their defaults
      reset();

      Map<String, String> configParamValues = getConfigFromStream(stream);

      List<ConfigIssue> issues = new ArrayList<>();

      for (Iterator<String> iterator = configParamValues.keySet().iterator(); iterator.hasNext();) {
         String paramId = iterator.next();
         String paramValue = configParamValues.get(paramId);

         AbstractConfigParam<?> param = searchParamForId(paramId);

         if (param == null)
            issues.add(new ConfigIssue(ConfigIssueType.UNKNOWN_PARAMETER, param, paramId, paramValue, null, null));

         else {
            if (paramValue == null || paramValue.isEmpty())
               issues.add(new ConfigIssue(ConfigIssueType.EMPTY_PARAMETER, param, paramId, paramValue, null, null));

            else {
               ConfigIssue issue = validateConfigParamValue(param, paramValue);

               // Add the parameter value
               if (issue == null)
                  m_paramValues.put(param, param.getHandler().convert((AbstractConfigParam) param, paramValue));

               else
                  issues.add(issue);
            }
         }
      }

      for (Iterator<AbstractConfigParam<?>> iterator = m_paramValues.keySet().iterator(); iterator.hasNext();) {
         AbstractConfigParam<?> param = iterator.next();

         if (!configParamValues.containsKey(param.getId()))
            issues.add(new ConfigIssue(ConfigIssueType.MISSING_PARAMETER, param, param.getId(), null, null, null));
      }

      return issues;
   }

   /**
    * Checks whether this {@link AbstractConfigLoader} can store, load and return values for the given
    * {@link AbstractConfigParam}.
    *
    * The methods {@link #getConfigParamValue(AbstractConfigParam)} and
    * {@link #setConfigParamValue(AbstractConfigParam, Object)} may not be called for an {@link AbstractConfigParam} for
    * which {@link #hasConfigParam(AbstractConfigParam)} returns false.
    *
    * @param configParam
    *           The {@link AbstractConfigParam} to query.
    * @return true if this {@link AbstractConfigLoader} can store, load and return values for the given
    *         {@link AbstractConfigParam}, false otherwise.
    */
   public boolean hasConfigParam(AbstractConfigParam<?> configParam) {
      return m_paramValues.containsKey(configParam);
   }

   /**
    * Returns the current value for the given {@link AbstractConfigParam}.
    *
    * @param <T>
    *           The concrete type of {@link AbstractConfigParam} to query.
    * @param configParam
    *           The {@link AbstractConfigParam}. Must be defined for this {@link AbstractConfigLoader} (see
    *           {@link #hasConfigParam(AbstractConfigParam)}).
    * @return The currently maintained value for the given {@link AbstractConfigParam}.
    */
   @SuppressWarnings("unchecked")
   public <T> T getConfigParamValue(AbstractConfigParam<T> configParam) {
      Reject.ifNull(configParam, "configParam");
      Reject.ifFalse(hasConfigParam(configParam), "hasConfigParam(configParam)");

      return (T) m_paramValues.get(configParam);
   }

   /**
    * Sets a new value for the given {@link AbstractConfigParam}. The value is checked to be in valid bounds and in
    * possible enumerated values for the {@link AbstractConfigParam}. If this is not the case, the
    * {@link AbstractConfigParam}'s value remains unchanged, and a single {@link ConfigIssue} reporting the kind of
    * error is returned.
    *
    * @param <T>
    *           The concrete type of {@link AbstractConfigParam} to query.
    * @param configParam
    *           The {@link AbstractConfigParam}. Must be defined for this {@link AbstractConfigLoader} (see
    *           {@link #hasConfigParam(AbstractConfigParam)}).
    * @param value
    *           The {@link AbstractConfigParam}'s value to set.
    * @return null if setting the {@link AbstractConfigParam} was successful, a {@link ConfigIssue} reporting one of the
    *         {@link ConfigIssueType}s {@link ConfigIssueType#PARAMETER_VALUE_OUT_OF_BOUNDS} if the value is out of its
    *         {@link AbstractConfigParam}'s bounds or {@link ConfigIssueType#NON_ENUMERATED_PARAMETER_VALUE} if the
    *         value is not within the defined possible enumerated values of the {@link AbstractConfigParam}.
    */
   public <T> ConfigIssue setConfigParamValue(AbstractConfigParam<T> configParam, T value) {
      Reject.ifNull(configParam, "configParam");
      Reject.ifNull(value, "value");
      Reject.ifFalse(hasConfigParam(configParam), "hasConfigParam(configParam)");

      boolean inBounds = configParam.getHandler().checkBounds(configParam, value);

      if (!inBounds)
         return new ConfigIssue(ConfigIssueType.PARAMETER_VALUE_OUT_OF_BOUNDS, configParam, configParam.getId(), null,
            value, null);

      if (configParam.getEnumValues() != null && !configParam.getEnumValues().contains(value))
         return new ConfigIssue(ConfigIssueType.NON_ENUMERATED_PARAMETER_VALUE, configParam, configParam.getId(), null,
            value, null);

      m_paramValues.put(configParam, value);

      return null;
   }

   /**
    * Resets all {@link AbstractConfigParam}s allowed for this {@link AbstractConfigLoader} to their default values.
    */
   public void reset() {
      // Initialize parameter values with defaults
      for (Iterator<AbstractConfigParam<?>> iterator = m_paramValues.keySet().iterator(); iterator.hasNext();) {
         AbstractConfigParam<?> nextParam = iterator.next();

         m_paramValues.put(nextParam, nextParam.getDefaultValue());
      }
   }

   /**
    * Implements the actual loading of configuration parameters. The method is expected to return a mapping of
    * {@link AbstractConfigParam} ids to their values stored in the stream. If there is no value for an id, the
    * {@link Map} must not contain the id as a key. If there are additional ids that are unknown to this
    * {@link AbstractConfigLoader}, these values must be contained in the returned {@link Map}
    *
    * @param stream
    *           The {@link InputStream} to load from.
    * @return A {@link Map} of {@link AbstractConfigParam} ids to their values.
    * @throws IOException
    *            if loading the {@link InputStream} failed.
    */
   protected abstract Map<String, String> getConfigFromStream(InputStream stream) throws IOException;

   /**
    * Searches the fitting {@link AbstractConfigParam} for the given string id. Returns null if there is none.
    *
    * @param paramId
    *           The parameter id.
    * @return {@link AbstractConfigParam} for the given string id. Returns null if there is none.
    */
   private AbstractConfigParam<?> searchParamForId(String paramId) {
      for (Iterator<AbstractConfigParam<?>> iterator = m_paramValues.keySet().iterator(); iterator.hasNext();) {
         AbstractConfigParam<?> param = iterator.next();

         if (param.getId().equals(paramId))
            return param;
      }

      return null;
   }

   /**
    * Validates the given value for the given {@link AbstractConfigParam} to be convertible, in bounds or within the
    * possible enumerated value for the given {@link AbstractConfigParam}.
    *
    * @param <T>
    *           The concrete type of {@link AbstractConfigParam} to query.
    * @param param
    *           The {@link AbstractConfigParam}.
    * @param value
    *           The parameter value as string representation.
    * @return A {@link ConfigIssue} if the parameter value is wrong, null otherwise.
    */
   private <T> ConfigIssue validateConfigParamValue(AbstractConfigParam<T> param, String value) {
      IConfigParamValueHandler<T> handler = param.getHandler();

      try {
         T convertedValue = handler.convert(param, value);

         if (!handler.checkBounds(param, convertedValue))
            return new ConfigIssue(ConfigIssueType.PARAMETER_VALUE_OUT_OF_BOUNDS, param, param.getId(), value,
               convertedValue, null);

         else if (param.getEnumValues() != null && !param.getEnumValues().contains(convertedValue))
            return new ConfigIssue(ConfigIssueType.NON_ENUMERATED_PARAMETER_VALUE, param, param.getId(), value,
               convertedValue, null);
      } catch (Exception e) {
         return new ConfigIssue(ConfigIssueType.PARAMETER_VALUE_CONVERSION_FAILED, param, param.getId(), value, null,
            e);
      }

      return null;
   }

   private final Map<AbstractConfigParam<?>, Object> m_paramValues = new HashMap<>();
}