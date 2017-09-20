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

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import de.je.util.javautil.common.configparams.AbstractConfigParam;
import de.je.util.javautil.common.configparams.AbstractConfigurableTest;
import de.je.util.javautil.common.configparams.EmptyEnum;
import de.je.util.javautil.common.configparams.IConfigurable;

/**
 * {@link FileMediumConfigurableTest} checks the configuration of {@link FileMedium}s.
 */
public class FileMediumConfigurableTest
   extends AbstractConfigurableTest<EmptyEnum, EmptyEnum> {

   /**
    * @see de.je.util.javautil.common.configparams.AbstractConfigurableTest#getConfigurableInstance()
    */
   @Override
   protected IConfigurable getConfigurableInstance() {

      return new FileMedium(new File("."), true);
   }

   /**
    * @see de.je.util.javautil.common.configparams.AbstractConfigurableTest#getValidValues(de.je.util.javautil.common.configparams.AbstractConfigParam)
    */
   @SuppressWarnings("unchecked")
   @Override
   protected <T extends Comparable<T>> T[] getValidValues(
      AbstractConfigParam<T> param) {

      if (param == FileMedium.MAX_CACHE_REGION_SIZE) {
         return (T[]) new Integer[] { 8192, 1, Integer.MAX_VALUE, 10 };
      } else if (param == FileMedium.MAX_WRITE_BLOCK_SIZE) {
         return (T[]) new Integer[] { 8192, 1, Integer.MAX_VALUE, 10 };
      } else if (param == FileMedium.MAX_CACHE_SIZE) {
         return (T[]) new Long[] { 8192L, 1L, Long.MAX_VALUE, 10L };
      } else if (param == FileMedium.ENABLE_CACHING) {
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

      if (param == FileMedium.MAX_CACHE_REGION_SIZE) {
         return (T[]) new Integer[] { 0, -1, null };
      } else if (param == FileMedium.MAX_WRITE_BLOCK_SIZE) {
         return (T[]) new Integer[] { -8192, -1, Integer.MIN_VALUE };
      } else if (param == FileMedium.MAX_CACHE_SIZE) {
         return (T[]) new Long[] { -8192L, 0L, -1L, null, };
      } else if (param == FileMedium.ENABLE_CACHING) {
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

      intParams.add(FileMedium.MAX_CACHE_REGION_SIZE);
      intParams.add(FileMedium.MAX_WRITE_BLOCK_SIZE);

      return intParams;
   }

   /**
    * @see de.je.util.javautil.common.configparams.AbstractConfigurableTest#getLongConfigParams()
    */
   @Override
   protected Set<AbstractConfigParam<Long>> getLongConfigParams() {

      Set<AbstractConfigParam<Long>> longParams = new HashSet<>();

      longParams.add(FileMedium.MAX_CACHE_SIZE);

      return longParams;
   }

   /**
    * @see de.je.util.javautil.common.configparams.AbstractConfigurableTest#getBooleanConfigParams()
    */
   @Override
   protected Set<AbstractConfigParam<Boolean>> getBooleanConfigParams() {

      Set<AbstractConfigParam<Boolean>> boolParams = new HashSet<>();

      boolParams.add(FileMedium.ENABLE_CACHING);

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
