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

import static com.github.jmeta.library.dataformats.api.exceptions.InvalidSpecificationException.VLD_FIELD_FUNC_UNRESOLVED;

import com.github.jmeta.library.dataformats.api.exceptions.InvalidSpecificationException;
import com.github.jmeta.library.dataformats.api.services.DataFormatSpecification;
import com.github.jmeta.utility.dbc.api.services.Reject;

/**
 * {@link SummedSizeOf} is a field function expressing that the field it refers to contains the summed size of two or
 * more consecutive other data blocks.
 */
public class SummedSizeOf extends SizeOf {

   private DataBlockCrossReference[] referencedBlocks;

   /**
    * Creates a new {@link SummedSizeOf} field function.
    *
    * @param referencedBlocks
    *           The referenced {@link DataBlockCrossReference}s, must not be null and must at least contain one entry
    */
   public SummedSizeOf(DataBlockCrossReference... referencedBlocks) {
      super(referencedBlocks[0]);

      Reject.ifFalse(referencedBlocks.length > 1, "referencedBlocks.length > 1");

      this.referencedBlocks = referencedBlocks;
   }

   /**
    * Returns the attribute {@link #referencedBlocks}.
    *
    * @return the attribute {@link #referencedBlocks}
    */
   public DataBlockCrossReference[] getReferencedBlocks() {
      return referencedBlocks;
   }

   /**
    * @see com.github.jmeta.library.dataformats.api.types.AbstractFieldFunction#withReplacedReferences(com.github.jmeta.library.dataformats.api.types.DataBlockCrossReference)
    */
   @Override
   public AbstractFieldFunction<Long> withReplacedReferences(DataBlockCrossReference... replacedReferences) {
      return new SummedSizeOf(replacedReferences);
   }

   /**
    * @see com.github.jmeta.library.dataformats.api.types.AbstractFieldFunction#validate(com.github.jmeta.library.dataformats.api.types.DataBlockDescription,
    *      com.github.jmeta.library.dataformats.api.services.DataFormatSpecification)
    */
   @Override
   public void validate(DataBlockDescription fieldDesc, DataFormatSpecification spec) {
      super.validate(fieldDesc, spec);

      // Validate field function is resolved
      for (DataBlockCrossReference dataBlockCrossReference : referencedBlocks) {
         if (!dataBlockCrossReference.isResolved()) {
            throw new InvalidSpecificationException(VLD_FIELD_FUNC_UNRESOLVED, fieldDesc, getClass());
         }
      }

      // TODO Validate only consecutive target blocks
      // TODO Validate that for at most one target block there is no easier size of or fixed size present
   }
}
