/**
 *
 * {@link AbstractExtensibleEnumTest}.java
 *
 * @author Jens
 *
 * @date 11.10.2014
 *
 */
package de.je.util.javautil.common.extenum;

import java.util.Iterator;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

/**
 * {@link AbstractExtensibleEnumTest} tests the {@link AbstractExtensibleEnum} class.
 *
 * @param <T>
 *           The concrete derived type of extensible enum class.
 */
public abstract class AbstractExtensibleEnumTest<T extends AbstractExtensibleEnum<T>> {

   /**
    * Test method for {@link de.je.util.javautil.common.extenum.AbstractExtensibleEnum#getId()}.
    */
   @Test
   public void testGetId() {
      Map<String, T> allEnumsToTest = getAllEnumsToTestForDerivedEnum();

      for (Iterator<String> iterator = allEnumsToTest.keySet().iterator(); iterator.hasNext();) {
         String nextKey = iterator.next();

         T nextValue = allEnumsToTest.get(nextKey);

         Assert.assertEquals(nextKey, nextValue.getId());
      }
   }

   /**
    * Test method for
    * {@link de.je.util.javautil.common.extenum.AbstractExtensibleEnum#valueOf(java.lang.Class, java.lang.String)}.
    */
   @SuppressWarnings("unchecked")
   @Test
   public void testValueOf() {
      Map<String, T> allEnumsToTest = getAllEnumsToTestForDerivedEnum();

      for (Iterator<String> iterator = allEnumsToTest.keySet().iterator(); iterator.hasNext();) {
         String nextKey = iterator.next();

         T nextValue = allEnumsToTest.get(nextKey);

         Assert.assertEquals(nextValue, AbstractExtensibleEnum.valueOf(nextValue.getClass(), nextKey));
      }
   }

   /**
    * Returns all enum instances of a test enum class derived from {@link AbstractExtensibleEnum}.
    * 
    * @return all enum instances of a test enum class derived from {@link AbstractExtensibleEnum}.
    */
   protected abstract Map<String, T> getAllEnumsToTestForDerivedEnum();
}
