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
import com.github.jmeta.library.dataformats.api.services.builder.FooterBuilder;
import com.github.jmeta.library.dataformats.api.types.DataBlockId;
import com.github.jmeta.library.dataformats.api.types.PhysicalDataBlockType;

/**
 * {@link StandardFooterBuilder}
 *
 */
public class StandardFooterBuilder<PayloadBuilder>
   extends AbstractFieldSequenceBuilder<PayloadBuilder, FooterBuilder<PayloadBuilder>>
   implements FooterBuilder<PayloadBuilder> {

   /**
    * @see com.github.jmeta.library.dataformats.api.services.builder.DataBlockDescriptionModifier#withStaticLengthOf(long)
    */
   @Override
   public FooterBuilder<PayloadBuilder> withStaticLengthOf(long staticByteLength) {
      setStaticLength(staticByteLength);
      return this;
   }

   /**
    * @see com.github.jmeta.library.dataformats.api.services.builder.DataBlockDescriptionModifier#withLengthOf(long,
    *      long)
    */
   @Override
   public FooterBuilder<PayloadBuilder> withLengthOf(long minimumByteLength, long maximumByteLength) {
      setLength(minimumByteLength, maximumByteLength);
      return this;
   }

   /**
    * @see com.github.jmeta.library.dataformats.api.services.builder.DataBlockDescriptionModifier#withOccurrences(int,
    *      int)
    */
   @Override
   public FooterBuilder<PayloadBuilder> withOccurrences(int minimumOccurrences, int maximumOccurrences) {
      setOccurrences(minimumOccurrences, maximumOccurrences);
      return this;
   }

   /**
    * @see com.github.jmeta.library.dataformats.api.services.builder.DataBlockDescriptionModifier#withOverriddenId(com.github.jmeta.library.dataformats.api.types.DataBlockId)
    */
   @Override
   public FooterBuilder<PayloadBuilder> withOverriddenId(DataBlockId overriddenId) {
      setOverriddenId(overriddenId);
      return this;
   }

   /**
    * Creates a new {@link StandardFooterBuilder}.
    */
   public StandardFooterBuilder(ContainerBuilder<PayloadBuilder> parentBuilder, String localId, String name,
      String description) {
      super(parentBuilder, localId, name, description, PhysicalDataBlockType.FOOTER);
   }

   @Override
   public ContainerBuilder<PayloadBuilder> finishFooter() {
      return super.finish();
   }
}
