/**
 *
 * {@link RunAllTests}.java
 *
 * @author Jens Ebert
 *
 * @date 28.01.2008
 *
 */

package de.je.util.javautil.testUtil.run;

import java.util.regex.Pattern;

/**
 * RunAllTests is used to run all available JUnit tests at once.
 */
public final class RunAllTests {

   /**
    * Runs all tests available in the test-folder.
    *
    * @param args
    *           No information in this array is processed by this program.
    */
   public static void main(final String[] args) {
      final RunTests testRunner = new RunTests(Pattern.compile(".*XXXTest"), ".class");

      testRunner.runAllTestsInPackage("de.mdb.test");
   }

   private RunAllTests() {

   }
}
