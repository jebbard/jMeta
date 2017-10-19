/**
 *
 * {@link PropertiesConfigLoaderTest}.java
 *
 * @author Jens Ebert
 *
 * @date 02.07.2011
 */
package de.je.util.javautil.common.config;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

import org.junit.Before;

import de.je.util.javautil.common.config.issue.ConfigIssue;
import de.je.util.javautil.common.config.issue.ConfigIssueType;

/**
 * {@link PropertiesConfigLoaderTest} tests {@link AbstractConfigLoader} for Java properties files.
 *
 */
public class PropertiesConfigLoaderTest extends AbstractConfigLoaderTest {

   /**
    * Sets up the test fixtures.
    */
   @Override
   @Before
   public void setUp() {
      super.setUp();

      HashMap<AbstractConfigParam<?>, Object> expectedValues1 = new HashMap<>();

      expectedValues1.put(TestConfigParams.LOG_FILE_PATH, new File("./donderland"));
      expectedValues1.put(TestConfigParams.COMPONENT_CONFIG_PATH,
         TestConfigParams.COMPONENT_CONFIG_PATH.getDefaultValue());
      expectedValues1.put(TestConfigParams.EXTENSIONS_CONFIG_PATH, "WUPIHUPI");
      expectedValues1.put(TestConfigParams.LOG_LEVEL, Level.CONFIG);
      expectedValues1.put(TestConfigParams.MAX_BINARY_VALUE_FRAGMENT_SIZE, 1677);
      expectedValues1.put(TestConfigParams.TIMEOUT_IDENTIFY, TestConfigParams.TIMEOUT_IDENTIFY.getDefaultValue());

      List<ConfigIssue> expectedIssues1 = new ArrayList<>();

      expectedIssues1.add(new ConfigIssue(ConfigIssueType.MISSING_PARAMETER, TestConfigParams.COMPONENT_CONFIG_PATH,
         TestConfigParams.COMPONENT_CONFIG_PATH.getId(), null, null, null));
      expectedIssues1.add(new ConfigIssue(ConfigIssueType.EMPTY_PARAMETER, TestConfigParams.TIMEOUT_IDENTIFY,
         TestConfigParams.TIMEOUT_IDENTIFY.getId(), "", null, null));
      expectedIssues1
         .add(new ConfigIssue(ConfigIssueType.UNKNOWN_PARAMETER, null, "Unknown", "my.Unknown.value", null, null));

      m_expectedValues.put(getInputStreams().get(0), expectedValues1);
      m_expectedIssues.put(getInputStreams().get(0), expectedIssues1);

      HashMap<AbstractConfigParam<?>, Object> expectedValues2 = new HashMap<>();

      expectedValues2.put(TestConfigParams.LOG_FILE_PATH, new File("."));
      expectedValues2.put(TestConfigParams.COMPONENT_CONFIG_PATH,
         TestConfigParams.COMPONENT_CONFIG_PATH.getDefaultValue());
      expectedValues2.put(TestConfigParams.EXTENSIONS_CONFIG_PATH, "HALLO");
      expectedValues2.put(TestConfigParams.LOG_LEVEL, TestConfigParams.LOG_LEVEL.getDefaultValue());
      expectedValues2.put(TestConfigParams.MAX_BINARY_VALUE_FRAGMENT_SIZE,
         TestConfigParams.MAX_BINARY_VALUE_FRAGMENT_SIZE.getDefaultValue());
      expectedValues2.put(TestConfigParams.TIMEOUT_IDENTIFY, TestConfigParams.TIMEOUT_IDENTIFY.getDefaultValue());

      List<ConfigIssue> expectedIssues2 = new ArrayList<>();

      expectedIssues2.add(new ConfigIssue(ConfigIssueType.NON_ENUMERATED_PARAMETER_VALUE, TestConfigParams.LOG_LEVEL,
         TestConfigParams.LOG_LEVEL.getId(), "ALL", Level.ALL, null));
      expectedIssues2.add(new ConfigIssue(ConfigIssueType.PARAMETER_VALUE_OUT_OF_BOUNDS,
         TestConfigParams.TIMEOUT_IDENTIFY, TestConfigParams.TIMEOUT_IDENTIFY.getId(), "-11", -11, null));
      expectedIssues2.add(
         new ConfigIssue(ConfigIssueType.PARAMETER_VALUE_OUT_OF_BOUNDS, TestConfigParams.MAX_BINARY_VALUE_FRAGMENT_SIZE,
            TestConfigParams.MAX_BINARY_VALUE_FRAGMENT_SIZE.getId(), "99999999", 99999999, null));

      m_expectedValues.put(getInputStreams().get(1), expectedValues2);
      m_expectedIssues.put(getInputStreams().get(1), expectedIssues2);

      HashMap<AbstractConfigParam<?>, Object> expectedValues3 = new HashMap<>();

      expectedValues3.put(TestConfigParams.LOG_FILE_PATH, TestConfigParams.LOG_FILE_PATH.getDefaultValue());
      expectedValues3.put(TestConfigParams.COMPONENT_CONFIG_PATH,
         TestConfigParams.COMPONENT_CONFIG_PATH.getDefaultValue());
      expectedValues3.put(TestConfigParams.EXTENSIONS_CONFIG_PATH,
         TestConfigParams.EXTENSIONS_CONFIG_PATH.getDefaultValue());
      expectedValues3.put(TestConfigParams.LOG_LEVEL, TestConfigParams.LOG_LEVEL.getDefaultValue());
      expectedValues3.put(TestConfigParams.MAX_BINARY_VALUE_FRAGMENT_SIZE,
         TestConfigParams.MAX_BINARY_VALUE_FRAGMENT_SIZE.getDefaultValue());
      expectedValues3.put(TestConfigParams.TIMEOUT_IDENTIFY, TestConfigParams.TIMEOUT_IDENTIFY.getDefaultValue());

      List<ConfigIssue> expectedIssues3 = new ArrayList<>();

      expectedIssues3.add(new ConfigIssue(ConfigIssueType.PARAMETER_VALUE_CONVERSION_FAILED, TestConfigParams.LOG_LEVEL,
         TestConfigParams.LOG_LEVEL.getId(), "JOHO", null, new IllegalArgumentException("Bad level \"JOHO\"")));
      expectedIssues3.add(new ConfigIssue(ConfigIssueType.MISSING_PARAMETER, TestConfigParams.LOG_FILE_PATH,
         TestConfigParams.LOG_FILE_PATH.getId(), null, null, null));
      expectedIssues3.add(new ConfigIssue(ConfigIssueType.MISSING_PARAMETER, TestConfigParams.TIMEOUT_IDENTIFY,
         TestConfigParams.TIMEOUT_IDENTIFY.getId(), null, null, null));
      expectedIssues3.add(new ConfigIssue(ConfigIssueType.MISSING_PARAMETER, TestConfigParams.EXTENSIONS_CONFIG_PATH,
         TestConfigParams.EXTENSIONS_CONFIG_PATH.getId(), null, null, null));
      expectedIssues3.add(new ConfigIssue(ConfigIssueType.MISSING_PARAMETER, TestConfigParams.COMPONENT_CONFIG_PATH,
         TestConfigParams.COMPONENT_CONFIG_PATH.getId(), null, null, null));
      expectedIssues3.add(new ConfigIssue(ConfigIssueType.PARAMETER_VALUE_CONVERSION_FAILED,
         TestConfigParams.MAX_BINARY_VALUE_FRAGMENT_SIZE, TestConfigParams.MAX_BINARY_VALUE_FRAGMENT_SIZE.getId(),
         "99הה9999", null, new NumberFormatException("For input string: \"99הה9999\"")));
      expectedIssues3
         .add(new ConfigIssue(ConfigIssueType.UNKNOWN_PARAMETER, null, "Jupheidi", "JUPHEIDIHEIDA", null, null));
      expectedIssues3.add(new ConfigIssue(ConfigIssueType.UNKNOWN_PARAMETER, null, "my.second.unknown.parameter",
         "unknwon", null, null));
      expectedIssues3.add(new ConfigIssue(ConfigIssueType.UNKNOWN_PARAMETER, null, "shit", "", null, null));
      expectedIssues3.add(new ConfigIssue(ConfigIssueType.UNKNOWN_PARAMETER, null,
         TestConfigParams.MAX_CACHE_REGION_SIZE.getId(), "6000", null, null));

      m_expectedValues.put(getInputStreams().get(2), expectedValues3);
      m_expectedIssues.put(getInputStreams().get(2), expectedIssues3);
   }

