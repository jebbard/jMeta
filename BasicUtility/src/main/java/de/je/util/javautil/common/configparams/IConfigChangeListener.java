package de.je.util.javautil.common.configparams;

/**
 * {@link IConfigChangeListener} represents a class that receives notifications whenever a configuration parameter value
 * changes.
 */
public interface IConfigChangeListener {

   /**
    * The notification method. Is called exactly once for each parameter that has changed its value.
    * 
    * @param param
    *           the {@link AbstractConfigParam} that has changed its value
    * @param value
    *           the new value of the {@link AbstractConfigParam}.
    */
   public void configurationParameterValueChanged(AbstractConfigParam<? extends Comparable<?>> param,
      Comparable<?> value);
}
