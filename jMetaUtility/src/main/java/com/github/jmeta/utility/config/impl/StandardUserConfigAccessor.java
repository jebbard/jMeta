/**
 * {@link UserConfiguration}.java
 *
 * @author Jens Ebert
 * @date 28.06.11 15:26:21 (June 28, 2011)
 */

package com.github.jmeta.utility.config.impl;

import java.util.HashSet;
import java.util.Set;

import com.github.jmeta.utility.config.api.services.AbstractConfigAccessor;
import com.github.jmeta.utility.config.api.services.IUserConfigAccessor;
import com.github.jmeta.utility.config.api.type.UserConfigParam;

import de.je.util.javautil.common.config.AbstractConfigParam;

/**
 * {@link StandardUserConfigAccessor} provides methods for reading all user configuration parameters at runtime. These
 * parameters are usually configured in an external configuration file accessible to the user.
 */
public class StandardUserConfigAccessor extends AbstractConfigAccessor implements IUserConfigAccessor {

   /**
    * Creates a new instance of {@link StandardUserConfigAccessor}. No parameters must be added as this class may be
    * dynamically instantiated by reflection by a component loader.
    */
   public StandardUserConfigAccessor() {
      super(USER_PARAMETERS);
   }

   /**
    * @see com.github.jmeta.utility.config.api.services.IUserConfigAccessor#hasUserConfigParam(com.github.jmeta.utility.config.api.type.UserConfigParam)
    */
   @Override
   public <T> boolean hasUserConfigParam(UserConfigParam<T> configParam) {

      return hasConfigParam(configParam);
   }

   /**
    * @see com.github.jmeta.utility.config.api.services.IUserConfigAccessor#getUserConfigParam(com.github.jmeta.utility.config.api.type.UserConfigParam)
    */
   @Override
   public <T> T getUserConfigParam(UserConfigParam<T> configParam) {

      return getConfigParam(configParam);
   }

   private final static Set<AbstractConfigParam<?>> USER_PARAMETERS = new HashSet<>();

   static {
      USER_PARAMETERS.add(UserConfigParam.LOG_FILE_PATH);
      USER_PARAMETERS.add(UserConfigParam.LOG_LEVEL);
   }
}
