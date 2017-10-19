package de.je.util.javautil.common.configparams;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;

/**
 * {@link AbstractConfigurableTest} is an abstract base class for testing classes that implement the
 * {@link IConfigurable} interface. For every concrete class that implements {@link IConfigurable}, extend this abstract
 * class and provide its abstract method implementation as described for these methods.
 *
 * @param <E>
 *           The first type of enum class to test enum config params, or {@link EmptyEnum} if no further class to test.
 * @param <F>
 *           The second type of enum class to test enum config params, or {@link EmptyEnum} if no further class to test.
 */
public abstract class AbstractConfigurableTest<E extends Enum<E>, F extends Enum<F>> {

   /**
    * Tests {@link IConfigurable#getSupportedConfigParams()}.
    */
   @Test
   public void getSupportedConfigParams_allExpectedParamsSupported() {
      IConfigurableChecker<E, F> configurableChecker = new IConfigurableChecker<>(this);

      configurableChecker.checkGetSupportedConfigParams();
   }

   /**
    * Tests {@link IConfigurable#getSupportedConfigParams()}.
    */
   @Test
   public void getSupportedConfigParams_doesNotReturnDuplicateNames() {
      IConfigurable testling = getConfigurableInstance();

      Set<String> configParamNames = new HashSet<>();

      Set<AbstractConfigParam<? extends Comparable<?>>> supportedConfigParams = testling.getSupportedConfigParams();

      for (AbstractConfigParam<? extends Comparable<?>> abstractConfigParam : supportedConfigParams) {
         configParamNames.add(abstractConfigParam.getName());
      }

      Assert.assertEquals(configParamNames.size(), supportedConfigParams.size());
   }

   /**
    * Tests {@link IConfigurable#getConfigParam(AbstractConfigParam)}.
    */
   @Test
   public void getConfigParam_initially_returnsConfigParamWithDefaultValue() {
      IConfigurableChecker<E, F> configurableChecker = new IConfigurableChecker<>(this);

      configurableChecker.setParamValueExpectation(Boolean.class, ConfigParamCheckType.DEFAULT_VALUE);
      configurableChecker.setParamValueExpectation(String.class, ConfigParamCheckType.DEFAULT_VALUE);
      configurableChecker.setParamValueExpectation(Double.class, ConfigParamCheckType.DEFAULT_VALUE);
      configurableChecker.setParamValueExpectation(Integer.class, ConfigParamCheckType.DEFAULT_VALUE);
      configurableChecker.setParamValueExpectation(Long.class, ConfigParamCheckType.DEFAULT_VALUE);
      configurableChecker.setParamValueExpectation(getFirstEnumClass(), ConfigParamCheckType.DEFAULT_VALUE);
      configurableChecker.setParamValueExpectation(getSecondEnumClass(), ConfigParamCheckType.DEFAULT_VALUE);

      configurableChecker.checkGetConfigParams();
   }

   /**
    * Tests {@link IConfigurable#getConfigParam(AbstractConfigParam)}.
    */
   @Test(expected = InvalidConfigParamException.class)
   public void getConfigParam_forUnsupportedParam_throwsException() {
      IConfigurable testling = getConfigurableInstance();

      testling.getConfigParam(new StringConfigParam("my param", "nothing"));
   }

   /**
    * Tests {@link IConfigurable#getAllConfigParams()}.
    */
   @Test
   public void getAllConfigParams_initially_returnsAllConfigParamsWithDefaultValue() {
      IConfigurableChecker<E, F> configurableChecker = new IConfigurableChecker<>(this);

      configurableChecker.checkGetAllConfigParams(ConfigParamCheckType.DEFAULT_VALUE);
   }

   /**
    * Tests {@link IConfigurable#getAllConfigParams}.
    */
   @Test
   public void getAllConfigParams_returnsAllExpectedParams() {
      IConfigurableChecker<E, F> configurableChecker = new IConfigurableChecker<>(this);

      configurableChecker.checkGetAllConfigParams(ConfigParamCheckType.PARAMS_ONLY);
   }

