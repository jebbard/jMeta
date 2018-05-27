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
import com.github.jmeta.library.dataformats.api.services.builder.FooterBuilder;
import com.github.jmeta.library.dataformats.api.types.PhysicalDataBlockType;

/**
 * {@link StandardFooterBuilder}
 *
 */
public class StandardFooterBuilder<P extends ContainerSequenceBuilder<P>, PB>
   extends AbstractFieldSequenceBuilder<ContainerBuilder<P, PB>, FooterBuilder<ContainerBuilder<P, PB>>>
   implements FooterBuilder<ContainerBuilder<P, PB>> {

   /**
    * Creates a new {@link StandardFooterBuilder}.
    * 
    * @param isGeneric
    *           TODO
    */
   public StandardFooterBuilder(ContainerBuilder<P, PB> parentBuilder, String localId, String name, String description,
      boolean isGeneric) {
      super(parentBuilder, localId, name, description, PhysicalDataBlockType.FOOTER, isGeneric);
   }

   @Override
   public ContainerBuilder<P, PB> finishFooter() {
      return super.finish();
   }
}
