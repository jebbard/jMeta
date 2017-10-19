package de.je.util.javautil.common.configparams;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.junit.Assert;

/**
 * {@link IConfigurableChecker} is a helper class to check {@link IConfigurable} instances. Basically, it holds all
 * specific configuration parameters of different concrete types and eases the handling of these different types. It
 * gets an instance of the test class {@link AbstractConfigurableTest} to be able to retrieve all data needed to perform
 * the checks.
 *
 *
 * @param <E>
 *           The first type of enum class to test enum config params, or {@link EmptyEnum} if no further class to test.
 * @param <F>
 *           The second type of enum class to test enum config params, or {@link EmptyEnum} if no further class to test.
 */
public class IConfigurableChecker<E extends Enum<E>, F extends Enum<F>> {

   private Set<AbstractConfigParam<String>> stringConfigParams;
   private Set<AbstractConfigParam<E>> firstEnumConfigParams;
   private Set<AbstractConfigParam<F>> secondEnumConfigParams;
   private Set<AbstractConfigParam<Double>> doubleConfigParams;
   private Set<AbstractConfigParam<Long>> longConfigParams;
   private Set<AbstractConfigParam<Integer>> integerConfigParams;
   private Set<AbstractConfigParam<Boolean>> booleanConfigParams;

   private final IConfigurable testling;

   private AbstractConfigurableTest<E, F> testClassInstance;
   private Set<Class<?>> acceptedClasses = new HashSet<>();

   private Map<Class<?>, ConfigParamCheckType> paramValueExpectations = new HashMap<>();

   /**
    * Creates a new {@link IConfigurableChecker}.
    * 
    * @param testClassInstance
    *           the test class instance.
    */
   public IConfigurableChecker(AbstractConfigurableTest<E, F> testClassInstance) {
      stringConfigParams = testClassInstance.getStringConfigParams();
      firstEnumConfigParams = testClassInstance.getFirstEnumConfigParams();
      secondEnumConfigParams = testClassInstance.getSecondEnumConfigParams();
      doubleConfigParams = testClassInstance.getDoubleConfigParams();
      integerConfigParams = testClassInstance.getIntegerConfigParams();
      longConfigParams = testClassInstance.getLongConfigParams();
      booleanConfigParams = testClassInstance.getBooleanConfigParams();
      testling = testClassInstance.getConfigurableInstance();

      this.testClassInstance = testClassInstance;

      acceptedClasses.add(testClassInstance.getFirstEnumClass());
      acceptedClasses.add(testClassInstance.getSecondEnumClass());
      acceptedClasses.add(Long.class);
      acceptedClasses.add(Integer.class);
      acceptedClasses.add(Double.class);
      acceptedClasses.add(String.class);
      acceptedClasses.add(Boolean.class);
   }

   /**
    * Sets the expected values for each type of {@link AbstractConfigParam}, for checking expected against actual values
    * after a method execution of the tested {@link IConfigurable}.
    * 
    * @param clazz
    *           The class instance, must be one of the supported types for {@link AbstractConfigParam}s or type E or
    *           type F (enum types). Currently, supported types are: {@link Boolean}, {@link Double}, {@link Integer},
    *           {@link String}.
    * @param paramValueExpectation
    *           Determines the expected values after calling a method under test. Could be
    *           {@link ConfigParamCheckType#DEFAULT_VALUE} or {@link ConfigParamCheckType#FIRST_VALID_VALUE}.
    */
   public void setParamValueExpectation(Class<?> clazz, ConfigParamCheckType paramValueExpectation) {
      validateAcceptedClass(clazz);

      validateDefaultOrFirstValid(paramValueExpectation);

      paramValueExpectations.put(clazz, paramValueExpectation);
   }

   /**
    * Checkgs {@link IConfigurable#resetConfigToDefault()}.
    */
   public void checkResetConfigToDefault() {

      setParamValueExpectation(Boolean.class, ConfigParamCheckType.DEFAULT_VALUE);
      setParamValueExpectation(String.class, ConfigParamCheckType.DEFAULT_VALUE);
      setParamValueExpectation(Double.class, ConfigParamCheckType.DEFAULT_VALUE);
      setParamValueExpectation(Integer.class, ConfigParamCheckType.DEFAULT_VALUE);
      setParamValueExpectation(Long.class, ConfigParamCheckType.DEFAULT_VALUE);
      setParamValueExpectation(testClassInstance.getFirstEnumClass(), ConfigParamCheckType.DEFAULT_VALUE);
      setParamValueExpectation(testClassInstance.getSecondEnumClass(), ConfigParamCheckType.DEFAULT_VALUE);

      testling.resetConfigToDefault();

      checkGetConfigParams();
      checkDefaultValues(testling.getAllConfigParams());
      checkDefaultValues(getCompleteConfigParamSet(), testling.getAllConfigParamsAsProperties());
   }

