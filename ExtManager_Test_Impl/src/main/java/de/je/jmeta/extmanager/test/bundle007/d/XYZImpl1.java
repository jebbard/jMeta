/**
 *
 * {@link XYZImpl1}.java
 *
 * @author Jens Ebert
 *
 * @date 29.04.2011
 */
package de.je.jmeta.extmanager.test.bundle007.d;

import de.je.jmeta.extmanager.testExtensionPoints.d.ExtensionPointXYZ;

/**
 * {@link XYZImpl1}
 *
 */
public class XYZImpl1 implements ExtensionPointXYZ {

   /**
    * @see de.je.jmeta.extmanager.export.IExtensionPoint#getExtensionId()
    */
   @Override
   public String getExtensionId() {

      return "6";
   }
}
