/**
 *
 * {@link UnresolvedFieldFunction}.java
 *
 * @author Jens Ebert
 *
 * @date 08.01.2011
 */

package com.github.jmeta.library.dataformats.api.types;

import com.github.jmeta.utility.dbc.api.services.Reject;

/**
 * {@link FieldFunction}
 *
 */
public class FieldFunction<T> {

   private final DataBlockCrossReference referencedBlock;

   private final FieldFunctionType<T> fieldFunctionType;

   private final String flagName;

   private final Integer flagValue;

   public DataBlockCrossReference getReferencedBlock() {
      return referencedBlock;
   }

   /**
    * Creates a new {@link FieldFunction}.
    *
    * @param fieldFunctionType
    * @param referencedBlocks
    * @param flagName
    * @param requiredFlagValue
    */
   public FieldFunction(FieldFunctionType<T> fieldFunctionType, DataBlockCrossReference referencedBlock,
      String flagName, Integer requiredFlagValue) {
      Reject.ifNull(referencedBlock, "referencedBlock");
      Reject.ifNull(fieldFunctionType, "fieldFunctionType");

      this.fieldFunctionType = fieldFunctionType;
      this.flagName = flagName;
      this.flagValue = requiredFlagValue;
      this.referencedBlock = referencedBlock;
   }

   /**
    * Returns affectedBlockIds
    *
    * @return affectedBlockIds
    */
   public DataBlockId getAffectedBlockId() {
      return referencedBlock.getReferencedId();
   }

   /**
    * Returns fieldFunctionType
    *
    * @return fieldFunctionType
    */
   public FieldFunctionType<T> getFieldFunctionType() {

      return fieldFunctionType;
   }

   /**
    * Returns flagName
    *
    * @return flagName
    */
   public String getFlagName() {

      return flagName;
   }

   /**
    * @return the flag value
    */
   public Integer getFlagValue() {

      return flagValue;
   }

   @Override
   public String toString() {
      return fieldFunctionType + ", " + getAffectedBlockId();
   }

}
