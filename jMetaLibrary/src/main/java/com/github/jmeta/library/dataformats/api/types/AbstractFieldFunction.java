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

import com.github.jmeta.utility.dbc.api.services.Reject;

/**
 * {@link AbstractFieldFunction} is the base class of all field functions. A field function expresses that a field
 * indicates some property of another data block by its value and thus a reference to another data block.
 */
public abstract class AbstractFieldFunction<F> {

   private final DataBlockCrossReference referencedBlock;
   private final FieldType<F> requiredFieldType;

   /**
    * Creates a new {@link AbstractFieldFunction}.
    *
    * @param referencedBlock
    *           The {@link DataBlockCrossReference} to the referenced data block, must not be null
    * @param requiredFieldType
    *           The {@link FieldType} required by this {@link AbstractFieldFunction}, must not be null
    */
   public AbstractFieldFunction(DataBlockCrossReference referencedBlock, FieldType<F> requiredFieldType) {
      Reject.ifNull(referencedBlock, "referencedBlock");
      Reject.ifNull(requiredFieldType, "requiredFieldType");

      this.referencedBlock = referencedBlock;
      this.requiredFieldType = requiredFieldType;
   }

   /**
    * @return the {@link DataBlockCrossReference} indicating the id of another data block which is referenced by this
    *         field
    */
   public DataBlockCrossReference getReferencedBlock() {
      return referencedBlock;
   }

   /**
    * @return the {@link FieldType} that is required by this {@link AbstractFieldFunction}
    */
   public FieldType<F> getRequiredFieldType() {
      return requiredFieldType;
   }

   /**
    * Tells whether this {@link AbstractFieldFunction} is allowed to reference a data block of the given
    * {@link PhysicalDataBlockType} or not.
    *
    * @param type
    *           The {@link PhysicalDataBlockType} to check, must not be null.
    * @return whether this {@link AbstractFieldFunction} is allowed to reference a data block of the given
    *         {@link PhysicalDataBlockType} or not
    */
   public abstract boolean isValidTargetType(PhysicalDataBlockType type);

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
}
