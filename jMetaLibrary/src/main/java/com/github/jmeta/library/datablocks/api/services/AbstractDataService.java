/**
 *
 * {@link AbstractDataService}.java
 *
 * @author Jens Ebert
 *
 * @date 28.04.2011
 */
package com.github.jmeta.library.datablocks.api.services;

import java.util.ArrayList;
import java.util.List;

import com.github.jmeta.library.dataformats.api.services.IDataFormatSpecification;
import com.github.jmeta.library.dataformats.api.types.DataFormat;

/**
 * {@link AbstractDataService} provides a default implementation of {@link IDataFormatsExtension} that only returns a
 * {@link DataFormat}. Methods can be overridden to change its behavior.
 */
public abstract class AbstractDataService implements IDataBlockService {

   /**
    * Creates a new {@link AbstractDataService}.
    * 
    * @param myDataFormat
    */
   public AbstractDataService(DataFormat myDataFormat) {
      m_myDataFormat = myDataFormat;
   }

   /**
    * @see com.github.jmeta.library.datablocks.api.services.IDataBlockService#getDataBlockFactory()
    */
   @Override
   public IExtendedDataBlockFactory getDataBlockFactory() {

      // Choose to use default IExtendedDataBlockFactory
      return null;
   }

   /**
    * @see com.github.jmeta.library.datablocks.api.services.IDataBlockService#getDataBlockReader(IDataFormatSpecification, int)
    */
   @Override
   public IDataBlockReader getDataBlockReader(IDataFormatSpecification spec, int lazyFieldSize) {

      // Choose to use default IDataBlockReader
      return null;
   }

   /**
    * @see com.github.jmeta.library.datablocks.api.services.IDataBlockService#getDataFormat()
    */
   @Override
   public DataFormat getDataFormat() {

      return m_myDataFormat;
   }

   /**
    * @see com.github.jmeta.library.datablocks.api.services.IDataBlockService#getTransformationHandlers(IDataFormatSpecification,
    *      IDataBlockFactory)
    */
   @Override
   public List<ITransformationHandler> getTransformationHandlers(IDataFormatSpecification spec,
      IDataBlockFactory dataBlockFactory) {

      return new ArrayList<>();
   }

   private final DataFormat m_myDataFormat;
}
