package de.je.util.javautil.common.config.handler;

import java.util.logging.Level;

import de.je.util.javautil.common.config.AbstractConfigParam;
import de.je.util.javautil.common.err.Reject;

/**
 * {@link LogLevelParamHandler} is the logging {@link Level} implementation for {@link IConfigParamValueHandler}.
 */
public class LogLevelParamHandler implements IConfigParamValueHandler<Level> {

   /**
    * @see IConfigParamValueHandler#convert
    */
   @Override
   public Level convert(AbstractConfigParam<Level> param, String configParamStringValue) throws Exception {
      Reject.ifNull(configParamStringValue, "configParamStringValue");
      Reject.ifNull(param, "param");

      return Level.parse(configParamStringValue);
   }

   /**
    * @see IConfigParamValueHandler#checkBounds
    */
   @Override
   public boolean checkBounds(AbstractConfigParam<Level> param, Level value) {
      Reject.ifNull(value, "value");
      Reject.ifNull(param, "param");

      return true;
   }
}