   /**
    * Tests {@link IConfigurable#getAllConfigParamsAsProperties()}.
    */
   @Test
   public void getAllConfigParamsAsProperties_initially_returnsAllConfigParamsWithDefaultValue() {
      IConfigurableChecker<E, F> configurableChecker = new IConfigurableChecker<>(this);

      configurableChecker.checkGetAllConfigParamsAsProperties(ConfigParamCheckType.DEFAULT_VALUE);
   }

   /**
    * Tests {@link IConfigurable#getAllConfigParamsAsProperties}.
    */
   @Test
   public void getAllConfigParamsAsProperties_returnsAllExpectedParams() {
      IConfigurable testling = getConfigurableInstance();

      Set<AbstractConfigParam<? extends Comparable<?>>> paramsKeySet = testling.getAllConfigParams().keySet();

      Properties allParamsAsProps = testling.getAllConfigParamsAsProperties();

      for (AbstractConfigParam<? extends Comparable<?>> abstractConfigParam : paramsKeySet) {

         Assert.assertTrue(allParamsAsProps.containsKey(abstractConfigParam.getName()));
      }
   }

   /**
    * Tests {@link IConfigurable#resetConfigToDefault()}.
    */
   @Test
   public void resetConfigToDefault_initially_changesNothing() {
      IConfigurableChecker<E, F> configurableChecker = new IConfigurableChecker<>(this);

      configurableChecker.checkResetConfigToDefault();
   }

   /**
    * Tests {@link IConfigurable#resetConfigToDefault()}.
    */
   @Test
   public void resetConfigToDefault_overridesChangesWithDefaults() {
      IConfigurableChecker<E, F> configurableChecker = new IConfigurableChecker<>(this);

      configurableChecker.setFirstValidValuesForAllParams();

      configurableChecker.checkResetConfigToDefault();
   }

   /**
    * Tests {@link IConfigurable#configureFromProperties(Properties)}.
    */
   @Test
   public void configureFromProperties_forMixOfValidAndInvalidValues_throwsException() {
      IConfigurable testling = getConfigurableInstance();

      IConfigurableChecker<E, F> configurableChecker = new IConfigurableChecker<>(this);

      Map<ConfigParamCheckType, Class<?>[]> classMapping = new HashMap<>();

      classMapping.put(ConfigParamCheckType.FIRST_VALID_VALUE, new Class<?>[] { getFirstEnumClass() });
      classMapping.put(ConfigParamCheckType.FIRST_INVALID_VALUE_NON_NULL,
         new Class<?>[] { String.class, getSecondEnumClass(), Double.class, Boolean.class, Integer.class });

      Properties invalidProperties = configurableChecker.getPropertiesFromProvidedValues(classMapping);

      try {
         testling.configureFromProperties(invalidProperties);
         Assert.fail("Expected exception of type " + InvalidConfigParamException.class);
      } catch (InvalidConfigParamException e) {
         Assert.assertNotNull("Exception as expected", e);
      }
   }

   /**
    * Tests {@link IConfigurable#configureFromProperties(Properties)}.
    */
   @Test
   public void configureFromProperties_forUnknownProperties_remainsUnchanged() {
      IConfigurableChecker<E, F> configurableChecker = new IConfigurableChecker<>(this);

      Properties unknownProperties = new Properties();

      unknownProperties.setProperty("Unknown 1", "i don't care");
      unknownProperties.setProperty("Unknown 2", "some unknown value");
      unknownProperties.setProperty("Unknown 3", "true");
      unknownProperties.setProperty("Unknown 4", "5.6");

      configurableChecker.setParamValueExpectation(Boolean.class, ConfigParamCheckType.DEFAULT_VALUE);
      configurableChecker.setParamValueExpectation(String.class, ConfigParamCheckType.DEFAULT_VALUE);
      configurableChecker.setParamValueExpectation(Double.class, ConfigParamCheckType.DEFAULT_VALUE);
      configurableChecker.setParamValueExpectation(Integer.class, ConfigParamCheckType.DEFAULT_VALUE);
      configurableChecker.setParamValueExpectation(Long.class, ConfigParamCheckType.DEFAULT_VALUE);
      configurableChecker.setParamValueExpectation(getFirstEnumClass(), ConfigParamCheckType.DEFAULT_VALUE);
      configurableChecker.setParamValueExpectation(getSecondEnumClass(), ConfigParamCheckType.DEFAULT_VALUE);

      configurableChecker.checkConfigureFromProperties(unknownProperties, false);
   }

