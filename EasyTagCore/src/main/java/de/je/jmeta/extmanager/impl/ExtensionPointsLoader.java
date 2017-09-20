/**
 *
 * {@link ConfigurationLoader}.java
 *
 * @author Jens Ebert
 *
 * @date 10.03.2010
 *
 */

package de.je.jmeta.extmanager.impl;

import java.util.List;

import de.je.jmeta.extmanager.impl.jaxb.extpoints.ExtensionPointType;
import de.je.jmeta.extmanager.impl.jaxb.extpoints.ExtensionPointsConfigurationType;
import de.je.util.javautil.common.err.Reject;
import de.je.util.javautil.xml.jaxbloader.AbstractJAXBLoader;

/**
 * {@link ExtensionPointsLoader} loads an extensions configuration file that stores information about all globally
 * available extension points. It uses JAXB to load an XML file that must have an expected data format.
 */
public class ExtensionPointsLoader
   extends AbstractJAXBLoader<ExtensionPointsConfigurationType> {

   /**
    * Creates a new {@link ExtensionPointsLoader}.
    */
   public ExtensionPointsLoader() {
      super("schema/ExtensionPointsConfiguration.xsd",
         ExtensionPointsConfigurationType.class);
   }

   /**
    * Returns all paths to extension bundles previously loaded from the configuration file.
    *
    * @return all paths to extension bundles previously loaded from the configuration file.
    *
    * @pre {@link #isLoaded()}
    */
   public List<String> getExtensionBundleSearchPaths() {

	  Reject.ifFalse(isLoaded(), "isLoaded()");

      return getRootObject().getExtensionBundleSearchPath().getPath();
   }

   /**
    * Returns all previously loaded extension points.
    *
    * @return all previously loaded extension points.
    *
    * @pre {@link #isLoaded()}
    */
   public List<ExtensionPointType> getExtensionPointDescriptions() {

	  Reject.ifFalse(isLoaded(), "isLoaded()");

      return getRootObject().getExtensionPoints().getExtensionPoint();
   }
}
