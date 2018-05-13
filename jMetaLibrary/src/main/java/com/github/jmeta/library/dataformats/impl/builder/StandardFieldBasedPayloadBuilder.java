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
import com.github.jmeta.library.dataformats.api.types.DataBlockId;
import com.github.jmeta.library.dataformats.api.types.PhysicalDataBlockType;

/**
 * {@link StandardFieldBasedPayloadBuilder}
 *
 */
public class StandardFieldBasedPayloadBuilder
   extends AbstractFieldSequenceBuilder<FieldBasedPayloadBuilder, FieldBasedPayloadBuilder>
   implements FieldBasedPayloadBuilder {

   /**
    * @see com.github.jmeta.library.dataformats.api.services.builder.DataBlockDescriptionModifier#withStaticLengthOf(long)
    */
   @Override
   public FieldBasedPayloadBuilder withStaticLengthOf(long staticByteLength) {
      setStaticLength(staticByteLength);
      return this;
   }

   /**
    * @see com.github.jmeta.library.dataformats.api.services.builder.DataBlockDescriptionModifier#withLengthOf(long,
    *      long)
    */
   @Override
   public FieldBasedPayloadBuilder withLengthOf(long minimumByteLength, long maximumByteLength) {
      setLength(minimumByteLength, maximumByteLength);
      return this;
   }

   /**
    * @see com.github.jmeta.library.dataformats.api.services.builder.DataBlockDescriptionModifier#withOccurrences(int,
    *      int)
    */
   @Override
   public FieldBasedPayloadBuilder withOccurrences(int minimumOccurrences, int maximumOccurrences) {
      setOccurrences(minimumOccurrences, maximumOccurrences);
      return this;
   }

   /**
    * @see com.github.jmeta.library.dataformats.api.services.builder.DataBlockDescriptionModifier#withOverriddenId(com.github.jmeta.library.dataformats.api.types.DataBlockId)
    */
   @Override
   public FieldBasedPayloadBuilder withOverriddenId(DataBlockId overriddenId) {
      setOverriddenId(overriddenId);
      return this;
   }

   /**
    * Creates a new {@link StandardFieldBasedPayloadBuilder}.
    */
   public StandardFieldBasedPayloadBuilder(ContainerBuilder<FieldBasedPayloadBuilder> parentBuilder, String localId,
      String name, String description) {
      super(parentBuilder, localId, name, description, PhysicalDataBlockType.FIELD_BASED_PAYLOAD);
   }

   @Override
   public ContainerBuilder<FieldBasedPayloadBuilder> finishFieldBasedPayload() {
      return super.finish();
   }

}
