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
import com.github.jmeta.library.dataformats.api.services.builder.FieldBasedPayloadBuilder;
import com.github.jmeta.library.dataformats.api.services.builder.FooterBuilder;
import com.github.jmeta.library.dataformats.api.services.builder.HeaderBuilder;
import com.github.jmeta.library.dataformats.api.types.ContainerDataFormat;
import com.github.jmeta.library.dataformats.api.types.PhysicalDataBlockType;

/**
 * {@link StandardFieldBasedPayloadContainerBuilder}
 *
 */
public class StandardFieldBasedPayloadContainerBuilder extends
   AbstractDataFormatSpecificationBuilder<ContainerBasedPayloadBuilder, ContainerBuilder<FieldBasedPayloadBuilder>>
   implements ContainerBuilder<FieldBasedPayloadBuilder> {

   private final FieldBasedPayloadBuilder payloadBuilder;

   /**
    * Creates a new {@link StandardFieldBasedPayloadContainerBuilder}.
    * 
    * @param isGeneric
    *           TODO
    */
   public StandardFieldBasedPayloadContainerBuilder(ContainerBasedPayloadBuilder parentBuilder, String localId,
      String name, String description, boolean isGeneric) {
      super(parentBuilder, localId, name, description, PhysicalDataBlockType.CONTAINER, isGeneric);

      payloadBuilder = new StandardFieldBasedPayloadBuilder(this, "payload", "payload", "The payload", isGeneric());
   }

   /**
    * Creates a new {@link StandardFieldBasedPayloadContainerBuilder}.
    * 
    * @param isGeneric
    *           TODO
    */
   public StandardFieldBasedPayloadContainerBuilder(DescriptionCollector collector, ContainerDataFormat dataFormat,
      String localId, String name, String description, boolean isGeneric) {
      super(null, collector, dataFormat, localId, name, description, PhysicalDataBlockType.CONTAINER, isGeneric);

      payloadBuilder = new StandardFieldBasedPayloadBuilder(this, "payload", "payload", "The payload", isGeneric());
   }

   @Override
   public FieldBasedPayloadBuilder getPayload() {
      return payloadBuilder;
   }

   @Override
   public HeaderBuilder<FieldBasedPayloadBuilder> addHeader(String localId, String name, String description) {
      return new StandardHeaderBuilder<>(this, localId, name, description, isGeneric());
   }

   @Override
   public FooterBuilder<FieldBasedPayloadBuilder> addFooter(String localId, String name, String description) {
      return new StandardFooterBuilder<>(this, localId, name, description, isGeneric());
   }

   @Override
   public ContainerBasedPayloadBuilder finishContainer() {
      return super.finish();
   }
}
