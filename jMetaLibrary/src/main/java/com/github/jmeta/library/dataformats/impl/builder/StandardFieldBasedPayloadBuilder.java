/**
 *
 * {@link StandardFieldBasedPayloadContainerBuilder}.java
 *
 * @author Jens Ebert
 *
 * @date 12.05.2018
 *
 */
package com.github.jmeta.library.dataformats.impl.builder;

import com.github.jmeta.library.dataformats.api.services.builder.ContainerBuilder;
import com.github.jmeta.library.dataformats.api.services.builder.ContainerSequenceBuilder;
import com.github.jmeta.library.dataformats.api.services.builder.FieldBasedPayloadBuilder;
import com.github.jmeta.library.dataformats.api.types.PhysicalDataBlockType;

/**
 * {@link StandardFieldBasedPayloadBuilder}
 *
 */
public class StandardFieldBasedPayloadBuilder<P extends ContainerSequenceBuilder<P>>
   extends AbstractFieldSequenceBuilder<ContainerBuilder<P, FieldBasedPayloadBuilder<P>>, FieldBasedPayloadBuilder<P>>
   implements FieldBasedPayloadBuilder<P> {

   /**
    * Creates a new {@link StandardFieldBasedPayloadBuilder}.
    * 
    * @param isGeneric
    *           TODO
    */
   public StandardFieldBasedPayloadBuilder(ContainerBuilder<P, FieldBasedPayloadBuilder<P>> parentBuilder,
      String localId, String name, String description, boolean isGeneric) {
      super(parentBuilder, localId, name, description, PhysicalDataBlockType.FIELD_BASED_PAYLOAD, isGeneric);
   }

   @Override
   public ContainerBuilder<P, FieldBasedPayloadBuilder<P>> finishFieldBasedPayload() {
      return finish();
   }

}
