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
    * @param spec
    * @return null to indicate usage of the default {@link DataBlockReader}
    */
   public DataBlockReader getDataBlockReader(DataFormatSpecification spec);
}
