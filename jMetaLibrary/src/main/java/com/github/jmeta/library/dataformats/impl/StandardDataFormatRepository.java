/**
 *
 * {@link StandardDataFormatRepository}.java
 *
 * @author Jens Ebert
 *
 * @date 31.12.2010
 */

package com.github.jmeta.library.dataformats.impl;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.jmeta.library.dataformats.api.services.IDataFormatRepository;
import com.github.jmeta.library.dataformats.api.services.IDataFormatSpecification;
import com.github.jmeta.library.dataformats.api.types.DataFormat;
import com.github.jmeta.utility.compregistry.api.services.ComponentRegistry;
import com.github.jmeta.utility.dbc.api.services.Reject;
import com.github.jmeta.utility.extmanager.api.exceptions.InvalidExtensionException;
import com.github.jmeta.utility.extmanager.api.services.IExtension;
import com.github.jmeta.utility.extmanager.api.services.IExtensionManager;
import com.github.jmeta.utility.logging.api.services.ILoggingMessageConstants;

/**
 * {@link StandardDataFormatRepository}
 *
 */
public class StandardDataFormatRepository implements IDataFormatRepository {

   private static final Logger LOGGER = LoggerFactory.getLogger(StandardDataFormatRepository.class);

   /**
    * Creates a new {@link StandardDataFormatRepository}. No parameters must be added as this class may be dynamically
    * instantiated by reflection by a component loader.
    */
   public StandardDataFormatRepository() {
      extManager = ComponentRegistry.lookupService(IExtensionManager.class);

      String validatingExtensions = "Validating registered data format extensions"
         + ILoggingMessageConstants.SUFFIX_TASK;

      LOGGER.info(ILoggingMessageConstants.PREFIX_TASK_STARTING + validatingExtensions);

      List<IExtension> availableExtensions = extManager.getAllExtensions();

      for (IExtension iExtension2 : availableExtensions) {

         List<IDataFormatSpecification> dataFormatSpecificationsInExtension = iExtension2
            .getAllServiceProviders(IDataFormatSpecification.class);

         for (IDataFormatSpecification dataFormatSpec : dataFormatSpecificationsInExtension) {
            final DataFormat extensionDataFormat = dataFormatSpec.getDataFormat();

            if (extensionDataFormat == null) {
               final String message = "The extension " + dataFormatSpec + " must not return null for its data format.";
               LOGGER.error(ILoggingMessageConstants.PREFIX_TASK_FAILED + ILoggingMessageConstants.PREFIX_CRITICAL_ERROR
                  + validatingExtensions);
               LOGGER.error(message);
               throw new InvalidExtensionException(message, iExtension2);
            }

            if (m_dataFormatMap.containsKey(extensionDataFormat)) {
               LOGGER.warn(
                  "The custom data formats extension <%1$s> is NOT REGISTERED and therefore ignored because it provides the data format <%2$s> that is already provided by another custom extension with id <%3$s>.",
                  iExtension2.getExtensionId(), extensionDataFormat, iExtension2.getExtensionId());
            }

            else {
               m_dataFormatMap.put(extensionDataFormat, dataFormatSpec);
            }
         }
      }

      LOGGER.info(ILoggingMessageConstants.PREFIX_TASK_DONE_NEUTRAL + validatingExtensions);
   }

   /**
    * @see com.github.jmeta.library.dataformats.api.services.IDataFormatRepository#getDataFormatSpecification(com.github.jmeta.library.dataformats.api.types.DataFormat)
    */
   @Override
   public IDataFormatSpecification getDataFormatSpecification(DataFormat dataFormat) {

      Reject.ifNull(dataFormat, "dataFormat");
      Reject.ifFalse(getSupportedDataFormats().contains(dataFormat), "getSupportedDataFormats().contains(dataFormat)");

      return m_dataFormatMap.get(dataFormat);
   }

   /**
    * @see com.github.jmeta.library.dataformats.api.services.IDataFormatRepository#getSupportedDataFormats()
    */
   @Override
   public Set<DataFormat> getSupportedDataFormats() {

      return Collections.unmodifiableSet(m_dataFormatMap.keySet());
   }

   private final Map<DataFormat, IDataFormatSpecification> m_dataFormatMap = new HashMap<>();

   private final IExtensionManager extManager;
}