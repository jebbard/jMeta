package de.je.util.javautil.common.configparams;

import java.util.HashSet;
import java.util.Set;

/**
 * {@link ConfigParamHandlerTest} is a full implementation of the {@link AbstractConfigurableTest} class for testing the
 * default {@link IConfigurable} implementation {@link ConfigParamHandler}
 */
public class ConfigParamHandlerTest extends AbstractConfigurableTest<ConfigParamTestEnum, SecondConfigParamTestEnum> {

   private final static AbstractConfigParam<Boolean> BOOL_PARAM_1 = new BooleanConfigParam("Boolean 1", true);
   private final static AbstractConfigParam<Boolean> BOOL_PARAM_2 = new BooleanConfigParam("Boolean 2", false);

   private final static AbstractConfigParam<Integer> INT_PARAM_1 = new IntegerConfigParam("Int 1", 55, -9999, 9999);
   private final static AbstractConfigParam<Integer> INT_PARAM_2 = new IntegerConfigParam("Int 2", 0, -90, 1700);
   private final static AbstractConfigParam<Integer> INT_PARAM_3 = new IntegerConfigParam("Int 3", -88880, -100000, 10);

   private final static AbstractConfigParam<Long> LONG_PARAM_1 = new LongConfigParam("Long 1", 55L, -9999L, 9999L);

   private final static AbstractConfigParam<Double> DOUBLE_PARAM_1 = new DoubleConfigParam("Double 1", 55.0, -9999.0,
      9999.0);
   private final static AbstractConfigParam<Double> DOUBLE_PARAM_2 = new DoubleConfigParam("Double 2", 0.0, -90.0,
      1700.0);

   private final static AbstractConfigParam<String> STRING_PARAM_1 = new StringConfigParam("String 1", "x1");
   private final static AbstractConfigParam<String> STRING_PARAM_2 = new StringConfigParam("String 2", "x2", "aaaaa",
      "zzzzz");
   private final static AbstractConfigParam<String> STRING_PARAM_3 = new StringConfigParam("String 3", "x3",
      new String[] { "haaaa", "x3" });

   private final static AbstractConfigParam<ConfigParamTestEnum> FIRST_ENUM_PARAM_1 = new EnumConfigParam<>(
      ConfigParamTestEnum.class, "Enum 1.1", ConfigParamTestEnum.SECOND_INSTANCE, ConfigParamTestEnum.values());

   private final static AbstractConfigParam<ConfigParamTestEnum> FIRST_ENUM_PARAM_2 = new EnumConfigParam<>(
      ConfigParamTestEnum.class, "Enum 1.2", ConfigParamTestEnum.FOURTH_INSTANCE,
      new ConfigParamTestEnum[] { ConfigParamTestEnum.FIRST_INSTANCE, ConfigParamTestEnum.FOURTH_INSTANCE });

   private final static AbstractConfigParam<ConfigParamTestEnum> FIRST_ENUM_PARAM_3 = new EnumConfigParam<>(
      ConfigParamTestEnum.class, "Enum 1.3", ConfigParamTestEnum.FOURTH_INSTANCE, ConfigParamTestEnum.FIRST_INSTANCE,
      ConfigParamTestEnum.FOURTH_INSTANCE, new ConfigParamTestEnum[] { ConfigParamTestEnum.FIRST_INSTANCE,
         ConfigParamTestEnum.SECOND_INSTANCE, ConfigParamTestEnum.FOURTH_INSTANCE });

   private final static AbstractConfigParam<SecondConfigParamTestEnum> SECOND_ENUM_PARAM_1 = new EnumConfigParam<>(
      SecondConfigParamTestEnum.class, "Enum 2.1", SecondConfigParamTestEnum.SEC_ENUM_SECOND_INSTANCE,
      SecondConfigParamTestEnum.values());

   /**
    * @see de.je.util.javautil.common.configparams.AbstractConfigurableTest#getConfigurableInstance()
    */
   @Override
   protected IConfigurable getConfigurableInstance() {
      return new ConfigParamHandler(new AbstractConfigParam<?>[] { BOOL_PARAM_1, BOOL_PARAM_2, INT_PARAM_1, INT_PARAM_2,
         INT_PARAM_3, LONG_PARAM_1, DOUBLE_PARAM_1, DOUBLE_PARAM_2, STRING_PARAM_1, STRING_PARAM_2, STRING_PARAM_3,
         FIRST_ENUM_PARAM_1, FIRST_ENUM_PARAM_2, FIRST_ENUM_PARAM_3, SECOND_ENUM_PARAM_1 });
   }

