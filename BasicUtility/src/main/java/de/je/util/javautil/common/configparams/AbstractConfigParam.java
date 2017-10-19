package de.je.util.javautil.common.configparams;

import java.util.HashSet;
import java.util.Set;

import de.je.util.javautil.common.err.Reject;

/**
 * {@link AbstractConfigurationParameter} represents a configuration parameter to dynamically configure a class,
 * component or library. Every configuration parameter must have a (non-null) default value.
 * 
 * Additionally it may have some optional constraints, i.e. a minimum value, a maximum value as well as a set of allowed
 * values.
 * 
 * To achieve this, the generic type parameter must implement {@link Comparable}.
 * 
 * @param <T>
 *           The value type of the configuration parameter, must implement {@link Comparable}.
 */
/**
 * {@link AbstractConfigParam}
 *
 * @param <T>
 */
public abstract class AbstractConfigParam<T extends Comparable<T>> {

   private final String name;
   private final T defaultValue;
   private final T minimumValue;
   private final T maximumValue;
   private final Set<T> possibleValues;
   private final Class<T> valueClass;

   /**
    * Creates a new {@link AbstractConfigParam}, without a minimum or maximum value and without a set of possible
    * values.
    * 
    * @param valueClass
    *           The class of the value
    * @param name
    *           The name of the parameter, must be non-null
    * @param defaultValue
    *           The default value of the parameter, must be non-null. If minimum and maximum values are given, the
    *           default value must be in their range (inclusive), if a set of possible values is given, the default
    *           value must be contained in this set.
    */
   public AbstractConfigParam(Class<T> valueClass, String name, T defaultValue) {
      this(valueClass, name, defaultValue, null, null, null);
   }

   /**
    * Creates a new {@link AbstractConfigParam} with a minimum and maximum value, but without a set of possible values.
    * 
    * @param name
    *           The name of the parameter, must be non-null
    * @param defaultValue
    *           The default value of the parameter, must be non-null. If minimum and maximum values are given, the
    *           default value must be in their range (inclusive), if a set of possible values is given, the default
    *           value must be contained in this set.
    * @param minimumValue
    *           The minimum value of the parameter, might be null to indicate there is no minimum value. If a maximum
    *           value is given, the minimum value must be smaller or equal than the maximum value. If a set of possible
    *           values is given, the minimum value must be contained in this set.
    * @param maximumValue
    *           The maximum value of the parameter, might be null to indicate there is no maximum value. If a minimum
    *           value is given, the maximum value must be greater or equal than the minimum value. If a set of possible
    *           values is given, the maximum value must be contained in this set.
    * @param valueClass
    *           The class of the value
    * 
    */
   public AbstractConfigParam(Class<T> valueClass, String name, T defaultValue, T minimumValue, T maximumValue) {
      this(valueClass, name, defaultValue, minimumValue, maximumValue, null);
   }

   /**
    * Creates a new {@link AbstractConfigParam} without a minimum and maximum value, but with a set of possible values.
    * 
    * @param name
    *           The name of the parameter, must be non-null
    * @param defaultValue
    *           The minimum value of the parameter, might be null to indicate there is no minimum value. If a maximum
    *           value is given, the minimum value must be smaller or equal than the maximum value. If a set of possible
    *           values is given, the minimum value must be contained in this set.
    * @param possibleValueArray
    *           An array of all possible values, might be null to indicate there are no limitations to the values. If
    *           non-null, at least one value must be contained. Each of the values must be in the range (minimum value,
    *           maximum value), if minimum and or maximum values are specified. If the array contains duplicates, these
    *           are silently ignored.
    * @param valueClass
    *           The class of the value
    */
   public AbstractConfigParam(Class<T> valueClass, String name, T defaultValue, T[] possibleValueArray) {
      this(valueClass, name, defaultValue, null, null, possibleValueArray);
   }

