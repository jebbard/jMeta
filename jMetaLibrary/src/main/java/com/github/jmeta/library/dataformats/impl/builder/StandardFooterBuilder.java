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

import com.github.jmeta.library.dataformats.api.services.builder.BinaryFieldBuilder;
import com.github.jmeta.library.dataformats.api.services.builder.ContainerBuilder;
import com.github.jmeta.library.dataformats.api.services.builder.EnumeratedFieldBuilder;
import com.github.jmeta.library.dataformats.api.services.builder.FlagsFieldBuilder;
import com.github.jmeta.library.dataformats.api.services.builder.FooterBuilder;
import com.github.jmeta.library.dataformats.api.services.builder.NumericFieldBuilder;
import com.github.jmeta.library.dataformats.api.services.builder.StringFieldBuilder;
import com.github.jmeta.library.dataformats.api.types.PhysicalDataBlockType;

/**
 * {@link StandardFooterBuilder}
 *
 */
public class StandardFooterBuilder<PayloadBuilder> extends
   AbstractDataFormatSpecificationBuilder<ContainerBuilder<PayloadBuilder>> implements FooterBuilder<PayloadBuilder> {

   /**
    * Creates a new {@link StandardFooterBuilder}.
    */
   public StandardFooterBuilder(ContainerBuilder<PayloadBuilder> parentBuilder, String localId, String name,
      String description) {
      super(parentBuilder, localId, name, description, PhysicalDataBlockType.FOOTER);
   }

   @Override
   public StringFieldBuilder<FooterBuilder<PayloadBuilder>> addStringField(String localId, String name,
      String description) {
      // TODO
      return null;
   }

   @Override
   public NumericFieldBuilder<FooterBuilder<PayloadBuilder>> addNumericField(String localId, String name,
      String description) {
      // TODO
      return null;
   }

   @Override
   public BinaryFieldBuilder<FooterBuilder<PayloadBuilder>> addBinaryField(String localId, String name,
      String description) {
      // TODO
      return null;
   }

   @Override
   public FlagsFieldBuilder<FooterBuilder<PayloadBuilder>> addFlagsField(String localId, String name,
      String description) {
      // TODO
      return null;
   }

   @Override
   public <FieldInterpretedType> EnumeratedFieldBuilder<FooterBuilder<PayloadBuilder>, FieldInterpretedType> addEnumeratedField(
      Class<FieldInterpretedType> type, String localId, String name, String description) {
      // TODO
      return null;
   }

   @Override
   public ContainerBuilder<PayloadBuilder> finishFooter() {
      return super.finish();
   }
}
