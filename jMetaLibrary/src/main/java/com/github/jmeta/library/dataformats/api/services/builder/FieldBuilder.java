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
public interface FieldBuilder<P, FIT, C extends FieldBuilder<P, FIT, C>> extends DataBlockDescriptionBuilder<C> {

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
    * @param targetId
    *           The target id, must not be null and refer to a generic container data block
    * @return This builder
    */
   C asIdOf(DataBlockId targetId);

   /**
    * Tags this field as indicating the presence of a target data block. The field must be of {@link FieldType#FLAGS}.
    * 
    * @param withFlagName
    *           The name of the flag indicating the presence of the target data block, must not be null and refer to an
    *           existing flag name of the underlying {@link FlagSpecification}
    * @param withFlagValue
    *           The value of the flag the indicates presence, any other value found during parsing indicates absence
    * @param targetId
    *           The target id, must not be null and refer to a target data block being optional, i.e. having minimum
    *           occurrences equal to 0 and maximum occurrences equal to 1
    * @return This builder
    */
   C indicatesPresenceOf(String withFlagName, int withFlagValue, DataBlockId targetId);

   /**
    * Tags this field as representing the size of a target data block. The field must be of
    * {@link FieldType#UNSIGNED_WHOLE_NUMBER}.
    * 
    * @param ids
    *           The target ids, must not be null and refer to sibling target data blocks having a dynamic size together
    * @return This builder
    */
   C asSizeOf(DataBlockId... ids);

   /**
    * Tags this field as representing the number of occurrences of a target data block. The field must be of
    * {@link FieldType#UNSIGNED_WHOLE_NUMBER}.
    * 
    * @param targetId
    *           The target id, must not be null and refer to a target data block having a dynamic number of occurrences
    * @return This builder
    */
   C asCountOf(DataBlockId targetId);

   /**
    * Tags this field as representing the id of a target generic container data block. The field must be of
    * {@link FieldType#STRING}.
    * 
    * @param targetId
    *           The target id, must not be null
    * @return This builder
    */
   C asByteOrderOf(DataBlockId targetId);

   /**
    * Tags this field as representing the id of a target generic container data block. The field must be of
    * {@link FieldType#STRING}.
    * 
    * @param targetId
    *           The target id, must not be null
    * @return This builder
    */
   C asCharacterEncodingOf(DataBlockId targetId);

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
    * Sets the default value of the built field, which is initially null
    * 
    * @param value
    *           The default value to set, may be null
    * @return This builder
    */
   C withDefaultValue(FIT value);

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
    * Finishes building the field
    * 
    * @return The parent builder
    */
   P finishField();
}