   /**
    * Checks that the initial call to {@link IConfigurable#addConfigChangeListener(IConfigChangeListener)} calls the
    * {@link IConfigChangeListener} with default values.
    * 
    * @param listener
    *           The test {@link IConfigChangeListener} instance
    */
   public void checkInitialAddConfigChangeListener(CalledConfigChangeListener listener) {

      Map<AbstractConfigParam<? extends Comparable<?>>, Comparable<?>> values = getAllDefaultValues();

      listener.setExpectedParams(values);

      testling.addConfigChangeListener(listener);

      checkListenerGotNotifiedOncePerValue(values, listener);
   }

   /**
    * Checks that subsequent calls to {@link IConfigurable#setConfigParam(AbstractConfigParam, Comparable)} call the
    * {@link IConfigChangeListener} with the newly set values.
    * 
    * @param listener
    *           The test {@link IConfigChangeListener} instance
    */
   public void checkSubsequentSetNotifiesListener(CalledConfigChangeListener listener) {
      // Reset the attributes of the listener to initial state, as it was notified before by adding it to the testling
      listener.reset();

      Map<AbstractConfigParam<? extends Comparable<?>>, Comparable<?>> expectedParams = getFirstValidValueForAllParams();

      listener.setExpectedParams(expectedParams);

      setFirstValidValuesForAllParams();

      checkListenerGotNotifiedOncePerValue(expectedParams, listener);
   }

   /**
    * Checks that a given test {@link IConfigChangeListener} was notified with exactly the given values.
    * 
    * @param expectedValues
    *           The values which are expected to be provided during notifications of the listener
    * @param listener
    *           The test {@link IConfigChangeListener} instance
    */
   public void checkListenerGotNotifiedOncePerValue(
      Map<AbstractConfigParam<? extends Comparable<?>>, Comparable<?>> expectedValues,
      CalledConfigChangeListener listener) {
      Assert.assertEquals(expectedValues, listener.getNotifiedParams());
      Assert.assertEquals(expectedValues.size(), listener.getCallCounter());
   }

   /**
    * Checks {@link IConfigurable#removeConfigChangeListener(IConfigChangeListener)} that after this, the
    * {@link IConfigurable} instance does not notify the listener anymore.
    * 
    * @param listener
    *           The {@link IConfigChangeListener} test instance
    */
   public void checkRemoveConfigChangeListener(CalledConfigChangeListener listener) {
      // Reset the attributes of the listener to initial state, as it was notified before by adding it to the testling
      listener.reset();

      testling.removeConfigChangeListener(listener);

      setFirstValidValuesForAllParams();

      Assert.assertEquals(0, listener.getCallCounter());
   }

   /**
    * Checks the method {@link IConfigurable#configureFromProperties(Properties)}.
    * 
    * @param properties
    *           The properties to use when calling {@link IConfigurable#configureFromProperties(Properties)}
    * @param checkGetAsProperties
    *           true if it is expected that {@link IConfigurable#getAllConfigParamsAsProperties()} must return exactly
    *           the same properties, false to not perform this check. false must be given if the passed properties
    *           contain additional unknown properties intentionally.
    */
   public void checkConfigureFromProperties(Properties properties, boolean checkGetAsProperties) {
      testling.configureFromProperties(properties);

      checkGetConfigParams();

      // Additionally check that all other methods return only defaults
      if (allParamsDefaultExpected()) {
         checkDefaultValues(testling.getAllConfigParams());
         checkDefaultValues(getCompleteConfigParamSet(), testling.getAllConfigParamsAsProperties());
      }

      if (checkGetAsProperties) {
         Assert.assertEquals(properties, testling.getAllConfigParamsAsProperties());
      }
   }

