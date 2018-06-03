/**
 *
 * {@link ContainerBasedPayloadBuilder}.java
 *
 * @author Jens Ebert
 *
 * @date 12.05.2018
 *
 */
package com.github.jmeta.library.dataformats.api.services.builder;

/**
 * {@link ContainerBasedPayloadBuilder} allows to add child containers to a payload data block.
 *
 * @param <P>
 *           The concrete parent builder interface
 */
public interface ContainerBasedPayloadBuilder<P extends ContainerSequenceBuilder<P>>
   extends ContainerSequenceBuilder<ContainerBasedPayloadBuilder<P>>,
   DataBlockDescriptionBuilder<ContainerBasedPayloadBuilder<P>> {

   /**
    * Finishes building the container-based payload
    * 
    * @return The parent builder
    */
   ContainerBuilder<P, ContainerBasedPayloadBuilder<P>> finishContainerBasedPayload();
}
