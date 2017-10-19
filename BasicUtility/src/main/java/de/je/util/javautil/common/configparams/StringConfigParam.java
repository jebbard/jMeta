package de.je.util.javautil.common.configparams;

/**
 * {@link StringConfigParam} represents a configuration parameter with string values.
 */
public class StringConfigParam extends AbstractConfigParam<String> {

   /**
    * Creates a new {@link DoubleConfigParam}.
    * 
    * @see AbstractConfigParam#AbstractConfigParam(Class, String, Comparable, Comparable, Comparable, Comparable[])
    * 
    * @param name
    *           See description in corresponding super constructor.
    * @param defaultValue
    *           See description in corresponding super constructor.
    * @param minimumValue
    *           See description in corresponding super constructor.
    * @param maximumValue
    *           See description in corresponding super constructor.
    * @param possibleValueArray
    *           See description in corresponding super constructor.
    */
   public StringConfigParam(String name, String defaultValue, String minimumValue, String maximumValue,
      String[] possibleValueArray) {
      super(String.class, name, defaultValue, minimumValue, maximumValue, possibleValueArray);
   }

   /**
    * Creates a new {@link DoubleConfigParam}.
    * 
    * @see AbstractConfigParam#AbstractConfigParam(Class, String, Comparable, Comparable, Comparable)
    * 
    * @param name
    *           See description in corresponding super constructor.
    * @param defaultValue
    *           See description in corresponding super constructor.
    * @param minimumValue
    *           See description in corresponding super constructor.
    * @param maximumValue
    *           See description in corresponding super constructor.
    */
   public StringConfigParam(String name, String defaultValue, String minimumValue, String maximumValue) {
      super(String.class, name, defaultValue, minimumValue, maximumValue);
   }

   /**
    * Creates a new {@link DoubleConfigParam}.
    * 
    * @see AbstractConfigParam#AbstractConfigParam(Class, String, Comparable, Comparable[])
    * 
    * @param name
    *           See description in corresponding super constructor.
    * @param defaultValue
    *           See description in corresponding super constructor.
    * @param possibleValueArray
    *           See description in corresponding super constructor.
    */
   public StringConfigParam(String name, String defaultValue, String[] possibleValueArray) {
      super(String.class, name, defaultValue, possibleValueArray);
   }

   /**
    * Creates a new {@link DoubleConfigParam}.
    * 
    * @see AbstractConfigParam#AbstractConfigParam(Class, String, Comparable)
    * 
    * @param name
    *           See description in corresponding super constructor.
    * @param defaultValue
    *           See description in corresponding super constructor.
    */
   public StringConfigParam(String name, String defaultValue) {
      super(String.class, name, defaultValue);
   }

   /**
    * @see de.je.util.javautil.common.configparams.AbstractConfigParam#stringToValue(java.lang.String)
    */
   @Override
   public String stringToValue(String value) {
      return value;
   }
}
