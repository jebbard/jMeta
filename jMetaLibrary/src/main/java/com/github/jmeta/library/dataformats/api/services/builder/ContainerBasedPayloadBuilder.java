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
 * {@link ContainerBasedPayloadBuilder}
 *
 */
public interface ContainerBasedPayloadBuilder extends ContainerSequenceBuilder<ContainerBasedPayloadBuilder>,
   DataBlockDescriptionBuilder<ContainerBasedPayloadBuilder> {

   ContainerBuilder<ContainerBasedPayloadBuilder> finishContainerBasedPayload();
}
