package de.je.util.javautil.common.configparams;

import java.util.Map;
import java.util.Properties;
import java.util.Set;

/**
 * {@link IConfigurable} represents a dynamically configurable class. It allows fixed given set of
 * {@link AbstractConfigParam}s, called the "supported {@link AbstractConfigParam}s" in this class documentation. Each
 * {@link AbstractConfigParam} can be set or retrieved from the instance in various ways. There are also methods that
 * support conversion to {@link Properties} instances. Furthermore the class offers possibilities to register
 * {@link IConfigChangeListener} instances that are notified whenever an {@link AbstractConfigParam} is changed by
 * calling one of the setter methods. Note that in addition, on every registration of a new
 * {@link IConfigChangeListener}, it is called once to receive the currently set value.
 * 
 * The names of all supported {@link AbstractConfigParam}s must be unique within this {@link IConfigurable} instance.
 * 
 * Whenever an invalid value is passed for an {@link AbstractConfigParam}, the setter methods will throw an
 * {@link InvalidConfigParamException}. In this case, no registered {@link IConfigChangeListener} will get notified.
 * 
 * All configuration parameters will be initialized with their default values.
 */
public interface IConfigurable {

   /**
    * @return all {@link AbstractConfigParam}s that are supported by this {@link IConfigurable} instance. No other
    *         {@link AbstractConfigParam}s are allowed to be passed to any of the methods, otherwise an
    *         {@link InvalidConfigParamException} is thrown.
    */
   public Set<AbstractConfigParam<? extends Comparable<?>>> getSupportedConfigParams();

   /**
    * @return all {@link AbstractConfigParam}s supported by this {@link IConfigurable} with their currently set values.
    */
   public Map<AbstractConfigParam<? extends Comparable<?>>, Comparable<?>> getAllConfigParams();

   /**
    * @return a {@link Properties} representation of all all {@link AbstractConfigParam}s supported by this
    *         {@link IConfigurable} with their currently set values.
    */
   public Properties getAllConfigParamsAsProperties();

   /**
    * Retrieves a value for a specific supported {@link AbstractConfigParam}.
    * 
    * @param parameter
    *           the {@link AbstractConfigParam} to set. Must be one of the supported {@link AbstractConfigParam}s.
    *           Otherwise an {@link InvalidConfigParamException} is thrown.
    * @return the currently set value of the given {@link AbstractConfigParam}.
    */
   public <T extends Comparable<T>> T getConfigParam(AbstractConfigParam<T> parameter);

   /**
    * Sets a new valid value for a specific supported {@link AbstractConfigParam}. If the parameter and its value are
    * valid, this notifies each currently registered {@link IConfigChangeListener}.
    * 
    * @param parameter
    *           the {@link AbstractConfigParam} to set. Must be one of the supported {@link AbstractConfigParam}s.
    *           Otherwise an {@link InvalidConfigParamException} is thrown.
    * @param value
    *           the value to set for the parameter. Must be a valid value for the parameter (see
    *           {@link AbstractConfigParam} documentation for what this means). If the parameter value is invalid, the
    *           method throws an {@link InvalidConfigParamException}.
    */
   public <T extends Comparable<T>> void setConfigParam(AbstractConfigParam<T> parameter, T value);

   /**
    * Resets all values of all the {@link AbstractConfigParam}s managed by this {@link IConfigurable} instance to their
    * default values. This notifies each currently registered {@link IConfigChangeListener}s.
    */
   public void resetConfigToDefault();

   /**
    * Configures this {@link IConfigurable} instance with properties coming from the given {@link Properties} instance.
    * Any properties whose names are managed by this {@link IConfigurable} as {@link AbstractConfigParam} will be taken
    * over. If the given parameter values of supported properties are invalid, this method will throw an
    * {@link InvalidConfigParamException}. The given {@link Properties} instance is allowed to contain additional
    * properties unknown to this {@link IConfigurable}.
    * 
    * For each known parameter with a valid value, each currently registered {@link IConfigChangeListener} is notified
    * once about the change.
    * 
    * @param properties
    *           The {@link Properties} instance used for configuration.
    */
   public void configureFromProperties(Properties properties);

   /**
    * Adds another {@link IConfigChangeListener}. Each added listener gets notified once as soon as any of the supported
    * {@link AbstractConfigParam}s are changed by calling any of the setter methods. It additionally is notified
    * directly once when setting it initially. Setting the same {@link IConfigChangeListener} twice has no effect,
    * especially it is not notified on each subsequent set, and also in case of any future changes, it is only notified
    * once per changed {@link AbstractConfigParam}.
    * 
    * Added listeners are not notified in any defined order, especially not necessarily in their add order.
    * 
    * @param listener
    *           the {@link IConfigChangeListener} to add
    */
   public void addConfigChangeListener(IConfigChangeListener listener);

   /**
    * Removes a previously added {@link IConfigChangeListener}. Passing a currently unregistered
    * {@link IConfigChangeListener} has no effect. After removing the {@link IConfigChangeListener} instance, no
    * notifications due to parameter value changes will be sent to the {@link IConfigChangeListener} instance anymore.
    * 
    * @param listener
    *           The {@link IConfigChangeListener} to remove.
    */
   public void removeConfigChangeListener(IConfigChangeListener listener);
}
