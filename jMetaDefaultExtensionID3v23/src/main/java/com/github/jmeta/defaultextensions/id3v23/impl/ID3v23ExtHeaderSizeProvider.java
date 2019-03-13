/**
 *
 * {@link ID3v23ExtHeaderSizeProvider}.java
 *
 * @author Jens Ebert
 *
 * @date 13.03.2019
 *
 */
package com.github.jmeta.defaultextensions.id3v23.impl;

import java.util.stream.Collectors;

import com.github.jmeta.library.datablocks.api.services.SizeProvider;
import com.github.jmeta.library.datablocks.api.types.ContainerContext;
import com.github.jmeta.library.datablocks.api.types.Field;
import com.github.jmeta.library.datablocks.api.types.Header;
import com.github.jmeta.library.dataformats.api.types.DataBlockDescription;
import com.github.jmeta.library.dataformats.api.types.DataBlockId;

/**
 * {@link ID3v23ExtHeaderSizeProvider} is needed due to the very awkward definition of the ID3v2.3 extended header size:
 * The size of the header is included in itself as first field, but excluding the size field. Thus there is no direct
 * size field of the dynamic length extended header. Instead we have to sum up the field sizes of the whole extended
 * header.
 */
public class ID3v23ExtHeaderSizeProvider implements SizeProvider {

   /**
    * @see com.github.jmeta.library.datablocks.api.services.SizeProvider#getSizeOf(com.github.jmeta.library.dataformats.api.types.DataBlockId,
    *      int, com.github.jmeta.library.datablocks.api.types.ContainerContext)
    */
   @Override
   public long getSizeOf(DataBlockId id, int sequenceNumber, ContainerContext containerContext) {

      if (id.equals(ID3v23Extension.REF_EXT_HEADER.getId())) {
         // The extended header has not been read yet! It must be present, as otherwise this method would not have been
         // called
         if (containerContext.getContainer().getHeaders().size() == 1) {
            // We return a long unequal to DataBlockDescription.UNDEFINED because we explicitly want to ensure no other
            // size determination code is executed
            return containerContext.getDataFormatSpecification().getDataBlockDescription(id).getMaximumByteLength();
         }

         Header id3v23ExtHeader = containerContext.getContainer().getHeaders().get(1);

         return id3v23ExtHeader.getFields().stream().collect(Collectors.summingLong(Field::getTotalSize));
      }

      return DataBlockDescription.UNDEFINED;
   }

}
