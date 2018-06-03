/**
 *
 * {@link FieldBuilder}.java
 *
 * @author Jens Ebert
 *
 * @date 12.05.2018
 *
 */
package com.github.jmeta.library.dataformats.api.services.builder;

import com.github.jmeta.library.dataformats.api.types.DataBlockId;

/**
 * {@link FieldBuilder}
 *
 */
public interface FieldBuilder<P, FIT, C extends FieldBuilder<P, FIT, C>> extends DataBlockDescriptionBuilder<C> {

   C withDefaultValue(FIT value);

   C asMagicKey();

   C asMagicKeyWithOddBitLength(int bitLength);

   C asIdOf(DataBlockId... ids);

   C indicatesPresenceOf(String withFlagName, int withFlagValue, DataBlockId... ids);

   C asSizeOf(DataBlockId... ids);

   C asCountOf(DataBlockId... ids);

   C asByteOrderOf(DataBlockId... ids);

   C asCharacterEncodingOf(DataBlockId... ids);

   C addEnumeratedValue(byte[] binaryValue, FIT interpretedValue);

   P finishField();
}
