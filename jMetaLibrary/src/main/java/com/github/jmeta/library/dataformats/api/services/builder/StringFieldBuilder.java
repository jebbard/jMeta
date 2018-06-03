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

import com.github.jmeta.library.dataformats.api.types.FieldType;

/**
 * {@link StringFieldBuilder} allows to set properties of field data blocks with {@link FieldType#STRING}.
 *
 * @param <P>
 *           The concrete parent builder interface
 */
public interface StringFieldBuilder<P> extends FieldBuilder<P, String, StringFieldBuilder<P>> {

   /**
    * Sets the termination character this field uses.
    * 
    * @param terminationChar
    *           The termination character to use or null
    * @return This builder
    */
   StringFieldBuilder<P> withTerminationCharacter(Character terminationChar);

   /**
    * Sets the termination character this field uses.
    * 
    * @param terminationChar
    *           The termination character to use or null
    * @return This builder
    */
   StringFieldBuilder<P> withFixedCharset(Charset charset);

}
