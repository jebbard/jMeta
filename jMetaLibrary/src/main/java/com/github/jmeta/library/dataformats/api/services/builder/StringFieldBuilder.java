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
public interface StringFieldBuilder<P>
   extends FieldBuilder<P, String, StringFieldBuilder<P>> {

   StringFieldBuilder<P> withTerminationCharacter(Character terminationChar);

   StringFieldBuilder<P> withFixedCharset(Charset charset);

}
