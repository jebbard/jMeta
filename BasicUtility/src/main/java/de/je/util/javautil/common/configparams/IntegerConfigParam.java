package de.je.util.javautil.common.configparams;

/**
 * {@link IntegerConfigParam} represents a configuration parameter with int values.
 */
public class IntegerConfigParam extends AbstractConfigParam<Integer> {

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
   public IntegerConfigParam(String name, Integer defaultValue, Integer minimumValue, Integer maximumValue,
      Integer[] possibleValueArray) {
      super(Integer.class, name, defaultValue, minimumValue, maximumValue, possibleValueArray);
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
   public IntegerConfigParam(String name, Integer defaultValue, Integer minimumValue, Integer maximumValue) {
      super(Integer.class, name, defaultValue, minimumValue, maximumValue);
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
   public IntegerConfigParam(String name, Integer defaultValue, Integer[] possibleValueArray) {
      super(Integer.class, name, defaultValue, possibleValueArray);
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
   public IntegerConfigParam(String name, Integer defaultValue) {
      super(Integer.class, name, defaultValue);
   }

   /**
    * @see de.je.util.javautil.common.configparams.AbstractConfigParam#stringToValue(java.lang.String)
    */
   @Override
   public Integer stringToValue(String value) {
      return Integer.parseInt(value);
   }
}
