/**
 *
 * {@link StringFieldBuilder}.java
 *
 * @author Jens Ebert
 *
 * @date 12.05.2018
 *
 */
package com.github.jmeta.library.dataformats.api.services.builder;

import java.nio.charset.Charset;

/**
 * {@link StringFieldBuilder}
 *
 */
public interface StringFieldBuilder<ParentBuilder> extends FieldBuilder<ParentBuilder, String>,
   FieldDescriptionModifier<ParentBuilder, String, StringFieldBuilder<ParentBuilder>>,
   DataBlockDescriptionModifier<StringFieldBuilder<ParentBuilder>> {

   StringFieldBuilder<ParentBuilder> withTerminationCharacter(Character terminationChar);

   StringFieldBuilder<ParentBuilder> withFixedCharset(Charset charset);

}
