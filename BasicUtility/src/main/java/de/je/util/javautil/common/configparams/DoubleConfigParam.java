package de.je.util.javautil.common.configparams;

/**
 * {@link DoubleConfigParam} represents configuration parameters with double precision floating point values.
 */
public class DoubleConfigParam extends AbstractConfigParam<Double> {

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
   public DoubleConfigParam(String name, Double defaultValue, Double minimumValue, Double maximumValue,
      Double[] possibleValueArray) {
      super(Double.class, name, defaultValue, minimumValue, maximumValue, possibleValueArray);
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
   public DoubleConfigParam(String name, Double defaultValue, Double minimumValue, Double maximumValue) {
      super(Double.class, name, defaultValue, minimumValue, maximumValue);
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
   public DoubleConfigParam(String name, Double defaultValue, Double[] possibleValueArray) {
      super(Double.class, name, defaultValue, possibleValueArray);
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
   public DoubleConfigParam(String name, Double defaultValue) {
      super(Double.class, name, defaultValue);
   }

   /**
    * @see de.je.util.javautil.common.configparams.AbstractConfigParam#stringToValue(java.lang.String)
    */
   @Override
   public Double stringToValue(String value) {
      return Double.parseDouble(value);
   }
}
