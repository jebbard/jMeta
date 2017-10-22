/**
 *
 * {@link IExtendedDataBlockFactory}.java
 *
 * @author Jens Ebert
 *
 * @date 04.01.2011
 */

package com.github.jmeta.library.datablocks.api.services;

import com.github.jmeta.library.datablocks.api.types.FieldFunctionStack;
import com.github.jmeta.library.datablocks.api.types.IPayload;
import com.github.jmeta.library.dataformats.api.types.DataBlockId;
import com.github.jmeta.library.media.api.services.IMediaAPI;
import com.github.jmeta.library.media.api.types.IMediumReference;

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
