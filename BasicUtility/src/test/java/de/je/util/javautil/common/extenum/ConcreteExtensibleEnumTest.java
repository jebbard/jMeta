/**
 *
 * {@link ConcreteExtensibleEnumTest}.java
 *
 * @author Jens
 *
 * @date 11.10.2014
 *
 */
package de.je.util.javautil.common.extenum;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;

/**
 * {@link ConcreteExtensibleEnumTest}
 *
 */
public class ConcreteExtensibleEnumTest extends AbstractExtensibleEnumTest<TestExtensibleEnum> {

   /**
    * @see de.je.util.javautil.common.extenum.AbstractExtensibleEnumTest#getAllEnumsToTestForDerivedEnum()
    */
   @Override
   protected Map<String, TestExtensibleEnum> getAllEnumsToTestForDerivedEnum() {
      return TEST_ENUM_MAP;
   }

   /**
    * Test method for {@link de.je.util.javautil.common.extenum.AbstractExtensibleEnum#values(java.lang.Class)}.
    */
   @Test
   public void testGetAllInstances() {
      Map<String, TestExtensibleEnum> allEnumsToTest = getAllEnumsToTestForDerivedEnum();

      Set<TestExtensibleEnum> values = new HashSet<>(allEnumsToTest.values());

      Set<TestExtensibleEnum> allInstances1 = AbstractExtensibleEnum.values(TestExtensibleEnum.class);

      Assert.assertEquals(values, allInstances1);

      Set<TestExtensibleEnum> allInstances2 = AbstractExtensibleEnum.values(TestExtensibleEnumDerived.class);

      Assert.assertEquals(values, allInstances2);

      // This works also for the other base enum class
      Set<TestExtensibleEnumSecondBaseClass> valuesOther = new HashSet<>(TEST_ENUM_MAP_SEC.values());

      Set<TestExtensibleEnumSecondBaseClass> allInstances3 = AbstractExtensibleEnum
         .values(TestExtensibleEnumSecondBaseClass.class);

      Assert.assertEquals(valuesOther, allInstances3);
   }

   /**
    * Tests {@link AbstractExtensibleEnum#getBaseEnumClass(Class)}.
    */
   @Test
   public void testGetBaseEnumClass() {
      Assert.assertEquals(AbstractExtensibleEnum.getBaseEnumClass(TestExtensibleEnum.class), TestExtensibleEnum.class);
      Assert.assertEquals(AbstractExtensibleEnum.getBaseEnumClass(TestExtensibleEnumDerived.class),
         TestExtensibleEnum.class);
      Assert.assertEquals(AbstractExtensibleEnum.getBaseEnumClass(TestExtensibleEnumSecondBaseClass.class),
         TestExtensibleEnumSecondBaseClass.class);
   }

   /**
    * Tests creation of enum constants with duplicate ids.
    */
   @Test(expected = EnumException.class)
   public void test_negative_duplicateIds() {
      @SuppressWarnings("unused")
      TestExtensibleEnum testInstance = new TestExtensibleEnum(TestExtensibleEnum.FIRST_ENUM_CONST.getId());
   }

   /**
    * Tests creation of enum constants with duplicate ids by explicitly creating a new derived class.
    */
   @Test(expected = EnumException.class)
   public void test_negative_duplicateIdsDerivedClass() {
      @SuppressWarnings("unused")
      TestExtensibleEnum testInstance = new YetAnotherTestExtensibleEnum(TestExtensibleEnum.THIRD_ENUM_CONST.getId());
   }

   /**
    * Tests creation of enum constants with duplicate ids by explicitly creating a new derived class.
    */
   @Test(expected = EnumException.class)
   public void test_negative_duplicateIdsDerivedClass2() {
      @SuppressWarnings("unused")
      TestExtensibleEnum testInstance = new YetAnotherTestExtensibleEnum(
         TestExtensibleEnumDerived.SECOND_ENUM_CONST.getId());
   }

   /**
    * {@link YetAnotherTestExtensibleEnum} a test class for method test_negative_duplicateIdsDerivedClass.
    */
   private static class YetAnotherTestExtensibleEnum extends TestExtensibleEnum {

      /**
       * Creates a new {@link YetAnotherTestExtensibleEnum}.
       * 
       * @param id
       *           the id
       */
      public YetAnotherTestExtensibleEnum(String id) {
         super(id);
      }
   }

   private final static Map<String, TestExtensibleEnum> TEST_ENUM_MAP = new HashMap<>();

   static {
      TEST_ENUM_MAP.put(TestExtensibleEnum.FIRST_ENUM_CONST.getId(), TestExtensibleEnum.FIRST_ENUM_CONST);
      TEST_ENUM_MAP.put(TestExtensibleEnum.SECOND_ENUM_CONST.getId(), TestExtensibleEnum.SECOND_ENUM_CONST);
      TEST_ENUM_MAP.put(TestExtensibleEnum.THIRD_ENUM_CONST.getId(), TestExtensibleEnum.THIRD_ENUM_CONST);
      TEST_ENUM_MAP.put(TestExtensibleEnumDerived.FIRST_ENUM_CONST.getId(), TestExtensibleEnumDerived.FIRST_ENUM_CONST);
      TEST_ENUM_MAP.put(TestExtensibleEnumDerived.SECOND_ENUM_CONST.getId(),
         TestExtensibleEnumDerived.SECOND_ENUM_CONST);
      TEST_ENUM_MAP.put(TestExtensibleEnumDerived.THIRD_ENUM_CONST.getId(), TestExtensibleEnumDerived.THIRD_ENUM_CONST);
   }

   private final static Map<String, TestExtensibleEnumSecondBaseClass> TEST_ENUM_MAP_SEC = new HashMap<>();

   static {
      TEST_ENUM_MAP_SEC.put(TestExtensibleEnumSecondBaseClass.FIRST_ENUM_CONST.getId(),
         TestExtensibleEnumSecondBaseClass.FIRST_ENUM_CONST);
      TEST_ENUM_MAP_SEC.put(TestExtensibleEnumSecondBaseClass.SECOND_ENUM_CONST.getId(),
         TestExtensibleEnumSecondBaseClass.SECOND_ENUM_CONST);
      TEST_ENUM_MAP_SEC.put(TestExtensibleEnumSecondBaseClass.THIRD_ENUM_CONST.getId(),
         TestExtensibleEnumSecondBaseClass.THIRD_ENUM_CONST);
   }
}
