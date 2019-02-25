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
 * {@link SizeOf} is a field function expressing that the field it refers to contains the size of another data block.
 * Here it must be considered that it might be just a summed size, i.e. the field indicates the size of several
 * consecutive data blocks summed together. If so, one must add multiple instances of this class to the same field, each
 * pointing to a different target id.
 */
public class SizeOf extends AbstractFieldFunction<Long> {

   /**
    * Creates a new {@link SizeOf} field function.
    *
    * @param referencedBlock
    *           The {@link DataBlockCrossReference} to the referenced data block, must not be null
    */
   public SizeOf(DataBlockCrossReference referencedBlock) {
      super(referencedBlock, Long.class, FieldType.UNSIGNED_WHOLE_NUMBER);
   }

   /**
    * @see com.github.jmeta.library.dataformats.api.types.AbstractFieldFunction#isValidTargetType(com.github.jmeta.library.dataformats.api.types.PhysicalDataBlockType)
    */
   @Override
   public boolean isValidTargetType(PhysicalDataBlockType type) {
      Reject.ifNull(type, "type");
      return true;
   }

   /**
    * @see com.github.jmeta.library.dataformats.api.types.AbstractFieldFunction#withReplacedReference(com.github.jmeta.library.dataformats.api.types.DataBlockCrossReference)
    */
   @Override
   public AbstractFieldFunction<Long> withReplacedReference(DataBlockCrossReference replacedReference) {
      return new SizeOf(replacedReference);
   }
}
