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
import com.github.jmeta.library.dataformats.api.services.builder.FooterBuilder;
import com.github.jmeta.library.dataformats.api.services.builder.HeaderBuilder;
import com.github.jmeta.library.dataformats.api.services.builder.UNKNOWN_IFACE;
import com.github.jmeta.library.dataformats.api.types.PhysicalDataBlockType;

/**
 * {@link StandardContainerBasedPayloadContainerBuilder}
 *
 */
public class StandardContainerBasedPayloadContainerBuilder<P extends ContainerSequenceBuilder<P> & UNKNOWN_IFACE>
   extends AbstractDataFormatSpecificationBuilder<P, ContainerBuilder<P, ContainerBasedPayloadBuilder<P>>>
   implements ContainerBuilder<P, ContainerBasedPayloadBuilder<P>> {

   private final ContainerBasedPayloadBuilder<P> payloadBuilder = new StandardContainerBasedPayloadBuilder<>(this,
      "payload", "payload", "The payload");

   /**
    * Creates a new {@link StandardContainerBasedPayloadContainerBuilder}.
    * 
    * @param isGeneric
    *           TODO
    */
   public StandardContainerBasedPayloadContainerBuilder(P parentBuilder, String localId, String name,
      String description, boolean isGeneric) {
      super(parentBuilder, localId, name, description, PhysicalDataBlockType.CONTAINER, isGeneric);
   }

   @Override
   public ContainerBasedPayloadBuilder<P> getPayload() {
      return payloadBuilder;
   }

   @Override
   public HeaderBuilder<ContainerBuilder<P, ContainerBasedPayloadBuilder<P>>> addHeader(String localId, String name,
      String description) {
      return new StandardHeaderBuilder<>(this, localId, name, description, isGeneric());
   }

   @Override
   public FooterBuilder<ContainerBuilder<P, ContainerBasedPayloadBuilder<P>>> addFooter(String localId, String name,
      String description) {
      return new StandardFooterBuilder<>(this, localId, name, description, isGeneric());
   }

   @Override
   public P finishContainer() {
      return super.finish();
   }
}
