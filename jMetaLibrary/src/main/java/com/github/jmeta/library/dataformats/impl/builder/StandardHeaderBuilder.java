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
import com.github.jmeta.library.dataformats.api.types.PhysicalDataBlockType;

/**
 * {@link StandardHeaderBuilder}
 *
 */
public class StandardHeaderBuilder<PB> extends AbstractFieldSequenceBuilder<PB, HeaderBuilder<PB>>
   implements HeaderBuilder<PB> {

   /**
    * Creates a new {@link StandardHeaderBuilder}.
    * 
    * @param isGeneric
    *           TODO
    */
   public StandardHeaderBuilder(ContainerBuilder<PB> parentBuilder, String localId, String name, String description,
      boolean isGeneric) {
      super(parentBuilder, localId, name, description, PhysicalDataBlockType.HEADER, isGeneric);
   }

   @Override
   public ContainerBuilder<PB> finishHeader() {
      return super.finish();
   }
}
