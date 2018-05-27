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
import com.github.jmeta.library.dataformats.api.services.builder.ContainerSequenceBuilder;
import com.github.jmeta.library.dataformats.api.services.builder.HeaderBuilder;
import com.github.jmeta.library.dataformats.api.types.PhysicalDataBlockType;

/**
 * {@link StandardHeaderBuilder} allows to build a header description.
 *
 * @param <P>
 *           The parent type of this builder
 * @param <PB>
 *           The payload type of the parent container
 */
public class StandardHeaderBuilder<P extends ContainerSequenceBuilder<P>, PB>
   extends AbstractFieldSequenceBuilder<ContainerBuilder<P, PB>, HeaderBuilder<ContainerBuilder<P, PB>>>
   implements HeaderBuilder<ContainerBuilder<P, PB>> {

   /**
    * @see AbstractFieldSequenceBuilder#AbstractFieldSequenceBuilder(com.github.jmeta.library.dataformats.api.services.builder.DataBlockDescriptionBuilder,
    *      String, String, String, PhysicalDataBlockType, boolean)
    */
   public StandardHeaderBuilder(ContainerBuilder<P, PB> parentBuilder, String localId, String name, String description,
      boolean isGeneric) {
      super(parentBuilder, localId, name, description, PhysicalDataBlockType.HEADER, isGeneric);
   }

   /**
    * @see com.github.jmeta.library.dataformats.api.services.builder.HeaderBuilder#finishHeader()
    */
   @Override
   public ContainerBuilder<P, PB> finishHeader() {
      return super.finish();
   }
}
