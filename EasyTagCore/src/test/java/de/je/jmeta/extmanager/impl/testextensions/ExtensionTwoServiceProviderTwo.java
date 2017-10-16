/**
 *
 * {@link ExtensionOneServiceProviderOne}.java
 *
 * @author Jens Ebert
 *
 * @date 15.10.2017
 *
 */
package de.je.jmeta.extmanager.impl.testextensions;

/**
 * {@link ExtensionTwoServiceProviderTwo} is just a test service.
 */
public class ExtensionTwoServiceProviderTwo implements ITestExtensionServiceTwo {

   /**
    * @see de.je.jmeta.extmanager.impl.testextensions.ITestExtensionServiceTwo#otherMethod(long)
    */
   @Override
   public int otherMethod(long x) {
      return 0;
   }
}
