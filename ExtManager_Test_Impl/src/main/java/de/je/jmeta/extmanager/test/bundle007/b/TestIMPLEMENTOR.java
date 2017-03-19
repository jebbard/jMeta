/**
 *
 * {@link TestIMPLEMENTOR}.java
 *
 * @author Jens Ebert
 *
 * @date 29.04.2011
 */
package de.je.jmeta.extmanager.test.bundle007.b;

import de.je.jmeta.extmanager.testExtensionPoints.b.TestExtensionPointCompB1;

/**
 * {@link TestIMPLEMENTOR}
 *
 */
public class TestIMPLEMENTOR implements TestExtensionPointCompB1 {

   /**
    * @see de.je.jmeta.extmanager.testExtensionPoints.b.TestExtensionPointCompB1#getAnything()
    */
   @Override
   public int getAnything() {

      return 0;
   }

   /**
    * @see de.je.jmeta.extmanager.export.IExtensionPoint#getExtensionId()
    */
   @Override
   public String getExtensionId() {

      return "5";
   }
}