   /**
    * Tests {@link IConfigurable#configureFromProperties(Properties)} and
    * {@link IConfigurable#getConfigParam(AbstractConfigParam)}.
    */
   @Test
   public void configureFromProperties_mixOfKnownAndUnknown_unsetParamsRemainUnchanged() {
      IConfigurableChecker<E, F> configurableChecker = new IConfigurableChecker<>(this);

      Map<ConfigParamCheckType, Class<?>[]> classMapping = new HashMap<>();

      classMapping.put(ConfigParamCheckType.FIRST_VALID_VALUE,
         new Class<?>[] { String.class, getSecondEnumClass(), Integer.class });
      classMapping.put(ConfigParamCheckType.FIRST_INVALID_VALUE_NON_NULL, new Class<?>[] {});

      Properties validProperties = configurableChecker.getPropertiesFromProvidedValues(classMapping);

      configurableChecker.setParamValueExpectation(Boolean.class, ConfigParamCheckType.DEFAULT_VALUE);
      configurableChecker.setParamValueExpectation(String.class, ConfigParamCheckType.FIRST_VALID_VALUE);
      configurableChecker.setParamValueExpectation(Double.class, ConfigParamCheckType.DEFAULT_VALUE);
      configurableChecker.setParamValueExpectation(Integer.class, ConfigParamCheckType.FIRST_VALID_VALUE);
      configurableChecker.setParamValueExpectation(Long.class, ConfigParamCheckType.DEFAULT_VALUE);
      configurableChecker.setParamValueExpectation(getFirstEnumClass(), ConfigParamCheckType.DEFAULT_VALUE);
      configurableChecker.setParamValueExpectation(getSecondEnumClass(), ConfigParamCheckType.FIRST_VALID_VALUE);

      validProperties.setProperty("Unknown 1", "i don't care");
      validProperties.setProperty("Unknown 2", "some unknown value");
      validProperties.setProperty("Unknown 3", "true");
      validProperties.setProperty("Unknown 4", "5.6");

      configurableChecker.checkConfigureFromProperties(validProperties, false);
   }

   /**
    * Tests {@link IConfigurable#configureFromProperties(Properties)} and
    * {@link IConfigurable#getConfigParam(AbstractConfigParam)}.
    */
   @Test
   public void configureFromProperties_getConfigParam_returnsValidValuesSet() {
      IConfigurableChecker<E, F> configurableChecker = new IConfigurableChecker<>(this);

      Map<ConfigParamCheckType, Class<?>[]> classMapping = new HashMap<>();

      classMapping.put(ConfigParamCheckType.FIRST_VALID_VALUE, new Class<?>[] { String.class, Integer.class, Long.class,
         Double.class, Boolean.class, getSecondEnumClass(), getFirstEnumClass() });
      classMapping.put(ConfigParamCheckType.FIRST_INVALID_VALUE_NON_NULL, new Class<?>[] {});

      Properties validProperties = configurableChecker.getPropertiesFromProvidedValues(classMapping);

      configurableChecker.setParamValueExpectation(Boolean.class, ConfigParamCheckType.FIRST_VALID_VALUE);
      configurableChecker.setParamValueExpectation(String.class, ConfigParamCheckType.FIRST_VALID_VALUE);
      configurableChecker.setParamValueExpectation(Double.class, ConfigParamCheckType.FIRST_VALID_VALUE);
      configurableChecker.setParamValueExpectation(Integer.class, ConfigParamCheckType.FIRST_VALID_VALUE);
      configurableChecker.setParamValueExpectation(Long.class, ConfigParamCheckType.FIRST_VALID_VALUE);
      configurableChecker.setParamValueExpectation(getFirstEnumClass(), ConfigParamCheckType.FIRST_VALID_VALUE);
      configurableChecker.setParamValueExpectation(getSecondEnumClass(), ConfigParamCheckType.FIRST_VALID_VALUE);

      configurableChecker.checkConfigureFromProperties(validProperties, false);
   }

