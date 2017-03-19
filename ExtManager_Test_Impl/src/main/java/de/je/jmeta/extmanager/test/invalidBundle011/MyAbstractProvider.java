/**
 *
 * {@link MyAbstractProvider}.java
 *
 * @author Jens Ebert
 *
 * @date 02.05.2011
 */
package de.je.jmeta.extmanager.test.invalidBundle011;

import de.je.jmeta.extmanager.testExtensionPoints.a.TestExtensionPointCompA2;

/**
 * {@link MyAbstractProvider}
 *
 */
public abstract class MyAbstractProvider implements TestExtensionPointCompA2 {

   /**
    * Creates a new {@link MyAbstractProvider}.
    */
   public MyAbstractProvider() {
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
