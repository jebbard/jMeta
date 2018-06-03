/**
 *
 * {@link FieldBasedPayloadBuilder}.java
 *
 * @author Jens Ebert
 *
 * @date 12.05.2018
 *
 */
package com.github.jmeta.library.dataformats.api.services.builder;

/**
 * {@link FieldBasedPayloadBuilder} allows to add fields to a field-bayed payload data block.
 *
 * @param <P>
 *           The concrete parent builder interface
 */
public interface FieldBasedPayloadBuilder<P extends ContainerSequenceBuilder<P>>
   extends FieldSequenceBuilder<FieldBasedPayloadBuilder<P>> {

   /**
    * Finishes building the field-based payload
    * 
    * @return The parent builder
    */
   ContainerBuilder<P, FieldBasedPayloadBuilder<P>> finishFieldBasedPayload();
}