   /**
    * Tests {@link IConfigurable#configureFromProperties(Properties)} and {@link IConfigurable#getAllConfigParams()}.
    */
   @Test
   public void configureFromProperties_getAllConfigParams_returnsValidValuesSet() {
      IConfigurableChecker<E, F> configurableChecker = new IConfigurableChecker<>(this);

      Map<ConfigParamCheckType, Class<?>[]> classMapping = new HashMap<>();

      classMapping.put(ConfigParamCheckType.FIRST_VALID_VALUE, new Class<?>[] { String.class, Integer.class, Long.class,
         Double.class, Boolean.class, getSecondEnumClass(), getFirstEnumClass() });
      classMapping.put(ConfigParamCheckType.FIRST_INVALID_VALUE_NON_NULL, new Class<?>[] {});

      Properties validProperties = configurableChecker.getPropertiesFromProvidedValues(classMapping);

      configurableChecker.setParamValueExpectation(Boolean.class, ConfigParamCheckType.FIRST_VALID_VALUE);
      configurableChecker.setParamValueExpectation(String.class, ConfigParamCheckType.FIRST_VALID_VALUE);
      configurableChecker.setParamValueExpectation(Double.class, ConfigParamCheckType.FIRST_VALID_VALUE);
      configurableChecker.setParamValueExpectation(Integer.class, ConfigParamCheckType.FIRST_VALID_VALUE);
      configurableChecker.setParamValueExpectation(Long.class, ConfigParamCheckType.FIRST_VALID_VALUE);
      configurableChecker.setParamValueExpectation(getFirstEnumClass(), ConfigParamCheckType.FIRST_VALID_VALUE);
      configurableChecker.setParamValueExpectation(getSecondEnumClass(), ConfigParamCheckType.FIRST_VALID_VALUE);

      configurableChecker.checkConfigureFromProperties(validProperties, false);
   }

   /**
    * Tests {@link IConfigurable#configureFromProperties(Properties)} and
    * {@link IConfigurable#getAllConfigParamsAsProperties()}.
    */
   @Test
   public void configureFromProperties_getAllConfigParamsAsProperties_returnsValidValuesSet() {
      IConfigurableChecker<E, F> configurableChecker = new IConfigurableChecker<>(this);

      Map<ConfigParamCheckType, Class<?>[]> classMapping = new HashMap<>();

      classMapping.put(ConfigParamCheckType.FIRST_VALID_VALUE, new Class<?>[] { String.class, Integer.class, Long.class,
         Double.class, Boolean.class, getSecondEnumClass(), getFirstEnumClass() });
      classMapping.put(ConfigParamCheckType.FIRST_INVALID_VALUE_NON_NULL, new Class<?>[] {});

      Properties validProperties = configurableChecker.getPropertiesFromProvidedValues(classMapping);

      configurableChecker.setParamValueExpectation(Boolean.class, ConfigParamCheckType.FIRST_VALID_VALUE);
      configurableChecker.setParamValueExpectation(String.class, ConfigParamCheckType.FIRST_VALID_VALUE);
      configurableChecker.setParamValueExpectation(Double.class, ConfigParamCheckType.FIRST_VALID_VALUE);
      configurableChecker.setParamValueExpectation(Integer.class, ConfigParamCheckType.FIRST_VALID_VALUE);
      configurableChecker.setParamValueExpectation(Long.class, ConfigParamCheckType.FIRST_VALID_VALUE);
      configurableChecker.setParamValueExpectation(getFirstEnumClass(), ConfigParamCheckType.FIRST_VALID_VALUE);
      configurableChecker.setParamValueExpectation(getSecondEnumClass(), ConfigParamCheckType.FIRST_VALID_VALUE);

      // We additionally want to check that getAllConfigParamsAsProperties() returns the expected properties
      configurableChecker.checkConfigureFromProperties(validProperties, true);
   }

   /**
    * Tests {@link IConfigurable#setConfigParam(AbstractConfigParam, Comparable)} and
    * {@link IConfigurable#getConfigParam(AbstractConfigParam)}.
    */
   @Test
   public void setConfigParam_getConfigParam_returnsValidValuesSet() {
      IConfigurableChecker<E, F> configurableChecker = new IConfigurableChecker<>(this);

      configurableChecker.checkSetValidParamValuesWorks();
   }