   /**
    * Checks {@link IConfigurable#getAllConfigParams()} to return the provided expected values
    * 
    * @param expectation
    */
   public void checkGetAllConfigParams(ConfigParamCheckType expectation) {

      if (expectation == ConfigParamCheckType.FIRST_VALID_VALUE) {
         Map<AbstractConfigParam<? extends Comparable<?>>, Comparable<?>> expectedValues = getFirstValidValueForAllParams();

         Map<AbstractConfigParam<? extends Comparable<?>>, Comparable<?>> allConfigParams = testling
            .getAllConfigParams();

         Assert.assertEquals(expectedValues, allConfigParams);
      } else if (expectation == ConfigParamCheckType.PARAMS_ONLY) {

         Assert.assertEquals(getCompleteConfigParamSet(), testling.getAllConfigParams().keySet());

      }
   }

   /**
    * Checks {@link IConfigurable#getAllConfigParamsAsProperties()}, either with default values or with first valid
    * values
    * 
    * @param expectation
    *           The expectation mode, either {@link ConfigParamCheckType#FIRST_VALID_VALUE} or
    *           {@link ConfigParamCheckType#DEFAULT_VALUE}
    */
   public void checkGetAllConfigParamsAsProperties(ConfigParamCheckType expectation) {

      validateDefaultOrFirstValid(expectation);

      Map<AbstractConfigParam<? extends Comparable<?>>, Comparable<?>> expectedValues = null;

      if (expectation == ConfigParamCheckType.FIRST_VALID_VALUE) {
         expectedValues = getFirstValidValueForAllParams();
      } else {
         expectedValues = getAllDefaultValues();
      }

      Properties allParamsAsProps = testling.getAllConfigParamsAsProperties();

      for (Iterator<AbstractConfigParam<? extends Comparable<?>>> iterator = expectedValues.keySet()
         .iterator(); iterator.hasNext();) {
         AbstractConfigParam<? extends Comparable<?>> nextKey = iterator.next();
         Object nextValue = expectedValues.get(nextKey);

         Assert.assertEquals(nextValue.toString(), allParamsAsProps.get(nextKey.getName()).toString());
      }
   }

   /**
    * Checks {@link IConfigurable#getConfigParam(AbstractConfigParam)} regarding the previously set expectations (with
    * {@link #setParamValueExpectation(Class, ConfigParamCheckType)}).
    */
   public void checkGetConfigParams() {
      validateExpectationVariables();

      checkGetConfigParam(stringConfigParams);
      checkGetConfigParam(doubleConfigParams);
      checkGetConfigParam(integerConfigParams);
      checkGetConfigParam(longConfigParams);
      checkGetConfigParam(booleanConfigParams);
      checkGetConfigParam(firstEnumConfigParams);
      checkGetConfigParam(secondEnumConfigParams);
   }

   /**
    * Checks {@link IConfigurable#getSupportedConfigParams()}.
    */
   public void checkGetSupportedConfigParams() {
      Assert.assertEquals(getCompleteConfigParamSet(), testling.getSupportedConfigParams());
   }

   /**
    * Checks {@link IConfigurable#setConfigParam(AbstractConfigParam, Comparable)} for setting valid values.
    */
   public void checkSetValidParamValuesWorks() {

      checkSetGetValidParamValues(integerConfigParams);
      checkSetGetValidParamValues(longConfigParams);
      checkSetGetValidParamValues(doubleConfigParams);
      checkSetGetValidParamValues(stringConfigParams);
      checkSetGetValidParamValues(firstEnumConfigParams);
      checkSetGetValidParamValues(secondEnumConfigParams);
      checkSetGetValidParamValues(booleanConfigParams);
   }

   /**
    * Checks {@link IConfigurable#setConfigParam(AbstractConfigParam, Comparable)} for invalid values.
    */
   public void checkSetInvalidParamValuesThrowsException() {

      checkSetInvalidParamValues(integerConfigParams);
      checkSetInvalidParamValues(longConfigParams);
      checkSetInvalidParamValues(doubleConfigParams);
      checkSetInvalidParamValues(stringConfigParams);
      checkSetInvalidParamValues(firstEnumConfigParams);
      checkSetInvalidParamValues(secondEnumConfigParams);
      checkSetInvalidParamValues(booleanConfigParams);
   }

