/**
 *
 * {@link TestReject}.java
 *
 * @author Jens Ebert
 *
 * @date 08.03.2008
 *
 */

package de.je.util.javautil.testUtil;

/**
 * {@link TestReject} is a class to check arguments for correct values and to raise a an
 * {@link IllegalArgumentException}, if necessary.
 */
public final class TestReject {

   /**
    * Checks a condition to be true. If this is the case, an {@link IllegalArgumentException} is thrown.
    *
    * @param condition
    *           The condition to check
    * @param conditionExpectedFalse
    *           The string representation of the expected condition.
    */
   public static void ifTrue(final boolean condition, String conditionExpectedFalse) {
      if (condition)
         throw new IllegalArgumentException(
            OBJECT_PARENTHESES_START + conditionExpectedFalse + OBJECT_PARENTHESES_END + " must be false");
   }

   /**
    * Checks an object to be null. If it is null, an {@link IllegalArgumentException} is thrown.
    *
    * @param object
    *           The object to check.
    * @param objectName
    *           The name of the object expected to be null.
    */
   public static void ifNull(final Object object, String objectName) {
      if (object == null)
         throw new IllegalArgumentException(
            OBJECT_PARENTHESES_START + objectName + OBJECT_PARENTHESES_END + " must not be null");
   }

   /**
    * Private constructor.
    */
   private TestReject() {
   }

   private static final String OBJECT_PARENTHESES_START = "<";
   private static final String OBJECT_PARENTHESES_END = ">";
}