   /**
    * @see de.je.util.javautil.common.configparams.AbstractConfigurableTest#getValidValues(de.je.util.javautil.common.configparams.AbstractConfigParam)
    */
   @Override
   @SuppressWarnings("unchecked")
   protected <T extends Comparable<T>> T[] getValidValues(AbstractConfigParam<T> param) {

      if (param == INT_PARAM_1) {
         return (T[]) new Integer[] { 768, 1, 55, 9999, -9999, 0 };
      } else if (param == INT_PARAM_2) {
         return (T[]) new Integer[] { 1, 55, 1700, -90, 0 };
      } else if (param == INT_PARAM_3) {
         return (T[]) new Integer[] { 1, 5, -88880, -100000, 10 };
      } else if (param == LONG_PARAM_1) {
         return (T[]) new Long[] { 768L, 1L, 55L, 9999L, -9999L, 0L };
      } else if (param == DOUBLE_PARAM_1) {
         return (T[]) new Double[] { 1.0, 0.0, 55.0, -9999.0, 9999.0 };
      } else if (param == DOUBLE_PARAM_2) {
         return (T[]) new Double[] { 1.0, 0.0, -90.0, 1700.0 };
      } else if (param == STRING_PARAM_1) {
         return (T[]) new String[] { "x1", "asdasdasd", "no matter what" };
      } else if (param == STRING_PARAM_2) {
         return (T[]) new String[] { "x2", "aaaaa", "zzzzz", "bbbbb" };
      } else if (param == STRING_PARAM_3) {
         return (T[]) new String[] { "x3", "haaaa" };
      } else if (param == BOOL_PARAM_1) {
         return (T[]) new Boolean[] { true, false };
      } else if (param == BOOL_PARAM_2) {
         return (T[]) new Boolean[] { true, false };
      } else if (param == FIRST_ENUM_PARAM_1) {
         return (T[]) new ConfigParamTestEnum[] { ConfigParamTestEnum.FIRST_INSTANCE,
            ConfigParamTestEnum.SECOND_INSTANCE, ConfigParamTestEnum.THIRD_INSTANCE,
            ConfigParamTestEnum.FOURTH_INSTANCE };
      } else if (param == FIRST_ENUM_PARAM_2) {
         return (T[]) new ConfigParamTestEnum[] { ConfigParamTestEnum.FIRST_INSTANCE,
            ConfigParamTestEnum.FOURTH_INSTANCE };
      } else if (param == FIRST_ENUM_PARAM_3) {
         return (T[]) new ConfigParamTestEnum[] { ConfigParamTestEnum.FIRST_INSTANCE,
            ConfigParamTestEnum.SECOND_INSTANCE, ConfigParamTestEnum.FOURTH_INSTANCE };
      } else if (param == SECOND_ENUM_PARAM_1) {
         return (T[]) new SecondConfigParamTestEnum[] { SecondConfigParamTestEnum.SEC_ENUM_FIRST_INSTANCE,
            SecondConfigParamTestEnum.SEC_ENUM_SECOND_INSTANCE, SecondConfigParamTestEnum.SEC_ENUM_THIRD_INSTANCE,
            SecondConfigParamTestEnum.SEC_ENUM_FIFTH_INSTANCE, SecondConfigParamTestEnum.SEC_ENUM_FOURTH_INSTANCE };
      }

      return null;
   }

