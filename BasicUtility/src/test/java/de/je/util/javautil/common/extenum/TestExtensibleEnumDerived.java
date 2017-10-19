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
 * {@link TestExtensibleEnumDerived} is a derived enum class for testing purposes.
 */
public class TestExtensibleEnumDerived extends TestExtensibleEnum {

   /**
    * Creates a new {@link TestExtensibleEnumDerived}.
    * 
    * @param id
    *           The id of the enum.
    */
   protected TestExtensibleEnumDerived(String id) {
      super(id);
   }

   /**
    * First constant, intentionally hiding
    */
   @SuppressWarnings("hiding")
   public final static TestExtensibleEnumDerived FIRST_ENUM_CONST = new TestExtensibleEnumDerived("first der");
   /**
    * Second constant, intentionally hiding
    */
   @SuppressWarnings("hiding")
   public final static TestExtensibleEnumDerived SECOND_ENUM_CONST = new TestExtensibleEnumDerived("second der");
   /**
    * Third constant, intentionally hiding
    */
   @SuppressWarnings("hiding")
   public final static TestExtensibleEnumDerived THIRD_ENUM_CONST = new TestExtensibleEnumDerived("third der");
}
