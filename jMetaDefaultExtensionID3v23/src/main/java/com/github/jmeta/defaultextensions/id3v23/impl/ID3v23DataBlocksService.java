package com.github.jmeta.defaultextensions.id3v23.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.github.jmeta.library.datablocks.api.services.AbstractDataBlockService;
import com.github.jmeta.library.datablocks.api.services.DataBlockFactory;
import com.github.jmeta.library.datablocks.api.services.DataBlockReader;
import com.github.jmeta.library.datablocks.api.services.ExtendedDataBlockFactory;
import com.github.jmeta.library.dataformats.api.services.DataFormatSpecification;
import com.github.jmeta.library.dataformats.api.types.DataBlockId;
import com.github.jmeta.utility.dbc.api.services.Reject;

/**
 * {@link ID3v23DataBlocksService}
 *
 */
public class ID3v23DataBlocksService extends AbstractDataBlockService {

   final ArrayList<DataTransformationType> transformations = new ArrayList<>();

   /**
    * @see com.github.jmeta.library.datablocks.api.services.AbstractDataBlockService#getDataBlockReader(com.github.jmeta.library.dataformats.api.services.DataFormatSpecification,
    *      int)
    */
   @Override
   public DataBlockReader getDataBlockReader(DataFormatSpecification spec, int lazyFieldSize) {

      List<DataBlockId> unsynchronisationContainers = new ArrayList<>();

      unsynchronisationContainers.add(ID3v23Extension.ID3V23_TAG_ID);

      transformations.add(new DataTransformationType("Unsynchronisation", unsynchronisationContainers, true, 0, 0));

      return new ID3v23DataBlockReader(spec, getTransformationHandlers(spec, getDataBlockFactory()), lazyFieldSize,
         transformations);
   }

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
   private Map<DataTransformationType, TransformationHandler> getTransformationHandlers(DataFormatSpecification spec,
      DataBlockFactory dataBlockFactory) {

      Reject.ifNull(dataBlockFactory, "dataBlockFactory");

      List<DataTransformationType> transformationTypes = transformations;

      final Map<DataTransformationType, TransformationHandler> transformations = new HashMap<>();

      for (int i = 0; i < transformationTypes.size(); ++i) {
         DataTransformationType type = transformationTypes.get(i);

         if (type.getName().equals("Compression"))
            transformations.put(type, new CompressionHandler(type, dataBlockFactory));

         else if (type.getName().equals("Unsynchronisation"))
            transformations.put(type, new UnsynchronisationHandler(type, dataBlockFactory));
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
