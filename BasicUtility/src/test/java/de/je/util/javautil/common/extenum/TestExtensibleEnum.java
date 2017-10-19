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
 * {@link TestExtensibleEnum} is an enum base class for testint {@link AbstractExtensibleEnum}.
 */
public class TestExtensibleEnum extends AbstractExtensibleEnum<TestExtensibleEnum> {

   /**
    * Creates a new {@link TestExtensibleEnum}.
    * 
    * @param id
    *           The id of the enum.
    */
   protected TestExtensibleEnum(String id) {
      super(id);
   }

   /**
    * First constant
    */
   public final static TestExtensibleEnum FIRST_ENUM_CONST = new TestExtensibleEnum("first");
   /**
    * Second constant
    */
   public final static TestExtensibleEnum SECOND_ENUM_CONST = new TestExtensibleEnum("second");
   /**
    * Third constant
    */
   public final static TestExtensibleEnum THIRD_ENUM_CONST = new TestExtensibleEnum("third");
}
