package de.je.jmeta.extmanager.test.invalidBundle013;

import de.je.jmeta.extmanager.testExtensionPoints.b.TestExtensionPointCompB1;

/**
 * {@link MyInvalidProvider}
 *
 */
public class MyInvalidProvider implements TestExtensionPointCompB1 {

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

      return null;
   }
}