   /**
    * @see de.je.util.javautil.common.config.AbstractConfigLoaderTest#getTestling()
    */
   @Override
   protected AbstractConfigLoader getTestling() {
      return new PropertiesConfigLoader(getConfigParamsToUse());
   }

   /**
    * @see de.je.util.javautil.common.config.AbstractConfigLoaderTest#getConfigParamsToUse()
    */
   @Override
   protected Set<AbstractConfigParam<?>> getConfigParamsToUse() {
      if (m_configParamsToUse == null) {
         m_configParamsToUse = new HashSet<>();

         m_configParamsToUse.add(TestConfigParams.LOG_FILE_PATH);
         m_configParamsToUse.add(TestConfigParams.COMPONENT_CONFIG_PATH);
         m_configParamsToUse.add(TestConfigParams.EXTENSIONS_CONFIG_PATH);
         m_configParamsToUse.add(TestConfigParams.LOG_LEVEL);
         m_configParamsToUse.add(TestConfigParams.MAX_BINARY_VALUE_FRAGMENT_SIZE);
         m_configParamsToUse.add(TestConfigParams.TIMEOUT_IDENTIFY);
      }

      return m_configParamsToUse;
   }

   /**
    * @see de.je.util.javautil.common.config.AbstractConfigLoaderTest#getConfigParamsUnused()
    */
   @Override
   protected Set<AbstractConfigParam<?>> getConfigParamsUnused() {
      if (m_configParamsUnused == null) {
         m_configParamsUnused = new HashSet<>();

         m_configParamsUnused.add(TestConfigParams.MAX_CACHE_REGION_SIZE);
         m_configParamsUnused.add(TestConfigParams.TIMEOUT_BLOCK);
      }

      return m_configParamsUnused;
   }

