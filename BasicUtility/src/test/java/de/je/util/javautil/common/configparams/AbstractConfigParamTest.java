package de.je.util.javautil.common.configparams;

import java.util.Set;

import org.junit.Assert;
import org.junit.Test;

/**
 * {@link AbstractConfigParamTest} tests the {@link AbstractConfigParam} class and its derived classes.
 */
public class AbstractConfigParamTest {

   /**
    * Tests {@link AbstractConfigParam#AbstractConfigParam(Class, String, Comparable, Comparable, Comparable, Comparable[])}
    */
   @Test
   public void getters_returnValidConstructorValues() {
      String name = "my name";
      Integer defaultValue = -10;
      Integer maximumValue = 56;
      Integer minimumValue = -maximumValue;
      Integer[] possibleValueArray = new Integer[] { -56, -10, 56 };

      AbstractConfigParam<Integer> testling = new IntegerConfigParam(name, defaultValue, minimumValue, maximumValue,
         possibleValueArray);

      Assert.assertEquals(maximumValue, testling.getMaximumValue());
      Assert.assertEquals(minimumValue, testling.getMinimumValue());
      Assert.assertEquals(defaultValue, testling.getDefaultValue());
      Set<Integer> possibleValues = testling.getPossibleValues();
      Assert.assertEquals(possibleValueArray.length, possibleValues.size());

      for (int i = 0; i < possibleValueArray.length; i++) {
         Assert.assertTrue(possibleValues.contains(possibleValueArray[i]));
      }
   }

   /**
    * Tests {@link AbstractConfigParam#valueToString(Comparable)}.
    */
   @Test
   public void valueToString_returnsStringRepresentationOfInt() {
      AbstractConfigParam<Integer> param = getDefaultIntegerParameter();

      String stringValue = param.valueToString(19);

      Assert.assertEquals("19", stringValue);
   }

   /**
    * Tests {@link AbstractConfigParam#valueToString(Comparable)}.
    */
   @Test
   public void valueToString_returnsStringRepresentationOfDouble() {
      AbstractConfigParam<Double> param = getDefaultFloatingParameter();

      String stringValue = param.valueToString(19.99);

      Assert.assertEquals("19.99", stringValue);
   }

   /**
    * Tests {@link AbstractConfigParam#valueToString(Comparable)}.
    */
   @Test
   public void valueToString_returnsStringRepresentationOfString() {
      AbstractConfigParam<String> param = getDefaultStringParameter();

      String stringValue = param.valueToString("huhuu");

      Assert.assertEquals("huhuu", stringValue);
   }

   /**
    * Tests {@link AbstractConfigParam#valueToString(Comparable)}.
    */
   @Test
   public void valueToString_returnsStringRepresentationOfBoolean() {
      AbstractConfigParam<Boolean> param = getDefaultBoolParameter();

      String stringValue = param.valueToString(true);

      Assert.assertEquals("true", stringValue);
   }

   /**
    * Tests {@link AbstractConfigParam#valueToString(Comparable)}.
    */
   @Test
   public void valueToString_returnsStringRepresentationOfEnum() {
      AbstractConfigParam<ConfigParamTestEnum> param = getDefaultEnumParameter();

      String stringValue = param.valueToString(ConfigParamTestEnum.SECOND_INSTANCE);

      Assert.assertEquals("SECOND_INSTANCE", stringValue);
   }

   /**
    * Tests {@link AbstractConfigParam#stringToValue}.
    */
   @Test
   public void stringToValue_returnsIntValueForString() {
      AbstractConfigParam<Integer> param = getDefaultIntegerParameter();

      Integer typedValue = param.stringToValue("19");

      Assert.assertEquals(new Integer(19), typedValue);
   }

   /**
    * Tests {@link AbstractConfigParam#stringToValue}.
    */
   @Test
   public void stringToValue_returnsDoubleValueForString() {
      AbstractConfigParam<Double> param = getDefaultFloatingParameter();

      Double typedValue = param.stringToValue("19.99");

      Assert.assertEquals(new Double(19.99), typedValue);
   }

   /**
    * Tests {@link AbstractConfigParam#stringToValue}.
    */
   @Test
   public void stringToValue_returnsStringValueForString() {
      AbstractConfigParam<String> param = getDefaultStringParameter();

      String typedValue = param.stringToValue("huhuh");

      Assert.assertEquals("huhuh", typedValue);
   }

   /**
    * Tests {@link AbstractConfigParam#stringToValue}.
    */
   @Test
   public void stringToValue_returnsBoolValueForString() {
      AbstractConfigParam<Boolean> param = getDefaultBoolParameter();

      Boolean typedValue = param.stringToValue("true");

      Assert.assertEquals(true, typedValue);
   }

