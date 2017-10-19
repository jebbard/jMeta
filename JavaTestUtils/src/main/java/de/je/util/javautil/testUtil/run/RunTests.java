/**
 *
 * {@link RunTests}.java
 *
 * @author Jens Ebert
 *
 * @date 06.02.2008
 *
 */

package de.je.util.javautil.testUtil.run;

import java.io.File;
import java.util.ArrayList;
import java.util.regex.Pattern;

import org.junit.runner.JUnitCore;

import de.je.util.javautil.testUtil.TestReject;

/**
 * RunTests is a class that allows to run unit tests for all test classes contained in a specific folder or given in an
 * array of test class names.
 *
 * A test class must be a compiled java class and is identified by a specific file extension (usually ".class") and a
 * pattern for the file name. I.e. class files that do not match this patterns are not recognized as test classes and
 * the test cases contained in these classes are not executed.
 *
 * Internally, RunTests uses JUnit to run the test by invoking JUnitCore.main.
 */
public class RunTests {

   /**
    * Creates an object of this class.
    *
    * @param testClassNamePattern
    *           The pattern for matching a test class name.
    * @param testClassExtension
    *           The file extension of a test class (usually ".class").
    *
    * @pre testClassNamePattern != null
    * @pre testClassExtension != null
    */
   public RunTests(final Pattern testClassNamePattern, final String testClassExtension) {
      TestReject.ifNull(testClassExtension, "testClassExtension");
      TestReject.ifNull(testClassNamePattern, "testClassNamePattern");

      m_testClassNamePattern = testClassNamePattern;
      m_testClassExtension = testClassExtension;
   }

   /**
    * Gets a vector of all test classes contained in a given package. This also finds all test classes in sub-packages
    * of the given package.
    *
    * @param packageName
    *           The name of the package. Must reside in the current working directory of the Java VM.
    *
    * @return A vector containing the fully qualified names of all test classes in the given package that match the
    *         pattern given to the constructor.
    *
    * @pre packageName != null
    */
   public ArrayList<String> getTestClassesInPackage(final String packageName) {
      TestReject.ifNull(packageName, "packageName");

      final File packageFolder = getPathFromPackageName(packageName);

      if (!packageFolder.exists())
         return new ArrayList<>();

      final ArrayList<String> testClasses = new ArrayList<>();
      final File[] files = packageFolder.listFiles();

      for (int i = 0; i < files.length; ++i) {
         if (files[i].isDirectory())
            testClasses.addAll(getTestClassesInPackage(getPackageNameFromPath(files[i])));

         String testClassName = files[i].getName();
         testClassName = testClassName.replace(m_testClassExtension, "");

         if (testClassName.matches(m_testClassNamePattern.pattern()))
            testClasses.add(packageName + PACKAGE_DELIMITER + testClassName);
      }

      return testClasses;
   }

   /**
    * Runs all tests contained in the given package. This also includes test classes that reside in sup-packages of the
    * given package.
    *
    * @param packageName
    *           Name of the package.
    *
    * @pre packageName != null
    */
   public void runAllTestsInPackage(final String packageName) {
      TestReject.ifNull(packageName, "packageName");

      runTests(getTestClassesInPackage(packageName));
   }

   /**
    * Runs arbitrary tests. The test names are given in the vector.
    *
    * @param testClassNames
    *           Vector containing fully qualified names of test classes. This classes do not need to match the pattern
    *           given to the constructor.
    *
    * @pre testClassNames != null
    */
   public void runTests(final ArrayList<String> testClassNames) {
      TestReject.ifNull(testClassNames, "testClassNames");

      final String[] testClassNamesArray = new String[testClassNames.size()];
      JUnitCore.main(testClassNames.toArray(testClassNamesArray));
   }

   /**
    * Returns the path representation of a package identifier.
    *
    * @param packageName
    *           The name of a package.
    *
    * @return Relative Path to a given package name.
    *
    * @pre packageName != null
    */
   private File getPathFromPackageName(final String packageName) {
      final String fileSeparator = System.getProperty("file.separator");

      return new File(packageName.replace(PACKAGE_DELIMITER, fileSeparator));
   }

   /**
    * Returns the fully qualified package name of a path.
    *
    * @param path
    *           The name of the package.
    *
    * @return The fully qualified package name of a path.
    *
    * @pre path != null
    */
   private String getPackageNameFromPath(final File path) {
      final String fileSeparator = System.getProperty("file.separator");

      return path.getPath().replace(fileSeparator, PACKAGE_DELIMITER);
   }

   /** An arbitrary pattern that identifies the test classes by name */
   private final Pattern m_testClassNamePattern;

   /** Extension of the test class files beginning with a dot */
   private final String m_testClassExtension;

   /** Standard Java-Package delimiter */
   private static final String PACKAGE_DELIMITER = ".";
}
