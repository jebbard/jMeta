/**
 *
 * {@link TestFileParamHandler}.java
 *
 * @author Jens Ebert
 *
 * @date 02.07.2011
 */
package de.je.util.javautil.common.config;

import java.io.File;

import de.je.util.javautil.common.config.AbstractConfigParam;
import de.je.util.javautil.common.config.handler.IConfigParamValueHandler;
import de.je.util.javautil.common.err.Reject;

/**
 * {@link TestFileParamHandler} is a param handler for {@link File}s.
 */
public class TestFileParamHandler implements IConfigParamValueHandler<File> {

   /**
    * @see de.je.util.javautil.common.config.handler.IConfigParamValueHandler#convert(de.je.util.javautil.common.config.AbstractConfigParam,
    *      java.lang.String)
    */
   @Override
   public File convert(AbstractConfigParam<File> param, String configParamStringValue) throws Exception {
      Reject.ifNull(configParamStringValue, "configParamStringValue");
      Reject.ifNull(param, "param");

      return new File(configParamStringValue);
   }

   /**
    * @see de.je.util.javautil.common.config.handler.IConfigParamValueHandler#checkBounds(de.je.util.javautil.common.config.AbstractConfigParam,
    *      java.lang.Object)
    */
   @Override
   public boolean checkBounds(AbstractConfigParam<File> param, File value) {
      Reject.ifNull(value, "value");
      Reject.ifNull(param, "param");

      return true;
   }
}
