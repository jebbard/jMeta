/**
 *
 * {@link DataBlockDescriptionBuilder}.java
 *
 * @author Jens Ebert
 *
 * @date 12.05.2018
 *
 */
package com.github.jmeta.library.dataformats.api.services.builder;

/**
 * {@link DataBlockDescriptionBuilder}
 *
 */
public interface DataBlockDescriptionBuilder<C extends DataBlockDescriptionBuilder<C>> extends DataFormatBuilder {

   public C withStaticLengthOf(long staticByteLength);

   public C withLengthOf(long minimumByteLength, long maximumByteLength);

   public C withOccurrences(int minimumOccurrences, int maximumOccurrences);

   public C withDescription(String name, String description);
}
