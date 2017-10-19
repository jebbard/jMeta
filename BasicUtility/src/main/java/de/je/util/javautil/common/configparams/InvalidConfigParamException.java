package de.je.util.javautil.common.configparams;

/**
 * {@link InvalidConfigParamException} is thrown whenever checks of a parameter or its value detects it is invalid.
 */
public class InvalidConfigParamException extends RuntimeException {

   private static final long serialVersionUID = 1L;

   private final AbstractConfigParam<? extends Comparable<?>> configParam;
   private final Comparable<?> value;

   /**
    * Creates a new {@link InvalidConfigParamException}.
    * 
    * @param message
    *           the message explaining the cause of invalidity
    * @param cause
    *           The causing exception, if any, or null, if none
    * @param configParam
    *           The (invalid) {@link AbstractConfigParam}
    * @param value
    *           The (invalid) value
    */
   public InvalidConfigParamException(String message, Throwable cause,
      AbstractConfigParam<? extends Comparable<?>> configParam, Comparable<?> value) {
      super(message, cause);

      this.value = value;
      this.configParam = configParam;
   }

   /**
    * @return the invalid value
    */
   public AbstractConfigParam<? extends Comparable<?>> getConfigParam() {
      return configParam;
   }

   /**
    * @return the invalid value
    */
   public Comparable<?> getValue() {
      return value;
   }

}
