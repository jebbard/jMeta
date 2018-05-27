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
import com.github.jmeta.library.dataformats.api.services.builder.FooterBuilder;
import com.github.jmeta.library.dataformats.api.services.builder.HeaderBuilder;
import com.github.jmeta.library.dataformats.api.services.builder.UNKNOWN_IFACE;
import com.github.jmeta.library.dataformats.api.types.PhysicalDataBlockType;

/**
 * {@link StandardFieldBasedPayloadContainerBuilder}
 *
 */
public class StandardFieldBasedPayloadContainerBuilder<P extends ContainerSequenceBuilder<P> & UNKNOWN_IFACE>
   extends AbstractDataFormatSpecificationBuilder<P, ContainerBuilder<P, FieldBasedPayloadBuilder<P>>>
   implements ContainerBuilder<P, FieldBasedPayloadBuilder<P>> {

   private final FieldBasedPayloadBuilder<P> payloadBuilder;

   /**
    * Creates a new {@link StandardFieldBasedPayloadContainerBuilder}.
    * 
    * @param isGeneric
    *           TODO
    */
   public StandardFieldBasedPayloadContainerBuilder(P parentBuilder, String localId, String name, String description,
      boolean isGeneric) {
      super(parentBuilder, localId, name, description, PhysicalDataBlockType.CONTAINER, isGeneric);

      payloadBuilder = new StandardFieldBasedPayloadBuilder<>(this, "payload", "payload", "The payload", isGeneric());
   }

   @Override
   public FieldBasedPayloadBuilder<P> getPayload() {
      return payloadBuilder;
   }

   @Override
   public HeaderBuilder<ContainerBuilder<P, FieldBasedPayloadBuilder<P>>> addHeader(String localId, String name,
      String description) {
      return new StandardHeaderBuilder<>(this, localId, name, description, isGeneric());
   }

   @Override
   public FooterBuilder<ContainerBuilder<P, FieldBasedPayloadBuilder<P>>> addFooter(String localId, String name,
      String description) {
      return new StandardFooterBuilder<>(this, localId, name, description, isGeneric());
   }

   @Override
   public P finishContainer() {
      return super.finish();
   }
}
