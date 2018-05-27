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

import com.github.jmeta.library.dataformats.api.services.builder.ContainerBasedPayloadBuilder;
import com.github.jmeta.library.dataformats.api.services.builder.ContainerBuilder;
import com.github.jmeta.library.dataformats.api.services.builder.ContainerSequenceBuilder;
import com.github.jmeta.library.dataformats.api.services.builder.FieldBasedPayloadBuilder;
import com.github.jmeta.library.dataformats.api.types.PhysicalDataBlockType;

/**
 * {@link StandardContainerBasedPayloadBuilder}
 *
 */
public class StandardContainerBasedPayloadBuilder<P extends ContainerSequenceBuilder<P>> extends
   AbstractDataFormatSpecificationBuilder<ContainerBuilder<P, ContainerBasedPayloadBuilder<P>>, ContainerBasedPayloadBuilder<P>>
   implements ContainerBasedPayloadBuilder<P> {

   /**
    * Creates a new {@link StandardContainerBasedPayloadBuilder}.
    */
   public StandardContainerBasedPayloadBuilder(ContainerBuilder<P, ContainerBasedPayloadBuilder<P>> parentBuilder,
      String localId, String name, String description) {
      super(parentBuilder, localId, name, description, PhysicalDataBlockType.CONTAINER_BASED_PAYLOAD, false);
   }

   @Override
   public ContainerBuilder<ContainerBasedPayloadBuilder<P>, FieldBasedPayloadBuilder<ContainerBasedPayloadBuilder<P>>> addContainerWithFieldBasedPayload(
      String localId, String name, String description) {
      return new StandardFieldBasedPayloadContainerBuilder<>(this, localId, name, description, false);
   }

   @Override
   public ContainerBuilder<ContainerBasedPayloadBuilder<P>, ContainerBasedPayloadBuilder<ContainerBasedPayloadBuilder<P>>> addContainerWithContainerBasedPayload(
      String localId, String name, String description) {
      return new StandardContainerBasedPayloadContainerBuilder<>(this, localId, name, description, false);
   }

   @Override
   public ContainerBuilder<ContainerBasedPayloadBuilder<P>, FieldBasedPayloadBuilder<ContainerBasedPayloadBuilder<P>>> addGenericContainerWithFieldBasedPayload(
      String localId, String name, String description) {
      return new StandardFieldBasedPayloadContainerBuilder<>(this, "${" + localId + "}", name, description, true);
   }

   @Override
   public ContainerBuilder<ContainerBasedPayloadBuilder<P>, ContainerBasedPayloadBuilder<ContainerBasedPayloadBuilder<P>>> addGenericContainerWithContainerBasedPayload(
      String localId, String name, String description) {
      return new StandardContainerBasedPayloadContainerBuilder<>(this, "${" + localId + "}", name, description, true);
   }

   /**
    * @see com.github.jmeta.library.dataformats.api.services.builder.ContainerBasedPayloadBuilder#finishContainerBasedPayload()
    */
   @Override
   public ContainerBuilder<P, ContainerBasedPayloadBuilder<P>> finishContainerBasedPayload() {
      return finish();
   }

}
