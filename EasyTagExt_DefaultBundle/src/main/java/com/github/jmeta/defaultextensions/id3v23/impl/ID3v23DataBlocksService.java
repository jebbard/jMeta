package com.github.jmeta.defaultextensions.id3v23.impl;

import java.util.ArrayList;
import java.util.List;

import com.github.jmeta.library.datablocks.api.services.AbstractDataService;
import com.github.jmeta.library.datablocks.api.services.IDataBlockFactory;
import com.github.jmeta.library.datablocks.api.services.IExtendedDataBlockFactory;
import com.github.jmeta.library.datablocks.api.services.ITransformationHandler;
import com.github.jmeta.library.dataformats.api.services.IDataFormatSpecification;
import com.github.jmeta.library.dataformats.api.types.DataTransformationType;
import com.github.jmeta.utility.dbc.api.services.Reject;

/**
 * {@link ID3v23DataBlocksService}
 *
 */
public class ID3v23DataBlocksService extends AbstractDataService {

   /**
    * Creates a new {@link ID3v23DataBlocksService}.
    */
   public ID3v23DataBlocksService() {
      super(ID3v23Extension.ID3v23);
   }

   /**
    * @see com.github.jmeta.library.datablocks.api.services.IDataBlockService#getTransformationHandlers(IDataFormatSpecification,
    *      IDataBlockFactory)
    */
   @Override
   public List<ITransformationHandler> getTransformationHandlers(IDataFormatSpecification spec,
      IDataBlockFactory dataBlockFactory) {

      Reject.ifNull(dataBlockFactory, "dataBlockFactory");

      List<DataTransformationType> transformationTypes = spec.getDataTransformations();

      final ArrayList<ITransformationHandler> transformations = new ArrayList<>();

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
    * @see com.github.jmeta.library.datablocks.api.services.AbstractDataService#getDataBlockFactory()
    */
   @Override
   public IExtendedDataBlockFactory getDataBlockFactory() {

      return new ID3v23DataBlockFactory();
   }
}
