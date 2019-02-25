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
 * {@link CountOf} expresses that the field it refers to contains the count of occurrences of the target data block id.
 */
public class CountOf extends AbstractFieldFunction<Long> {

   /**
    * Creates a new {@link CountOf} field function.
    *
    * @param referencedBlock
    *           The {@link DataBlockCrossReference} to the referenced data block, must not be null
    */
   public CountOf(DataBlockCrossReference referencedBlock) {
      super(referencedBlock, Long.class);
   }

   /**
    * @see com.github.jmeta.library.dataformats.api.types.AbstractFieldFunction#isValidFieldType(com.github.jmeta.library.dataformats.api.types.FieldType)
    */
   @Override
   public boolean isValidFieldType(FieldType<?> type) {
      Reject.ifNull(type, "type");
      return type == FieldType.UNSIGNED_WHOLE_NUMBER;
   }

   /**
    * @see com.github.jmeta.library.dataformats.api.types.AbstractFieldFunction#isValidTargetType(com.github.jmeta.library.dataformats.api.types.PhysicalDataBlockType)
    */
   @Override
   public boolean isValidTargetType(PhysicalDataBlockType type) {
      Reject.ifNull(type, "type");
      return type == PhysicalDataBlockType.HEADER || type == PhysicalDataBlockType.FOOTER
         || type == PhysicalDataBlockType.FIELD;
   }
}
