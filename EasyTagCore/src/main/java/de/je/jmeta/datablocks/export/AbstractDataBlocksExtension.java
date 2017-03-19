/**
 *
 * {@link AbstractDataBlocksExtension}.java
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
import de.je.jmeta.dataformats.export.IDataFormatsExtension;

/**
 * {@link AbstractDataBlocksExtension} provides a default implementation of {@link IDataFormatsExtension} that only
 * returns a {@link DataFormat}. Methods can be overridden to change its behavior.
 */
public abstract class AbstractDataBlocksExtension
   implements IDataBlocksExtension {

   /**
    * Creates a new {@link AbstractDataBlocksExtension}.
    * 
    * @param myDataFormat
    */
   public AbstractDataBlocksExtension(DataFormat myDataFormat) {
      m_myDataFormat = myDataFormat;
   }

   /**
    * @see de.je.jmeta.datablocks.export.IDataBlocksExtension#getDataBlockFactory()
    */
   @Override
   public IExtendedDataBlockFactory getDataBlockFactory() {

      // Choose to use default IExtendedDataBlockFactory
      return null;
   }

   /**
    * @see de.je.jmeta.datablocks.export.IDataBlocksExtension#getDataBlockReader(IDataFormatSpecification, int)
    */
   @Override
   public IDataBlockReader getDataBlockReader(IDataFormatSpecification spec,
      int lazyFieldSize) {

      // Choose to use default IDataBlockReader
      return null;
   }

   /**
    * @see de.je.jmeta.datablocks.export.IDataBlocksExtension#getDataFormat()
    */
   @Override
   public DataFormat getDataFormat() {

      return m_myDataFormat;
   }

   /**
    * @see de.je.jmeta.datablocks.export.IDataBlocksExtension#getTransformationHandlers(IDataFormatSpecification,
    *      IDataBlockFactory)
    */
   @Override
   public List<ITransformationHandler> getTransformationHandlers(
      IDataFormatSpecification spec, IDataBlockFactory dataBlockFactory) {

      return new ArrayList<>();
   }

   private final DataFormat m_myDataFormat;
}
