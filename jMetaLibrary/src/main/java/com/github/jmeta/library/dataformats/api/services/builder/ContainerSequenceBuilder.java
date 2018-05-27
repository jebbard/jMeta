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
 * {@link ContainerSequenceBuilder}
 *
 */
public interface ContainerSequenceBuilder<C extends ContainerSequenceBuilder<C>> {

   ContainerBuilder<C, FieldBasedPayloadBuilder<C>> addContainerWithFieldBasedPayload(String localId, String name,
      String description);

   ContainerBuilder<C, ContainerBasedPayloadBuilder<C>> addContainerWithContainerBasedPayload(String localId,
      String name, String description);

   ContainerBuilder<C, FieldBasedPayloadBuilder<C>> addGenericContainerWithFieldBasedPayload(String localId,
      String name, String description);

   ContainerBuilder<C, ContainerBasedPayloadBuilder<C>> addGenericContainerWithContainerBasedPayload(String localId,
      String name, String description);
}
