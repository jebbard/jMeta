package de.je.jmeta.extmanager.test.invalidBundle012;

import de.je.jmeta.extmanager.testExtensionPoints.a.TestExtensionPointCompA2;

/**
 * {@link MyNonAccessibleConstructorProvider}
 *
 */
public class MyNonAccessibleConstructorProvider
   implements TestExtensionPointCompA2 {

   /**
    * Creates a new {@link MyNonAccessibleConstructorProvider}.
    */
   private MyNonAccessibleConstructorProvider() {
      super();
   }

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

      return null;
   }
}
