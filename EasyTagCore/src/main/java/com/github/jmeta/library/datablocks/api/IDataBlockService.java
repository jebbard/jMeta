package de.je.jmeta.datablocks.export;

import java.util.List;

import de.je.jmeta.datablocks.IDataBlockFactory;
import de.je.jmeta.datablocks.ITransformationHandler;
import de.je.jmeta.dataformats.DataFormat;
import de.je.jmeta.dataformats.IDataFormatSpecification;

/**
 * {@link IDataBlockService}
 *
 */
public interface IDataBlockService {

   /**
    * @return the {@link DataFormat}
    */
   public DataFormat getDataFormat();

   /**
    * @return null to indicate usage of the default {@link IExtendedDataBlockFactory}
    */
   public IExtendedDataBlockFactory getDataBlockFactory();

   /**
    * @param spec
    * @param lazyFieldSize
    * @return null to indicate usage of the default {@link IDataBlockReader}
    */
   public IDataBlockReader getDataBlockReader(IDataFormatSpecification spec, int lazyFieldSize);

   /**
    * @param spec
    * @param dataBlockFactory
    * @return an empty {@link List} if there are no {@link ITransformationHandler}s.
    */
   public List<ITransformationHandler> getTransformationHandlers(IDataFormatSpecification spec,
      IDataBlockFactory dataBlockFactory);
}
