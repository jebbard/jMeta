/**
 *
 * {@link StandardStringFieldBuilder}.java
 *
 * @author Jens Ebert
 *
 * @date 13.05.2018
 *
 */
package com.github.jmeta.library.dataformats.impl.builder;

import com.github.jmeta.library.dataformats.api.services.builder.DataFormatSpecificationBuilder;
import com.github.jmeta.library.dataformats.api.services.builder.FlagsFieldBuilder;
import com.github.jmeta.library.dataformats.api.types.DataBlockId;
import com.github.jmeta.library.dataformats.api.types.FieldFunction;
import com.github.jmeta.library.dataformats.api.types.FieldType;
import com.github.jmeta.library.dataformats.api.types.FlagSpecification;
import com.github.jmeta.library.dataformats.api.types.Flags;

/**
 * {@link StandardFlagsFieldBuilder}
 *
 */
public class StandardFlagsFieldBuilder<ParentBuilder extends DataFormatSpecificationBuilder>
   extends AbstractFieldBuilder<ParentBuilder, Flags> implements FlagsFieldBuilder<ParentBuilder> {

   @Override
   public FlagsFieldBuilder<ParentBuilder> withFieldFunction(FieldFunction function) {
      getFunctions().add(function);
      return this;
   }

   /**
    * @see com.github.jmeta.library.dataformats.api.services.builder.FieldDescriptionModifier#asMagicKey()
    */
   @Override
   public FlagsFieldBuilder<ParentBuilder> asMagicKey() {
      setAsMagicKey();
      return this;
   }

   /**
    * @see com.github.jmeta.library.dataformats.api.services.builder.FlagsFieldBuilder#withDefaultValue(com.github.jmeta.library.dataformats.api.types.Flags)
    */
   @Override
   public FlagsFieldBuilder<ParentBuilder> withDefaultValue(Flags value) {
      setDefaultValue(value);
      return this;
   }

   /**
    * Creates a new {@link StandardFlagsFieldBuilder}.
    * 
    * @param parentBuilder
    * @param localId
    * @param name
    * @param description
    * @param fieldType
    */
   public StandardFlagsFieldBuilder(ParentBuilder parentBuilder, String localId, String name, String description) {
      super(parentBuilder, localId, name, description, FieldType.FLAGS);
   }

   /**
    * @see com.github.jmeta.library.dataformats.api.services.builder.FlagsFieldBuilder#withFlagSpecification(com.github.jmeta.library.dataformats.api.types.FlagSpecification)
    */
   @Override
   public FlagsFieldBuilder<ParentBuilder> withFlagSpecification(FlagSpecification spec) {
      setFlagSpecification(spec);
      return this;
   }

   /**
    * @see com.github.jmeta.library.dataformats.api.services.builder.DataBlockDescriptionModifier#withOverriddenId(com.github.jmeta.library.dataformats.api.types.DataBlockId)
    */
   @Override
   public FlagsFieldBuilder<ParentBuilder> withOverriddenId(DataBlockId overriddenId) {
      setOverriddenId(overriddenId);
      return this;
   }

   /**
    * @see com.github.jmeta.library.dataformats.api.services.builder.DataBlockDescriptionModifier#withStaticLengthOf(long)
    */
   @Override
   public FlagsFieldBuilder<ParentBuilder> withStaticLengthOf(long staticByteLength) {
      setStaticLength(staticByteLength);
      return this;
   }

   /**
    * @see com.github.jmeta.library.dataformats.api.services.builder.DataBlockDescriptionModifier#withLengthOf(long,
    *      long)
    */
   @Override
   public FlagsFieldBuilder<ParentBuilder> withLengthOf(long minimumByteLength, long maximumByteLength) {
      setLength(minimumByteLength, maximumByteLength);
      return this;
   }

   /**
    * @see com.github.jmeta.library.dataformats.api.services.builder.DataBlockDescriptionModifier#withOccurrences(int,
    *      int)
    */
   @Override
   public FlagsFieldBuilder<ParentBuilder> withOccurrences(int minimumOccurrences, int maximumOccurrences) {
      setOccurrences(minimumOccurrences, maximumOccurrences);
      return this;
   }

}