   /**
    * Tests {@link AbstractConfigParam#stringToValue}.
    */
   @Test
   public void stringToValue_returnsEnumValueForString() {
      AbstractConfigParam<ConfigParamTestEnum> param = getDefaultEnumParameter();

      ConfigParamTestEnum typedValue = param.stringToValue("SECOND_INSTANCE");

      Assert.assertEquals(ConfigParamTestEnum.SECOND_INSTANCE, typedValue);
   }

   /**
    * Tests {@link AbstractConfigParam#checkValue}.
    * 
    * No exception is expected, just a green test case
    */
   @Test
   public void checkValue_validIntegerValues_doNotCauseException() {
      AbstractConfigParam<Integer> intParam = getDefaultIntegerParameter();
      checkCheckValue(intParam, 15);
   }

   /**
    * Tests {@link AbstractConfigParam#checkValue}.
    * 
    * No exception is expected, just a green test case
    */
   @Test
   public void checkValue_validStringValues_doNotCauseException() {
      AbstractConfigParam<String> stringParam = getDefaultStringParameter();
      checkCheckValue(stringParam, "bllal");
   }

   /**
    * Tests {@link AbstractConfigParam#checkValue}.
    * 
    * No exception is expected, just a green test case
    */
   @Test
   public void checkValue_validBoolValues_doNotCauseException() {
      AbstractConfigParam<Boolean> boolParam = getDefaultBoolParameter();
      checkCheckValue(boolParam, false);
   }

   /**
    * Tests {@link AbstractConfigParam#checkValue}.
    * 
    * No exception is expected, just a green test case
    */
   @Test
   public void checkValue_validDoubleValues_doNotCauseException() {
      AbstractConfigParam<Double> doubleParam = getDefaultFloatingParameter();
      checkCheckValue(doubleParam, 18.88);
   }

   /**
    * Tests {@link AbstractConfigParam#checkValue}.
    * 
    * No exception is expected, just a green test case
    */
   @Test
   public void checkValue_validEnumValues_doNotCauseException() {
      AbstractConfigParam<ConfigParamTestEnum> enumParam = getDefaultEnumParameter();
      checkCheckValue(enumParam, ConfigParamTestEnum.SECOND_INSTANCE);
   }

   /**
    * Tests {@link AbstractConfigParam#checkValue} in a negative case.
    */
   @Test(expected = InvalidConfigParamException.class)
   public void checkValue_tooSmallIntegerValue_causesException() {
      AbstractConfigParam<Integer> intParam = getDefaultIntegerParameter();
      checkCheckValue(intParam, intParam.getMinimumValue() - 10);
   }

   /**
    * Tests {@link AbstractConfigParam#checkValue} in a negative case.
    */
   @Test(expected = InvalidConfigParamException.class)
   public void checkValue_tooBigIntegerValue_causesException() {
      AbstractConfigParam<Integer> intParam = getDefaultIntegerParameter();
      checkCheckValue(intParam, intParam.getMaximumValue() + 4);
   }

   /**
    * Tests {@link AbstractConfigParam#checkValue} in a negative case.
    */
   @Test(expected = InvalidConfigParamException.class)
   public void checkValue_tooSmallDoubleValue_causesException() {
      AbstractConfigParam<Double> doubleParam = getDefaultFloatingParameter();
      checkCheckValue(doubleParam, doubleParam.getMinimumValue() - 10);
   }

   /**
    * Tests {@link AbstractConfigParam#checkValue} in a negative case.
    */
   @Test(expected = InvalidConfigParamException.class)
   public void checkValue_tooBigDoubleValue_causesException() {
      AbstractConfigParam<Double> doubleParam = getDefaultFloatingParameter();
      checkCheckValue(doubleParam, doubleParam.getMaximumValue() + 4);
   }

   /**
    * Tests {@link AbstractConfigParam#checkValue} in a negative case.
    */
   @Test(expected = InvalidConfigParamException.class)
   public void checkValue_unlistedStringValue_causesException() {
      AbstractConfigParam<String> stringParam = new StringConfigParam("string test", "ggggg", "aaaaa", "zzzzz",
         new String[] { "ggggg", "aaaaa", "zzzzz", "huhaa" });
      checkCheckValue(stringParam, "notli");
   }

   /**
    * Tests {@link AbstractConfigParam#checkValue} in a negative case.
    */
   @Test(expected = InvalidConfigParamException.class)
   public void checkValue_unlistedEnumValue_causesException() {
      AbstractConfigParam<ConfigParamTestEnum> enumParam = new EnumConfigParam<>(ConfigParamTestEnum.class,
         "enum test", ConfigParamTestEnum.SECOND_INSTANCE,
         new ConfigParamTestEnum[] { ConfigParamTestEnum.FIRST_INSTANCE,
            ConfigParamTestEnum.SECOND_INSTANCE, ConfigParamTestEnum.FOURTH_INSTANCE });
      checkCheckValue(enumParam, ConfigParamTestEnum.THIRD_INSTANCE);
   }