   /**
    * Populates a {@link Properties} instance with the given types of values. For each type of parameters to set, either
    * {@link ConfigParamCheckType#FIRST_VALID_VALUE} or {@link ConfigParamCheckType#FIRST_INVALID_VALUE_NON_NULL} is
    * specified.
    * 
    * @param classMapping
    *           The maps an array of classes to each type of values to use for populating. Each class must only be
    *           present once and must be one of the supported classes
    * @return The populated {@link Properties} instance.
    */
   public Properties getPropertiesFromProvidedValues(Map<ConfigParamCheckType, Class<?>[]> classMapping) {

      Properties propertiesToPopulate = new Properties();

      Set<Class<?>> validClasses = arrayToSet(classMapping.get(ConfigParamCheckType.FIRST_VALID_VALUE));
      Set<Class<?>> invalidClasses = arrayToSet(classMapping.get(ConfigParamCheckType.FIRST_INVALID_VALUE_NON_NULL));

      validateClasses(validClasses, invalidClasses);

      for (Class<?> validClass : validClasses) {
         if (validClass == String.class) {
            Map<AbstractConfigParam<String>, String> paramValues = getFirstValidValueForParams(stringConfigParams);
            setPropertiesFromParamValues(propertiesToPopulate, paramValues);
         } else if (validClass == Integer.class) {
            Map<AbstractConfigParam<Integer>, Integer> paramValues = getFirstValidValueForParams(integerConfigParams);
            setPropertiesFromParamValues(propertiesToPopulate, paramValues);
         } else if (validClass == Long.class) {
            Map<AbstractConfigParam<Long>, Long> paramValues = getFirstValidValueForParams(longConfigParams);
            setPropertiesFromParamValues(propertiesToPopulate, paramValues);
         } else if (validClass == Double.class) {
            Map<AbstractConfigParam<Double>, Double> paramValues = getFirstValidValueForParams(doubleConfigParams);
            setPropertiesFromParamValues(propertiesToPopulate, paramValues);
         } else if (validClass == Boolean.class) {
            Map<AbstractConfigParam<Boolean>, Boolean> paramValues = getFirstValidValueForParams(booleanConfigParams);
            setPropertiesFromParamValues(propertiesToPopulate, paramValues);
         } else if (validClass == testClassInstance.getFirstEnumClass()) {
            Map<AbstractConfigParam<E>, E> paramValues = getFirstValidValueForParams(firstEnumConfigParams);
            setPropertiesFromParamValues(propertiesToPopulate, paramValues);
         } else if (validClass == testClassInstance.getSecondEnumClass()) {
            Map<AbstractConfigParam<F>, F> paramValues = getFirstValidValueForParams(secondEnumConfigParams);
            setPropertiesFromParamValues(propertiesToPopulate, paramValues);
         }
      }

      for (Class<?> invalidClass : invalidClasses) {
         if (invalidClass == String.class) {
            Map<AbstractConfigParam<String>, String> paramValues = getFirstInvalidNonNullValueForParams(
               stringConfigParams);
            setPropertiesFromParamValues(propertiesToPopulate, paramValues);
         } else if (invalidClass == Integer.class) {
            Map<AbstractConfigParam<Integer>, Integer> paramValues = getFirstInvalidNonNullValueForParams(
               integerConfigParams);
            setPropertiesFromParamValues(propertiesToPopulate, paramValues);
         } else if (invalidClass == Long.class) {
            Map<AbstractConfigParam<Long>, Long> paramValues = getFirstInvalidNonNullValueForParams(longConfigParams);
            setPropertiesFromParamValues(propertiesToPopulate, paramValues);
         } else if (invalidClass == Double.class) {
            Map<AbstractConfigParam<Double>, Double> paramValues = getFirstInvalidNonNullValueForParams(
               doubleConfigParams);
            setPropertiesFromParamValues(propertiesToPopulate, paramValues);
         } else if (invalidClass == Boolean.class) {
            Map<AbstractConfigParam<Boolean>, Boolean> paramValues = getFirstInvalidNonNullValueForParams(
               booleanConfigParams);
            setPropertiesFromParamValues(propertiesToPopulate, paramValues);
         } else if (invalidClass == testClassInstance.getFirstEnumClass()) {
            Map<AbstractConfigParam<E>, E> paramValues = getFirstInvalidNonNullValueForParams(firstEnumConfigParams);
            setPropertiesFromParamValues(propertiesToPopulate, paramValues);
         } else if (invalidClass == testClassInstance.getSecondEnumClass()) {
            Map<AbstractConfigParam<F>, F> paramValues = getFirstInvalidNonNullValueForParams(secondEnumConfigParams);
            setPropertiesFromParamValues(propertiesToPopulate, paramValues);
         }
      }

      return propertiesToPopulate;
   }

