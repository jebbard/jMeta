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

import com.github.jmeta.utility.dbc.api.services.Reject;

/**
 * {@link PresenceOf} expresses that the field it refers tells whether the target data block id is present or not.
 */
public class PresenceOf extends AbstractFieldFunction<Flags> {

   private final String flagName;
   private final int flagValue;

   /**
    * Creates a new {@link PresenceOf} field function.
    *
    * @param referencedBlock
    *           The {@link DataBlockCrossReference} to the referenced data block, must not be null
    * @param flagName
    *           The name of the flag indicating presence or absence, must not be null
    * @param flagValue
    *           The flag's value indicating presence, if the value differs, this indicates absence
    */
   public PresenceOf(DataBlockCrossReference referencedBlock, String flagName, int flagValue) {
      super(referencedBlock, FieldType.FLAGS);

      Reject.ifNull(flagName, "flagName");

      this.flagName = flagName;
      this.flagValue = flagValue;
   }

   /**
    * @see com.github.jmeta.library.dataformats.api.types.AbstractFieldFunction#isValidTargetType(com.github.jmeta.library.dataformats.api.types.PhysicalDataBlockType)
    */
   @Override
   public boolean isValidTargetType(PhysicalDataBlockType type) {
      Reject.ifNull(type, "type");
      return type == PhysicalDataBlockType.FOOTER || type == PhysicalDataBlockType.HEADER
         || type == PhysicalDataBlockType.FIELD;
   }

   /**
    * @return the flag's name indicating absence or presence
    */
   public String getFlagName() {
      return flagName;
   }

   /**
    * @return the flag's value indicating presence
    */
   public int getFlagValue() {
      return flagValue;
   }

   /**
    * @see com.github.jmeta.library.dataformats.api.types.AbstractFieldFunction#withReplacedReference(com.github.jmeta.library.dataformats.api.types.DataBlockCrossReference)
    */
   @Override
   public AbstractFieldFunction<Flags> withReplacedReference(DataBlockCrossReference replacedReference) {
      return new PresenceOf(replacedReference, flagName, flagValue);
   }
}
