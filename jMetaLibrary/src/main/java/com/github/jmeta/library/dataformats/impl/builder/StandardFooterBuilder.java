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
import com.github.jmeta.library.dataformats.api.services.builder.FooterBuilder;
import com.github.jmeta.library.dataformats.api.types.PhysicalDataBlockType;

/**
 * {@link StandardFooterBuilder}
 *
 */
public class StandardFooterBuilder<PayloadBuilder>
   extends AbstractFieldSequenceBuilder<PayloadBuilder, FooterBuilder<PayloadBuilder>>
   implements FooterBuilder<PayloadBuilder> {

   /**
    * Creates a new {@link StandardFooterBuilder}.
    */
   public StandardFooterBuilder(ContainerBuilder<PayloadBuilder> parentBuilder, String localId, String name,
      String description) {
      super(parentBuilder, localId, name, description, PhysicalDataBlockType.FOOTER);
   }

   @Override
   public ContainerBuilder<PayloadBuilder> finishFooter() {
      return super.finish();
   }
}
