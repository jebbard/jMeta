package de.je.jmeta.defext.datablocks.impl;

import java.util.ArrayList;
import java.util.List;

import de.je.jmeta.datablocks.IDataBlockFactory;
import de.je.jmeta.datablocks.ITransformationHandler;
import de.je.jmeta.datablocks.export.AbstractDataBlocksExtension;
import de.je.jmeta.datablocks.export.IExtendedDataBlockFactory;
import de.je.jmeta.dataformats.DataTransformationType;
import de.je.jmeta.dataformats.IDataFormatSpecification;
import de.je.jmeta.defext.datablocks.impl.id3v23.CompressionHandler;
import de.je.jmeta.defext.datablocks.impl.id3v23.ID3v23DataBlockFactory;
import de.je.jmeta.defext.datablocks.impl.id3v23.UnsynchronisationHandler;
import de.je.jmeta.defext.dataformats.DefaultExtensionsDataFormat;
import de.je.util.javautil.common.err.Reject;

/**
 * {@link ID3v23DataBlocksExtension}
 *
 */
public class ID3v23DataBlocksExtension extends AbstractDataBlocksExtension {

   /**
    * Creates a new {@link ID3v23DataBlocksExtension}.
    */
   public ID3v23DataBlocksExtension() {
      super(DefaultExtensionsDataFormat.ID3v23);
   }

   /**
    * @see de.je.jmeta.datablocks.export.IDataBlocksExtension#getTransformationHandlers(IDataFormatSpecification,
    *      IDataBlockFactory)
    */
   @Override
   public List<ITransformationHandler> getTransformationHandlers(
      IDataFormatSpecification spec, IDataBlockFactory dataBlockFactory) {

      Reject.ifNull(dataBlockFactory, "dataBlockFactory");

      List<DataTransformationType> transformationTypes = spec
         .getDataTransformations();

      final ArrayList<ITransformationHandler> transformations = new ArrayList<>();

      for (int i = 0; i < transformationTypes.size(); ++i) {
         DataTransformationType type = transformationTypes.get(i);

         if (type.getName().equals("Compression"))
            transformations
               .add(new CompressionHandler(type, dataBlockFactory));

         else if (type.getName().equals("Unsynchronisation"))
            transformations.add(
               new UnsynchronisationHandler(type, dataBlockFactory));
      }

      return transformations;
   }

   /**
    * @see de.je.jmeta.extmanager.export.IExtensionPoint#getExtensionId()
    */
   @Override
   public String getExtensionId() {

      return "DEFAULT_de.je.jmeta.defext.datablocks.impl.ID3v23DataBlocksExtension";
   }

   /**
    * @see de.je.jmeta.datablocks.export.AbstractDataBlocksExtension#getDataBlockFactory()
    */
   @Override
   public IExtendedDataBlockFactory getDataBlockFactory() {

      return new ID3v23DataBlockFactory();
   }
}
