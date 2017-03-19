/**
 *
 * {@link MyListener}.java
 *
 * @author Jens Ebert
 *
 * @date 17.09.2011
 */
package de.je.jmeta.testtools.multRunner;

import java.io.PrintStream;

import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunListener;

/**
 * {@link FailureArchiverJUnit}
 *
 */
public class FailureArchiverJUnit extends RunListener {

   private static final String TABULATOR = "\t";

   private static final String NEW_LINE = "\n";

   public FailureArchiverJUnit(PrintStream out) {
      m_out = out;
   }

   @Override
   public void testFailure(Failure failure) throws Exception {

      super.testFailure(failure);

      m_out.println(printFailure(failure));
   }

   @Override
   public void testAssumptionFailure(Failure failure) {

      super.testAssumptionFailure(failure);

      m_out.println(printFailure(failure));
   }

   private String printFailure(Failure failure) {

      StringBuffer buffer = new StringBuffer();

      buffer.append("Test run: ");
      buffer.append(NEW_LINE);
      buffer.append(TABULATOR);
      buffer.append(failure.getTestHeader());
      buffer.append(NEW_LINE);
      buffer.append(NEW_LINE);
      buffer.append("Message: ");
      buffer.append(NEW_LINE);
      buffer.append(TABULATOR);
      buffer.append(failure.getMessage());
      buffer.append(NEW_LINE);
      buffer.append(NEW_LINE);
      buffer.append("Stack Trace: ");
      buffer.append(NEW_LINE);
      buffer.append(TABULATOR);
      buffer.append(failure.getTrace());

      return buffer.toString();
   }

   private final PrintStream m_out;
}
