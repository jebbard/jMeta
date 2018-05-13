/**
 *
 * {@link StringFieldBuilder}.java
 *
 * @author Jens Ebert
 *
 * @date 12.05.2018
 *
 */
package com.github.jmeta.library.dataformats.api.services.builder;

import com.github.jmeta.library.dataformats.api.types.FlagSpecification;
import com.github.jmeta.library.dataformats.api.types.Flags;

/**
 * {@link FlagsFieldBuilder}
 *
 */
public interface FlagsFieldBuilder<ParentBuilder> extends FieldBuilder<ParentBuilder, Flags>,
   FieldDescriptionModifier<ParentBuilder, Flags, FlagsFieldBuilder<ParentBuilder>>,
   DataBlockDescriptionModifier<FlagsFieldBuilder<ParentBuilder>> {

   FlagsFieldBuilder<ParentBuilder> withFlagSpecification(FlagSpecification spec);
}