   /**
    * Sets the first valid values for all parameters in the {@link IConfigurable} instance.
    */
   public void setFirstValidValuesForAllParams() {

      setValueForParams(getFirstValidValueForParams(longConfigParams));
      setValueForParams(getFirstValidValueForParams(integerConfigParams));
      setValueForParams(getFirstValidValueForParams(doubleConfigParams));
      setValueForParams(getFirstValidValueForParams(stringConfigParams));
      setValueForParams(getFirstValidValueForParams(firstEnumConfigParams));
      setValueForParams(getFirstValidValueForParams(secondEnumConfigParams));
      setValueForParams(getFirstValidValueForParams(booleanConfigParams));
   }

   /**
    * @return the first values provided by {@link AbstractConfigurableTest#getValidValues(AbstractConfigParam)} method
    *         for each configuration parameter returned by its actual subclass.
    */
   public Map<AbstractConfigParam<? extends Comparable<?>>, Comparable<?>> getFirstValidValueForAllParams() {

      Map<AbstractConfigParam<? extends Comparable<?>>, Comparable<?>> expectedValues = new HashMap<>();

      expectedValues.putAll(getFirstValidValueForParams(longConfigParams));
      expectedValues.putAll(getFirstValidValueForParams(integerConfigParams));
      expectedValues.putAll(getFirstValidValueForParams(doubleConfigParams));
      expectedValues.putAll(getFirstValidValueForParams(stringConfigParams));
      expectedValues.putAll(getFirstValidValueForParams(firstEnumConfigParams));
      expectedValues.putAll(getFirstValidValueForParams(secondEnumConfigParams));
      expectedValues.putAll(getFirstValidValueForParams(booleanConfigParams));

      return expectedValues;
   }

   /**
    * Converts an array to a set
    * 
    * @param array
    *           The array to convert
    * @return the converted set
    */
   private static <T> Set<T> arrayToSet(T[] array) {
      Set<T> returnedSet = new HashSet<>();

      for (int i = 0; i < array.length; i++) {
         returnedSet.add(array[i]);
      }

      return returnedSet;
   }

   private void validateDefaultOrFirstValid(ConfigParamCheckType paramValueExpectation) {
      if (paramValueExpectation != ConfigParamCheckType.DEFAULT_VALUE
         && paramValueExpectation != ConfigParamCheckType.FIRST_VALID_VALUE) {
         throw new IllegalArgumentException(
            "Invalid check type " + paramValueExpectation + ", only DEFAULT_VALUE and FIRST_VALID_VALUE are allowed");
      }
   }

   private void validateExpectationVariables() {
      if (paramValueExpectations.size() != acceptedClasses.size()) {
         throw new IllegalStateException(
            "All of the parameter value expectations for all types must be set to a non-null value before calling this method");
      }
   }

   private void validateOnlyAcceptedClasses(Set<Class<?>> classes) {
      for (Class<?> clazz : classes) {
         validateAcceptedClass(clazz);
      }
   }

   private void validateAcceptedClass(Class<?> clazz) {
      if (!acceptedClasses.contains(clazz)) {
         throw new IllegalArgumentException(
            "The class " + clazz + " is none of the accepted classes. Accepted classes are: " + acceptedClasses);
      }
   }

   private void validateClasses(Set<Class<?>> validClasses, Set<Class<?>> invalidClasses) {
      Set<Class<?>> copy = new HashSet<>(validClasses);

      copy.retainAll(invalidClasses);

      if (copy.size() != 0) {
         // It is allowed that both valid and invalid classes contain the EmptyEnum
         if (copy.size() != 1 || copy.iterator().next() != EmptyEnum.class) {
            throw new IllegalArgumentException(
               "The set of valid classes must not contain any classes contained in the set of invalid classes and vice versa");
         }
      }

      validateOnlyAcceptedClasses(validClasses);
      validateOnlyAcceptedClasses(invalidClasses);
   }

   /**
    * @return all default values of all supported parameters
    */
   private Map<AbstractConfigParam<? extends Comparable<?>>, Comparable<?>> getAllDefaultValues() {

      Map<AbstractConfigParam<? extends Comparable<?>>, Comparable<?>> returnedMap = new HashMap<>();

      for (Iterator<AbstractConfigParam<?>> iterator = getCompleteConfigParamSet().iterator(); iterator.hasNext();) {
         AbstractConfigParam<?> abstractConfigParam = iterator.next();
         returnedMap.put(abstractConfigParam, abstractConfigParam.getDefaultValue());
      }

      return returnedMap;
   }

