
package de.je.jmeta.config.export;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import de.je.util.javautil.common.config.AbstractConfigLoader;
import de.je.util.javautil.common.config.AbstractConfigParam;
import de.je.util.javautil.common.config.PropertiesConfigLoader;
import de.je.util.javautil.common.config.issue.ConfigIssue;
import de.je.util.javautil.common.config.issue.ConfigIssueType;
import de.je.util.javautil.common.err.Contract;
import de.je.util.javautil.common.err.Reject;

/**
 * {@link AbstractConfigAccessor} provides access to configuration parameters loaded from an external file.
 */
public abstract class AbstractConfigAccessor {

   /**
    * Creates a new instance of {@link AbstractConfigAccessor}.
    * 
    * @param parameters
    *           The parameters expected to be stored in the file.
    */
   public AbstractConfigAccessor(Set<AbstractConfigParam<?>> parameters) {
      m_paramLoader = new PropertiesConfigLoader(parameters);
   }

   /**
    * Returns whether currently a configuration {@link InputStream} is loaded.
    *
    * @return true if currently a configuration {@link InputStream} is loaded, false otherwise.
    */
   public boolean isLoaded() {

      return m_configStream != null;
   }

   /**
    * Loads data from a configuration {@link InputStream}, thereby discarding previously loaded configuration data.
    * Returns a {@link List} of {@link ConfigIssue}s that occurred while loading the configuration parameters. The
    * {@link List} may be empty which means no errors occurred during configuration loading.
    * 
    * @param inputStream
    *           The configuration {@link InputStream} to load.
    *
    * @return a {@link List} of {@link ConfigIssue}s that occurred while loading the configuration parameters. The
    *         {@link List} may be empty which means no errors occurred during configuration loading.
    */
   public List<ConfigIssue> load(InputStream inputStream) {

      m_configStream = inputStream;

      m_configIssues.clear();

      try {
         List<ConfigIssue> issues = m_paramLoader.load(inputStream);

         for (int i = 0; i < issues.size(); ++i)
            m_configIssues.add(issues.get(i));
      } catch (Exception e) {
         m_configIssues.add(new ConfigIssue(ConfigIssueType.CONFIG_FILE_LOADING,
            null, null, null, null, e));
      }
      return Collections.unmodifiableList(m_configIssues);
   }

   /**
    * Returns whether the given {@link AbstractConfigParam} is maintained by this {@link AbstractConfigAccessor}.
    *
    * @param configParam
    *           The {@link AbstractConfigParam}. Must be defined in the configuration.
    * @param <T>
    *           The concrete type of the {@link UserConfigParam}'s value.
    * @return true if the given {@link AbstractConfigParam} is maintained by this {@link AbstractConfigAccessor}, false
    *         otherwise.
    */
   protected <T> boolean hasConfigParam(AbstractConfigParam<T> configParam) {

      return m_paramLoader.hasConfigParam(configParam);
   }

   /**
    * Returns the current value of the given {@link AbstractConfigParam}.
    *
    * @param configParam
    *           The {@link AbstractConfigParam}.
    * @param <T>
    *           The concrete type of the {@link AbstractConfigParam}'s value.
    * @return the current value of the given {@link AbstractConfigParam}.
    */
   protected <T> T getConfigParam(AbstractConfigParam<T> configParam) {

      Reject.ifNull(configParam, "configParam");
      Contract.checkPrecondition(m_paramLoader.hasConfigParam(configParam),
         "m_paramLoader.hasConfigParam(configParam) was false");

      return m_paramLoader.getConfigParamValue(configParam);
   }

   private InputStream m_configStream;

   private final List<ConfigIssue> m_configIssues = new ArrayList<>();

   private final AbstractConfigLoader m_paramLoader;
}