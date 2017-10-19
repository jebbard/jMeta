/**
 *
 * {@link AbstractDataService}.java
 *
 * @author Jens Ebert
 *
 * @date 28.04.2011
 */
package de.je.jmeta.datablocks.export;

import java.util.ArrayList;
import java.util.List;

import de.je.jmeta.datablocks.IDataBlockFactory;
import de.je.jmeta.datablocks.ITransformationHandler;
import de.je.jmeta.dataformats.DataFormat;
import de.je.jmeta.dataformats.IDataFormatSpecification;

/**
 * {@link AbstractDataService} provides a default implementation of {@link IDataFormatsExtension} that only returns a
 * {@link DataFormat}. Methods can be overridden to change its behavior.
 */
public abstract class AbstractDataService implements IDataBlockService {

   /**
    * Creates a new {@link AbstractDataService}.
    * 
    * @param myDataFormat
    */
   public AbstractDataService(DataFormat myDataFormat) {
      m_myDataFormat = myDataFormat;
   }

   /**
    * @see de.je.jmeta.datablocks.export.IDataBlockService#getDataBlockFactory()
    */
   @Override
   public IExtendedDataBlockFactory getDataBlockFactory() {

      // Choose to use default IExtendedDataBlockFactory
      return null;
   }

   /**
    * @see de.je.jmeta.datablocks.export.IDataBlockService#getDataBlockReader(IDataFormatSpecification, int)
    */
   @Override
   public IDataBlockReader getDataBlockReader(IDataFormatSpecification spec, int lazyFieldSize) {

      // Choose to use default IDataBlockReader
      return null;
   }

   /**
    * @see de.je.jmeta.datablocks.export.IDataBlockService#getDataFormat()
    */
   @Override
   public DataFormat getDataFormat() {

      return m_myDataFormat;
   }

   /**
    * @see de.je.jmeta.datablocks.export.IDataBlockService#getTransformationHandlers(IDataFormatSpecification,
    *      IDataBlockFactory)
    */
   @Override
   public List<ITransformationHandler> getTransformationHandlers(IDataFormatSpecification spec,
      IDataBlockFactory dataBlockFactory) {

      return new ArrayList<>();
   }

   private final DataFormat m_myDataFormat;
}
