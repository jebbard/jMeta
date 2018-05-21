/**
 *
 * {@link DataBlockDescriptionModifier}.java
 *
 * @author Jens Ebert
 *
 * @date 13.05.2018
 *
 */
package com.github.jmeta.library.dataformats.api.services.builder;

import com.github.jmeta.library.dataformats.api.types.DataBlockId;

/**
 * {@link DataBlockDescriptionModifier}
 *
 */
public interface DataBlockDescriptionModifier<ConcreteBuilder extends DataFormatSpecificationBuilder> {

   public ConcreteBuilder withStaticLengthOf(long staticByteLength);

   public ConcreteBuilder withLengthOf(long minimumByteLength, long maximumByteLength);

   public ConcreteBuilder withOccurrences(int minimumOccurrences, int maximumOccurrences);

   public ConcreteBuilder withOverriddenId(DataBlockId overriddenId);

   public ConcreteBuilder withDescription(String name, String description);
}