   /**
    * Tests {@link AbstractConfigParam#AbstractConfigParam(Class, String, Comparable, Comparable, Comparable, Comparable[])} in
    * a negative case.
    */
   @Test(expected = InvalidConfigParamException.class)
   public void constructor_minBiggerThanMax_causesException() {
      @SuppressWarnings("unused")
      AbstractConfigParam<Integer> testling = new IntegerConfigParam("test", 10, 10, 9, null);
   }

   /**
    * Tests {@link AbstractConfigParam#AbstractConfigParam(Class, String, Comparable, Comparable, Comparable, Comparable[])} in
    * a negative case.
    */
   @Test(expected = InvalidConfigParamException.class)
   public void constructor_defaultBiggerThanMax_causesException() {
      @SuppressWarnings("unused")
      AbstractConfigParam<Integer> testling = new IntegerConfigParam("test", 25, 0, 20, null);
   }

   /**
    * Tests {@link AbstractConfigParam#AbstractConfigParam(Class, String, Comparable, Comparable, Comparable, Comparable[])} in
    * a negative case.
    */
   @Test(expected = InvalidConfigParamException.class)
   public void constructor_defaultSmallerThanMin_causesException() {
      @SuppressWarnings("unused")
      AbstractConfigParam<Integer> testling = new IntegerConfigParam("test", -25, 0, 20, null);
   }

   /**
    * Tests {@link AbstractConfigParam#AbstractConfigParam(Class, String, Comparable, Comparable, Comparable, Comparable[])} in
    * a negative case.
    */
   @Test(expected = InvalidConfigParamException.class)
   public void constructor_defaultNotInPossibleValues_causesException() {
      @SuppressWarnings("unused")
      AbstractConfigParam<Integer> testling = new IntegerConfigParam("test", -25, new Integer[] { 1, 2, 3 });
   }

   /**
    * Tests {@link AbstractConfigParam#AbstractConfigParam(Class, String, Comparable, Comparable, Comparable, Comparable[])} in
    * a negative case.
    */
   @Test(expected = InvalidConfigParamException.class)
   public void constructor_minNotInPossibleValues_causesException() {
      @SuppressWarnings("unused")
      AbstractConfigParam<Integer> testling = new IntegerConfigParam("test", -25, 0, 20,
         new Integer[] { -25, 20, 1, 2, 3 });
   }

   /**
    * Tests {@link AbstractConfigParam#AbstractConfigParam(Class, String, Comparable, Comparable, Comparable, Comparable[])} in
    * a negative case.
    */
   @Test(expected = InvalidConfigParamException.class)
   public void constructor_maxNotInPossibleValues_causesException() {
      @SuppressWarnings("unused")
      AbstractConfigParam<Integer> testling = new IntegerConfigParam("test", -25, 0, 20,
         new Integer[] { -25, 0, 1, 2, 3 });
   }

   /**
    * @return a default integer parameter
    */
   private IntegerConfigParam getDefaultIntegerParameter() {
      return new IntegerConfigParam("int test", 20, 11, 22, null);
   }

   /**
    * @return a default enum parameter
    */
   private EnumConfigParam<ConfigParamTestEnum> getDefaultEnumParameter() {
      return new EnumConfigParam<>(ConfigParamTestEnum.class, "enum test", ConfigParamTestEnum.SECOND_INSTANCE,
         ConfigParamTestEnum.FIRST_INSTANCE, ConfigParamTestEnum.FOURTH_INSTANCE, ConfigParamTestEnum.values());
   }

   /**
    * @return a default floating parameter
    */
   private DoubleConfigParam getDefaultFloatingParameter() {
      return new DoubleConfigParam("double test", 20.09, 11.11, 22.12, null);
   }

   /**
    * @return a default string parameter
    */
   private StringConfigParam getDefaultStringParameter() {
      return new StringConfigParam("string test", "ggggg", "aaaaa", "zzzzz", null);
   }

   /**
    * @return a default boolean parameter
    */
   private BooleanConfigParam getDefaultBoolParameter() {
      return new BooleanConfigParam("bool test", true);
   }

   /**
    * Checks the method {@link AbstractConfigParam#checkValue(Comparable)} to work for default, minimum, maximum and
    * possible values as well as some random in-between value.
    * 
    * @param param
    *           The {@link AbstractConfigParam} instance to check
    * @param randomValidValueToCheck
    *           The random in-between value that is expected to be valid based on the given param
    */
   private <T extends Comparable<T>> void checkCheckValue(AbstractConfigParam<T> param, T randomValidValueToCheck) {
      param.checkValue(randomValidValueToCheck);
      param.checkValue(param.getDefaultValue());

      if (param.getMinimumValue() != null) {
         param.checkValue(param.getMinimumValue());
      }

      if (param.getMaximumValue() != null) {
         param.checkValue(param.getMaximumValue());
      }

      if (param.getPossibleValues() != null) {
         for (T possibleValue : param.getPossibleValues()) {
            param.checkValue(possibleValue);
         }
      }
   }

}