   /**
    * @see de.je.util.javautil.common.configparams.AbstractConfigurableTest#getInvalidValues(de.je.util.javautil.common.configparams.AbstractConfigParam)
    */
   @SuppressWarnings("unchecked")
   @Override
   protected <T extends Comparable<T>> T[] getInvalidValues(AbstractConfigParam<T> param) {

      if (param == INT_PARAM_1) {
         return (T[]) new Integer[] { -10000, 10000, 1200000, null };
      } else if (param == INT_PARAM_2) {
         return (T[]) new Integer[] { -91, 1701, 99999, -1281218212, null };
      } else if (param == INT_PARAM_3) {
         return (T[]) new Integer[] { -100001, 734573245, 55, null };
      } else if (param == LONG_PARAM_1) {
         return (T[]) new Long[] { -10000L, 10000L, 1200000L, null };
      } else if (param == DOUBLE_PARAM_1) {
         return (T[]) new Double[] { null, 10120121.22, -9999.1, 9999.1 };
      } else if (param == DOUBLE_PARAM_2) {
         return (T[]) new Double[] { null, -90.00002, 1700.0004, 9999.1 };
      } else if (param == STRING_PARAM_1) {
         return (T[]) new String[] { null };
      } else if (param == STRING_PARAM_2) {
         return (T[]) new String[] { "zzzzzz", null };
      } else if (param == STRING_PARAM_3) {
         return (T[]) new String[] { "zzzzzz", null };
      } else if (param == BOOL_PARAM_1) {
         return (T[]) new Boolean[] { null };
      } else if (param == BOOL_PARAM_2) {
         return (T[]) new Boolean[] { null };
      } else if (param == FIRST_ENUM_PARAM_1) {
         return (T[]) new ConfigParamTestEnum[] { null };
      } else if (param == FIRST_ENUM_PARAM_2) {
         return (T[]) new ConfigParamTestEnum[] { ConfigParamTestEnum.SECOND_INSTANCE,
            ConfigParamTestEnum.THIRD_INSTANCE, null };
      } else if (param == FIRST_ENUM_PARAM_3) {
         return (T[]) new ConfigParamTestEnum[] { ConfigParamTestEnum.THIRD_INSTANCE, null };
      } else if (param == SECOND_ENUM_PARAM_1) {
         return (T[]) new SecondConfigParamTestEnum[] { null };
      }

      return null;
   }

   @Override
   protected Class<ConfigParamTestEnum> getFirstEnumClass() {
      return ConfigParamTestEnum.class;
   }

   @Override
   protected Class<SecondConfigParamTestEnum> getSecondEnumClass() {
      return SecondConfigParamTestEnum.class;
   }

   /**
    * @see de.je.util.javautil.common.configparams.AbstractConfigurableTest#getStringConfigParams()
    */
   @Override
   protected Set<AbstractConfigParam<String>> getStringConfigParams() {
      HashSet<AbstractConfigParam<String>> params = new HashSet<>();

      params.add(STRING_PARAM_1);
      params.add(STRING_PARAM_2);
      params.add(STRING_PARAM_3);

      return params;
   }

   /**
    * @see de.je.util.javautil.common.configparams.AbstractConfigurableTest#getFirstEnumConfigParams()
    */
   @Override
   protected Set<AbstractConfigParam<ConfigParamTestEnum>> getFirstEnumConfigParams() {
      HashSet<AbstractConfigParam<ConfigParamTestEnum>> params = new HashSet<>();

      params.add(FIRST_ENUM_PARAM_1);
      params.add(FIRST_ENUM_PARAM_2);
      params.add(FIRST_ENUM_PARAM_3);

      return params;
   }

   /**
    * @see de.je.util.javautil.common.configparams.AbstractConfigurableTest#getSecondEnumConfigParams()
    */
   @Override
   protected Set<AbstractConfigParam<SecondConfigParamTestEnum>> getSecondEnumConfigParams() {
      HashSet<AbstractConfigParam<SecondConfigParamTestEnum>> params = new HashSet<>();

      params.add(SECOND_ENUM_PARAM_1);

      return params;
   }

   /**
    * @see de.je.util.javautil.common.configparams.AbstractConfigurableTest#getDoubleConfigParams()
    */
   @Override
   protected Set<AbstractConfigParam<Double>> getDoubleConfigParams() {
      HashSet<AbstractConfigParam<Double>> params = new HashSet<>();

      params.add(DOUBLE_PARAM_1);
      params.add(DOUBLE_PARAM_2);

      return params;
   }

   /**
    * @see de.je.util.javautil.common.configparams.AbstractConfigurableTest#getIntegerConfigParams()
    */
   @Override
   protected Set<AbstractConfigParam<Integer>> getIntegerConfigParams() {
      HashSet<AbstractConfigParam<Integer>> params = new HashSet<>();

      params.add(INT_PARAM_1);
      params.add(INT_PARAM_2);
      params.add(INT_PARAM_3);

      return params;
   }

   @Override
   protected Set<AbstractConfigParam<Long>> getLongConfigParams() {
      HashSet<AbstractConfigParam<Long>> params = new HashSet<>();

      params.add(LONG_PARAM_1);

      return params;
   }

   /**
    * @see de.je.util.javautil.common.configparams.AbstractConfigurableTest#getBooleanConfigParams()
    */
   @Override
   protected Set<AbstractConfigParam<Boolean>> getBooleanConfigParams() {
      HashSet<AbstractConfigParam<Boolean>> params = new HashSet<>();

      params.add(BOOL_PARAM_1);
      params.add(BOOL_PARAM_2);

      return params;
   }
}
