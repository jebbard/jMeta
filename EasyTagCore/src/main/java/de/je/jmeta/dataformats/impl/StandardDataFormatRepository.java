/**
 *
 * {@link StandardDataFormatRepository}.java
 *
 * @author Jens Ebert
 *
 * @date 31.12.2010
 */

package de.je.jmeta.dataformats.impl;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.je.jmeta.common.ILoggingMessageConstants;
import de.je.jmeta.dataformats.DataFormat;
import de.je.jmeta.dataformats.IDataFormatRepository;
import de.je.jmeta.dataformats.IDataFormatSpecification;
import de.je.jmeta.dataformats.export.IDataFormatsExtension;
import de.je.jmeta.extmanager.export.BundleType;
import de.je.jmeta.extmanager.export.IExtensionBundle;
import de.je.jmeta.extmanager.export.IExtensionManager;
import de.je.jmeta.extmanager.export.InvalidExtensionException;
import de.je.util.javautil.common.err.Reject;
import de.je.util.javautil.simpleregistry.AbstractComponentImplementation;
import de.je.util.javautil.simpleregistry.ComponentDescription;
import de.je.util.javautil.simpleregistry.ISimpleComponentRegistry;

/**
 * {@link StandardDataFormatRepository}
 *
 */
public class StandardDataFormatRepository extends AbstractComponentImplementation<IDataFormatRepository>
   implements IDataFormatRepository {

   private static final Logger LOGGER = LoggerFactory.getLogger(StandardDataFormatRepository.class);

   private static final ComponentDescription<IDataFormatRepository> COMPONENT_DESCRIPTION = new ComponentDescription<>(
      "DataFormatRepository", IDataFormatRepository.class, "Jens Ebert", "v0.1",
      "Component for managing data format properties");

   /**
    * Creates a new {@link StandardDataFormatRepository}.
    * 
    * @param registry
    */
   public StandardDataFormatRepository(ISimpleComponentRegistry registry) {
      super(COMPONENT_DESCRIPTION, IDataFormatRepository.class, registry);
      extManager = registry.getComponentImplementation(IExtensionManager.class);

      List<IExtensionBundle> extBundles = extManager.getRegisteredExtensionBundles();

      Map<DataFormat, IDataFormatsExtension> defaultDataFormatsExtensions = new HashMap<>();
      Map<DataFormat, IDataFormatsExtension> customDataFormatsExtensions = new HashMap<>();

      String validatingExtensions = "Validating registered data format extensions"
         + ILoggingMessageConstants.SUFFIX_TASK;

      LOGGER.info(ILoggingMessageConstants.PREFIX_TASK_STARTING + validatingExtensions);

      for (int i = 0; i < extBundles.size(); ++i) {
         IExtensionBundle bundle = extBundles.get(i);

         List<IDataFormatsExtension> bundleDataFormatsExtensions = bundle
            .getExtensionsForExtensionPoint(IDataFormatsExtension.class);

         for (int j = 0; j < bundleDataFormatsExtensions.size(); ++j) {
            IDataFormatsExtension dataFormatsExtension = bundleDataFormatsExtensions.get(j);

            final DataFormat extensionDataFormat = dataFormatsExtension.getDataFormat();

            if (extensionDataFormat == null) {
               final String message = "The extension " + dataFormatsExtension
                  + " must not return null for its data format.";
               LOGGER.error(ILoggingMessageConstants.PREFIX_TASK_FAILED + ILoggingMessageConstants.PREFIX_CRITICAL_ERROR
                  + validatingExtensions);
               LOGGER.error(message);
               throw new InvalidExtensionException(message, bundle, dataFormatsExtension);
            }

            // Default bundle data formats may not override existing extension data formats
            if (bundle.getDescription().getType().equals(BundleType.DEFAULT)) {
               if (defaultDataFormatsExtensions.containsKey(extensionDataFormat))
                  LOGGER.warn(
                     "The default data formats extension <%1$s> is NOT REGISTERED and therefore ignored because it provides the data format <%2$s> that is already provided by another default extension with id <%3$s>.",
                     dataFormatsExtension.getExtensionId(), extensionDataFormat,
                     defaultDataFormatsExtensions.get(extensionDataFormat).getExtensionId());

               else if (customDataFormatsExtensions.containsKey(extensionDataFormat))
                  LOGGER.warn(
                     "The default data formats extension <%1$s> is NOT REGISTERED and therefore ignored because it provides the data format <%2$s> that is already provided by another custom extension with id <%3$s>.",
                     dataFormatsExtension.getExtensionId(), extensionDataFormat,
                     customDataFormatsExtensions.get(extensionDataFormat).getExtensionId());

               else
                  defaultDataFormatsExtensions.put(extensionDataFormat, dataFormatsExtension);
            }

            // Custom bundle data formats may override existing default extension data formats, but no other custom ones
            else if (bundle.getDescription().getType().equals(BundleType.CUSTOM)) {
               if (customDataFormatsExtensions.containsKey(extensionDataFormat))
                  LOGGER.warn(
                     "The custom data formats extension <%1$s> is NOT REGISTERED and therefore ignored because it provides the data format <%2$s> that is already provided by another custom extension with id <%3$s>.",
                     dataFormatsExtension.getExtensionId(), extensionDataFormat,
                     customDataFormatsExtensions.get(extensionDataFormat).getExtensionId());

               else
                  customDataFormatsExtensions.put(extensionDataFormat, dataFormatsExtension);
            }
         }

         for (Iterator<DataFormat> iterator = customDataFormatsExtensions.keySet().iterator(); iterator.hasNext();) {
            DataFormat nextFormat = iterator.next();
            IDataFormatsExtension nextExtension = customDataFormatsExtensions.get(nextFormat);

            final DataFormat specDataFormat = nextExtension.getSpecification().getDataFormat();
            if (!nextFormat.equals(specDataFormat)) {
               final String message = "The extension " + nextExtension.getExtensionId() + " specified data format "
                  + nextFormat + " that does not equal the data format of its specification which is " + specDataFormat;
               LOGGER.error(ILoggingMessageConstants.PREFIX_TASK_FAILED + ILoggingMessageConstants.PREFIX_CRITICAL_ERROR
                  + validatingExtensions);
               LOGGER.error(message);
               throw new InvalidExtensionException(message, bundle, nextExtension);
            }

            m_dataFormatMap.put(nextFormat, nextExtension.getSpecification());
         }

         for (Iterator<DataFormat> iterator = defaultDataFormatsExtensions.keySet().iterator(); iterator.hasNext();) {
            DataFormat nextFormat = iterator.next();
            IDataFormatsExtension nextExtension = defaultDataFormatsExtensions.get(nextFormat);

            final DataFormat specDataFormat = nextExtension.getSpecification().getDataFormat();
            if (!nextFormat.equals(specDataFormat)) {
               final String message = "The extension " + nextExtension.getExtensionId() + " specified data format "
                  + nextFormat + " that does not equal the data format of its specification which is " + specDataFormat;
               LOGGER.error(ILoggingMessageConstants.PREFIX_TASK_FAILED + ILoggingMessageConstants.PREFIX_CRITICAL_ERROR
                  + validatingExtensions);
               LOGGER.error(message);
               throw new InvalidExtensionException(message, bundle, nextExtension);
            }

            if (m_dataFormatMap.containsKey(nextFormat)) {
               LOGGER.warn(
                  "The default data format extension <%1$s> is OVERRIDDEN by the default data formats extension <%2$s>.",
                  nextExtension.getExtensionId(), m_dataFormatMap.get(nextFormat));
            }

            else
               m_dataFormatMap.put(nextFormat, nextExtension.getSpecification());
         }
      }

      LOGGER.info(ILoggingMessageConstants.PREFIX_TASK_DONE_NEUTRAL + validatingExtensions);
   }

   /**
    * @see de.je.jmeta.dataformats.IDataFormatRepository#getDataFormatSpecification(de.je.jmeta.dataformats.DataFormat)
    */
   @Override
   public IDataFormatSpecification getDataFormatSpecification(DataFormat dataFormat) {

      Reject.ifNull(dataFormat, "dataFormat");
      Reject.ifFalse(getSupportedDataFormats().contains(dataFormat),
         "getSupportedDataFormats().contains(dataFormat)");

      return m_dataFormatMap.get(dataFormat);
   }

   /**
    * @see de.je.jmeta.dataformats.IDataFormatRepository#getSupportedDataFormats()
    */
   @Override
   public Set<DataFormat> getSupportedDataFormats() {

      return Collections.unmodifiableSet(m_dataFormatMap.keySet());
   }

   private final Map<DataFormat, IDataFormatSpecification> m_dataFormatMap = new HashMap<>();

   private final IExtensionManager extManager;
}
