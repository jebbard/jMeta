/**
 *
 * {@link TestIMPLCompC1}.java
 *
 * @author Jens Ebert
 *
 * @date 29.04.2011
 */
package de.je.jmeta.extmanager.test.bundle001.c;

import de.je.jmeta.extmanager.testExtensionPoints.c.TestExtensionPointCompC1;

/**
 * {@link TestIMPLCompC1}
 *
 */
public class TestIMPLCompC1 implements TestExtensionPointCompC1 {

   /**
    * @see de.je.jmeta.extmanager.testExtensionPoints.c.TestExtensionPointCompC1#getAnything2()
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

      return "3";
   }
}
