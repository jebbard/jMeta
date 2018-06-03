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
 * {@link FooterBuilder} allows to add fields to a footer data block
 *
 * @param <P>
 *           The concrete parent builder interface
 */
public interface FooterBuilder<P> extends FieldSequenceBuilder<FooterBuilder<P>> {

   /**
    * Finishes the builder
    * 
    * @return The parent builder
    */
   P finishFooter();
}
