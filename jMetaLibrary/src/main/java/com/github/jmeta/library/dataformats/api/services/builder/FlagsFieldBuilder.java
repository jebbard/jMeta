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

import com.github.jmeta.library.dataformats.api.types.FieldType;
import com.github.jmeta.library.dataformats.api.types.FlagSpecification;
import com.github.jmeta.library.dataformats.api.types.Flags;

/**
 * {@link FlagsFieldBuilder} allows to set properties of field data blocks with {@link FieldType#FLAGS}.
 *
 * @param <P>
 *           The concrete parent builder interface
 */
public interface FlagsFieldBuilder<P> extends FieldBuilder<P, Flags, FlagsFieldBuilder<P>> {

   /**
    * Sets the {@link FlagSpecification} to be used for this field.
    * 
    * @param byteLength
    *           The byte length of the flags, must be strictly positive
    * @param byteOrder
    *           The {@link ByteOrder} of the flags, must not be null
    * @return The {@link FlagSpecificationBuilder} to add flags
    */
   FlagSpecificationBuilder<P> withFlagSpecification(int byteLength, ByteOrder byteOrder);
}
