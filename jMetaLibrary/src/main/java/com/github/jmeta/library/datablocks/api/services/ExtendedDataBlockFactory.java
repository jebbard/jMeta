/**
 *
 * {@link ExtendedDataBlockFactory}.java
 *
 * @author Jens Ebert
 *
 * @date 04.01.2011
 */

package com.github.jmeta.library.datablocks.api.services;

import com.github.jmeta.library.datablocks.api.types.FieldFunctionStack;
import com.github.jmeta.library.datablocks.api.types.Payload;
import com.github.jmeta.library.dataformats.api.types.DataBlockId;
import com.github.jmeta.library.media.api.services.MediaAPI;
import com.github.jmeta.library.media.api.types.MediumReference;

/**
 * {@link ExtendedDataBlockFactory}
 *
 */
public interface ExtendedDataBlockFactory extends DataBlockFactory {

   /**
    * @param id
    * @param reference
    * @param totalSize
    * @param reader
    * @param context
    * @return the {@link Payload}
    */
   public Payload createPayloadAfterRead(DataBlockId id,
      MediumReference reference, long totalSize, DataBlockReader reader,
      FieldFunctionStack context);

   /**
    * @param dataBlockReader
    */
   public void setDataBlockReader(DataBlockReader dataBlockReader);

   /**
    * @param mediumFactory
    */
   public void setMediumFactory(MediaAPI mediumFactory);
}
