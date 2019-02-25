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
 * {@link IdOf} expresses that the field it refers to contains the concrete id of the target data block id.
 *
 */
public class IdOf extends AbstractFieldFunction<String> {

   /**
    * Creates a new {@link IdOf} field function.
    *
    * @param referencedBlock
    *           The {@link DataBlockCrossReference} to the referenced data block, must not be null
    */
   public IdOf(DataBlockCrossReference referencedBlock) {
      super(referencedBlock, String.class, FieldType.STRING);
   }

   /**
    * @see com.github.jmeta.library.dataformats.api.types.AbstractFieldFunction#isValidTargetType(com.github.jmeta.library.dataformats.api.types.PhysicalDataBlockType)
    */
   @Override
   public boolean isValidTargetType(PhysicalDataBlockType type) {
      Reject.ifNull(type, "type");
      return type == PhysicalDataBlockType.CONTAINER;
   }

   /**
    * @see com.github.jmeta.library.dataformats.api.types.AbstractFieldFunction#withReplacedReference(com.github.jmeta.library.dataformats.api.types.DataBlockCrossReference)
    */
   @Override
   public AbstractFieldFunction<String> withReplacedReference(DataBlockCrossReference replacedReference) {
      return new IdOf(replacedReference);
   }
}