   private <T extends Comparable<T>> Map<AbstractConfigParam<T>, T> getFirstInvalidNonNullValueForParams(
      Set<AbstractConfigParam<T>> configParams) {

      Map<AbstractConfigParam<T>, T> returnedMap = new HashMap<>();

      for (Iterator<AbstractConfigParam<T>> iterator = configParams.iterator(); iterator.hasNext();) {
         AbstractConfigParam<T> abstractConfigParam = iterator.next();

         T[] invalidValues = testClassInstance.getInvalidValues(abstractConfigParam);

         for (int i = 0; i < invalidValues.length; i++) {
            T value = invalidValues[0];

            if (value != null) {
               returnedMap.put(abstractConfigParam, value);
            }
         }
      }

      return returnedMap;
   }

   private boolean allParamsDefaultExpected() {

      for (Iterator<Class<?>> iterator = paramValueExpectations.keySet().iterator(); iterator.hasNext();) {
         Class<?> nextKey = iterator.next();
         ConfigParamCheckType nextValue = paramValueExpectations.get(nextKey);

         if (nextValue != ConfigParamCheckType.DEFAULT_VALUE) {
            return false;
         }
      }

      return true;
   }

   /**
    * Sets the properties for all specified parameters within the provided {@link Properties} object.
    * 
    * @param properties
    *           The {@link Properties} to populate
    * @param configParamValues
    *           The configuration parameters with their values to be set within the provided {@link Properties} object.
    */
   private <T extends Comparable<T>> void setPropertiesFromParamValues(Properties properties,
      Map<AbstractConfigParam<T>, T> configParamValues) {

      for (Iterator<AbstractConfigParam<T>> iterator = configParamValues.keySet().iterator(); iterator.hasNext();) {
         AbstractConfigParam<T> nextKey = iterator.next();
         T nextValue = configParamValues.get(nextKey);

         properties.setProperty(nextKey.getName(), nextValue.toString());
      }
   }

   /**
    * Retrieves a map of the first valid value for each given parameter
    * 
    * @param configParams
    *           The parameters for which to retrieve the first valid values
    * @return a map of the first valid value for each given parameter
    */
   private <T extends Comparable<T>> Map<AbstractConfigParam<T>, T> getFirstValidValueForParams(
      Set<AbstractConfigParam<T>> configParams) {

      Map<AbstractConfigParam<T>, T> returnedMap = new HashMap<>();

      for (Iterator<AbstractConfigParam<T>> iterator = configParams.iterator(); iterator.hasNext();) {
         AbstractConfigParam<T> abstractConfigParam = iterator.next();

         T[] validValues = testClassInstance.getValidValues(abstractConfigParam);

         if (validValues.length > 0) {
            T value = validValues[0];

            returnedMap.put(abstractConfigParam, value);
         }
      }

      return returnedMap;
   }

   /**
    * Checks that {@link IConfigurable#getConfigParam(AbstractConfigParam)} returns the expected values.
    */
   private <T extends Comparable<T>> void checkGetConfigParam(Set<AbstractConfigParam<T>> paramSet) {

      Map<AbstractConfigParam<T>, T> expectedParamValues = getExpectedParamValues(paramSet);

      for (Iterator<AbstractConfigParam<T>> iterator = expectedParamValues.keySet().iterator(); iterator.hasNext();) {
         AbstractConfigParam<T> nextKey = iterator.next();
         T nextValue = expectedParamValues.get(nextKey);

         Assert.assertEquals(testling.getConfigParam(nextKey), nextValue);
      }
   }

   private <T extends Comparable<T>> Map<AbstractConfigParam<T>, T> getExpectedParamValues(
      Set<AbstractConfigParam<T>> paramSet) {

      if (paramSet.isEmpty()) {
         return new HashMap<>();
      }

      ConfigParamCheckType expectation = paramValueExpectations.get(paramSet.iterator().next().getValueClass());

      switch (expectation) {
         case DEFAULT_VALUE:
            return getDefaultValues(paramSet);

         case FIRST_VALID_VALUE:
            return getFirstValidValueForParams(paramSet);

         default:
            throw new IllegalStateException("Unhandled enum value for ParamValueExpectation");
      }
   }

