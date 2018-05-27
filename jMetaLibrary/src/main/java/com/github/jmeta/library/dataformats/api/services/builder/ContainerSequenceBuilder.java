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

   ContainerBuilder<FieldBasedPayloadBuilder> addContainerWithFieldBasedPayload(String localId, String name,
      String description);

   ContainerBuilder<ContainerBasedPayloadBuilder> addContainerWithContainerBasedPayload(String localId, String name,
      String description);

   ContainerBuilder<FieldBasedPayloadBuilder> addGenericContainerWithFieldBasedPayload(String localId, String name,
      String description);

   ContainerBuilder<ContainerBasedPayloadBuilder> addGenericContainerWithContainerBasedPayload(String localId,
      String name, String description);
}
