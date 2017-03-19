/**
 *
 * {@link IExtendedDataBlockFactory}.java
 *
 * @author Jens Ebert
 *
 * @date 04.01.2011
 */

package de.je.jmeta.datablocks.export;

import de.je.jmeta.datablocks.IDataBlockFactory;
import de.je.jmeta.datablocks.IPayload;
import de.je.jmeta.dataformats.DataBlockId;
import de.je.jmeta.media.api.IMediaAPI;
import de.je.jmeta.media.api.IMediumReference;

/**
 * {@link IExtendedDataBlockFactory}
 *
 */
public interface IExtendedDataBlockFactory extends IDataBlockFactory {

   /**
    * @param id
    * @param reference
    * @param totalSize
    * @param reader
    * @param context
    * @return the {@link IPayload}
    */
   public IPayload createPayloadAfterRead(DataBlockId id,
      IMediumReference reference, long totalSize, IDataBlockReader reader,
      FieldFunctionStack context);

   /**
    * @param dataBlockReader
    */
   public void setDataBlockReader(IDataBlockReader dataBlockReader);

   /**
    * @param mediumFactory
    */
   public void setMediumFactory(IMediaAPI mediumFactory);
}
