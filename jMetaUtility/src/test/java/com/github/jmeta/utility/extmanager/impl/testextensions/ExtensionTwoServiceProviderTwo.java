/**
 *
 * {@link ExtensionOneServiceProviderOne}.java
 *
 * @author Jens Ebert
 *
 * @date 15.10.2017
 *
 */
package com.github.jmeta.utility.extmanager.impl.testextensions;

/**
 * {@link ExtensionTwoServiceProviderTwo} is just a test service.
 */
public class ExtensionTwoServiceProviderTwo implements TestExtensionServiceTwo {

   /**
    * @see com.github.jmeta.utility.extmanager.impl.testextensions.TestExtensionServiceTwo#otherMethod(long)
    */
   @Override
   public int otherMethod(long x) {
      return 0;
   }
}
