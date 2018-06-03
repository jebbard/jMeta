/**
 *
 * {@link FieldSequenceBuilder}.java
 *
 * @author Jens Ebert
 *
 * @date 12.05.2018
 *
 */
package com.github.jmeta.library.dataformats.api.services.builder;

import com.github.jmeta.library.dataformats.api.types.DataBlockId;
import com.github.jmeta.library.dataformats.api.types.FieldType;

/**
 * {@link FieldSequenceBuilder} allows to build a sequence of ordered field data blocks.
 *
 * @param <C>
 *           The concrete {@link FieldSequenceBuilder} interface derived from this interface
 */
public interface FieldSequenceBuilder<C extends FieldSequenceBuilder<C>> extends DataBlockDescriptionBuilder<C> {

   /**
    * Adds a field of {@link FieldType#STRING}.
    * 
    * @param localId
    *           The local id of the new data block, must not be null, must not contain the character
    *           {@link DataBlockId#SEGMENT_SEPARATOR}
    * @param name
    *           The human-readable specification name of the data block or null
    * @param description
    *           A description of the data block preferably taken from the data format specification or null
    * @return The child builder for the new data block
    */
   StringFieldBuilder<C> addStringField(String localId, String name, String description);

   /**
    * Adds a field of {@link FieldType#UNSIGNED_WHOLE_NUMBER}.
    * 
    * @param localId
    *           The local id of the new data block, must not be null, must not contain the character
    *           {@link DataBlockId#SEGMENT_SEPARATOR}
    * @param name
    *           The human-readable specification name of the data block or null
    * @param description
    *           A description of the data block preferably taken from the data format specification or null
    * @return The child builder for the new data block
    */
   NumericFieldBuilder<C> addNumericField(String localId, String name, String description);

   /**
    * Adds a field of {@link FieldType#BINARY}.
    * 
    * @param localId
    *           The local id of the new data block, must not be null, must not contain the character
    *           {@link DataBlockId#SEGMENT_SEPARATOR}
    * @param name
    *           The human-readable specification name of the data block or null
    * @param description
    *           A description of the data block preferably taken from the data format specification or null
    * @return The child builder for the new data block
    */
   BinaryFieldBuilder<C> addBinaryField(String localId, String name, String description);

   /**
    * Adds a field of {@link FieldType#FLAGS}.
    * 
    * @param localId
    *           The local id of the new data block, must not be null, must not contain the character
    *           {@link DataBlockId#SEGMENT_SEPARATOR}
    * @param name
    *           The human-readable specification name of the data block or null
    * @param description
    *           A description of the data block preferably taken from the data format specification or null
    * @return The child builder for the new data block
    */
   FlagsFieldBuilder<C> addFlagsField(String localId, String name, String description);

   /**
    * Allows to remove a field already added to another field sequence which has been cloned before.
    * 
    * @param localId
    *           The local id of the field to remove, must not be null and must indeed refer to a child field previously
    *           added
    * @return The current builder
    */
   FieldSequenceBuilder<C> withoutField(String localId);
}