   /**
    * Creates a new {@link AbstractConfigParam}.
    * 
    * @param name
    *           The name of the parameter, must be non-null
    * @param defaultValue
    *           The minimum value of the parameter, might be null to indicate there is no minimum value. If a maximum
    *           value is given, the minimum value must be smaller or equal than the maximum value. If a set of possible
    *           values is given, the minimum value must be contained in this set.
    * @param minimumValue
    *           The minimum value of the parameter, might be null to indicate there is no minimum value. If a maximum
    *           value is given, the minimum value must be smaller or equal than the maximum value. If a set of possible
    *           values is given, the minimum value must be contained in this set.
    * @param maximumValue
    *           The maximum value of the parameter, might be null to indicate there is no maximum value. If a minimum
    *           value is given, the maximum value must be greater or equal than the minimum value. If a set of possible
    *           values is given, the maximum value must be contained in this set.
    * @param possibleValueArray
    *           An array of all possible values, might be null to indicate there are no limitations to the values. If
    *           non-null, at least one value must be contained. Each of the values must be in the range (minimum value,
    *           maximum value), if minimum and or maximum values are specified. If the array contains duplicates, these
    *           are silently ignored.
    * @param valueClass
    *           The class of the value
    */
   public AbstractConfigParam(Class<T> valueClass, String name, T defaultValue, T minimumValue, T maximumValue,
      T[] possibleValueArray) {
      Reject.ifNull(valueClass, "valueClass");
      Reject.ifNull(name, "name");
      Reject.ifNull(defaultValue, "defaultValue");

      this.valueClass = valueClass;
      this.name = name;
      this.defaultValue = defaultValue;
      this.minimumValue = minimumValue;
      this.maximumValue = maximumValue;

      if (possibleValueArray != null) {
         if (possibleValueArray.length == 0) {
            throw new IllegalArgumentException("If possibleValues are non-null, they must at least contain one entry");
         }

         possibleValues = new HashSet<>();

         for (int i = 0; i < possibleValueArray.length; i++) {
            this.possibleValues.add(possibleValueArray[i]);
         }
      } else {
         possibleValues = null;
      }

      checkValue(defaultValue);

      if (maximumValue != null) {
         checkValue(maximumValue);
      }

      if (minimumValue != null) {
         checkValue(minimumValue);
      }
   }

   /**
    * Converts a string representation of a value to an instance of the value type.
    * 
    * If the conversion fails, the method throws an arbitrary, implementation dependent runtime exception.
    * 
    * After converting this to an instance of the value type, this method does not perform any additional checks on this
    * value.
    * 
    * @param value
    *           The string value to convert
    * @return An instance of the value type.
    */
   public abstract T stringToValue(String value);

   /**
    * Converts an instance of the value type to a string representation. By default, this method simply calls
    * Object.toString(). If this is not sufficient, derived classes can override this method to implement their custom
    * string conversion.
    * 
    * This method does not perform any additional checks on the passed value before converting it to a string.
    * 
    * @param value
    *           The value to convert to a string
    * @return A string representation of the given value.
    */
   public String valueToString(Comparable<?> value) {
      return value.toString();
   }

   /**
    * Checks the given value for validity. If the given value is considered invalid, this method throws an
    * {@link InvalidConfigParamException}. Otherwise it returns without throwing an exception.
    * 
    * A null value is always considered as invalid.
    * 
    * The default validity checks done are:
    * <ul>
    * <li>The value must be non-null</li>
    * <li>If parameter has a minimum value: The value must be greater or equal to the minimum value</li>
    * <li>If parameter has a maximum value: The value must be greater or equal to the maximum value</li>
    * <li>If parameter has a set of possible values: The value must be in the set of possible values</li>
    * </ul>
    * 
    * Derived classes may override this method to define additional checks, but should call the superclass method at the
    * beginning to perform these basic checks.
    * 
    * @param value
    *           The value to check
    * 
    * @throws InvalidConfigParamException
    *            If the given value is invalid.
    */
   @SuppressWarnings("unchecked")
   public void checkValue(Comparable<?> value) {

      if (value == null) {
         throw new InvalidConfigParamException("Invalid null value", null, this, null);
      }

      if (value.getClass() != valueClass) {
         throw new InvalidConfigParamException("Invalid value type, value.toString() = " + value.toString(), null, this,
            null);
      }

      if (getMinimumValue() != null && ((T) value).compareTo(getMinimumValue()) < 0) {
         throw new InvalidConfigParamException(
            "Value is smaller than the minimum allowed value which is = " + getMinimumValue(), null, this, value);
      }

      if (getMaximumValue() != null && ((T) value).compareTo(getMaximumValue()) > 0) {
         throw new InvalidConfigParamException(
            "Value is greater than the maximum allowed value which is = " + getMaximumValue(), null, this, value);
      }

      if (getPossibleValues() != null && !getPossibleValues().contains(value)) {
         throw new InvalidConfigParamException(
            "Value is not contained in the range of possible values which is = " + getPossibleValues(), null, this,
            value);
      }
   }

   /**
    * @return the class of the parameter values
    */
   public Class<T> getValueClass() {
      return valueClass;
   }

   /**
    * @return the name of the {@link AbstractConfigParam}
    */
   public String getName() {
      return name;
   }

   /**
    * @return the default value of the {@link AbstractConfigParam}
    */
   public T getDefaultValue() {
      return defaultValue;
   }

   /**
    * @return the minimum value of the {@link AbstractConfigParam}, or null if there is none
    */
   public T getMinimumValue() {
      return minimumValue;
   }

   /**
    * @return the maximum value of the {@link AbstractConfigParam}, or null if there is none
    */
   public T getMaximumValue() {
      return maximumValue;
   }

   /**
    * @return the set of possible values of the {@link AbstractConfigParam}, or null if there is none
    */
   public Set<T> getPossibleValues() {
      return possibleValues;
   }
}
