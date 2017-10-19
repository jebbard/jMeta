/**
 *
 * {@link TestTest}.java
 *
 * @author Jens Ebert
 *
 * @date 09.02.2008
 *
 */

package de.je.util.javautil.testutil.run.test;

import java.util.ArrayList;
import java.util.regex.Pattern;

import de.je.util.javautil.testUtil.run.RunTests;

/**
 * {@link TestTest} tests the test runner class {@link RunTests}.
 */
public final class TestTest {

   /**
    * Executes the test of the {@link RunTests} class.
    *
    * @param args
    *           No information in this array is processed by this program.
    *
    * @throws Exception
    *            If the test failed.
    */
   public static void main(final String[] args) throws Exception {
      final RunTests r = new RunTests(Pattern.compile(".*xTESTTEST"), ".class");

      final ArrayList<String> tests = new ArrayList<>();

      for (int i = 0; i < TEST_CLASS_NAMES.length; ++i)
         if (!testTest(r, tests, TEST_CLASS_NAMES[i], PACKAGE_NAMES[i]))
            throw new Exception("Test of RunTest failed!");

      r.runTests(tests);
   }

   private TestTest() {

   }

   /**
    * Runs the first test of the RunTest class.
    *
    * @param r
    *           The RunTest object to test.
    * @param testClassReturn
    *           The vector that holds the fully qualified names of all classes to test.
    * @param testClassNames
    *           The array with all test class names that are expected to be in the given test package.
    * @param packageName
    *           The name of the package the test classes reside in. This package must contain the test classes given in
    *           the testClassNames argument. If this is not the case, the test will fail.
    *
    * @return true if the test succeeded, false if the test failed.
    */
   private static boolean testTest(final RunTests r, final ArrayList<String> testClassReturn,
      final String[] testClassNames, final String packageName) {
      if (r == null || testClassNames == null || testClassReturn == null || packageName == null)
         throw new IllegalArgumentException("Parameter to method 'testTest' must not be null!");

      final ArrayList<String> v = r.getTestClassesInPackage(packageName);
      testClassReturn.addAll(v);

      final int size = v.size();

      if (size != testClassNames.length)
         return false;

      for (final String s : testClassNames)
         if (!v.contains(s))
            return false;

      return true;
   }

   private static final String[][] TEST_CLASS_NAMES = {
      { "de.je.util.testutil.run.test.test1.OtherTEstxxxTESTTEST",
         "de.je.util.testutil.run.test.test1.VeryLongTestClassxNamexTESTTEST",
         "de.je.util.testutil.run.test.test1.ThirdTestxTESTTEST" },
      { "de.je.util.testutil.run.test.test2.SingleTestClassInThisPackagexTESTTEST",
         "de.je.util.testutil.run.test.test2.sub.AnotherTestClassxTESTTEST" } };

   private static final String[] PACKAGE_NAMES = { "de.je.util.testutil.run.test.test1",
      "de.je.util.testutil.run.test.test2" };
}
