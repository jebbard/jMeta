package de.je.util.javautil.common.config.handler;

import de.je.util.javautil.common.config.AbstractConfigParam;

/**
 * {@link IConfigParamValueHandler} converts and checks string representations of a given {@link AbstractConfigParam}
 * for correctness, depending on its concrete type.
 *
 * @param <T>
 *           The concrete type of {@link AbstractConfigParam}.
 */
public interface IConfigParamValueHandler<T> {

   /**
    * Converts the string representation of an {@link AbstractConfigParam} to its typed representation.
    *
    * @param param
    *           The {@link AbstractConfigParam}.
    * @param configParamStringValue
    *           The string representation of its value.
    * @return The typed representation for the value of this {@link AbstractConfigParam}.
    * @throws Exception
    *            if conversion failed due to arbitrary reasons.
    */
   public T convert(AbstractConfigParam<T> param, String configParamStringValue) throws Exception;

   /**
    * Checks the bounds of the given (already converted) {@link AbstractConfigParam} value, if a minimum and maximum
    * value are defined for its {@link AbstractConfigParam}.
    *
    * @param param
    *           The {@link AbstractConfigParam}.
    * @param value
    *           The value to check.
    * @return true if in bounds, false otherwise.
    */
   public boolean checkBounds(AbstractConfigParam<T> param, T value);
}
