/**
 *
 * {@link FlagSpecificationBuilder}.java
 *
 * @author Jens Ebert
 *
 * @date 21.05.2018
 *
 */
package com.github.jmeta.library.dataformats.api.services.builder;

import com.github.jmeta.library.dataformats.api.types.FlagDescription;

/**
 * {@link FlagSpecificationBuilder}
 *
 */
public interface FlagSpecificationBuilder<ParentBuilder> {

   FlagSpecificationBuilder<ParentBuilder> addFlagDescription(FlagDescription flagDesc);

   FlagSpecificationBuilder<ParentBuilder> withDefaultFlagBytes(byte[] defaultFlagBytes);

   FlagsFieldBuilder<ParentBuilder> finishFlagSpecification();
}
