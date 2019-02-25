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
      super(referencedBlock, Long.class, FieldType.UNSIGNED_WHOLE_NUMBER);
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

   /**
    * @see com.github.jmeta.library.dataformats.api.types.AbstractFieldFunction#withReplacedReference(com.github.jmeta.library.dataformats.api.types.DataBlockCrossReference)
    */
   @Override
   public AbstractFieldFunction<Long> withReplacedReference(DataBlockCrossReference replacedReference) {
      return new CountOf(replacedReference);
   }
}
