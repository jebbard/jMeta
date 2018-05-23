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

import java.nio.ByteOrder;

import com.github.jmeta.library.dataformats.api.types.Flags;

/**
 * {@link FlagsFieldBuilder}
 *
 */
public interface FlagsFieldBuilder<P>
   extends FieldBuilder<P, Flags, FlagsFieldBuilder<P>> {

   FlagSpecificationBuilder<P> withFlagSpecification(int byteLength, ByteOrder byteOrder);
}
