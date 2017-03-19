/**
 *
 * {@link TestImplCompA1}.java
 *
 * @author Jens Ebert
 *
 * @date 29.04.2011
 */
package de.je.jmeta.extmanager.test.bundle006.a;

import de.je.jmeta.extmanager.testExtensionPoints.a.TestExtensionPointCompA1;

/**
 * {@link TestImplCompA1}
 *
 */
public class TestImplCompA1 implements TestExtensionPointCompA1 {

   /**
    * @see de.je.jmeta.extmanager.testExtensionPoints.a.TestExtensionPointCompA1#getAnything()
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

      return "11";
   }
}
