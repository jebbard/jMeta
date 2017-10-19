package de.je.util.javautil.common.configparams;

/**
 * {@link BooleanConfigParam} allows to define boolean parameters.
 */
public class BooleanConfigParam extends AbstractConfigParam<Boolean> {

   /**
    * Creates a new {@link BooleanConfigParam}.
    * 
    * @see AbstractConfigParam#AbstractConfigParam(Class, String, Comparable, Comparable[])
    * 
    * @param name
    *           See description in corresponding super constructor.
    * @param defaultValue
    *           See description in corresponding super constructor.
    */
   public BooleanConfigParam(String name, Boolean defaultValue) {
      super(Boolean.class, name, defaultValue, null, null, null);
   }

   /**
    * @see de.je.util.javautil.common.configparams.AbstractConfigParam#stringToValue(java.lang.String)
    */
   @Override
   public Boolean stringToValue(String value) {
      return Boolean.valueOf(value);
   }

}
