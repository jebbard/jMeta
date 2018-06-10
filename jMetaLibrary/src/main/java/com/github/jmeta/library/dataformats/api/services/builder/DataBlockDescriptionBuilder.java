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
 * {@link DataBlockDescriptionBuilder} is the base interface for all data block builders. It offers a chained builder
 * API.
 *
 * @param <C>
 *           The concrete {@link DataBlockDescriptionBuilder} interface derived from this interface
 */
public interface DataBlockDescriptionBuilder<C extends DataBlockDescriptionBuilder<C>> extends DataFormatBuilder {

   /**
    * Allows to change the specification name and description of a data block.
    * 
    * @param name
    *           The name to set, may be null
    * @param description
    *           The description to set, may be null
    * @return The concrete builder instance
    */
   C withDescription(String name, String description);
}
