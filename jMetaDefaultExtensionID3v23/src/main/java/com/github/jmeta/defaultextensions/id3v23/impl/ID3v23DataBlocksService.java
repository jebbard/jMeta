package com.github.jmeta.defaultextensions.id3v23.impl;

import java.util.ArrayList;
import java.util.List;

import com.github.jmeta.library.datablocks.api.services.AbstractDataBlockService;
import com.github.jmeta.library.datablocks.api.services.DataBlockFactory;
import com.github.jmeta.library.datablocks.api.services.ExtendedDataBlockFactory;
import com.github.jmeta.library.datablocks.api.services.TransformationHandler;
import com.github.jmeta.library.dataformats.api.services.DataFormatSpecification;
import com.github.jmeta.library.dataformats.api.types.DataTransformationType;
import com.github.jmeta.utility.dbc.api.services.Reject;

/**
 * {@link ID3v23DataBlocksService}
 *
 */
public class ID3v23DataBlocksService extends AbstractDataBlockService {

   /**
    * Creates a new {@link ID3v23DataBlocksService}.
    */
   public ID3v23DataBlocksService() {
      super(ID3v23Extension.ID3v23);
   }

   /**
    * @see com.github.jmeta.library.datablocks.api.services.DataBlockService#getTransformationHandlers(DataFormatSpecification,
    *      DataBlockFactory)
    */
   @Override
   public List<TransformationHandler> getTransformationHandlers(DataFormatSpecification spec,
      DataBlockFactory dataBlockFactory) {

      Reject.ifNull(dataBlockFactory, "dataBlockFactory");

      List<DataTransformationType> transformationTypes = spec.getDataTransformations();

      final ArrayList<TransformationHandler> transformations = new ArrayList<>();

      for (int i = 0; i < transformationTypes.size(); ++i) {
         DataTransformationType type = transformationTypes.get(i);

         if (type.getName().equals("Compression"))
            transformations.add(new CompressionHandler(type, dataBlockFactory));

         else if (type.getName().equals("Unsynchronisation"))
            transformations.add(new UnsynchronisationHandler(type, dataBlockFactory));
      }

      return transformations;
   }

   /**
    * @see com.github.jmeta.library.datablocks.api.services.AbstractDataBlockService#getDataBlockFactory()
    */
   @Override
   public ExtendedDataBlockFactory getDataBlockFactory() {

      return new ID3v23DataBlockFactory();
   }
}
