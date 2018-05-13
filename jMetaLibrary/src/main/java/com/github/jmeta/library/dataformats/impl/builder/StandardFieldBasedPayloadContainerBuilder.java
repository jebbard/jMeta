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
import com.github.jmeta.library.dataformats.api.services.builder.FieldBasedPayloadBuilder;
import com.github.jmeta.library.dataformats.api.services.builder.FooterBuilder;
import com.github.jmeta.library.dataformats.api.services.builder.HeaderBuilder;
import com.github.jmeta.library.dataformats.api.types.DataBlockId;
import com.github.jmeta.library.dataformats.api.types.PhysicalDataBlockType;

/**
 * {@link StandardFieldBasedPayloadContainerBuilder}
 *
 */
public class StandardFieldBasedPayloadContainerBuilder
   extends AbstractDataFormatSpecificationBuilderWithParent<ContainerBasedPayloadBuilder>
   implements ContainerBuilder<FieldBasedPayloadBuilder> {

   private final FieldBasedPayloadBuilder payloadBuilder = null;

   /**
    * @see com.github.jmeta.library.dataformats.api.services.builder.DataBlockDescriptionModifier#withStaticLengthOf(long)
    */
   @Override
   public ContainerBuilder<FieldBasedPayloadBuilder> withStaticLengthOf(long staticByteLength) {
      setStaticLength(staticByteLength);
      return this;
   }

   /**
    * @see com.github.jmeta.library.dataformats.api.services.builder.DataBlockDescriptionModifier#withLengthOf(long,
    *      long)
    */
   @Override
   public ContainerBuilder<FieldBasedPayloadBuilder> withLengthOf(long minimumByteLength, long maximumByteLength) {
      setLength(minimumByteLength, maximumByteLength);
      return this;
   }

   /**
    * @see com.github.jmeta.library.dataformats.api.services.builder.DataBlockDescriptionModifier#withOccurrences(int,
    *      int)
    */
   @Override
   public ContainerBuilder<FieldBasedPayloadBuilder> withOccurrences(int minimumOccurrences, int maximumOccurrences) {
      setOccurrences(minimumOccurrences, maximumOccurrences);
      return this;
   }

   /**
    * @see com.github.jmeta.library.dataformats.api.services.builder.DataBlockDescriptionModifier#withOverriddenId(com.github.jmeta.library.dataformats.api.types.DataBlockId)
    */
   @Override
   public ContainerBuilder<FieldBasedPayloadBuilder> withOverriddenId(DataBlockId overriddenId) {
      setOverriddenId(overriddenId);
      return this;
   }

   /**
    * Creates a new {@link StandardFieldBasedPayloadContainerBuilder}.
    */
   public StandardFieldBasedPayloadContainerBuilder(ContainerBasedPayloadBuilder parentBuilder, String localId,
      String name, String description) {
      super(parentBuilder, localId, name, description, PhysicalDataBlockType.CONTAINER);
   }

   @Override
   public FieldBasedPayloadBuilder getPayload() {
      return payloadBuilder;
   }

   @Override
   public HeaderBuilder<FieldBasedPayloadBuilder> addHeader(String localId, String name, String description) {
      return new StandardHeaderBuilder<>(this, localId, name, description);
   }

   @Override
   public FooterBuilder<FieldBasedPayloadBuilder> addFooter(String localId, String name, String description) {
      return new StandardFooterBuilder<>(this, localId, name, description);
   }

   @Override
   public ContainerBasedPayloadBuilder finishContainer() {
      return super.finish();
   }
}
