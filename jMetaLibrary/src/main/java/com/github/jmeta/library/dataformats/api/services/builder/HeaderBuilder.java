/**
 *
 * {@link HeaderBuilder}.java
 *
 * @author Jens Ebert
 *
 * @date 12.05.2018
 *
 */
package com.github.jmeta.library.dataformats.api.services.builder;

/**
 * {@link HeaderBuilder} allows to add fields to a header data block
 *
 * @param <P>
 *           The concrete parent builder interface
 */
public interface HeaderBuilder<P> extends FieldSequenceBuilder<HeaderBuilder<P>> {

   /**
    * Finishes the builder
    * 
    * @return The parent builder
    */
   P finishHeader();
}
