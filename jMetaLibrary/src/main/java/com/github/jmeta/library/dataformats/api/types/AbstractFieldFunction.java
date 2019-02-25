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
public abstract class AbstractFieldFunction<T> {

   private final DataBlockCrossReference referencedBlock;
   private final Class<T> interpretedValueClass;

   /**
    * Creates a new {@link AbstractFieldFunction}.
    *
    * @param referencedBlock
    *           The {@link DataBlockCrossReference} to the referenced data block, must not be null
    * @param interpretedValueClass
    *           The concrete interpreted type of a field value, must not be null
    */
   public AbstractFieldFunction(DataBlockCrossReference referencedBlock, Class<T> interpretedValueClass) {
      Reject.ifNull(referencedBlock, "referencedBlock");
      Reject.ifNull(interpretedValueClass, "interpretedValueClass");

      this.referencedBlock = referencedBlock;
      this.interpretedValueClass = interpretedValueClass;
   }

   /**
    * @return the {@link DataBlockCrossReference} indicating the id of another data block which is referenced by this
    *         field
    */
   public DataBlockCrossReference getReferencedBlock() {
      return referencedBlock;
   }

   /**
    * @return the interpreted value type
    */
   public Class<T> getInterpretedValueClass() {
      return interpretedValueClass;
   }

   /**
    * Tells whether this {@link AbstractFieldFunction} is allowed for the given {@link FieldType} or not.
    *
    * @param type
    *           The {@link FieldType} to check, must not be null
    * @return whether this {@link AbstractFieldFunction} is allowed for the given {@link FieldType} or not
    */
   public abstract boolean isValidFieldType(FieldType<?> type);

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
}
