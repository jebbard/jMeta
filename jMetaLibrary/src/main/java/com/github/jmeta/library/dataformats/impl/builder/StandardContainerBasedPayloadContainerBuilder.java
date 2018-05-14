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
import com.github.jmeta.library.dataformats.api.types.DataBlockId;
import com.github.jmeta.library.dataformats.api.types.PhysicalDataBlockType;

/**
 * {@link StandardContainerBasedPayloadContainerBuilder}
 *
 */
public class StandardContainerBasedPayloadContainerBuilder
   extends AbstractDataFormatSpecificationBuilderWithParent<ContainerBasedPayloadBuilder>
   implements ContainerBuilder<ContainerBasedPayloadBuilder> {

   /**
    * @see com.github.jmeta.library.dataformats.api.services.builder.DataBlockDescriptionModifier#withStaticLengthOf(long)
    */
   @Override
   public ContainerBuilder<ContainerBasedPayloadBuilder> withStaticLengthOf(long staticByteLength) {
      setStaticLength(staticByteLength);
      return this;
   }

   /**
    * @see com.github.jmeta.library.dataformats.api.services.builder.DataBlockDescriptionModifier#withLengthOf(long,
    *      long)
    */
   @Override
   public ContainerBuilder<ContainerBasedPayloadBuilder> withLengthOf(long minimumByteLength, long maximumByteLength) {
      setLength(minimumByteLength, maximumByteLength);
      return this;
   }

   /**
    * @see com.github.jmeta.library.dataformats.api.services.builder.DataBlockDescriptionModifier#withOccurrences(int,
    *      int)
    */
   @Override
   public ContainerBuilder<ContainerBasedPayloadBuilder> withOccurrences(int minimumOccurrences,
      int maximumOccurrences) {
      setOccurrences(minimumOccurrences, maximumOccurrences);
      return this;
   }

   /**
    * @see com.github.jmeta.library.dataformats.api.services.builder.DataBlockDescriptionModifier#withOverriddenId(com.github.jmeta.library.dataformats.api.types.DataBlockId)
    */
   @Override
   public ContainerBuilder<ContainerBasedPayloadBuilder> withOverriddenId(DataBlockId overriddenId) {
      setOverriddenId(overriddenId);
      return this;
   }

   private final ContainerBasedPayloadBuilder payloadBuilder = new StandardContainerBasedPayloadBuilder(this, "payload",
      "payload", "The payload");

   /**
    * Creates a new {@link StandardContainerBasedPayloadContainerBuilder}.
    */
   public StandardContainerBasedPayloadContainerBuilder(ContainerBasedPayloadBuilder parentBuilder, String localId,
      String name, String description) {
      super(parentBuilder, localId, name, description, PhysicalDataBlockType.CONTAINER);
   }

   /**
    * Creates a new {@link StandardContainerBasedPayloadContainerBuilder}.
    */
   public StandardContainerBasedPayloadContainerBuilder(DescriptionCollector collector, ContainerDataFormat dataFormat,
      String localId, String name, String description) {
      super(collector, dataFormat, localId, name, description, PhysicalDataBlockType.CONTAINER);
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
