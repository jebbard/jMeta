/**
 *
 * {@link AbstractFieldFunction}.java
 *
 * @author Jens Ebert
 *
 * @date 25.02.2019
 *
 */
package com.github.jmeta.library.dataformats.api.types;

import static com.github.jmeta.library.dataformats.api.exceptions.InvalidSpecificationException.VLD_FIELD_FUNC_INVALID_FIELD_TYPE;
import static com.github.jmeta.library.dataformats.api.exceptions.InvalidSpecificationException.VLD_FIELD_FUNC_REFERENCING_WRONG_TYPE;
import static com.github.jmeta.library.dataformats.api.exceptions.InvalidSpecificationException.VLD_FIELD_FUNC_UNRESOLVED;

import java.util.Arrays;

import com.github.jmeta.library.dataformats.api.exceptions.InvalidSpecificationException;
import com.github.jmeta.library.dataformats.api.services.DataFormatSpecification;
import com.github.jmeta.utility.dbc.api.services.Reject;

/**
 * {@link AbstractFieldFunction} is the base class of all field functions. A field function expresses that a field
 * indicates some property of another data block by its value and thus a reference to another data block.
 */
public abstract class AbstractFieldFunction<F> {

   private final DataBlockCrossReference referencedBlock;

   /**
    * Creates a new {@link AbstractFieldFunction}.
    *
    * @param referencedBlock
    *           The {@link DataBlockCrossReference} to the referenced data block, must not be null
    */
   public AbstractFieldFunction(DataBlockCrossReference referencedBlock) {
      Reject.ifNull(referencedBlock, "referencedBlock");

      this.referencedBlock = referencedBlock;
   }

   /**
    * @return the {@link DataBlockCrossReference} indicating the id of another data block which is referenced by this
    *         field
    */
   public DataBlockCrossReference getReferencedBlock() {
      return referencedBlock;
   }

   /**
    * Validates this {@link AbstractFieldFunction} when used for the given field {@link DataBlockDescription}. If it is
    * invalid for this field or target type, this method throws an {@link InvalidSpecificationException}.
    *
    * @param fieldDesc
    *           The field {@link DataBlockDescription} this field function is used for, must not be null
    * @param spec
    *           The {@link DataFormatSpecification} used to aid validation, must not be null
    */
   public abstract void validate(DataBlockDescription fieldDesc, DataFormatSpecification spec);

   /**
    * Creates a cloned instance of this {@link AbstractFieldFunction} with the given {@link DataBlockCrossReference}
    * instead of the current one.
    *
    * @param replacedReference
    *           The replacement {@link DataBlockCrossReference}, must not be null
    * @return a cloned instance of this {@link AbstractFieldFunction} with the given {@link DataBlockCrossReference}
    *         instead of the current one
    */
   public abstract AbstractFieldFunction<F> withReplacedReference(DataBlockCrossReference replacedReference);

   /**
    * Performs a default validation of this {@link AbstractFieldFunction} against the provided
    * {@link DataBlockDescription} host field: It checks that the field has the expected type and that referenced block
    * descriptions have correct target type. Finally, it checks that all referenced blocks have already been resolved.
    * Thus this method should only be called once this is guaranteed.
    *
    * Throws an {@link InvalidSpecificationException} if one of the validations fail.
    *
    * @param fieldDesc
    *           The host field's {@link DataBlockDescription}, must not be null
    * @param expectedFieldType
    *           The expected {@link FieldType} of the host field, must not be null
    * @param referencedBlockDesc
    *           The referenced block {@link DataBlockDescription}s, must not be null
    * @param validTypes
    *           The valid target {@link PhysicalDataBlockType}s, must not be null or empty
    */
   protected void performDefaultValidation(DataBlockDescription fieldDesc, FieldType<F> expectedFieldType,
      DataBlockDescription referencedBlockDesc, PhysicalDataBlockType... validTypes) {
      Reject.ifNull(validTypes, "validTypes");
      Reject.ifNull(referencedBlockDesc, "referencedBlockDesc");
      Reject.ifNull(expectedFieldType, "expectedFieldType");
      Reject.ifNull(fieldDesc, "fieldDesc");
      Reject.ifTrue(validTypes.length == 0, "validTypes.length == 0");

      // Validate field type
      if (fieldDesc.getFieldProperties().getFieldType() != expectedFieldType) {
         throw new InvalidSpecificationException(VLD_FIELD_FUNC_INVALID_FIELD_TYPE, fieldDesc, getClass(),
            fieldDesc.getFieldProperties().getFieldType(), expectedFieldType);
      }

      // Validate target type
      PhysicalDataBlockType typeToCheck = referencedBlockDesc.getPhysicalType();
      if (!Arrays.asList(validTypes).contains(typeToCheck)) {
         throw new InvalidSpecificationException(VLD_FIELD_FUNC_REFERENCING_WRONG_TYPE, fieldDesc, getClass(),
            Arrays.toString(validTypes), referencedBlockDesc.getId(), typeToCheck);
      }

      // Validate field function is resolved
      if (!getReferencedBlock().isResolved()) {
         throw new InvalidSpecificationException(VLD_FIELD_FUNC_UNRESOLVED, fieldDesc, getClass());
      }
   }
}