   /**
    * Tests {@link IConfigurable#setConfigParam(AbstractConfigParam, Comparable)} and
    * {@link IConfigurable#getAllConfigParams()}.
    */
   @Test
   public void setConfigParam_getAllConfigParams_returnsValidValuesSet() {
      IConfigurableChecker<E, F> configurableChecker = new IConfigurableChecker<>(this);

      configurableChecker.setFirstValidValuesForAllParams();

      configurableChecker.checkGetAllConfigParams(ConfigParamCheckType.FIRST_VALID_VALUE);
   }

   /**
    * Tests {@link IConfigurable#setConfigParam(AbstractConfigParam, Comparable)} and
    * {@link IConfigurable#getAllConfigParamsAsProperties()}.
    */
   @Test
   public void setConfigParam_getAllConfigParamsAsProperties_returnsValidValuesSet() {
      IConfigurableChecker<E, F> configurableChecker = new IConfigurableChecker<>(this);

      configurableChecker.setFirstValidValuesForAllParams();

      configurableChecker.checkGetAllConfigParamsAsProperties(ConfigParamCheckType.FIRST_VALID_VALUE);
   }

   /**
    * Tests {@link IConfigurable#setConfigParam}.
    */
   @Test
   public void setConfigParam_forInvalidValues_throwsException() {
      IConfigurableChecker<E, F> configurableChecker = new IConfigurableChecker<>(this);

      configurableChecker.checkSetInvalidParamValuesThrowsException();
   }

   /**
    * Tests {@link IConfigurable#setConfigParam(AbstractConfigParam, Comparable)}.
    */
   @Test(expected = InvalidConfigParamException.class)
   public void setConfigParam_forUnsupportedParam_throwsException() {
      IConfigurable testling = getConfigurableInstance();

      testling.setConfigParam(new StringConfigParam("my param", "nothing"), "new value");
   }

   /**
    * Tests {@link IConfigurable#addConfigChangeListener(IConfigChangeListener)}.
    */
   @Test
   public void addConfigChangeListener_initialAddCall_notifiesListener() {
      IConfigurableChecker<E, F> configurableChecker = new IConfigurableChecker<>(this);

      CalledConfigChangeListener listener = new CalledConfigChangeListener();

      configurableChecker.checkInitialAddConfigChangeListener(listener);
   }

   /**
    * Tests {@link IConfigurable#addConfigChangeListener(IConfigChangeListener)}.
    */
   @Test
   public void addConfigChangeListener_setConfigParamWithValidValue_notifiesListener() {
      IConfigurableChecker<E, F> configurableChecker = new IConfigurableChecker<>(this);

      CalledConfigChangeListener listener = new CalledConfigChangeListener();

      configurableChecker.checkInitialAddConfigChangeListener(listener);
      configurableChecker.checkSubsequentSetNotifiesListener(listener);
   }

   /**
    * Tests {@link IConfigurable#addConfigChangeListener(IConfigChangeListener)}.
    */
   @Test
   public void addConfigChangeListenerTwice_setConfigParamWithValidValue_notifiesListenerOnce() {
      IConfigurableChecker<E, F> configurableChecker = new IConfigurableChecker<>(this);

      CalledConfigChangeListener listener = new CalledConfigChangeListener();

      // Add the listener twice
      configurableChecker.checkInitialAddConfigChangeListener(listener);
      configurableChecker.checkInitialAddConfigChangeListener(listener);

      configurableChecker.checkSubsequentSetNotifiesListener(listener);
   }

   /**
    * Tests {@link IConfigurable#addConfigChangeListener(IConfigChangeListener)}.
    */
   @Test
   public void addConfigChangeListenerTwice_initialAddCall_notifiesListenerOnce() {
      IConfigurableChecker<E, F> configurableChecker = new IConfigurableChecker<>(this);

      CalledConfigChangeListener listener = new CalledConfigChangeListener();

      // Add the listener twice
      configurableChecker.checkInitialAddConfigChangeListener(listener);

      int expectedCallCounter = listener.getCallCounter();

      configurableChecker.checkInitialAddConfigChangeListener(listener);

      Assert.assertEquals(expectedCallCounter, listener.getCallCounter());
   }

