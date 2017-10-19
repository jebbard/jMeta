/**
 *
 * {@link TestExtensibleEnum}.java
 *
 * @author Jens
 *
 * @date 11.10.2014
 *
 */
package de.je.util.javautil.common.extenum;

/**
 * {@link TestExtensibleEnumSecondBaseClass} is another enum base class for testing {@link AbstractExtensibleEnum}. The
 * class definition itself contains already a static test case: The enum constants {@link #FIRST_ENUM_CONST} and so on
 * use the same ids as the enum constants in the class {@link TestExtensibleEnum}. This already checks that such
 * definitions are possible and do not lead to {@link EnumException} during the constructor call.
 *
 */
public class TestExtensibleEnumSecondBaseClass extends AbstractExtensibleEnum<TestExtensibleEnumSecondBaseClass> {

   /**
    * Creates a new {@link TestExtensibleEnumSecondBaseClass}.
    * 
    * @param id
    *           The id of the enum.
    */
   protected TestExtensibleEnumSecondBaseClass(String id) {
      super(id);
   }

   /**
    * First constant
    */
   public final static TestExtensibleEnumSecondBaseClass FIRST_ENUM_CONST = new TestExtensibleEnumSecondBaseClass(
      "first");
   /**
    * Second constant
    */
   public final static TestExtensibleEnumSecondBaseClass SECOND_ENUM_CONST = new TestExtensibleEnumSecondBaseClass(
      "second");
   /**
    * Third constant
    */
   public final static TestExtensibleEnumSecondBaseClass THIRD_ENUM_CONST = new TestExtensibleEnumSecondBaseClass(
      "third");
}
