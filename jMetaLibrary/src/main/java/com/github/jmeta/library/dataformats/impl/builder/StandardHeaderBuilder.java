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
import com.github.jmeta.library.dataformats.api.services.builder.HeaderBuilder;
import com.github.jmeta.library.dataformats.api.services.builder.NumericFieldBuilder;
import com.github.jmeta.library.dataformats.api.services.builder.StringFieldBuilder;
import com.github.jmeta.library.dataformats.api.types.PhysicalDataBlockType;

/**
 * {@link StandardHeaderBuilder}
 *
 */
public class StandardHeaderBuilder<PayloadBuilder> extends
   AbstractDataFormatSpecificationBuilder<ContainerBuilder<PayloadBuilder>> implements HeaderBuilder<PayloadBuilder> {

   /**
    * Creates a new {@link StandardHeaderBuilder}.
    */
   public StandardHeaderBuilder(ContainerBuilder<PayloadBuilder> parentBuilder, String localId, String name,
      String description) {
      super(parentBuilder, localId, name, description, PhysicalDataBlockType.HEADER);
   }

   @Override
   public StringFieldBuilder<HeaderBuilder<PayloadBuilder>> addStringField(String localId, String name,
      String description) {
      // TODO
      return null;
   }

   @Override
   public NumericFieldBuilder<HeaderBuilder<PayloadBuilder>> addNumericField(String localId, String name,
      String description) {
      // TODO
      return null;
   }

   @Override
   public BinaryFieldBuilder<HeaderBuilder<PayloadBuilder>> addBinaryField(String localId, String name,
      String description) {
      // TODO
      return null;
   }

   @Override
   public FlagsFieldBuilder<HeaderBuilder<PayloadBuilder>> addFlagsField(String localId, String name,
      String description) {
      // TODO
      return null;
   }

   @Override
   public <FieldInterpretedType> EnumeratedFieldBuilder<HeaderBuilder<PayloadBuilder>, FieldInterpretedType> addEnumeratedField(
      Class<FieldInterpretedType> type, String localId, String name, String description) {
      // TODO
      return null;
   }

   @Override
   public ContainerBuilder<PayloadBuilder> finishHeader() {
      return super.finish();
   }
}
