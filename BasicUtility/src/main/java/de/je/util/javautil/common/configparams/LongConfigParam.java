package de.je.util.javautil.common.configparams;

/**
 * {@link LongConfigParam} represents a configuration parameter with long values.
 */
public class LongConfigParam extends AbstractConfigParam<Long> {

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
   public LongConfigParam(String name, Long defaultValue, Long minimumValue, Long maximumValue,
      Long[] possibleValueArray) {
      super(Long.class, name, defaultValue, minimumValue, maximumValue, possibleValueArray);
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
   public LongConfigParam(String name, Long defaultValue, Long minimumValue, Long maximumValue) {
      super(Long.class, name, defaultValue, minimumValue, maximumValue);
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
   public LongConfigParam(String name, Long defaultValue, Long[] possibleValueArray) {
      super(Long.class, name, defaultValue, possibleValueArray);
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
   public LongConfigParam(String name, Long defaultValue) {
      super(Long.class, name, defaultValue);
   }

   /**
    * @see de.je.util.javautil.common.configparams.AbstractConfigParam#stringToValue(java.lang.String)
    */
   @Override
   public Long stringToValue(String value) {
      return Long.parseLong(value);
   }
}
