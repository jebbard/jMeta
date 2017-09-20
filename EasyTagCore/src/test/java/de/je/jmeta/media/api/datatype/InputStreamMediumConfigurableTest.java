/**
 *
 * InMemoryMediumConfigurableTest.java
 *
 * @author Jens
 *
 * @date 16.05.2016
 *
 */
package de.je.jmeta.media.api.datatype;

import java.io.ByteArrayInputStream;
import java.util.HashSet;
import java.util.Set;

import de.je.util.javautil.common.configparams.AbstractConfigParam;
import de.je.util.javautil.common.configparams.AbstractConfigurableTest;
import de.je.util.javautil.common.configparams.EmptyEnum;
import de.je.util.javautil.common.configparams.IConfigurable;

/**
 * {@link InputStreamMediumConfigurableTest} checks the configuration of {@link InputStreamMedium}s.
 */
public class InputStreamMediumConfigurableTest
   extends AbstractConfigurableTest<EmptyEnum, EmptyEnum> {

   /**
    * @see de.je.util.javautil.common.configparams.AbstractConfigurableTest#getConfigurableInstance()
    */
   @Override
   protected IConfigurable getConfigurableInstance() {

      return new InputStreamMedium(
         new ByteArrayInputStream(new byte[] { 1, 2, 3 }), null);
   }

   /**
    * @see de.je.util.javautil.common.configparams.AbstractConfigurableTest#getValidValues(de.je.util.javautil.common.configparams.AbstractConfigParam)
    */
   @SuppressWarnings("unchecked")
   @Override
   protected <T extends Comparable<T>> T[] getValidValues(
      AbstractConfigParam<T> param) {

      if (param == InputStreamMedium.MAX_CACHE_REGION_SIZE) {
         return (T[]) new Integer[] { 8192, 1, Integer.MAX_VALUE, 10 };
      } else if (param == InputStreamMedium.READ_TIMEOUT_MILLIS) {
         return (T[]) new Integer[] { 8192, 0, 1, Integer.MAX_VALUE, 10 };
      } else if (param == InputStreamMedium.MAX_CACHE_SIZE) {
         return (T[]) new Long[] { 8192L, 1L, Long.MAX_VALUE, 10L };
      } else if (param == InputStreamMedium.ENABLE_CACHING) {
         return (T[]) new Boolean[] { true, false };
      } else if (param == InputStreamMedium.SKIP_ON_FORWARD_READ) {
         return (T[]) new Boolean[] { true, false };
      } else {
         throw new IllegalStateException();
      }

   }

   /**
    * @see de.je.util.javautil.common.configparams.AbstractConfigurableTest#getInvalidValues(de.je.util.javautil.common.configparams.AbstractConfigParam)
    */
   @SuppressWarnings("unchecked")
   @Override
   protected <T extends Comparable<T>> T[] getInvalidValues(
      AbstractConfigParam<T> param) {

      if (param == InputStreamMedium.MAX_CACHE_REGION_SIZE) {
         return (T[]) new Integer[] { -1, -2, null };
      } else if (param == InputStreamMedium.READ_TIMEOUT_MILLIS) {
         return (T[]) new Integer[] { -8192, -1, Integer.MIN_VALUE };
      } else if (param == InputStreamMedium.MAX_CACHE_SIZE) {
         return (T[]) new Long[] { -8192L, 0L, -1L, null, };
      } else if (param == InputStreamMedium.ENABLE_CACHING) {
         return (T[]) new Boolean[] { null };
      } else if (param == InputStreamMedium.SKIP_ON_FORWARD_READ) {
         return (T[]) new Boolean[] { null };
      } else {
         throw new IllegalStateException();
      }
   }

   /**
    * @see de.je.util.javautil.common.configparams.AbstractConfigurableTest#getIntegerConfigParams()
    */
   @Override
   protected Set<AbstractConfigParam<Integer>> getIntegerConfigParams() {

      Set<AbstractConfigParam<Integer>> intParams = new HashSet<>();

      intParams.add(InputStreamMedium.MAX_CACHE_REGION_SIZE);
      intParams.add(InputStreamMedium.READ_TIMEOUT_MILLIS);

      return intParams;
   }

   /**
    * @see de.je.util.javautil.common.configparams.AbstractConfigurableTest#getLongConfigParams()
    */
   @Override
   protected Set<AbstractConfigParam<Long>> getLongConfigParams() {

      Set<AbstractConfigParam<Long>> longParams = new HashSet<>();

      longParams.add(InputStreamMedium.MAX_CACHE_SIZE);

      return longParams;
   }

   /**
    * @see de.je.util.javautil.common.configparams.AbstractConfigurableTest#getBooleanConfigParams()
    */
   @Override
   protected Set<AbstractConfigParam<Boolean>> getBooleanConfigParams() {

      Set<AbstractConfigParam<Boolean>> boolParams = new HashSet<>();

      boolParams.add(InputStreamMedium.ENABLE_CACHING);
      boolParams.add(InputStreamMedium.SKIP_ON_FORWARD_READ);

      return boolParams;
   }

   /**
    * @see de.je.util.javautil.common.configparams.AbstractConfigurableTest#getFirstEnumClass()
    */
   @Override
   protected Class<EmptyEnum> getFirstEnumClass() {

      return EmptyEnum.class;
   }

   /**
    * @see de.je.util.javautil.common.configparams.AbstractConfigurableTest#getSecondEnumClass()
    */
   @Override
   protected Class<EmptyEnum> getSecondEnumClass() {

      return EmptyEnum.class;
   }

}
