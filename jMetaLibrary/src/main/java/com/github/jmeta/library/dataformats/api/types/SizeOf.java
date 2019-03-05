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

import com.github.jmeta.library.dataformats.api.services.DataFormatSpecification;
import com.github.jmeta.utility.dbc.api.services.Reject;

/**
 * {@link SizeOf} is a field function expressing that the field it refers to contains the size of exactly one other data
 * block.
 */
public class SizeOf extends AbstractFieldFunction<Long> {

   /**
    * Creates a new {@link SizeOf} field function.
    *
    * @param referencedBlock
    *           The {@link DataBlockCrossReference} to the referenced data block, must not be null
    */
   public SizeOf(DataBlockCrossReference referencedBlock) {
      super(referencedBlock);
   }

   /**
    * @see com.github.jmeta.library.dataformats.api.types.AbstractFieldFunction#withReplacedReferences(com.github.jmeta.library.dataformats.api.types.DataBlockCrossReference)
    */
   @Override
   public AbstractFieldFunction<Long> withReplacedReferences(DataBlockCrossReference... replacedReferences) {
      return new SizeOf(replacedReferences[0]);
   }

   /**
    * @see com.github.jmeta.library.dataformats.api.types.AbstractFieldFunction#validate(com.github.jmeta.library.dataformats.api.types.DataBlockDescription,
    *      com.github.jmeta.library.dataformats.api.services.DataFormatSpecification)
    */
   @Override
   public void validate(DataBlockDescription fieldDesc, DataFormatSpecification spec) {
      Reject.ifNull(fieldDesc, "fieldDesc");
      Reject.ifNull(spec, "spec");

      performDefaultValidation(fieldDesc, FieldType.UNSIGNED_WHOLE_NUMBER,
         spec.getDataBlockDescription(getReferencedBlock().getId()), PhysicalDataBlockType.CONTAINER,
         PhysicalDataBlockType.CONTAINER_BASED_PAYLOAD, PhysicalDataBlockType.FIELD,
         PhysicalDataBlockType.FIELD_BASED_PAYLOAD, PhysicalDataBlockType.FOOTER, PhysicalDataBlockType.HEADER);

      // TODO Validate only consecutive target blocks
      // TODO Validate that for at most one target block there is no easier size of or fixed size present
   }
}
