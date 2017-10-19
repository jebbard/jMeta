package de.je.util.javautil.common.configparams;

/**
 * {@link ConfigParamCheckType} represents the kinds of checks to be done with configuration parameters.
 */
public enum ConfigParamCheckType {
   /**
    * Check against the first valid value of a parameter
    */
   FIRST_VALID_VALUE,
   /**
    * Check against the first invalid and non-null value of a parameter
    */
   FIRST_INVALID_VALUE_NON_NULL,
   /**
    * Check against the default value of a parameter
    */
   DEFAULT_VALUE,
   /**
    * Check only that the parameter is supported / present / set
    */
   PARAMS_ONLY,
}
