/**
 *
 * {@link UnresolvedFieldFunction}.java
 *
 * @author Jens Ebert
 *
 * @date 08.01.2011
 */

package com.github.jmeta.library.dataformats.api.types;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import com.github.jmeta.utility.dbc.api.services.Reject;

/**
 * {@link FieldFunction}
 *
 */
public class FieldFunction {

   private final Set<DataBlockCrossReference> referencedBlocks = new HashSet<>();

   private final FieldFunctionType<?> fieldFunctionType;

   private final String flagName;

   private final Integer flagValue;

   public Set<DataBlockCrossReference> getReferencedBlocks() {
      return Collections.unmodifiableSet(referencedBlocks);
   }

   /**
    * Creates a new {@link FieldFunction}.
    * 
    * @param fieldFunctionType
    * @param referencedBlocks
    * @param flagName
    * @param requiredFlagValue
    */
   public FieldFunction(FieldFunctionType<?> fieldFunctionType, Set<DataBlockCrossReference> referencedBlocks,
      String flagName, Integer requiredFlagValue) {
      Reject.ifNull(referencedBlocks, "affectedBlockIds");
      Reject.ifNull(fieldFunctionType, "fieldFunctionType");
      Reject.ifTrue(referencedBlocks.isEmpty(), "affectedBlockIds.isEmpty()");

      this.fieldFunctionType = fieldFunctionType;
      this.flagName = flagName;
      this.flagValue = requiredFlagValue;
      this.referencedBlocks.addAll(referencedBlocks);
   }

   /**
    * Returns affectedBlockIds
    *
    * @return affectedBlockIds
    */
   public Set<DataBlockId> getAffectedBlockIds() {
      return Collections
         .unmodifiableSet(referencedBlocks.stream().map(ref -> ref.getReferencedId()).collect(Collectors.toSet()));
   }

   /**
    * Returns fieldFunctionType
    *
    * @return fieldFunctionType
    */
   public FieldFunctionType<?> getFieldFunctionType() {

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
      return fieldFunctionType + ", " + getAffectedBlockIds();
   }

}
