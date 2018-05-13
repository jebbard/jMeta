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
public interface StringFieldBuilder<ParentBuilder> extends FieldBuilder<ParentBuilder, String> {

   void withTerminationCharacter(Character terminationChar);

   void withFixedCharset(Charset charset);

}