   /**
    * Tests {@link IConfigurable#addConfigChangeListener(IConfigChangeListener)}.
    */
   @Test
   public void addConfigChangeListener_setConfigParamWithMultipleListeners_notifiesAllListeners() {
      IConfigurableChecker<E, F> configurableChecker = new IConfigurableChecker<>(this);

      CalledConfigChangeListener listenerOne = new CalledConfigChangeListener();
      CalledConfigChangeListener listenerTwo = new CalledConfigChangeListener();

      configurableChecker.checkInitialAddConfigChangeListener(listenerOne);
      configurableChecker.checkInitialAddConfigChangeListener(listenerTwo);

      listenerOne.reset();
      listenerTwo.reset();

      Map<AbstractConfigParam<? extends Comparable<?>>, Comparable<?>> allValidValues = configurableChecker
         .getFirstValidValueForAllParams();

      listenerOne.setExpectedParams(allValidValues);
      listenerTwo.setExpectedParams(allValidValues);

      configurableChecker.setFirstValidValuesForAllParams();

      configurableChecker.checkListenerGotNotifiedOncePerValue(allValidValues, listenerOne);
      configurableChecker.checkListenerGotNotifiedOncePerValue(allValidValues, listenerTwo);
   }

   /**
    * Tests {@link IConfigurable#addConfigChangeListener(IConfigChangeListener)}.
    */
   @Test
   public void addConfigChangeListener_setConfigParamWithInvalidValue_neverNotifiesListener() {
      IConfigurableChecker<E, F> configurableChecker = new IConfigurableChecker<>(this);

      CalledConfigChangeListener listener = new CalledConfigChangeListener();

      configurableChecker.checkInitialAddConfigChangeListener(listener);

      listener.reset();

      configurableChecker.checkSetInvalidParamValuesThrowsException();

      Assert.assertEquals(0, listener.getCallCounter());
   }

   /**
    * Tests {@link IConfigurable#removeConfigChangeListener(IConfigChangeListener)}.
    */
   @Test
   public void removeConfigChangeListener_removingUnknownListener_hasNoEffect() {
      IConfigurable testling = getConfigurableInstance();

      testling.removeConfigChangeListener(new CalledConfigChangeListener());
   }

   /**
    * Tests {@link IConfigurable#removeConfigChangeListener(IConfigChangeListener)}.
    */
   @Test
   public void removeConfigChangeListener_removingPreviouslyAddedListener_isNotNotifiedAnymore() {
      IConfigurableChecker<E, F> configurableChecker = new IConfigurableChecker<>(this);

      CalledConfigChangeListener listener = new CalledConfigChangeListener();

      configurableChecker.checkInitialAddConfigChangeListener(listener);
      configurableChecker.checkRemoveConfigChangeListener(listener);
   }

   /**
    * Tests {@link IConfigurable#removeConfigChangeListener(IConfigChangeListener)}.
    */
   @Test
   public void removeConfigChangeListener_addAndRemoveMultiple_notifiedAsExpected() {
      CalledConfigChangeListener listenerOne = new CalledConfigChangeListener();
      CalledConfigChangeListener listenerTwo = new CalledConfigChangeListener();

      IConfigurableChecker<E, F> configurableChecker = new IConfigurableChecker<>(this);

      configurableChecker.checkInitialAddConfigChangeListener(listenerOne);
      configurableChecker.checkInitialAddConfigChangeListener(listenerTwo);

      listenerTwo.reset();

      Map<AbstractConfigParam<? extends Comparable<?>>, Comparable<?>> values = configurableChecker
         .getFirstValidValueForAllParams();

      listenerTwo.setExpectedParams(values);

      configurableChecker.checkRemoveConfigChangeListener(listenerOne);

      configurableChecker.checkListenerGotNotifiedOncePerValue(values, listenerTwo);
   }

   /**
    * @return the {@link IConfigurable} instance to use in all test methods. Should be a newly created instance.
    */
   protected abstract IConfigurable getConfigurableInstance();

