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
import com.github.jmeta.library.dataformats.api.services.builder.DescriptionCollector;
import com.github.jmeta.library.dataformats.api.services.builder.FooterBuilder;
import com.github.jmeta.library.dataformats.api.services.builder.HeaderBuilder;
import com.github.jmeta.library.dataformats.api.types.ContainerDataFormat;
import com.github.jmeta.library.dataformats.api.types.PhysicalDataBlockType;

/**
 * {@link StandardContainerBasedPayloadContainerBuilder}
 *
 */
public class StandardContainerBasedPayloadContainerBuilder extends
   AbstractDataFormatSpecificationBuilder<ContainerBasedPayloadBuilder, ContainerBuilder<ContainerBasedPayloadBuilder>>
   implements ContainerBuilder<ContainerBasedPayloadBuilder> {

   private final ContainerBasedPayloadBuilder payloadBuilder = new StandardContainerBasedPayloadBuilder(this, "payload",
      "payload", "The payload");

   /**
    * Creates a new {@link StandardContainerBasedPayloadContainerBuilder}.
    * 
    * @param isGeneric
    *           TODO
    */
   public StandardContainerBasedPayloadContainerBuilder(ContainerBasedPayloadBuilder parentBuilder, String localId,
      String name, String description, boolean isGeneric) {
      super(parentBuilder, localId, name, description, PhysicalDataBlockType.CONTAINER, isGeneric);
   }

   /**
    * Creates a new {@link StandardContainerBasedPayloadContainerBuilder}.
    * 
    * @param isGeneric
    *           TODO
    */
   public StandardContainerBasedPayloadContainerBuilder(DescriptionCollector collector, ContainerDataFormat dataFormat,
      String localId, String name, String description, boolean isGeneric) {
      super(null, collector, dataFormat, localId, name, description, PhysicalDataBlockType.CONTAINER, isGeneric);
   }

   @Override
   public ContainerBasedPayloadBuilder getPayload() {
      return payloadBuilder;
   }

   @Override
   public HeaderBuilder<ContainerBasedPayloadBuilder> addHeader(String localId, String name, String description) {
      return new StandardHeaderBuilder<>(this, localId, name, description, isGeneric());
   }

   @Override
   public FooterBuilder<ContainerBasedPayloadBuilder> addFooter(String localId, String name, String description) {
      return new StandardFooterBuilder<>(this, localId, name, description, isGeneric());
   }

   @Override
   public ContainerBasedPayloadBuilder finishContainer() {
      return super.finish();
   }
}
