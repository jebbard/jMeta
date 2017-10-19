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
 * {@link IntegerParamHandler} is an {@link Integer} implementation of {@link IConfigParamValueHandler}.
 */
public class IntegerParamHandler implements IConfigParamValueHandler<Integer> {

   /**
    * Default implementation of this stateless {@link IConfigParamValueHandler}.
    */
   public static final IntegerParamHandler DEFAULT_INSTANCE = new IntegerParamHandler();

   /**
    * @see IConfigParamValueHandler#convert
    */
   @Override
   public Integer convert(AbstractConfigParam<Integer> param, String configParamStringValue) throws Exception {
      Reject.ifNull(configParamStringValue, "configParamStringValue");
      Reject.ifNull(param, "param");

      return Integer.parseInt(configParamStringValue);
   }

   /**
    * @see IConfigParamValueHandler#checkBounds
    */
   @Override
   public boolean checkBounds(AbstractConfigParam<Integer> param, Integer value) {
      boolean inBounds = true;

      if (param.getMinimumValue() != null)
         inBounds &= param.getMinimumValue() <= value;

      if (param.getMaximumValue() != null)
         inBounds &= param.getMaximumValue() >= value;

      return inBounds;
   }
}
