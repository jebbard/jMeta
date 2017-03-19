/**
 *
 * {@link XYZImpl}.java
 *
 * @author Jens Ebert
 *
 * @date 29.04.2011
 */
package de.je.jmeta.extmanager.test.bundle002.d;

import de.je.jmeta.extmanager.testExtensionPoints.d.ExtensionPointXYZ;

/**
 * {@link XYZImpl}
 *
 */
public class XYZImpl implements ExtensionPointXYZ {

   /**
    * @see de.je.jmeta.extmanager.export.IExtensionPoint#getExtensionId()
    */
   @Override
   public String getExtensionId() {

      return "13";
   }
}
