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

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.github.jmeta.library.dataformats.api.exceptions.InvalidSpecificationException;
import com.github.jmeta.library.dataformats.api.services.DataFormatSpecification;

/**
 * {@link SummedSizeOf} is a field function expressing that the field it refers to contains the summed size of two or
 * more consecutive other data blocks.
 */
public class SummedSizeOf extends SizeOf {

   /**
    * Creates a new {@link SummedSizeOf} field function.
    *
    * @param referencedBlocks
    *           The referenced {@link DataBlockCrossReference}s, must not be null and must at least contain one entry
    */
   public SummedSizeOf(DataBlockCrossReference... referencedBlocks) {
      super(referencedBlocks);
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

      List<DataBlockId> targetDataBlockIds = getReferencedBlocks().stream().map(DataBlockCrossReference::getId)
         .collect(Collectors.toList());

      Map<DataBlockId, List<AbstractFieldFunction<?>>> fieldFunctionsByTargetId = spec.getAllFieldFunctionsByTargetId();

      Set<DataBlockId> targetBlocksWithoutSize = new HashSet<>();

      for (DataBlockId targetId : targetDataBlockIds) {
         DataBlockDescription targetDescc = spec.getDataBlockDescription(targetId);

         boolean hasSuitableFieldFunction = fieldFunctionsByTargetId.containsKey(targetId)
            && fieldFunctionsByTargetId.get(targetId).stream().anyMatch(ff -> ff.getClass().equals(SizeOf.class));

         if (!targetDescc.hasFixedSize() && !hasSuitableFieldFunction) {
            targetBlocksWithoutSize.add(targetId);
         }
      }

      if (targetBlocksWithoutSize.size() > 1) {
         throw new InvalidSpecificationException(InvalidSpecificationException.VLD_FIELD_FUNC_INVALID_SUMMED_SIZE,
            fieldDesc, targetBlocksWithoutSize);
      }
   }
}
