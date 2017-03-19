/**
 *
 * {@link MyOtherTestImple}.java
 *
 * @author Jens Ebert
 *
 * @date 29.04.2011
 */
package de.je.jmeta.extmanager.test.bundle005.a;

import de.je.jmeta.extmanager.testExtensionPoints.a.TestExtensionPointCompA2;

/**
 * {@link MyOtherTestImple}
 *
 */
public class MyOtherTestImple implements TestExtensionPointCompA2 {

   /**
    * @see de.je.jmeta.extmanager.testExtensionPoints.a.TestExtensionPointCompA2#getAnything2()
    */
   @Override
   public int getAnything2() {

      return 0;
   }

   /**
    * @see de.je.jmeta.extmanager.export.IExtensionPoint#getExtensionId()
    */
   @Override
   public String getExtensionId() {

      return "12";
   }
}
