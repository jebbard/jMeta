/**
 *
 * {@link FieldFunction}.java
 *
 * @author Jens Ebert
 *
 * @date 08.01.2011
 */

package com.github.jmeta.library.dataformats.api.types;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import com.github.jmeta.utility.dbc.api.services.Reject;

/**
 * {@link FieldFunction}
 *
 */
public class FieldFunction {

   private final Set<DataBlockId> m_affectedBlockIds = new HashSet<>();

   private final FieldFunctionType<?> m_fieldFunctionType;

   private final String m_flagName;

   private final Integer m_flagValue;

   /**
    * Creates a new {@link FieldFunction}.
    * 
    * @param fieldFunctionType
    * @param affectedBlockIds
    * @param flagName
    * @param requiredFlagValue
    */
   public FieldFunction(FieldFunctionType<?> fieldFunctionType, Set<DataBlockId> affectedBlockIds, String flagName,
      Integer requiredFlagValue) {
      Reject.ifNull(affectedBlockIds, "affectedBlockIds");
      Reject.ifNull(fieldFunctionType, "fieldFunctionType");
      Reject.ifTrue(affectedBlockIds.isEmpty(), "affectedBlockIds.isEmpty()");

      m_fieldFunctionType = fieldFunctionType;
      m_flagName = flagName;
      m_flagValue = requiredFlagValue;
      m_affectedBlockIds.addAll(affectedBlockIds);
   }

   /**
    * Returns affectedBlockIds
    *
    * @return affectedBlockIds
    */
   public Set<DataBlockId> getAffectedBlockIds() {

      return Collections.unmodifiableSet(m_affectedBlockIds);
   }

   /**
    * Returns fieldFunctionType
    *
    * @return fieldFunctionType
    */
   public FieldFunctionType<?> getFieldFunctionType() {

      return m_fieldFunctionType;
   }

   /**
    * Returns flagName
    *
    * @return flagName
    */
   public String getFlagName() {

      return m_flagName;
   }

   /**
    * @return the flag value
    */
   public Integer getFlagValue() {

      return m_flagValue;
   }

   @Override
   public String toString() {
      return m_fieldFunctionType + ", " + getAffectedBlockIds();
   }

}
