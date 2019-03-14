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

import java.util.stream.Collectors;

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
    * @see com.github.jmeta.library.dataformats.api.types.AbstractFieldFunction#withReplacedReferences(com.github.jmeta.library.dataformats.api.types.DataBlockCrossReference)
    */
   @Override
   public AbstractFieldFunction<Long> withReplacedReferences(DataBlockCrossReference... replacedReferences) {
      return new CountOf(replacedReferences[0]);
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
         getReferencedBlocks().stream().map(ref -> spec.getDataBlockDescription(ref.getId()))
            .collect(Collectors.toSet()),
         PhysicalDataBlockType.CONTAINER, PhysicalDataBlockType.FIELD, PhysicalDataBlockType.FOOTER,
         PhysicalDataBlockType.HEADER);
   }
}
