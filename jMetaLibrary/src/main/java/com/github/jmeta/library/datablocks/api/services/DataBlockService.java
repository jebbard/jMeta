package com.github.jmeta.library.datablocks.api.services;

import java.util.List;

import com.github.jmeta.library.dataformats.api.services.DataFormatSpecification;
import com.github.jmeta.library.dataformats.api.types.DataFormat;

/**
 * {@link DataBlockService}
 *
 */
public interface DataBlockService {

   /**
    * @return the {@link DataFormat}
    */
   public DataFormat getDataFormat();

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

   /**
    * @param spec
    * @param dataBlockFactory
    * @return an empty {@link List} if there are no {@link TransformationHandler}s.
    */
   public List<TransformationHandler> getTransformationHandlers(DataFormatSpecification spec,
      DataBlockFactory dataBlockFactory);
}
