/**
 *
 * {@link AbstractConfigLoaderTest}.java
 *
 * @author Jens Ebert
 *
 * @date 02.07.2011
 */
package de.je.util.javautil.common.config;

import java.io.InputStream;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import de.je.util.javautil.common.config.issue.ConfigIssue;
import de.je.util.javautil.common.config.issue.ConfigIssueType;
import de.je.util.javautil.testUtil.setup.TestDataException;

/**
 * {@link AbstractConfigLoaderTest} tests the {@link AbstractConfigLoader} class.
 */
public abstract class AbstractConfigLoaderTest {

   /**
    * Sets up the test fixtures.
    */
   @Before
   public void setUp() {
      m_testling = getTestling();
      m_streams = getInputStreams();
      m_configParamsToUse = getConfigParamsToUse();
      m_configParamsUnused = getConfigParamsUnused();

      if (m_testling == null || m_streams == null || m_configParamsToUse == null)
         throw new TestDataException("Test data may not be null", null);
   }

   /**
    * Tests {@link AbstractConfigLoader#hasConfigParam(AbstractConfigParam)},
    * {@link AbstractConfigLoader#getConfigParamValue(AbstractConfigParam)} and
    * {@link AbstractConfigLoader#load(InputStream)}.
    */
   @SuppressWarnings("resource")
   @Test
   public void test_hasGetConfigParamAndLoad() {
      checkDefaultValues();

      // For each stream: Load parameters
      for (int i = 0; i < m_streams.size(); ++i) {
         InputStream stream = m_streams.get(i);

         if (stream == null)
            throw new TestDataException("Stream must not be null", null);

         try {
            List<ConfigIssue> issues = m_testling.load(stream);

            List<ConfigIssue> expectedIssues = getExpectedIssues(stream);

            if (expectedIssues == null)
               throw new TestDataException("expectedIssues must not be null", null);

            Assert.assertEquals(expectedIssues.size(), issues.size());

            for (int j = 0; j < expectedIssues.size(); ++j) {
               ConfigIssue expectedIssue = expectedIssues.get(j);

               Assert.assertTrue(issues.contains(expectedIssue));
            }

            Map<AbstractConfigParam<?>, Object> expectedValues = getExpectedValuesInStream(stream);

            if (expectedValues == null)
               throw new TestDataException("expectedValues must not be null", null);

            // Only values contained in the stream have expected values
            for (Iterator<AbstractConfigParam<?>> iterator = m_configParamsToUse.iterator(); iterator.hasNext();) {
               AbstractConfigParam<?> configParam = iterator.next();

               Assert.assertTrue(m_testling.hasConfigParam(configParam));

               if (expectedValues.containsKey(configParam))
                  Assert.assertEquals(expectedValues.get(configParam), m_testling.getConfigParamValue(configParam));

               else
                  Assert.assertEquals(configParam.getDefaultValue(), m_testling.getConfigParamValue(configParam));
            }
         } catch (Exception e) {
            e.printStackTrace();
            Assert.fail("Unexpected exception: " + e);
         }

         m_testling.reset();

         checkDefaultValues();
      }
   }

   /**
    * Checks whether all parameters have their default values.
    */
   private void checkDefaultValues() {
      for (Iterator<AbstractConfigParam<?>> iterator = m_configParamsToUse.iterator(); iterator.hasNext();) {
         AbstractConfigParam<?> configParam = iterator.next();

         // All params are set to their default values
         Assert.assertTrue(m_testling.hasConfigParam(configParam));
         Assert.assertEquals(configParam.getDefaultValue(), m_testling.getConfigParamValue(configParam));
      }
   }

   /**
    * Tests {@link AbstractConfigLoader#setConfigParamValue(AbstractConfigParam, Object)} in positive case.
    */
   @Test
   @SuppressWarnings({ "unchecked", "rawtypes" })
   public void test_setConfigParamValue() {
      for (Iterator<AbstractConfigParam<?>> configParamIterator = m_configParamsToUse.iterator(); configParamIterator
         .hasNext();) {
         AbstractConfigParam<?> param = configParamIterator.next();

         Object maxValue = param.getMaximumValue();
         Object minValue = param.getMinimumValue();
         Object defValue = param.getDefaultValue();
         Set<?> enumValues = param.getEnumValues();

         // Default value can be set without errors
         checkSetConfigParamValidValue(param, defValue);

         // Min value can be set without errors
         if (minValue != null)
            checkSetConfigParamValidValue(param, minValue);

         // Max value can be set without errors
         if (maxValue != null)
            checkSetConfigParamValidValue(param, maxValue);

         // Enum value can be set without errors
         if (enumValues != null) {
            for (Iterator<?> enumValueIterator = enumValues.iterator(); enumValueIterator.hasNext();) {
               Object value = enumValueIterator.next();

               checkSetConfigParamValidValue(param, value);
            }
         }

         // Valid values work
         Map<AbstractConfigParam<?>, List<Object>> validValueMap = getValidConfigParamValuesToSet();

         if (validValueMap == null)
            throw new TestDataException("validValueMap must not be null", null);

         List<Object> validValues = validValueMap.get(param);

         if (validValues == null)
            throw new TestDataException("validValues must not be null", null);

         for (Iterator<?> validValueIterator = validValues.iterator(); validValueIterator.hasNext();) {
            Object value = validValueIterator.next();

            checkSetConfigParamValidValue(param, value);
         }

         // Invalid values do not work
         Map<AbstractConfigParam<?>, List<Object>> invalidValueMap = getInvalidConfigParamValuesToSet();

         if (invalidValueMap == null)
            throw new TestDataException("invalidValueMap must not be null", null);

         List<Object> invalidValues = invalidValueMap.get(param);

         if (invalidValues == null)
            throw new TestDataException("invalidValues must not be null", null);

         for (Iterator<?> invalidValueIterator = invalidValues.iterator(); invalidValueIterator.hasNext();) {
            Object value = invalidValueIterator.next();
            Object valueBefore = m_testling.getConfigParamValue(param);

            ConfigIssue issue = m_testling.setConfigParamValue((AbstractConfigParam) param, value);

            Assert.assertNotNull(issue);
            Assert.assertEquals(param, issue.getParam());
            Assert.assertEquals(value, issue.getParamConvertedValue());
            Assert.assertTrue(issue.getType().equals(ConfigIssueType.PARAMETER_VALUE_OUT_OF_BOUNDS)
               || issue.getType().equals(ConfigIssueType.NON_ENUMERATED_PARAMETER_VALUE));

            Assert.assertTrue(m_testling.hasConfigParam(param));
            Assert.assertNotSame(value, m_testling.getConfigParamValue(param));
            Assert.assertEquals(valueBefore, m_testling.getConfigParamValue(param));
         }

         m_testling.reset();

         checkDefaultValues();
      }
   }

