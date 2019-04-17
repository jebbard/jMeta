package com.github.jmeta.library.datablocks.api.services;

import com.github.jmeta.library.dataformats.api.services.DataFormatSpecification;
import com.github.jmeta.library.dataformats.api.types.ContainerDataFormat;
import com.github.jmeta.library.media.api.services.MediumStore;

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
    * @param mediumStore TODO
    * @return null to indicate usage of the default {@link DataBlockReader}
    */
   public DataBlockReader createForwardDataBlockReader(DataFormatSpecification spec, MediumStore mediumStore);

   /**
    * @param spec
    * @param mediumStore TODO
    * @return null to indicate usage of the default {@link DataBlockReader}
    */
   public DataBlockReader createBackwardDataBlockReader(DataFormatSpecification spec, MediumStore mediumStore);
}
