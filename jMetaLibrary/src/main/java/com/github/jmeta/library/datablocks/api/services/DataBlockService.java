package com.github.jmeta.library.datablocks.api.services;

import com.github.jmeta.library.dataformats.api.services.DataFormatSpecification;
import com.github.jmeta.library.dataformats.api.types.ContainerDataFormat;

/**
 * {@link DataBlockService}
 *
 */
public interface DataBlockService {

   /**
    * @return the {@link ContainerDataFormat}
    */
   public ContainerDataFormat getDataFormat();

   /**
    * @return null to indicate usage of the default {@link ExtendedDataBlockFactory}
    */
   public ExtendedDataBlockFactory getDataBlockFactory();

   /**
    * @param spec
    * @param lazyFieldSize
    * @return null to indicate usage of the default {@link DataBlockReader}
    */
   public DataBlockReader getDataBlockReader(DataFormatSpecification spec, int lazyFieldSize);
}
