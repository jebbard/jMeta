/**
 *
 * {@link XYZ}.java
 *
 * @author Jens Ebert
 *
 * @date 12.09.2009
 *
 */
package de.je.jmeta.extmanager.testExtensionPoints.d;

import de.je.jmeta.extmanager.export.IExtensionPoint;

/**
 * {@link MyWrongIF1} is only a dummy.
 */
public class MyWrongIF1 implements IExtensionPoint {

   /**
    * @see de.je.jmeta.extmanager.export.IExtensionPoint#getExtensionId()
    */
   @Override
   public String getExtensionId() {

      return null;
   }
}
