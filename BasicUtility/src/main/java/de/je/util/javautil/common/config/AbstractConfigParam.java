/**
 *
 * {@link AbstractConfigParam}.java
 *
 * @author Jens Ebert
 *
 * @date 28.06.2011
 */
package de.je.util.javautil.common.config;

import java.util.Set;

import de.je.util.javautil.common.config.handler.IConfigParamValueHandler;
import de.je.util.javautil.common.err.Reject;
import de.je.util.javautil.common.extenum.AbstractExtensibleEnum;

/**
 * {@link AbstractConfigParam} represents a configuration parameter of specific meaning for which values can be stored
 * of a given type. The {@link AbstractConfigParam} stores constraints and defaults for its possible values.
 *
 * @param <T>
 *           The concrete type of value of this {@link AbstractConfigParam}.
 */
public class AbstractConfigParam<T> extends AbstractExtensibleEnum<AbstractConfigParam<T>> {

   /**
    * Creates a new {@link AbstractConfigParam}.
    * 
    * @param id
    *           The id of the enumeration value. Must be unique.
    * @param minimumValue
    *           The minimum value (including) or null if there is none.
    * @param maximumValue
    *           The maximum value (including) or null if there is none.
    * @param defaultValue
    *           The default value. Must not be null.
    * @param enumValues
    *           An enumeration of allowed values or null if there is none.
    * @param handler
    *           The {@link IConfigParamValueHandler} that checks and converts {@link AbstractConfigParam}s.
    */
   public AbstractConfigParam(String id, T minimumValue, T maximumValue, T defaultValue, Set<T> enumValues,
      IConfigParamValueHandler<T> handler) {
      super(id);

      Reject.ifNull(defaultValue, "defaultValue");
      Reject.ifNull(handler, "handler");

      m_minimumValue = minimumValue;
      m_maximumValue = maximumValue;
      m_defaultValue = defaultValue;
      m_enumValues = enumValues;
      m_handler = handler;
   }

   /**
    * Returns the minimum value (inclusive) allowed for this {@link AbstractConfigParam} or null if there is no minimum
    * value.
    *
    * @return the minimum value (inclusive) allowed for this {@link AbstractConfigParam} or null if there is no minimum
    *         value.
    */
   public T getMinimumValue() {
      return m_minimumValue;
   }

   /**
    * Returns the maximum value (inclusive) allowed for this {@link AbstractConfigParam} or null if there is no minimum
    * value.
    *
    * @return the maximum value (inclusive) allowed for this {@link AbstractConfigParam} or null if there is no minimum
    *         value.
    */
   public T getMaximumValue() {
      return m_maximumValue;
   }

   /**
    * Returns the default value of this {@link AbstractConfigParam}. May not be null, i.e. every
    * {@link AbstractConfigParam} must have a default value.
    *
    * @return the default value of this {@link AbstractConfigParam}. May not be null, i.e. every
    *         {@link AbstractConfigParam} must have a default value.
    */
   public T getDefaultValue() {
      return m_defaultValue;
   }

   /**
    * Returns a {@link Set} of possible values this {@link AbstractConfigParam} can hold. Other values are considered as
    * invalid. May return null if every value of the data type can be held.
    *
    * @return a {@link Set} of possible values this {@link AbstractConfigParam} can hold. Other values are considered as
    *         invalid. May return null if every value of the data type can be held.
    */
   public Set<T> getEnumValues() {
      return m_enumValues;
   }

   /**
    * Returns the {@link IConfigParamValueHandler} that is used for checking and converting the
    * {@link AbstractConfigParam}'s string representation.
    *
    * @return the {@link IConfigParamValueHandler} that is used for checking and converting the
    *         {@link AbstractConfigParam}'s string representation.
    */
   public IConfigParamValueHandler<T> getHandler() {
      return m_handler;
   }

   private final T m_minimumValue;
   private final T m_maximumValue;
   private final T m_defaultValue;
   private final Set<T> m_enumValues;
   private final IConfigParamValueHandler<T> m_handler;
}
