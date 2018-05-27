/**
 *
 * {@link DataFormatSpecificationBuilder}.java
 *
 * @author Jens Ebert
 *
 * @date 23.05.2018
 *
 */
package com.github.jmeta.library.dataformats.api.services;

import java.nio.ByteOrder;
import java.nio.charset.Charset;

import com.github.jmeta.library.dataformats.api.services.builder.ContainerSequenceBuilder;
import com.github.jmeta.library.dataformats.api.services.builder.DataFormatBuilder;
import com.github.jmeta.library.dataformats.api.types.DataBlockDescription;
import com.github.jmeta.library.dataformats.api.types.DataBlockId;
import com.github.jmeta.library.dataformats.api.types.MagicKey;

/**
 * {@link DataFormatSpecificationBuilder}
 *
 */
public interface DataFormatSpecificationBuilder
   extends ContainerSequenceBuilder<DataFormatSpecificationBuilder>, DataFormatBuilder {

   public DataFormatSpecification build();

   public DataFormatSpecificationBuilder addDataBlockDescription(DataBlockDescription newDescription,
      boolean isTopLevel, boolean isDefaultNestedContainer);

   public DataFormatSpecificationBuilder addCustomHeaderMagicKey(DataBlockId containerId, MagicKey magicKey);

   public DataFormatSpecificationBuilder addCustomFooterMagicKey(DataBlockId containerId, MagicKey magicKey);

   public DataFormatSpecificationBuilder withByteOrders(ByteOrder defaultByteOrder,
      ByteOrder... furtherSupportedByteOrders);

   public DataFormatSpecificationBuilder withCharsets(Charset defaultCharset, Charset... furtherSupportedCharsets);
}
