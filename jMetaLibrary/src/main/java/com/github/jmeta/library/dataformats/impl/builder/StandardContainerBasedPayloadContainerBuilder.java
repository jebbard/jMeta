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
import com.github.jmeta.library.dataformats.api.services.builder.FooterBuilder;
import com.github.jmeta.library.dataformats.api.services.builder.HeaderBuilder;
import com.github.jmeta.library.dataformats.api.types.PhysicalDataBlockType;

/**
 * {@link StandardContainerBasedPayloadContainerBuilder}
 *
 */
public class StandardContainerBasedPayloadContainerBuilder
   extends AbstractDataFormatSpecificationBuilder<ContainerBasedPayloadBuilder>
   implements ContainerBuilder<ContainerBasedPayloadBuilder> {

   private final ContainerBasedPayloadBuilder payloadBuilder = null;

   /**
    * Creates a new {@link StandardContainerBasedPayloadContainerBuilder}.
    */
   public StandardContainerBasedPayloadContainerBuilder(ContainerBasedPayloadBuilder parentBuilder, String localId,
      String name, String description) {
      super(parentBuilder, localId, name, description, PhysicalDataBlockType.CONTAINER);
   }

   @Override
   public ContainerBasedPayloadBuilder getPayload() {
      return payloadBuilder;
   }

   @Override
   public HeaderBuilder<ContainerBasedPayloadBuilder> addHeader(String localId, String name, String description) {
      return new StandardHeaderBuilder<>(this, localId, name, description);
   }

   @Override
   public FooterBuilder<ContainerBasedPayloadBuilder> addFooter(String localId, String name, String description) {
      return new StandardFooterBuilder<>(this, localId, name, description);
   }

   @Override
   public ContainerBasedPayloadBuilder finishContainer() {
      return super.finish();
   }
}
