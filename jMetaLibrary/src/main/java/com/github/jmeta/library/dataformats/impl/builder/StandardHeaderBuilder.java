/**
 *
 * {@link StandardHeaderBuilder}.java
 *
 * @author Jens Ebert
 *
 * @date 12.05.2018
 *
 */
package com.github.jmeta.library.dataformats.impl.builder;

import com.github.jmeta.library.dataformats.api.services.builder.ContainerBuilder;
import com.github.jmeta.library.dataformats.api.services.builder.HeaderBuilder;
import com.github.jmeta.library.dataformats.api.types.DataBlockId;
import com.github.jmeta.library.dataformats.api.types.PhysicalDataBlockType;

/**
 * {@link StandardHeaderBuilder}
 *
 */
public class StandardHeaderBuilder<PayloadBuilder>
   extends AbstractFieldSequenceBuilder<PayloadBuilder, HeaderBuilder<PayloadBuilder>>
   // AbstractDataFormatSpecificationBuilderWithParent<ContainerBuilder<PayloadBuilder>>
   implements HeaderBuilder<PayloadBuilder> {

   /**
    * Creates a new {@link StandardHeaderBuilder}.
    */
   public StandardHeaderBuilder(ContainerBuilder<PayloadBuilder> parentBuilder, String localId, String name,
      String description) {
      super(parentBuilder, localId, name, description, PhysicalDataBlockType.HEADER);
   }

   @Override
   public ContainerBuilder<PayloadBuilder> finishHeader() {
      return super.finish();
   }
}
