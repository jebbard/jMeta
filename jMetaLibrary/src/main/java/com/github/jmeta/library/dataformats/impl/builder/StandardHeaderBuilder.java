/**
 *
 * {@link StandardHeaderBuilder}.java
 *
 * @author Jens Ebert
 *
 * @date 12.05.2018
 *
 */
package com.github.jmeta.library.dataformats.impl.builder;

import com.github.jmeta.library.dataformats.api.services.builder.ContainerBuilder;
import com.github.jmeta.library.dataformats.api.services.builder.HeaderBuilder;
import com.github.jmeta.library.dataformats.api.types.DataBlockId;
import com.github.jmeta.library.dataformats.api.types.PhysicalDataBlockType;

/**
 * {@link StandardHeaderBuilder}
 *
 */
public class StandardHeaderBuilder<PayloadBuilder>
   extends AbstractFieldSequenceBuilder<PayloadBuilder, HeaderBuilder<PayloadBuilder>>
   // AbstractDataFormatSpecificationBuilderWithParent<ContainerBuilder<PayloadBuilder>>
   implements HeaderBuilder<PayloadBuilder> {

   /**
    * Creates a new {@link StandardHeaderBuilder}.
    */
   public StandardHeaderBuilder(ContainerBuilder<PayloadBuilder> parentBuilder, String localId, String name,
      String description) {
      super(parentBuilder, localId, name, description, PhysicalDataBlockType.HEADER);
   }

   @Override
   public ContainerBuilder<PayloadBuilder> finishHeader() {
      return super.finish();
   }

   /**
    * @see com.github.jmeta.library.dataformats.api.services.builder.DataBlockDescriptionModifier#withStaticLengthOf(long)
    */
   @Override
   public HeaderBuilder<PayloadBuilder> withStaticLengthOf(long staticByteLength) {
      setStaticLength(staticByteLength);
      return this;
   }

   /**
    * @see com.github.jmeta.library.dataformats.api.services.builder.DataBlockDescriptionModifier#withLengthOf(long,
    *      long)
    */
   @Override
   public HeaderBuilder<PayloadBuilder> withLengthOf(long minimumByteLength, long maximumByteLength) {
      setLength(minimumByteLength, maximumByteLength);
      return this;
   }

   /**
    * @see com.github.jmeta.library.dataformats.api.services.builder.DataBlockDescriptionModifier#withOccurrences(int,
    *      int)
    */
   @Override
   public HeaderBuilder<PayloadBuilder> withOccurrences(int minimumOccurrences, int maximumOccurrences) {
      setOccurrences(minimumOccurrences, maximumOccurrences);
      return this;
   }

   /**
    * @see com.github.jmeta.library.dataformats.api.services.builder.DataBlockDescriptionModifier#withOverriddenId(com.github.jmeta.library.dataformats.api.types.DataBlockId)
    */
   @Override
   public HeaderBuilder<PayloadBuilder> withOverriddenId(DataBlockId overriddenId) {
      setOverriddenId(overriddenId);
      return this;
   }
}