   /**
    * Tests {@link AbstractConfigLoader#getConfigParamValue(AbstractConfigParam)},
    * {@link AbstractConfigLoader#hasConfigParam(AbstractConfigParam)} and
    * {@link AbstractConfigLoader#setConfigParamValue(AbstractConfigParam, Object)} when specifying unknown config
    * params.
    */
   @SuppressWarnings({ "unchecked", "rawtypes" })
   @Test
   public void test_unknownConfigParam() {
      for (Iterator<AbstractConfigParam<?>> iterator = m_configParamsUnused.iterator(); iterator.hasNext();) {
         AbstractConfigParam<?> unusedParam = iterator.next();

         Assert.assertFalse(m_testling.hasConfigParam(unusedParam));
         try {
            m_testling.getConfigParamValue(unusedParam);
            Assert.fail("Expected exception");
         } catch (Exception e) {
            Assert.assertNotNull("Exception as expected", e);
         }
         try {
            m_testling.setConfigParamValue((AbstractConfigParam) unusedParam, unusedParam.getDefaultValue());
            Assert.fail("Expected exception");
         } catch (Exception e) {
            Assert.assertNotNull("Exception as expected", e);
         }
      }
   }

   /**
    * Checks setting a valid config param value.
    *
    * @param param
    *           The {@link AbstractConfigParam}.
    * @param validValue
    *           The valid value to set.
    */
   @SuppressWarnings({ "unchecked", "rawtypes" })
   private void checkSetConfigParamValidValue(AbstractConfigParam<?> param, Object validValue) {
      Assert.assertNull(m_testling.setConfigParamValue((AbstractConfigParam) param, validValue));
      Assert.assertTrue(m_testling.hasConfigParam(param));
      Assert.assertEquals(validValue, m_testling.getConfigParamValue(param));
   }

   /**
    * Returns the {@link AbstractConfigLoader} to test.
    *
    * @return the {@link AbstractConfigLoader} to test.
    */
   protected abstract AbstractConfigLoader getTestling();

   /**
    * Returns the {@link AbstractConfigParam}s to use.
    *
    * @return the {@link AbstractConfigParam}s to use.
    */
   protected abstract Set<AbstractConfigParam<?>> getConfigParamsToUse();

   /**
    * Returns {@link AbstractConfigParam}s invalid for the {@link AbstractConfigLoader}.
    *
    * @return {@link AbstractConfigParam}s invalid for the {@link AbstractConfigLoader}.
    */
   protected abstract Set<AbstractConfigParam<?>> getConfigParamsUnused();

   /**
    * Returns the {@link InputStream}s for testing load.
    *
    * @return the {@link InputStream}s for testing load.
    */
   protected abstract List<InputStream> getInputStreams();

   /**
    * Returns the expected configuration parameter values loaded from the stream without error.
    *
    * @param stream
    *           The {@link InputStream}.
    * @return the expected configuration parameter values loaded from the stream without error.
    */
   protected abstract Map<AbstractConfigParam<?>, Object> getExpectedValuesInStream(InputStream stream);

   /**
    * Returns the expected {@link ConfigIssue}s when loading from the stream.
    *
    * @param stream
    *           The {@link InputStream}.
    * @return the expected {@link ConfigIssue}s when loading from the stream.
    */
   protected abstract List<ConfigIssue> getExpectedIssues(InputStream stream);

   /**
    * Returns valid configuration parameter values for testing
    * {@link AbstractConfigLoader#setConfigParamValue(AbstractConfigParam, Object)}. No errors are expected for these
    * values.
    *
    * @return valid configuration parameter values for testing
    *         {@link AbstractConfigLoader#setConfigParamValue(AbstractConfigParam, Object)}.
    */
   protected abstract Map<AbstractConfigParam<?>, List<Object>> getValidConfigParamValuesToSet();

   /**
    * Returns invalid configuration parameter values for testing
    * {@link AbstractConfigLoader#setConfigParamValue(AbstractConfigParam, Object)}. An out-of-range error is expected
    * for these values.
    *
    * @return invalid configuration parameter values for testing
    *         {@link AbstractConfigLoader#setConfigParamValue(AbstractConfigParam, Object)}.
    */
   protected abstract Map<AbstractConfigParam<?>, List<Object>> getInvalidConfigParamValuesToSet();

   private AbstractConfigLoader m_testling;
   private List<InputStream> m_streams;
   private Set<AbstractConfigParam<?>> m_configParamsToUse;
   private Set<AbstractConfigParam<?>> m_configParamsUnused;
}
