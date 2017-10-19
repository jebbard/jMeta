package de.je.util.javautil.common.configparams;

import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import de.je.util.javautil.common.err.Reject;

/**
 * {@link ConfigParamHandler} is the default implementation of {@link IConfigurable}. Every class implementing
 * {@link IConfigurable} should either derive from {@link ConfigParamHandler} (if possible) or create a new private
 * instance of {@link ConfigParamHandler} and delegate all method calls to this class. Its constructor takes all
 * configuration parameters that need to be supported.
 */
public class ConfigParamHandler implements IConfigurable {

   private Map<AbstractConfigParam<? extends Comparable<?>>, Comparable<?>> currentParamValues = new HashMap<>();
   private Map<String, AbstractConfigParam<? extends Comparable<?>>> paramsByName = new HashMap<>();

   private Set<IConfigChangeListener> listeners = new HashSet<>();

   /**
    * Creates a new {@link ConfigParamHandler}.
    * 
    * @param supportedParameters
    *           The supported {@link AbstractConfigParam}s. For ease of specification, it is an array. The array must
    *           not contain two {@link AbstractConfigParam} instances with the same name, especially they must not
    *           contain the same instance twice. The specified array must contain at least one
    *           {@link AbstractConfigParam}.
    */
   public ConfigParamHandler(AbstractConfigParam<? extends Comparable<?>>[] supportedParameters) {
      Reject.ifNull(supportedParameters, "supportedParameters");
      Reject.ifTrue(supportedParameters.length == 0, "supportedParameters.length == 0");

      for (int i = 0; i < supportedParameters.length; i++) {
         AbstractConfigParam<? extends Comparable<?>> currentParam = supportedParameters[i];

         if (paramsByName.containsKey(currentParam.getName())) {
            throw new IllegalArgumentException("The parameter with name <" + currentParam.getName()
               + "> is contained twice. The given array must contain unique parameter names only.");
         }

         setConfigParamInternal(currentParam, currentParam.getDefaultValue());
         paramsByName.put(currentParam.getName(), currentParam);
      }
   }

   /**
    * @see de.je.util.javautil.common.configparams.IConfigurable#getSupportedConfigParams()
    */
   @Override
   public Set<AbstractConfigParam<? extends Comparable<?>>> getSupportedConfigParams() {
      return currentParamValues.keySet();
   }

   /**
    * @see de.je.util.javautil.common.configparams.IConfigurable#getAllConfigParams()
    */
   @Override
   public Map<AbstractConfigParam<? extends Comparable<?>>, Comparable<?>> getAllConfigParams() {
      return Collections.unmodifiableMap(currentParamValues);
   }

   /**
    * @see de.je.util.javautil.common.configparams.IConfigurable#getAllConfigParamsAsProperties()
    */
   @Override
   public Properties getAllConfigParamsAsProperties() {
      Properties returnedProperties = new Properties();

      for (Iterator<AbstractConfigParam<? extends Comparable<?>>> iterator = currentParamValues.keySet()
         .iterator(); iterator.hasNext();) {
         AbstractConfigParam<? extends Comparable<?>> nextConfigParam = iterator.next();
         String string = nextConfigParam.valueToString(currentParamValues.get(nextConfigParam));
         returnedProperties.setProperty(nextConfigParam.getName(), string);
      }

      return returnedProperties;
   }

   /**
    * @see de.je.util.javautil.common.configparams.IConfigurable#getConfigParam(de.je.util.javautil.common.configparams.AbstractConfigParam)
    */
   @SuppressWarnings("unchecked")
   @Override
   public <T extends Comparable<T>> T getConfigParam(AbstractConfigParam<T> parameter) {

      checkSupportedParameter(parameter);

      return (T) currentParamValues.get(parameter);
   }

   /**
    * @see de.je.util.javautil.common.configparams.IConfigurable#setConfigParam(de.je.util.javautil.common.configparams.AbstractConfigParam,
    *      java.lang.Comparable)
    */
   @Override
   public <T extends Comparable<T>> void setConfigParam(AbstractConfigParam<T> parameter, T value) {
      checkSupportedParameter(parameter);

      setConfigParamInternal(parameter, value);
   }

   /**
    * @see de.je.util.javautil.common.configparams.IConfigurable#resetConfigToDefault()
    */
   @Override
   public void resetConfigToDefault() {
      for (Iterator<AbstractConfigParam<? extends Comparable<?>>> iterator = currentParamValues.keySet()
         .iterator(); iterator.hasNext();) {
         AbstractConfigParam<? extends Comparable<?>> nextConfigParam = iterator.next();
         currentParamValues.put(nextConfigParam, nextConfigParam.getDefaultValue());
      }
   }

   /**
    * @see de.je.util.javautil.common.configparams.IConfigurable#configureFromProperties(java.util.Properties)
    */
   @Override
   public void configureFromProperties(Properties properties) {
      Enumeration<?> propertyNames = properties.propertyNames();

      while (propertyNames.hasMoreElements()) {
         Object next = propertyNames.nextElement();

         String currentPropertyName = next.toString();
         if (paramsByName.containsKey(currentPropertyName)) {

            AbstractConfigParam<? extends Comparable<?>> param = paramsByName.get(currentPropertyName);

            String propertyValue = properties.getProperty(currentPropertyName);

            Comparable<?> value = param.stringToValue(propertyValue);

            param.checkValue(value);

            currentParamValues.put(param, value);
         }
      }
   }

   /**
    * @see de.je.util.javautil.common.configparams.IConfigurable#addConfigChangeListener(de.je.util.javautil.common.configparams.IConfigChangeListener)
    */
   @Override
   public void addConfigChangeListener(IConfigChangeListener listener) {

      if (!listeners.contains(listener)) {
         listeners.add(listener);

         for (Iterator<AbstractConfigParam<? extends Comparable<?>>> iterator = currentParamValues.keySet()
            .iterator(); iterator.hasNext();) {
            AbstractConfigParam<? extends Comparable<?>> nextKey = iterator.next();
            Comparable<?> nextValue = currentParamValues.get(nextKey);

            listener.configurationParameterValueChanged(nextKey, nextValue);
         }
      }
   }

   /**
    * @see de.je.util.javautil.common.configparams.IConfigurable#removeConfigChangeListener(de.je.util.javautil.common.configparams.IConfigChangeListener)
    */
   @Override
   public void removeConfigChangeListener(IConfigChangeListener listener) {
      listeners.remove(listener);
   }

   private void setConfigParamInternal(AbstractConfigParam<? extends Comparable<?>> parameter, Comparable<?> value) {

      parameter.checkValue(value);

      currentParamValues.put(parameter, value);

      notifyListeners(parameter, value);
   }

   private void checkSupportedParameter(AbstractConfigParam<? extends Comparable<?>> parameter) {
      if (!currentParamValues.containsKey(parameter)) {
         throw new InvalidConfigParamException("The configuration parameter <" + parameter
            + "> is not supported by this " + IConfigurable.class.getSimpleName() + " instance", null, parameter, null);
      }
   }

   private void notifyListeners(AbstractConfigParam<? extends Comparable<?>> parameter, Comparable<?> value) {
      for (IConfigChangeListener configChangeListener : listeners) {
         configChangeListener.configurationParameterValueChanged(parameter, value);
      }
   }

}
