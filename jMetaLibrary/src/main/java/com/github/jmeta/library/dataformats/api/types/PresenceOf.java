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

import static com.github.jmeta.library.dataformats.api.exceptions.InvalidSpecificationException.VLD_FIELD_FUNC_PRESENCE_OF_UNSPECIFIED_FLAG_NAME;

import java.util.stream.Collectors;

import com.github.jmeta.library.dataformats.api.exceptions.InvalidSpecificationException;
import com.github.jmeta.library.dataformats.api.services.DataFormatSpecification;
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
      super(referencedBlock);

      Reject.ifNull(flagName, "flagName");

      this.flagName = flagName;
      this.flagValue = flagValue;
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
    * @see com.github.jmeta.library.dataformats.api.types.AbstractFieldFunction#withReplacedReferences(com.github.jmeta.library.dataformats.api.types.DataBlockCrossReference)
    */
   @Override
   public AbstractFieldFunction<Flags> withReplacedReferences(DataBlockCrossReference... replacedReferences) {
      Reject.ifNull(replacedReferences, "replacedReferences");

      return new PresenceOf(replacedReferences[0], flagName, flagValue);
   }

   /**
    * @see com.github.jmeta.library.dataformats.api.types.AbstractFieldFunction#validate(com.github.jmeta.library.dataformats.api.types.DataBlockDescription,
    *      com.github.jmeta.library.dataformats.api.services.DataFormatSpecification)
    */
   @Override
   public void validate(DataBlockDescription fieldDesc, DataFormatSpecification spec) {
      Reject.ifNull(fieldDesc, "fieldDesc");
      Reject.ifNull(spec, "spec");

      performDefaultValidation(fieldDesc, FieldType.FLAGS,
         getReferencedBlocks().stream().map(ref -> spec.getDataBlockDescription(ref.getId()))
            .collect(Collectors.toSet()),
         PhysicalDataBlockType.FIELD, PhysicalDataBlockType.FOOTER, PhysicalDataBlockType.HEADER);

      if (!fieldDesc.getFieldProperties().getFlagSpecification().hasFlag(flagName)) {
         throw new InvalidSpecificationException(VLD_FIELD_FUNC_PRESENCE_OF_UNSPECIFIED_FLAG_NAME, fieldDesc, flagName);
      }
   }
}
