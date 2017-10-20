/**
 *
 * {@link Reject}.java
 *
 * @author Jens Ebert
 *
 * @date 08.03.2008
 *
 */

package com.github.jmeta.utility.dbc.api.services;

import com.github.jmeta.utility.dbc.api.exception.PreconditionUnfullfilledException;

/**
 * {@link Reject} is a class to check preconditions to be met, its methods raise a
 * {@link PreconditionUnfullfilledException}, if necessary.
 */
public final class Reject {

   /**
    * Checks a condition. If it is true, an {@link PreconditionUnfullfilledException} is thrown.
    *
    * @param condition
    *           The condition to check
    * @param conditionExpectedFalse
    *           The string representation of the expected condition
    */
   public static void ifTrue(final boolean condition, String conditionExpectedFalse) {
      if (condition) {
         String exceptionMessage = wrapTokenForMessage(conditionExpectedFalse)
            + " is expected to be false, but was true";
         throw new PreconditionUnfullfilledException(exceptionMessage);
      }
   }

   /**
    * Checks a condition. If it is false, an {@link PreconditionUnfullfilledException} is thrown.
    *
    * @param condition
    *           The condition to check
    * @param conditionExpectedTrue
    *           The string representation of the expected condition
    */
   public static void ifFalse(final boolean condition, String conditionExpectedTrue) {
      if (!condition) {
         String exceptionMessage = wrapTokenForMessage(conditionExpectedTrue)
            + " is expected to be true, but was false";
         throw new PreconditionUnfullfilledException(exceptionMessage);
      }
   }

   /**
    * Checks an object to be null. If it is null, an {@link PreconditionUnfullfilledException} is thrown.
    *
    * @param object
    *           The object to check
    * @param objectName
    *           The name of the object expected to be null
    */
   public static void ifNull(final Object object, String objectName) {
      if (object == null) {
         String exceptionMessage = wrapTokenForMessage(objectName) + " must not be null";
         throw new PreconditionUnfullfilledException(exceptionMessage);
      }
   }

   /**
    * Checks a numeric value to be positive (incl. zero). If it is strictly negative, an
    * {@link PreconditionUnfullfilledException} is thrown.
    *
    * @param value
    *           The value to check
    * @param valueName
    *           The name of the value expected to be positive or zero
    */
   public static void ifNegative(final long value, String valueName) {
      if (value < 0L) {
         String exceptionMessage = wrapTokenForMessage(valueName) + " must not be negative, i.e. < 0, but it was "
            + wrapTokenForMessage(Long.toString(value));
         throw new PreconditionUnfullfilledException(exceptionMessage);
      }
   }

   /**
    * Checks a numeric value to be strictly positive. If it is strictly negative or zero, an
    * {@link PreconditionUnfullfilledException} is thrown.
    *
    * @param value
    *           The value to check
    * @param valueName
    *           The name of the value expected to be strictly positive
    */
   public static void ifNegativeOrZero(final long value, String valueName) {
      if (value <= 0L) {
         String exceptionMessage = wrapTokenForMessage(valueName)
            + " must not be negative or zero, i.e. <= 0, but it was " + wrapTokenForMessage(Long.toString(value));
         throw new PreconditionUnfullfilledException(exceptionMessage);
      }
   }

   /**
    * Checks a numerical value to be between upper and lower bound (both inclusive).
    *
    * @param value
    *           The value to check for.
    * @param lower
    *           The lower bound (inclusive).
    * @param upper
    *           The upper bound (inclusive).
    * @param numberName
    *           The name of the checked number.
    */
   public static void ifNotInInterval(long value, long lower, long upper, String numberName) {
      if (value < lower || value > upper) {
         String exceptionMessage = wrapTokenForMessage(numberName) + " must be in interval [" + lower + ", " + upper
            + "], but its value is = " + wrapTokenForMessage(Long.toString(value));
         throw new PreconditionUnfullfilledException(exceptionMessage);
      }
   }

   private static String wrapTokenForMessage(String token) {
      return "<" + token + ">";
   }

   /**
    * Private constructor.
    */
   private Reject() {
   }
}
