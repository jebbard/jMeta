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

import java.util.HashSet;
import java.util.Set;

import de.je.util.javautil.common.configparams.AbstractConfigParam;
import de.je.util.javautil.common.configparams.AbstractConfigurableTest;
import de.je.util.javautil.common.configparams.EmptyEnum;
import de.je.util.javautil.common.configparams.IConfigurable;

/**
 * {@link InMemoryMediumConfigurableTest} checks the configuration of {@link InMemoryMedium}s.
 */
public class InMemoryMediumConfigurableTest
   extends AbstractConfigurableTest<EmptyEnum, EmptyEnum> {

   /**
    * @see de.je.util.javautil.common.configparams.AbstractConfigurableTest#getConfigurableInstance()
    */
   @Override
   protected IConfigurable getConfigurableInstance() {

      return new InMemoryMedium(new byte[] { 1, 2, 3 }, null, false);
   }

   /**
    * @see de.je.util.javautil.common.configparams.AbstractConfigurableTest#getValidValues(de.je.util.javautil.common.configparams.AbstractConfigParam)
    */
   @SuppressWarnings("unchecked")
   @Override
   protected <T extends Comparable<T>> T[] getValidValues(
      AbstractConfigParam<T> param) {

      return (T[]) new Integer[] { 8192, 1, Integer.MAX_VALUE, 10 };
   }

   /**
    * @see de.je.util.javautil.common.configparams.AbstractConfigurableTest#getInvalidValues(de.je.util.javautil.common.configparams.AbstractConfigParam)
    */
   @SuppressWarnings("unchecked")
   @Override
   protected <T extends Comparable<T>> T[] getInvalidValues(
      AbstractConfigParam<T> param) {

      return (T[]) new Integer[] { -1, 0, Integer.MIN_VALUE };
   }

   /**
    * @see de.je.util.javautil.common.configparams.AbstractConfigurableTest#getIntegerConfigParams()
    */
   @Override
   protected Set<AbstractConfigParam<Integer>> getIntegerConfigParams() {

      Set<AbstractConfigParam<Integer>> intParams = new HashSet<>();

      intParams.add(InMemoryMedium.MAX_WRITE_BLOCK_SIZE);

      return intParams;
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
