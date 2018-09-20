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

import com.github.jmeta.library.dataformats.api.types.DataBlockDescription;
import com.github.jmeta.library.dataformats.api.types.FieldType;
import com.github.jmeta.library.dataformats.api.types.FlagSpecification;
import com.github.jmeta.library.dataformats.api.types.converter.FieldConverter;

/**
 * {@link FieldBuilder} allows to set properties of a field data block.
 *
 * @param <P>
 *           The concrete parent builder interface
 * @param <FIT>
 *           The field's interpreted type
 * @param <C>
 *           The concrete {@link FieldBuilder} interface derived from this interface
 */
public interface FieldBuilder<P, FIT, C extends FieldBuilder<P, FIT, C>>
   extends DataBlockDescriptionBuilder<C>, DynamicOccurrenceBuilder<C> {

   /**
    * Tags this field as representing a magic key. The field must be of type {@link FieldType#STRING},
    * {@link FieldType#BINARY} or {@link FieldType#FLAGS} and it must either have enumerated valuer or a non-null
    * default value with a fixed length.
    * 
    * @return This builder
    */
   C asMagicKey();

   /**
    * Tags this field as representing a magic key with an odd bit length. The field must be of type STRING, BINARY or
    * FLAGS and it must either have enumerated valuer or a non-null default value with a fixed length.
    * 
    * @param bitLength
    *           The bit length to set, must be strictly positive
    * @return This builder
    */
   C asMagicKeyWithOddBitLength(long bitLength);

   /**
    * Tags this field as representing the id of a target generic container data block. The field must be of
    * {@link FieldType#STRING}.
    * 
    * @param referencedBlock
    *           The target data block identified by its {@link DataBlockCrossReference}, must not be null
    * @return This builder
    */
   C asIdOf(DataBlockCrossReference referencedBlock);

   /**
    * Tags this field as indicating the presence of a target data block. The field must be of {@link FieldType#FLAGS}.
    * 
    * @param withFlagName
    *           The name of the flag indicating the presence of the target data block, must not be null and refer to an
    *           existing flag name of the underlying {@link FlagSpecification}
    * @param withFlagValue
    *           The value of the flag the indicates presence, any other value found during parsing indicates absence
    * @param referencedBlock
    *           The target data block identified by its {@link DataBlockCrossReference}, must not be null
    * @return This builder
    */
   C indicatesPresenceOf(String withFlagName, int withFlagValue, DataBlockCrossReference referencedBlock);

   /**
    * Tags this field as representing the size of a target data block. The field must be of
    * {@link FieldType#UNSIGNED_WHOLE_NUMBER}.
    * 
    * @param referencedBlock
    *           The target data blocks identified by their {@link DataBlockCrossReference}s, must not be null
    * @return This builder
    */
   C asSizeOf(DataBlockCrossReference... referencedBlocks);

   /**
    * Tags this field as representing the number of occurrences of a target data block. The field must be of
    * {@link FieldType#UNSIGNED_WHOLE_NUMBER}.
    * 
    * @param referencedBlock
    *           The target data block identified by its {@link DataBlockCrossReference}, must not be null
    * @return This builder
    */
   C asCountOf(DataBlockCrossReference referencedBlock);

   /**
    * Tags this field as representing the id of a target generic container data block. The field must be of
    * {@link FieldType#STRING}.
    * 
    * @param referencedBlock
    *           The target data block identified by its {@link DataBlockCrossReference}, must not be null
    * @return This builder
    */
   C asByteOrderOf(DataBlockCrossReference referencedBlock);

   /**
    * Tags this field as representing the id of a target generic container data block. The field must be of
    * {@link FieldType#STRING}.
    * 
    * @param referencedBlock
    *           The target data block identified by its {@link DataBlockCrossReference}, must not be null
    * @return This builder
    */
   C asCharacterEncodingOf(DataBlockCrossReference referencedBlock);

   /**
    * Adds an enumerated value to this field's data block description.
    * 
    * @param binaryValue
    *           The binary value to add, must be unique
    * @param interpretedValue
    *           The interpreted value to add, must be unique
    * @return This builder
    */
   C addEnumeratedValue(byte[] binaryValue, FIT interpretedValue);

   /**
    * Sets a custom {@link FieldConverter} to be used when converting binary values into interpreted values and vice
    * versa.
    * 
    * @param customConverter
    *           The custom converter, must not be null
    * @return This builder
    */
   C withCustomConverter(FieldConverter<FIT> customConverter);

   /**
    * Sets the default value of the built field, which is initially null
    * 
    * @param value
    *           The default value to set, may be null
    * @return This builder
    */
   C withDefaultValue(FIT value);

   /**
    * Assigns a dynamic length (min length not equal to max length) to the data block. If this method is not called, the
    * default lengths are {@link DataBlockDescription#getMinimumByteLength()} = {@link DataBlockDescription#UNDEFINED}
    * {@link DataBlockDescription#getMaximumByteLength()} = {@link DataBlockDescription#UNDEFINED}.
    * 
    * @param minimumByteLength
    *           The minimum byte length of the data block, must not be negative and must be smaller than or equal to the
    *           maximum byte length
    * @param maximumByteLength
    *           The maximum byte length of the data block, must not be negative and must be bigger than or equal to the
    *           minimum byte length
    * @return The concrete builder instance
    */
   C withLengthOf(long minimumByteLength, long maximumByteLength);

   /**
    * Assigns a static length (min length = max length) to the data block. If this method is not called, the default
    * lengths are {@link DataBlockDescription#getMinimumByteLength()} = {@link DataBlockDescription#UNDEFINED} and
    * {@link DataBlockDescription#getMaximumByteLength()} = {@link DataBlockDescription#UNDEFINED}.
    * 
    * @param staticByteLength
    *           The static length of the data block, must not be negative
    * @return The concrete builder instance
    */
   C withStaticLengthOf(long staticByteLength);

   /**
    * Finishes building the field
    * 
    * @return The parent builder
    */
   P finishField();
}
