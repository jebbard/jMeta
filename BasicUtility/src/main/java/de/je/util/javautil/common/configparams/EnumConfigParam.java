package de.je.util.javautil.common.configparams;

/**
 * {@link EnumConfigParam} allows to define parameters based on enumeration types.
 *
 * @param <T>
 *           The type of enumeration
 */
public class EnumConfigParam<T extends Enum<T>> extends AbstractConfigParam<T> {

   /**
    * Creates a new {@link EnumConfigParam} without the need to specify a minimum and maximum value, which is usually
    * unnecessary for enums.
    * 
    * @param enumClass
    *           The concrete enum class
    * @param name
    *           See description in corresponding super constructor.
    * @param defaultValue
    *           See description in corresponding super constructor.
    * @param possibleValueArray
    *           See description in corresponding super constructor. Use the static values() method of the enum class to
    *           get all enum instances.
    * 
    * @see AbstractConfigParam#AbstractConfigParam(Class, String, Comparable, Comparable[])
    */
   public EnumConfigParam(Class<T> enumClass, String name, T defaultValue, T[] possibleValueArray) {
      this(enumClass, name, defaultValue, null, null, possibleValueArray);
   }

   /**
    * Creates a new {@link EnumConfigParam}.
    * 
    * @param enumClass
    *           The concrete enum class
    * @param name
    *           See description in corresponding super constructor.
    * @param defaultValue
    *           See description in corresponding super constructor.
    * @param minimumValue
    *           See description in corresponding super constructor.
    * @param maximumValue
    *           See description in corresponding super constructor.
    * @param possibleValueArray
    *           See description in corresponding super constructor. Use the static values() method of the enum class to
    *           get all enum instances.
    * 
    * @see AbstractConfigParam#AbstractConfigParam(Class, String, Comparable, Comparable, Comparable, Comparable[])
    */
   public EnumConfigParam(Class<T> enumClass, String name, T defaultValue, T minimumValue, T maximumValue,
      T[] possibleValueArray) {
      super(enumClass, name, defaultValue, minimumValue, maximumValue, possibleValueArray);
   }

   /**
    * @see de.je.util.javautil.common.configparams.AbstractConfigParam#stringToValue(java.lang.String)
    */
   @Override
   public T stringToValue(String value) {
      return Enum.valueOf(getValueClass(), value);
   }

}
