/**
 *
 * {@link XYZImpl2}.java
 *
 * @author Jens Ebert
 *
 * @date 29.04.2011
 */
package de.je.jmeta.extmanager.test.bundle007.d;

import de.je.jmeta.extmanager.testExtensionPoints.d.ExtensionPointXYZ;

/**
 * {@link XYZImpl2}
 *
 */
public class XYZImpl2 implements ExtensionPointXYZ {

   /**
    * @see de.je.jmeta.extmanager.export.IExtensionPoint#getExtensionId()
    */
   @Override
   public String getExtensionId() {

      return "7";
   }
}
