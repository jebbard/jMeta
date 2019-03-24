/**
 *
 * {@link AbstractDataBlockService}.java
 *
 * @author Jens Ebert
 *
 * @date 28.04.2011
 */
package com.github.jmeta.library.datablocks.api.services;

import com.github.jmeta.library.dataformats.api.services.DataFormatSpecification;
import com.github.jmeta.library.dataformats.api.types.ContainerDataFormat;

/**
 * {@link AbstractDataBlockService} provides a default implementation of {@link DataBlockService} that only returns a
 * {@link ContainerDataFormat}. Methods can be overridden to change its behavior.
 */
public abstract class AbstractDataBlockService implements DataBlockService {

   /**
    * Creates a new {@link AbstractDataBlockService}.
    *
    * @param myDataFormat
    */
   public AbstractDataBlockService(ContainerDataFormat myDataFormat) {
      m_myDataFormat = myDataFormat;
   }

   /**
    * @see com.github.jmeta.library.datablocks.api.services.DataBlockService#getForwardDataBlockReader(DataFormatSpecification)
    */
   @Override
   public DataBlockReader getForwardDataBlockReader(DataFormatSpecification spec) {

      // Choose to use default IDataBlockReader
      return null;
   }

   @Override
   public DataBlockReader getBackwardDataBlockReader(DataFormatSpecification spec) {

      // Choose to use default IDataBlockReader
      return null;
   }

   /**
    * @see com.github.jmeta.library.datablocks.api.services.DataBlockService#getDataFormat()
    */
   @Override
   public ContainerDataFormat getDataFormat() {

      return m_myDataFormat;
   }

   private final ContainerDataFormat m_myDataFormat;
}