   /**
    * @see de.je.util.javautil.common.config.AbstractConfigLoaderTest#getInputStreams()
    */
   @Override
   protected List<InputStream> getInputStreams() {
      if (m_inputStreams == null) {
         m_inputStreams = new ArrayList<>();

         m_inputStreams.add(PropertiesConfigLoaderTest.class.getResourceAsStream("testConfig_1.properties"));
         m_inputStreams.add(PropertiesConfigLoaderTest.class.getResourceAsStream("testConfig_2.properties"));
         m_inputStreams.add(PropertiesConfigLoaderTest.class.getResourceAsStream("testConfig_3.properties"));
      }

      return m_inputStreams;
   }

   /**
    * @see de.je.util.javautil.common.config.AbstractConfigLoaderTest#getExpectedValuesInStream(java.io.InputStream)
    */
   @Override
   protected Map<AbstractConfigParam<?>, Object> getExpectedValuesInStream(InputStream stream) {
      return m_expectedValues.get(stream);
   }

   /**
    * @see de.je.util.javautil.common.config.AbstractConfigLoaderTest#getExpectedIssues(java.io.InputStream)
    */
   @Override
   protected List<ConfigIssue> getExpectedIssues(InputStream stream) {
      return m_expectedIssues.get(stream);
   }

   /**
    * @see de.je.util.javautil.common.config.AbstractConfigLoaderTest#getValidConfigParamValuesToSet()
    */
   @Override
   protected Map<AbstractConfigParam<?>, List<Object>> getValidConfigParamValuesToSet() {
      if (m_validConfigParamsToSet == null) {
         m_validConfigParamsToSet = new HashMap<>();

         List<Object> validConfigParams1 = new ArrayList<>();

         validConfigParams1.add(new File("."));
         validConfigParams1.add(new File("./Hallo.txt"));

         m_validConfigParamsToSet.put(TestConfigParams.LOG_FILE_PATH, validConfigParams1);

         List<Object> validConfigParams2 = new ArrayList<>();

         validConfigParams2.add(".");
         validConfigParams2.add("./Hallo.txt");

         m_validConfigParamsToSet.put(TestConfigParams.COMPONENT_CONFIG_PATH, validConfigParams2);

         List<Object> validConfigParams3 = new ArrayList<>();

         validConfigParams3.add(".");
         validConfigParams3.add("./Hallo.txt");

         m_validConfigParamsToSet.put(TestConfigParams.EXTENSIONS_CONFIG_PATH, validConfigParams3);

         List<Object> validConfigParams4 = new ArrayList<>();

         validConfigParams4.add(Level.CONFIG);
         validConfigParams4.add(Level.FINER);

         m_validConfigParamsToSet.put(TestConfigParams.LOG_LEVEL, validConfigParams4);

         List<Object> validConfigParams5 = new ArrayList<>();

         validConfigParams5.add(1000);
         validConfigParams5.add(10000);
         validConfigParams5.add(999999);
         validConfigParams5.add(9999999);

         m_validConfigParamsToSet.put(TestConfigParams.MAX_BINARY_VALUE_FRAGMENT_SIZE, validConfigParams5);

         List<Object> validConfigParams6 = new ArrayList<>();

         validConfigParams6.add(1000);
         validConfigParams6.add(10000);
         validConfigParams6.add(999999);
         validConfigParams6.add(Integer.MAX_VALUE);

         m_validConfigParamsToSet.put(TestConfigParams.TIMEOUT_IDENTIFY, validConfigParams6);
      }

      return m_validConfigParamsToSet;
   }

