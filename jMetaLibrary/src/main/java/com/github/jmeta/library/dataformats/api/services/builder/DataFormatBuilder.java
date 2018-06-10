/**
 *
 * {@link DataFormatBuilder}.java
 *
 * @author Jens Ebert
 *
 * @date 27.05.2018
 *
 */
package com.github.jmeta.library.dataformats.api.services.builder;

import com.github.jmeta.library.dataformats.api.services.DataFormatSpecification;
import com.github.jmeta.library.dataformats.api.services.DataFormatSpecificationBuilder;
import com.github.jmeta.library.dataformats.api.types.ContainerDataFormat;
import com.github.jmeta.library.dataformats.api.types.DataBlockDescription;

/**
 * {@link DataFormatBuilder} defines the methods necessary for building a {@link DataFormatSpecification} that are
 * needed both by {@link DataFormatSpecificationBuilder} as well as {@link DataBlockDescriptionBuilder}.
 */
public interface DataFormatBuilder {

   /**
    * Returns a string representation of the built data block's global id or null if the built data block is a top-level
    * data block.
    * 
    * @return a string representation of the built data block's global id or null if the built data block is a top-level
    *         data block.
    */
   String getGlobalId();

   /**
    * Allows child builders as well as end users to add a new {@link DataBlockDescription} as child description.
    * 
    * @param childDescription
    *           The child {@link DataBlockDescription} to add, must not be null
    */
   void addChildDescription(DataBlockDescription childDescription);

   /**
    * Returns the {@link ContainerDataFormat} this builder belongs to.
    * 
    * @return The {@link ContainerDataFormat} this builder belongs to
    */
   ContainerDataFormat getDataFormat();

   /**
    * Returns the root {@link DataFormatSpecificationBuilder} of this builder hierarchy. This is necessary to add
    * {@link DataBlockDescription} to the builder from anywhere in the hierarchy
    * 
    * @return The root {@link DataFormatSpecificationBuilder} of this builder hierarchy
    */
   DataFormatSpecificationBuilder getRootBuilder();
}