   /**
    * Returns an array containing an arbitrary number of valid values for the given {@link AbstractConfigParam}.
    * 
    * @param param
    *           The {@link AbstractConfigParam} for which to retrieve valid values
    * @return The array of valid values.
    */
   protected abstract <T extends Comparable<T>> T[] getValidValues(AbstractConfigParam<T> param);

   /**
    * Returns an array containing an arbitrary number of invalid values for the given {@link AbstractConfigParam}.
    * 
    * @param param
    *           The {@link AbstractConfigParam} for which to retrieve invalid values
    * @return The array of invalid values.
    */
   protected abstract <T extends Comparable<T>> T[] getInvalidValues(AbstractConfigParam<T> param);

   /**
    * @return the {@link Class} instance of type E (the first enum type supported by this test class).
    */
   protected abstract Class<E> getFirstEnumClass();

   /**
    * @return the {@link Class} instance of type F (the second enum type supported by this test class).
    */
   protected abstract Class<F> getSecondEnumClass();

   /**
    * @return the {@link Set} of {@link AbstractConfigParam}s with an Integer type that are supported by the tested
    *         {@link IConfigurable} instance returned by {@link #getConfigurableInstance()}. By default, it returns an
    *         empty {@link Set}. If subclasses need to test for specific supported parameters, they should override this
    *         method and return them here.
    */
   protected Set<AbstractConfigParam<Integer>> getIntegerConfigParams() {
      return new HashSet<>();
   }

   /**
    * @return the {@link Set} of {@link AbstractConfigParam}s with a Long type that are supported by the tested
    *         {@link IConfigurable} instance returned by {@link #getConfigurableInstance()}. By default, it returns an
    *         empty {@link Set}. If subclasses need to test for specific supported parameters, they should override this
    *         method and return them here.
    */
   protected Set<AbstractConfigParam<Long>> getLongConfigParams() {
      return new HashSet<>();
   }

   /**
    * @return the {@link Set} of {@link AbstractConfigParam}s with a Double type that are supported by the tested
    *         {@link IConfigurable} instance returned by {@link #getConfigurableInstance()}. By default, it returns an
    *         empty {@link Set}. If subclasses need to test for specific supported parameters, they should override this
    *         method and return them here.
    */
   protected Set<AbstractConfigParam<Double>> getDoubleConfigParams() {
      return new HashSet<>();
   }

   /**
    * @return the {@link Set} of {@link AbstractConfigParam}s with a String type that are supported by the tested
    *         {@link IConfigurable} instance returned by {@link #getConfigurableInstance()}. By default, it returns an
    *         empty {@link Set}. If subclasses need to test for specific supported parameters, they should override this
    *         method and return them here.
    */
   protected Set<AbstractConfigParam<String>> getStringConfigParams() {
      return new HashSet<>();
   }

   /**
    * @return the {@link Set} of {@link AbstractConfigParam}s with a Boolean type that are supported by the tested
    *         {@link IConfigurable} instance returned by {@link #getConfigurableInstance()}. By default, it returns an
    *         empty {@link Set}. If subclasses need to test for specific supported parameters, they should override this
    *         method and return them here.
    */
   protected Set<AbstractConfigParam<Boolean>> getBooleanConfigParams() {
      return new HashSet<>();
   }

   /**
    * @return the {@link Set} of {@link AbstractConfigParam}s with type E (the first enum type supported by this test
    *         class) that are supported by the tested {@link IConfigurable} instance returned by
    *         {@link #getConfigurableInstance()}. By default, it returns an empty {@link Set}. If subclasses need to
    *         test for specific supported parameters, they should override this method and return them here.
    */
   protected Set<AbstractConfigParam<E>> getFirstEnumConfigParams() {
      return new HashSet<>();
   }

   /**
    * @return the {@link Set} of {@link AbstractConfigParam}s with type F (the second enum type supported by this test
    *         class) that are supported by the tested {@link IConfigurable} instance returned by
    *         {@link #getConfigurableInstance()}. By default, it returns an empty {@link Set}. If subclasses need to
    *         test for specific supported parameters, they should override this method and return them here.
    */
   protected Set<AbstractConfigParam<F>> getSecondEnumConfigParams() {
      return new HashSet<>();
   }
}
