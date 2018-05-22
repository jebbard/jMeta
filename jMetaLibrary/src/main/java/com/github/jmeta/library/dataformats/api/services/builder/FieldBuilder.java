/**
 *
 * {@link FieldBuilder}.java
 *
 * @author Jens Ebert
 *
 * @date 12.05.2018
 *
 */
package com.github.jmeta.library.dataformats.api.services.builder;

import com.github.jmeta.library.dataformats.api.types.DataBlockId;

/**
 * {@link FieldBuilder}
 *
 */
public interface FieldBuilder<ParentBuilder, FieldInterpretedType, ConcreteFieldBuilder extends FieldBuilder<ParentBuilder, FieldInterpretedType, ConcreteFieldBuilder>>
   extends DataFormatSpecificationBuilder {

   ConcreteFieldBuilder withDefaultValue(FieldInterpretedType value);

   ConcreteFieldBuilder asMagicKey();

   ConcreteFieldBuilder asIdOf(DataBlockId... ids);

   ConcreteFieldBuilder indicatesPresenceOf(String withFlagName, int withFlagValue, DataBlockId... ids);

   ConcreteFieldBuilder asSizeOf(DataBlockId... ids);

   ConcreteFieldBuilder asCountOf(DataBlockId... ids);

   ConcreteFieldBuilder asByteOrderOf(DataBlockId... ids);

   ConcreteFieldBuilder asCharacterEncodingOf(DataBlockId... ids);

   ParentBuilder finishField();
}
