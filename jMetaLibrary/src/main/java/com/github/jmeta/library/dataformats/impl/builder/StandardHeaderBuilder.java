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
 * {@link StandardHeaderBuilder}
 *
 */
public class StandardHeaderBuilder<P extends ContainerSequenceBuilder<P>, PB>
   extends AbstractFieldSequenceBuilder<ContainerBuilder<P, PB>, HeaderBuilder<ContainerBuilder<P, PB>>>
   implements HeaderBuilder<ContainerBuilder<P, PB>> {

   /**
    * Creates a new {@link StandardHeaderBuilder}.
    * 
    * @param isGeneric
    *           TODO
    */
   public StandardHeaderBuilder(ContainerBuilder<P, PB> parentBuilder, String localId, String name, String description,
      boolean isGeneric) {
      super(parentBuilder, localId, name, description, PhysicalDataBlockType.HEADER, isGeneric);
   }

   @Override
   public ContainerBuilder<P, PB> finishHeader() {
      return super.finish();
   }
}
