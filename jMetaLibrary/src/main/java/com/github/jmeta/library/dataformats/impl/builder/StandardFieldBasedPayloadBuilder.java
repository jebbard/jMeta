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

import com.github.jmeta.library.dataformats.api.services.builder.BinaryFieldBuilder;
import com.github.jmeta.library.dataformats.api.services.builder.ContainerBuilder;
import com.github.jmeta.library.dataformats.api.services.builder.EnumeratedFieldBuilder;
import com.github.jmeta.library.dataformats.api.services.builder.FieldBasedPayloadBuilder;
import com.github.jmeta.library.dataformats.api.services.builder.FlagsFieldBuilder;
import com.github.jmeta.library.dataformats.api.services.builder.NumericFieldBuilder;
import com.github.jmeta.library.dataformats.api.services.builder.StringFieldBuilder;
import com.github.jmeta.library.dataformats.api.types.PhysicalDataBlockType;

/**
 * {@link StandardFieldBasedPayloadBuilder}
 *
 */
public class StandardFieldBasedPayloadBuilder
   extends AbstractDataFormatSpecificationBuilder<ContainerBuilder<FieldBasedPayloadBuilder>>
   implements FieldBasedPayloadBuilder {

   /**
    * Creates a new {@link StandardFieldBasedPayloadBuilder}.
    */
   public StandardFieldBasedPayloadBuilder(ContainerBuilder<FieldBasedPayloadBuilder> parentBuilder, String localId,
      String name, String description) {
      super(parentBuilder, localId, name, description, PhysicalDataBlockType.FIELD_BASED_PAYLOAD);
   }

   @Override
   public StringFieldBuilder<FieldBasedPayloadBuilder> addStringField(String localId, String name, String description) {
      return null;
   }

   @Override
   public NumericFieldBuilder<FieldBasedPayloadBuilder> addNumericField(String localId, String name,
      String description) {
      return null;
   }

   @Override
   public BinaryFieldBuilder<FieldBasedPayloadBuilder> addBinaryField(String localId, String name, String description) {
      return null;
   }

   @Override
   public FlagsFieldBuilder<FieldBasedPayloadBuilder> addFlagsField(String localId, String name, String description) {
      return null;
   }

   @Override
   public <FieldInterpretedType> EnumeratedFieldBuilder<FieldBasedPayloadBuilder, FieldInterpretedType> addEnumeratedField(
      Class<FieldInterpretedType> type, String localId, String name, String description) {
      return null;
   }

   @Override
   public ContainerBuilder<FieldBasedPayloadBuilder> finishFieldBasedPayload() {
      return super.finish();
   }

}
