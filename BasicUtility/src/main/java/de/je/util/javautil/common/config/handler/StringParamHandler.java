/**
 *
 * {@link StringParamHandler}.java
 *
 * @author Jens Ebert
 *
 * @date 29.06.2011
 */
package de.je.util.javautil.common.config.handler;

import de.je.util.javautil.common.config.AbstractConfigParam;
import de.je.util.javautil.common.err.Reject;

/**
 * {@link StringParamHandler} is the {@link String} implementation of {@link IConfigParamValueHandler}.
 */
public class StringParamHandler implements IConfigParamValueHandler<String> {

   /**
    * Default instance of this stateless {@link IConfigParamValueHandler}.
    */
   public static final StringParamHandler DEFAULT_INSTANCE = new StringParamHandler();

   /**
    * @see IConfigParamValueHandler#convert
    */
   @Override
   public String convert(AbstractConfigParam<String> param, String configParamStringValue) {
      Reject.ifNull(configParamStringValue, "configParamStringValue");
      Reject.ifNull(param, "param");

      return configParamStringValue;
   }

   /**
    * @see IConfigParamValueHandler#checkBounds
    */
   @Override
   public boolean checkBounds(AbstractConfigParam<String> param, String value) {
      Reject.ifNull(value, "value");
      Reject.ifNull(param, "param");

      boolean inBounds = true;

      if (param.getMinimumValue() != null)
         inBounds &= param.getMinimumValue().compareTo(value) <= 0;

      if (param.getMaximumValue() != null)
         inBounds &= param.getMaximumValue().compareTo(value) >= 0;

      return inBounds;
   }

}
