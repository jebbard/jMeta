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
import com.github.jmeta.library.dataformats.api.services.builder.FieldBasedPayloadBuilder;
import com.github.jmeta.library.dataformats.api.types.PhysicalDataBlockType;

/**
 * {@link StandardFieldBasedPayloadBuilder}
 *
 */
public class StandardFieldBasedPayloadBuilder
   extends AbstractFieldSequenceBuilder<FieldBasedPayloadBuilder, FieldBasedPayloadBuilder>
   implements FieldBasedPayloadBuilder {

   /**
    * Creates a new {@link StandardFieldBasedPayloadBuilder}.
    */
   public StandardFieldBasedPayloadBuilder(ContainerBuilder<FieldBasedPayloadBuilder> parentBuilder, String localId,
      String name, String description) {
      super(parentBuilder, localId, name, description, PhysicalDataBlockType.FIELD_BASED_PAYLOAD);
   }

   @Override
   public ContainerBuilder<FieldBasedPayloadBuilder> finishFieldBasedPayload() {
      return finish();
   }

}
