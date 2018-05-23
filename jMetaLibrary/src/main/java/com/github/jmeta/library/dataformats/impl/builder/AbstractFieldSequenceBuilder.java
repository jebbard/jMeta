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
import com.github.jmeta.library.dataformats.api.services.builder.DataBlockDescriptionBuilder;
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
public abstract class AbstractFieldSequenceBuilder<P extends DataBlockDescriptionBuilder<P>, C extends DataBlockDescriptionBuilder<C>>
   extends AbstractDataFormatSpecificationBuilder<P, C> implements FieldSequenceBuilder<C> {

   /**
    * Creates a new {@link AbstractFieldSequenceBuilder}.
    * 
    * @param isGeneric
    *           TODO
    */
   public AbstractFieldSequenceBuilder(P parentBuilder, String localId, String name, String description,
      PhysicalDataBlockType type, boolean isGeneric) {
      super(parentBuilder, localId, name, description, type, isGeneric);
   }

   @Override
   public StringFieldBuilder<C> addStringField(String localId, String name, String description) {
      return new StandardStringFieldBuilder<>((C) this, localId, name, description, isGeneric());
   }

   @Override
   public NumericFieldBuilder<C> addNumericField(String localId, String name, String description) {
      return new StandardNumericFieldBuilder<>((C) this, localId, name, description, isGeneric());
   }

   @Override
   public BinaryFieldBuilder<C> addBinaryField(String localId, String name, String description) {
      return new StandardBinaryFieldBuilder<>((C) this, localId, name, description, isGeneric());
   }

   @Override
   public FlagsFieldBuilder<C> addFlagsField(String localId, String name, String description) {
      return new StandardFlagsFieldBuilder<>((C) this, localId, name, description, isGeneric());
   }

   @Override
   public <FIT> EnumeratedFieldBuilder<C, FIT> addEnumeratedField(Class<FIT> type, String localId, String name,
      String description) {
      return new StandardEnumeratedFieldBuilder<>((C) this, localId, name, description, isGeneric());
   }
}
