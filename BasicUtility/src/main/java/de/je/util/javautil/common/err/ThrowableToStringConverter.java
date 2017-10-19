package de.je.util.javautil.common.err;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * {@link ThrowableToStringConverter} is a utility class that makes it a little bit easier to get a string
 * representation of the stack trace from a {@link Throwable}.
 */
public class ThrowableToStringConverter {

   /**
    * Returns a string representation of the stack trace of the given {@link Throwable}, with the same content as
    * {@link Throwable#printStackTrace()}.
    *
    * @param t
    *           The {@link Throwable} instance.
    * @return A string representation of the stack trace of the given {@link Throwable}, with the same content as
    *         {@link Throwable#printStackTrace()}.
    */
   public static StringBuffer getStackTraceAsString(Throwable t) {
      Reject.ifNull(t, "t");

      final StringWriter stringWriter = new StringWriter();
      PrintWriter pwr = new PrintWriter(stringWriter);

      t.printStackTrace(pwr);

      return stringWriter.getBuffer();
   }
}
