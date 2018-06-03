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

import com.github.jmeta.library.dataformats.api.types.DataBlockDescription;

/**
 * {@link DataBlockDescriptionBuilder} is the base interface for all data block builders. It offers a chained builder
 * API.
 */
public interface DataBlockDescriptionBuilder<C extends DataBlockDescriptionBuilder<C>> extends DataFormatBuilder {

   /**
    * Assigns a static length (min length = max length) to the data block. If this method is not called, the default
    * lengths are {@link DataBlockDescription#getMinimumByteLength()} = 0 and
    * {@link DataBlockDescription#getMaximumByteLength()} = {@link DataBlockDescription#UNLIMITED}.
    * 
    * @param staticByteLength
    *           The static length of the data block, must not be negative
    * @return The concrete builder instance
    */
   public C withStaticLengthOf(long staticByteLength);

   /**
    * Assigns a dynamic length (min length not equal to max length) to the data block. If this method is not called, the
    * default lengths are {@link DataBlockDescription#getMinimumByteLength()} = 0 and
    * {@link DataBlockDescription#getMaximumByteLength()} = {@link DataBlockDescription#UNLIMITED}.
    * 
    * @param minimumByteLength
    *           The minimum byte length of the data block, must not be negative and must be smaller than or equal to the
    *           maximum byte length
    * @param maximumByteLength
    *           The maximum byte length of the data block, must not be negative and must be bigger than or equal to the
    *           minimum byte length
    * @return The concrete builder instance
    */
   public C withLengthOf(long minimumByteLength, long maximumByteLength);

   /**
    * Assigns a number of occurrences to the data block. If this method is not called, the default occurrences are
    * {@link DataBlockDescription#getMinimumOccurrences()} = 1 and {@link DataBlockDescription#getMaximumOccurrences()}
    * = 1.
    * 
    * @param minimumOccurrences
    *           The minimum number of occurrences, must be 0 or strictly positive and smaller than or equal to the
    *           maximum number of occurrences
    * @param maximumOccurrences
    *           The maximum number of occurrences, must be strictly positive and bigger than or equal to the minimum
    *           number of occurrences
    * @return The concrete builder instance
    */
   public C withOccurrences(int minimumOccurrences, int maximumOccurrences);

   /**
    * Allows to change the specification name and description of a data block.
    * 
    * @param name
    *           The name to set, may be null
    * @param description
    *           The description to set, may be null
    * @return The concrete builder instance
    */
   public C withDescription(String name, String description);
}
