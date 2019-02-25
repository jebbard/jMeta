/**
 *
 * {@link SizeOf}.java
 *
 * @author Jens Ebert
 *
 * @date 25.02.2019
 *
 */
package com.github.jmeta.library.dataformats.api.types;

import com.github.jmeta.utility.dbc.api.services.Reject;

/**
 * {@link CharacterEncodingOf} expresses that the field it refers to contains the character encoding of the target data
 * block id.
 */
public class CharacterEncodingOf extends AbstractFieldFunction<String> {

   /**
    * Creates a new {@link CharacterEncodingOf} field function.
    *
    * @param referencedBlock
    *           The {@link DataBlockCrossReference} to the referenced data block, must not be null
    */
   public CharacterEncodingOf(DataBlockCrossReference referencedBlock) {
      super(referencedBlock, String.class);
   }

   /**
    * @see com.github.jmeta.library.dataformats.api.types.AbstractFieldFunction#isValidFieldType(com.github.jmeta.library.dataformats.api.types.FieldType)
    */
   @Override
   public boolean isValidFieldType(FieldType<?> type) {
      Reject.ifNull(type, "type");
      return type == FieldType.STRING;
   }

   /**
    * @see com.github.jmeta.library.dataformats.api.types.AbstractFieldFunction#isValidTargetType(com.github.jmeta.library.dataformats.api.types.PhysicalDataBlockType)
    */
   @Override
   public boolean isValidTargetType(PhysicalDataBlockType type) {
      Reject.ifNull(type, "type");
      return true;
   }
}
