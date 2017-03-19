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

import de.je.jmeta.extmanager.export.BundleType;
import de.je.jmeta.extmanager.export.ExtensionBundleDescription;
import de.je.jmeta.extmanager.export.IExtensionBundle;
import de.je.jmeta.extmanager.impl.jaxb.extbundles.BundlePropertiesType;
import de.je.jmeta.extmanager.impl.jaxb.extbundles.ExtensionBundleType;
import de.je.jmeta.extmanager.impl.jaxb.extbundles.ExtensionType;
import de.je.util.javautil.common.err.Contract;
import de.je.util.javautil.xml.jaxbloader.AbstractJAXBLoader;

/**
 * {@link ExtensionBundleLoader} loads information about a single {@link IExtensionBundle} from a corresponding XML
 * configuration file using JAXB.
 */
public class ExtensionBundleLoader
   extends AbstractJAXBLoader<ExtensionBundleType> {

   /**
    * Creates a new {@link ExtensionBundleLoader}.
    */
   public ExtensionBundleLoader() {
      super("schema/ExtensionBundleConfiguration.xsd",
         ExtensionBundleType.class);
   }

   /**
    * Gets the extension bundle name as previously loaded from the configuration file.
    *
    * @return the extension bundle name as previously loaded from the configuration file.
    *
    * @pre {@link #isLoaded()}
    */
   public String getBundleName() {

      Contract.checkPrecondition(isLoaded(), "isLoaded() was false");

      return getRootObject().getName();
   }

   /**
    * Gets the {@link ExtensionBundleDescription} name as previously loaded from the configuration file.
    *
    * @return the {@link ExtensionBundleDescription} name as previously loaded from the configuration file.
    *
    * @pre {@link #isLoaded()}
    */
   public ExtensionBundleDescription getBundleDescription() {

      Contract.checkPrecondition(isLoaded(), "isLoaded() was false");

      return convertToBundleDescription(getRootObject().getBundleProperties());
   }

   /**
    * Returns the extension bundle information.
    *
    * @return the extension bundle information.
    *
    * @pre {@link #isLoaded()}
    */
   public List<ExtensionType> getExtensionDescriptions() {

      Contract.checkPrecondition(isLoaded(), "isLoaded() was false");

      return getRootObject().getBundledExtensions().getExtension();
   }

   /**
    * Converts {@link BundlePropertiesType} as loaded from XML to its external representation which is
    * {@link ExtensionBundleDescription}.
    *
    * @param properties
    *           The {@link BundlePropertiesType}.
    * @return The corresponding {@link ExtensionBundleDescription}.
    */
   private ExtensionBundleDescription convertToBundleDescription(
      BundlePropertiesType properties) {

      return new ExtensionBundleDescription(properties.getName(),
         properties.getAuthor(), properties.getVersion(),
         properties.getDate().toGregorianCalendar().getTime(),
         properties.getDescription(),
         BundleType.valueOf(properties.getType().toString()));
   }
}
