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

import java.nio.ByteOrder;

import com.github.jmeta.library.dataformats.api.services.builder.DataFormatSpecificationBuilder;
import com.github.jmeta.library.dataformats.api.services.builder.NumericFieldBuilder;
import com.github.jmeta.library.dataformats.api.types.DataBlockId;
import com.github.jmeta.library.dataformats.api.types.FieldType;

/**
 * {@link StandardNumericFieldBuilder}
 *
 */
public class StandardNumericFieldBuilder<ParentBuilder extends DataFormatSpecificationBuilder>
   extends AbstractFieldBuilder<ParentBuilder, Long> implements NumericFieldBuilder<ParentBuilder> {

   /**
    * @see com.github.jmeta.library.dataformats.api.services.builder.NumericFieldBuilder#withDefaultValue(java.lang.Long)
    */
   @Override
   public NumericFieldBuilder<ParentBuilder> withDefaultValue(Long value) {
      setDefaultValue(value);
      return this;
   }

   /**
    * @see com.github.jmeta.library.dataformats.impl.builder.AbstractFieldBuilder#asMagicKey()
    */
   @Override
   public NumericFieldBuilder<ParentBuilder> asMagicKey() {
      setAsMagicKey();
      return this;
   }

   /**
    * Creates a new {@link StandardNumericFieldBuilder}.
    * 
    * @param parentBuilder
    * @param localId
    * @param name
    * @param description
    * @param fieldType
    */
   public StandardNumericFieldBuilder(ParentBuilder parentBuilder, String localId, String name, String description) {
      super(parentBuilder, localId, name, description, FieldType.UNSIGNED_WHOLE_NUMBER);
   }

   /**
    * @see com.github.jmeta.library.dataformats.api.services.builder.NumericFieldBuilder#withFixedByteOrder(java.nio.ByteOrder)
    */
   @Override
   public NumericFieldBuilder<ParentBuilder> withFixedByteOrder(ByteOrder byteOrder) {
      setFixedByteOrder(byteOrder);
      return this;
   }

   /**
    * @see com.github.jmeta.library.dataformats.api.services.builder.DataBlockDescriptionModifier#withOverriddenId(com.github.jmeta.library.dataformats.api.types.DataBlockId)
    */
   @Override
   public NumericFieldBuilder<ParentBuilder> withOverriddenId(DataBlockId overriddenId) {
      setOverriddenId(overriddenId);
      return this;
   }

   /**
    * @see com.github.jmeta.library.dataformats.api.services.builder.DataBlockDescriptionModifier#withStaticLengthOf(long)
    */
   @Override
   public NumericFieldBuilder<ParentBuilder> withStaticLengthOf(long staticByteLength) {
      setStaticLength(staticByteLength);
      return this;
   }

   /**
    * @see com.github.jmeta.library.dataformats.api.services.builder.DataBlockDescriptionModifier#withLengthOf(long,
    *      long)
    */
   @Override
   public NumericFieldBuilder<ParentBuilder> withLengthOf(long minimumByteLength, long maximumByteLength) {
      setLength(minimumByteLength, maximumByteLength);
      return this;
   }

   /**
    * @see com.github.jmeta.library.dataformats.api.services.builder.DataBlockDescriptionModifier#withOccurrences(int,
    *      int)
    */
   @Override
   public NumericFieldBuilder<ParentBuilder> withOccurrences(int minimumOccurrences, int maximumOccurrences) {
      setOccurrences(minimumOccurrences, maximumOccurrences);
      return this;
   }

}