   /**
    * @see de.je.util.javautil.common.config.AbstractConfigLoaderTest#getInvalidConfigParamValuesToSet()
    */
   @Override
   protected Map<AbstractConfigParam<?>, List<Object>> getInvalidConfigParamValuesToSet() {
      if (m_invalidConfigParamsToSet == null) {
         m_invalidConfigParamsToSet = new HashMap<>();

         List<Object> invalidConfigParams1 = new ArrayList<>();

         m_invalidConfigParamsToSet.put(TestConfigParams.LOG_FILE_PATH, invalidConfigParams1);

         List<Object> invalidConfigParams2 = new ArrayList<>();

         m_invalidConfigParamsToSet.put(TestConfigParams.COMPONENT_CONFIG_PATH, invalidConfigParams2);

         List<Object> invalidConfigParams3 = new ArrayList<>();

         m_invalidConfigParamsToSet.put(TestConfigParams.EXTENSIONS_CONFIG_PATH, invalidConfigParams3);

         List<Object> invalidConfigParams4 = new ArrayList<>();

         invalidConfigParams4.add(Level.ALL);
         invalidConfigParams4.add(Level.OFF);

         m_invalidConfigParamsToSet.put(TestConfigParams.LOG_LEVEL, invalidConfigParams4);

         List<Object> invalidConfigParams5 = new ArrayList<>();

         invalidConfigParams5.add(999);
         invalidConfigParams5.add(0);
         invalidConfigParams5.add(Integer.MIN_VALUE);

         m_invalidConfigParamsToSet.put(TestConfigParams.MAX_BINARY_VALUE_FRAGMENT_SIZE, invalidConfigParams5);

         List<Object> invalidConfigParams6 = new ArrayList<>();

         invalidConfigParams6.add(999);
         invalidConfigParams6.add(0);
         invalidConfigParams6.add(Integer.MIN_VALUE);

         m_invalidConfigParamsToSet.put(TestConfigParams.TIMEOUT_IDENTIFY, invalidConfigParams6);
      }

      return m_invalidConfigParamsToSet;
   }

   private List<InputStream> m_inputStreams;
   private final Map<InputStream, Map<AbstractConfigParam<?>, Object>> m_expectedValues = new HashMap<>();
   private final Map<InputStream, List<ConfigIssue>> m_expectedIssues = new HashMap<>();
   private Map<AbstractConfigParam<?>, List<Object>> m_validConfigParamsToSet;
   private Map<AbstractConfigParam<?>, List<Object>> m_invalidConfigParamsToSet;
   private Set<AbstractConfigParam<?>> m_configParamsToUse;
   private Set<AbstractConfigParam<?>> m_configParamsUnused;
}