   /**
    * Returns the default values for the given configuration parameters of type T.
    * 
    * @param configParams
    *           The {@link AbstractConfigParam}s for which to retrieve default values.
    * @return The Map of default values for each given parameter.
    * 
    */
   private <T extends Comparable<T>> Map<AbstractConfigParam<T>, T> getDefaultValues(
      Set<AbstractConfigParam<T>> configParams) {

      Map<AbstractConfigParam<T>, T> returnedMap = new HashMap<>();

      for (Iterator<AbstractConfigParam<T>> iterator = configParams.iterator(); iterator.hasNext();) {
         AbstractConfigParam<T> abstractConfigParam = iterator.next();
         returnedMap.put(abstractConfigParam, abstractConfigParam.getDefaultValue());
      }

      return returnedMap;
   }

   private <T extends Comparable<T>> void checkSetGetValidParamValues(Set<AbstractConfigParam<T>> configParams) {
      for (Iterator<AbstractConfigParam<T>> iterator = configParams.iterator(); iterator.hasNext();) {
         AbstractConfigParam<T> abstractConfigParam = iterator.next();

         T[] validValues = testClassInstance.getValidValues(abstractConfigParam);

         for (int i = 0; i < validValues.length; i++) {
            testling.setConfigParam(abstractConfigParam, validValues[i]);
            Assert.assertEquals(validValues[i], testling.getConfigParam(abstractConfigParam));
         }
      }
   }

   private <T extends Comparable<T>> void setValueForParams(Map<AbstractConfigParam<T>, T> paramValueMap) {

      for (Iterator<AbstractConfigParam<T>> iterator = paramValueMap.keySet().iterator(); iterator.hasNext();) {
         AbstractConfigParam<T> abstractConfigParam = iterator.next();
         T value = paramValueMap.get(abstractConfigParam);

         testling.setConfigParam(abstractConfigParam, value);
      }
   }

   private <T extends Comparable<T>> void checkSetInvalidParamValues(Set<AbstractConfigParam<T>> configParams) {
      for (Iterator<AbstractConfigParam<T>> iterator = configParams.iterator(); iterator.hasNext();) {
         AbstractConfigParam<T> abstractConfigParam = iterator.next();

         T[] invalidValues = testClassInstance.getInvalidValues(abstractConfigParam);

         for (int i = 0; i < invalidValues.length; i++) {

            try {
               testling.setConfigParam(abstractConfigParam, invalidValues[i]);
               Assert.fail("Expected exception of type " + InvalidConfigParamException.class);
            } catch (InvalidConfigParamException e) {
               Assert.assertNotNull("Exception as expected", e);
            }
         }
      }
   }

   private void checkDefaultValues(Map<AbstractConfigParam<? extends Comparable<?>>, Comparable<?>> allConfigParams) {
      for (Iterator<AbstractConfigParam<? extends Comparable<?>>> iterator = allConfigParams.keySet()
         .iterator(); iterator.hasNext();) {
         AbstractConfigParam<? extends Comparable<?>> nextKey = iterator.next();
         Object nextValue = allConfigParams.get(nextKey);

         Assert.assertEquals(nextKey.getDefaultValue(), nextValue);
      }
   }

   private void checkDefaultValues(Set<AbstractConfigParam<?>> allConfigParams, Properties allValues) {
      for (Iterator<AbstractConfigParam<?>> iterator = allConfigParams.iterator(); iterator.hasNext();) {
         AbstractConfigParam<?> nextKey = iterator.next();
         String nextValue = allValues.getProperty(nextKey.getName());

         Assert.assertEquals(nextKey.getDefaultValue().toString(), nextValue);
      }
   }

   /**
    * @return a complete {@link Set} of all {@link AbstractConfigParam}s that are expected for the tested
    *         {@link IConfigurable} instance.
    */
   private Set<AbstractConfigParam<?>> getCompleteConfigParamSet() {
      Set<AbstractConfigParam<?>> completeSet = new HashSet<>();

      completeSet.addAll(stringConfigParams);
      completeSet.addAll(firstEnumConfigParams);
      completeSet.addAll(secondEnumConfigParams);
      completeSet.addAll(booleanConfigParams);
      completeSet.addAll(integerConfigParams);
      completeSet.addAll(longConfigParams);
      completeSet.addAll(doubleConfigParams);

      return completeSet;
   }
}
