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
public interface FlagSpecificationBuilder<P> {

   FlagSpecificationBuilder<P> addFlagDescription(FlagDescription flagDesc);

   FlagSpecificationBuilder<P> withDefaultFlagBytes(byte[] defaultFlagBytes);

   FlagsFieldBuilder<P> finishFlagSpecification();
}
