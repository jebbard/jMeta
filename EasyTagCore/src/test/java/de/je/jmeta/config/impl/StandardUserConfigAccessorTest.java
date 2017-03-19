package de.je.jmeta.config.impl;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import de.je.jmeta.config.export.IUserConfigAccessor;
import de.je.jmeta.config.export.UserConfigParam;
import de.je.jmeta.config.iface.IUserConfigAccessorTest;
import de.je.jmeta.testHelpers.basics.JMetaTestBasics;

/**
 * {@link StandardUserConfigAccessorTest} tests {@link StandardUserConfigAccessor}.
 */
public class StandardUserConfigAccessorTest extends IUserConfigAccessorTest {

   /**
    * @see de.je.jmeta.config.iface.IUserConfigAccessorTest#getUserConfigTestling()
    */
   @Override
   protected IUserConfigAccessor getUserConfigTestling() {

      if (m_userAccessor == null)
         m_userAccessor = new StandardUserConfigAccessor(
            JMetaTestBasics.setupComponents());

      return m_userAccessor;
   }

   /**
    * @see de.je.jmeta.config.iface.IUserConfigAccessorTest#getUserConfigStreams()
    */
   @Override
   protected List<InputStream> getUserConfigStreams() {

      if (m_userConfigStreams == null) {
         m_userConfigStreams = new ArrayList<>();

         m_userConfigStreams
            .add(getClass().getResourceAsStream("userConfig1.properties"));
         m_userConfigStreams
            .add(getClass().getResourceAsStream("userConfig2.properties"));
         m_userConfigStreams
            .add(getClass().getResourceAsStream("userConfig3.properties"));
      }

      return m_userConfigStreams;
   }

   /**
    * @see de.je.jmeta.config.iface.IUserConfigAccessorTest#getExpectedUserConfigValues()
    */
   @Override
   protected Map<InputStream, Map<UserConfigParam<?>, Object>> getExpectedUserConfigValues() {

      if (m_userConfigExpectedValues == null) {
         m_userConfigExpectedValues = new HashMap<>();

         final HashMap<UserConfigParam<?>, Object> map1 = new HashMap<>();

         map1.put(UserConfigParam.LOG_FILE_PATH, ".");
         map1.put(UserConfigParam.LOG_LEVEL, Level.SEVERE);

         m_userConfigExpectedValues.put(getUserConfigStreams().get(0), map1);

         final HashMap<UserConfigParam<?>, Object> map2 = new HashMap<>();

         map2.put(UserConfigParam.LOG_FILE_PATH, "C:\\My\\Nice\\Software");
         map2.put(UserConfigParam.LOG_LEVEL,
            UserConfigParam.LOG_LEVEL.getDefaultValue());

         m_userConfigExpectedValues.put(getUserConfigStreams().get(1), map2);

         final HashMap<UserConfigParam<?>, Object> map3 = new HashMap<>();

         map3.put(UserConfigParam.LOG_FILE_PATH,
            UserConfigParam.LOG_FILE_PATH.getDefaultValue());
         map3.put(UserConfigParam.LOG_LEVEL,
            UserConfigParam.LOG_LEVEL.getDefaultValue());

         m_userConfigExpectedValues.put(getUserConfigStreams().get(2), map3);
      }

      return m_userConfigExpectedValues;
   }

   private IUserConfigAccessor m_userAccessor;

   private List<InputStream> m_userConfigStreams;

   private Map<InputStream, Map<UserConfigParam<?>, Object>> m_userConfigExpectedValues;
}
