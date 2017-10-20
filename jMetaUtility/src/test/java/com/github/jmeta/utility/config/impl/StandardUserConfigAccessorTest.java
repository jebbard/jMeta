package com.github.jmeta.utility.config.impl;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import com.github.jmeta.utility.config.api.services.IUserConfigAccessor;
import com.github.jmeta.utility.config.api.services.IUserConfigAccessorTest;
import com.github.jmeta.utility.config.api.type.UserConfigParam;

/**
 * {@link StandardUserConfigAccessorTest} tests {@link StandardUserConfigAccessor}.
 */
public class StandardUserConfigAccessorTest extends IUserConfigAccessorTest {

   /**
    * @see com.github.jmeta.utility.config.api.services.IUserConfigAccessorTest#getUserConfigTestling()
    */
   @Override
   protected IUserConfigAccessor getUserConfigTestling() {

      if (userAccessor == null)
         userAccessor = new StandardUserConfigAccessor();

      return userAccessor;
   }

   /**
    * @see com.github.jmeta.utility.config.api.services.IUserConfigAccessorTest#getUserConfigStreams()
    */
   @Override
   protected List<InputStream> getUserConfigStreams() {

      if (userConfigStreams == null) {
         userConfigStreams = new ArrayList<>();

         userConfigStreams.add(getClass().getResourceAsStream("userConfig1.properties"));
         userConfigStreams.add(getClass().getResourceAsStream("userConfig2.properties"));
         userConfigStreams.add(getClass().getResourceAsStream("userConfig3.properties"));
      }

      return userConfigStreams;
   }

   /**
    * @see com.github.jmeta.utility.config.api.services.IUserConfigAccessorTest#getExpectedUserConfigValues()
    */
   @Override
   protected Map<InputStream, Map<UserConfigParam<?>, Object>> getExpectedUserConfigValues() {

      if (userConfigExpectedValues == null) {
         userConfigExpectedValues = new HashMap<>();

         final HashMap<UserConfigParam<?>, Object> map1 = new HashMap<>();

         map1.put(UserConfigParam.LOG_FILE_PATH, ".");
         map1.put(UserConfigParam.LOG_LEVEL, Level.SEVERE);

         userConfigExpectedValues.put(getUserConfigStreams().get(0), map1);

         final HashMap<UserConfigParam<?>, Object> map2 = new HashMap<>();

         map2.put(UserConfigParam.LOG_FILE_PATH, "C:\\My\\Nice\\Software");
         map2.put(UserConfigParam.LOG_LEVEL, UserConfigParam.LOG_LEVEL.getDefaultValue());

         userConfigExpectedValues.put(getUserConfigStreams().get(1), map2);

         final HashMap<UserConfigParam<?>, Object> map3 = new HashMap<>();

         map3.put(UserConfigParam.LOG_FILE_PATH, UserConfigParam.LOG_FILE_PATH.getDefaultValue());
         map3.put(UserConfigParam.LOG_LEVEL, UserConfigParam.LOG_LEVEL.getDefaultValue());

         userConfigExpectedValues.put(getUserConfigStreams().get(2), map3);
      }

      return userConfigExpectedValues;
   }

   private IUserConfigAccessor userAccessor;

   private List<InputStream> userConfigStreams;

   private Map<InputStream, Map<UserConfigParam<?>, Object>> userConfigExpectedValues;
}
