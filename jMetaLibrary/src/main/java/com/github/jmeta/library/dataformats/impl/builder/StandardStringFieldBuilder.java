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

import java.nio.charset.Charset;

import com.github.jmeta.library.dataformats.api.services.builder.DataFormatSpecificationBuilder;
import com.github.jmeta.library.dataformats.api.services.builder.StringFieldBuilder;
import com.github.jmeta.library.dataformats.api.types.DataBlockId;
import com.github.jmeta.library.dataformats.api.types.FieldType;

/**
 * {@link StandardStringFieldBuilder}
 *
 */
public class StandardStringFieldBuilder<ParentBuilder extends DataFormatSpecificationBuilder>
   extends AbstractFieldBuilder<ParentBuilder, String> implements StringFieldBuilder<ParentBuilder> {

   /**
    * Creates a new {@link StandardStringFieldBuilder}.
    * 
    * @param parentBuilder
    * @param localId
    * @param name
    * @param description
    * @param fieldType
    */
   public StandardStringFieldBuilder(ParentBuilder parentBuilder, String localId, String name, String description) {
      super(parentBuilder, localId, name, description, FieldType.STRING);
   }

   /**
    * @see com.github.jmeta.library.dataformats.api.services.builder.StringFieldBuilder#withDefaultValue(java.lang.String)
    */
   @Override
   public StringFieldBuilder<ParentBuilder> withDefaultValue(String value) {
      setDefaultValue(value);
      return this;
   }

   /**
    * @see com.github.jmeta.library.dataformats.impl.builder.AbstractFieldBuilder#asMagicKey()
    */
   @Override
   public StringFieldBuilder<ParentBuilder> asMagicKey() {
      setAsMagicKey();
      return this;
   }

   /**
    * @see com.github.jmeta.library.dataformats.api.services.builder.StringFieldBuilder#withTerminationCharacter(java.lang.Character)
    */
   @Override
   public StringFieldBuilder<ParentBuilder> withTerminationCharacter(Character terminationChar) {
      setTerminationCharacter(terminationChar);
      return this;
   }

   /**
    * @see com.github.jmeta.library.dataformats.api.services.builder.StringFieldBuilder#withFixedCharset(java.nio.charset.Charset)
    */
   @Override
   public StringFieldBuilder<ParentBuilder> withFixedCharset(Charset charset) {
      setFixedCharset(charset);
      return this;
   }

   /**
    * @see com.github.jmeta.library.dataformats.api.services.builder.DataBlockDescriptionModifier#withOverriddenId(com.github.jmeta.library.dataformats.api.types.DataBlockId)
    */
   @Override
   public StringFieldBuilder<ParentBuilder> withOverriddenId(DataBlockId overriddenId) {
      setOverriddenId(overriddenId);
      return this;
   }

   /**
    * @see com.github.jmeta.library.dataformats.api.services.builder.DataBlockDescriptionModifier#withStaticLengthOf(long)
    */
   @Override
   public StringFieldBuilder<ParentBuilder> withStaticLengthOf(long staticByteLength) {
      setStaticLength(staticByteLength);
      return this;
   }

   /**
    * @see com.github.jmeta.library.dataformats.api.services.builder.DataBlockDescriptionModifier#withLengthOf(long,
    *      long)
    */
   @Override
   public StringFieldBuilder<ParentBuilder> withLengthOf(long minimumByteLength, long maximumByteLength) {
      setLength(minimumByteLength, maximumByteLength);
      return this;
   }

   /**
    * @see com.github.jmeta.library.dataformats.api.services.builder.DataBlockDescriptionModifier#withOccurrences(int,
    *      int)
    */
   @Override
   public StringFieldBuilder<ParentBuilder> withOccurrences(int minimumOccurrences, int maximumOccurrences) {
      setOccurrences(minimumOccurrences, maximumOccurrences);
      return this;
   }

}
