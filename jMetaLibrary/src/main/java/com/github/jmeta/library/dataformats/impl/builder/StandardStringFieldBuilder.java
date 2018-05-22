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

import com.github.jmeta.library.dataformats.api.services.builder.DataBlockDescriptionBuilder;
import com.github.jmeta.library.dataformats.api.services.builder.StringFieldBuilder;
import com.github.jmeta.library.dataformats.api.types.FieldType;

/**
 * {@link StandardStringFieldBuilder}
 *
 */
public class StandardStringFieldBuilder<P extends DataBlockDescriptionBuilder<P>>
   extends AbstractFieldBuilder<P, StringFieldBuilder<P>, String> implements StringFieldBuilder<P> {

   /**
    * Creates a new {@link StandardStringFieldBuilder}.
    * 
    * @param parentBuilder
    * @param localId
    * @param name
    * @param description
    * @param isGeneric
    *           TODO
    * @param fieldType
    */
   public StandardStringFieldBuilder(P parentBuilder, String localId, String name, String description,
      boolean isGeneric) {
      super(parentBuilder, localId, name, description, FieldType.STRING, isGeneric);
   }

   /**
    * @see com.github.jmeta.library.dataformats.api.services.builder.StringFieldBuilder#withTerminationCharacter(java.lang.Character)
    */
   @Override
   public StringFieldBuilder<P> withTerminationCharacter(Character terminationChar) {
      setTerminationCharacter(terminationChar);
      return this;
   }

   /**
    * @see com.github.jmeta.library.dataformats.api.services.builder.StringFieldBuilder#withFixedCharset(java.nio.charset.Charset)
    */
   @Override
   public StringFieldBuilder<P> withFixedCharset(Charset charset) {
      setFixedCharset(charset);
      return this;
   }

}
