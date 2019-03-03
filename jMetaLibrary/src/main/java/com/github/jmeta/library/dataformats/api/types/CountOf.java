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
      super(referencedBlock);
   }

   /**
    * @see com.github.jmeta.library.dataformats.api.types.AbstractFieldFunction#withReplacedReference(com.github.jmeta.library.dataformats.api.types.DataBlockCrossReference)
    */
   @Override
   public AbstractFieldFunction<Long> withReplacedReference(DataBlockCrossReference replacedReference) {
      return new CountOf(replacedReference);
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
         PhysicalDataBlockType.FIELD, PhysicalDataBlockType.FOOTER, PhysicalDataBlockType.HEADER);
   }
}
