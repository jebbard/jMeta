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
import com.github.jmeta.library.dataformats.api.services.builder.DataFormatSpecificationBuilder;
import com.github.jmeta.library.dataformats.api.services.builder.EnumeratedFieldBuilder;
import com.github.jmeta.library.dataformats.api.services.builder.FieldSequenceBuilder;
import com.github.jmeta.library.dataformats.api.services.builder.FlagsFieldBuilder;
import com.github.jmeta.library.dataformats.api.services.builder.NumericFieldBuilder;
import com.github.jmeta.library.dataformats.api.services.builder.StringFieldBuilder;
import com.github.jmeta.library.dataformats.api.types.PhysicalDataBlockType;

/**
 * {@link AbstractFieldSequenceBuilder}
 *
 */
public abstract class AbstractFieldSequenceBuilder<PayloadBuilder, ConcreteFieldSequenceBuilder extends DataFormatSpecificationBuilder>
   extends AbstractDataFormatSpecificationBuilderWithParent<ContainerBuilder<PayloadBuilder>>
   implements FieldSequenceBuilder<ConcreteFieldSequenceBuilder> {

   /**
    * Creates a new {@link AbstractFieldSequenceBuilder}.
    */
   public AbstractFieldSequenceBuilder(ContainerBuilder<PayloadBuilder> parentBuilder, String localId, String name,
      String description, PhysicalDataBlockType type) {
      super(parentBuilder, localId, name, description, type);
   }

   @Override
   public StringFieldBuilder<ConcreteFieldSequenceBuilder> addStringField(String localId, String name,
      String description) {
      return new StandardStringFieldBuilder<>((ConcreteFieldSequenceBuilder) this, localId, name, description);
   }

   @Override
   public NumericFieldBuilder<ConcreteFieldSequenceBuilder> addNumericField(String localId, String name,
      String description) {
      return new StandardNumericFieldBuilder<>((ConcreteFieldSequenceBuilder) this, localId, name, description);
   }

   @Override
   public BinaryFieldBuilder<ConcreteFieldSequenceBuilder> addBinaryField(String localId, String name,
      String description) {
      return new StandardBinaryFieldBuilder<>((ConcreteFieldSequenceBuilder) this, localId, name, description);
   }

   @Override
   public FlagsFieldBuilder<ConcreteFieldSequenceBuilder> addFlagsField(String localId, String name,
      String description) {
      return new StandardFlagsFieldBuilder<>((ConcreteFieldSequenceBuilder) this, localId, name, description);
   }

   @Override
   public <FieldInterpretedType> EnumeratedFieldBuilder<ConcreteFieldSequenceBuilder, FieldInterpretedType> addEnumeratedField(
      Class<FieldInterpretedType> type, String localId, String name, String description) {
      return new StandardEnumeratedFieldBuilder<>((ConcreteFieldSequenceBuilder) this, localId, name, description);
   }
}
