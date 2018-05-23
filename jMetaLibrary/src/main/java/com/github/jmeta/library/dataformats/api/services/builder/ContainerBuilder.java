/**
 *
 * {@link ContainerBuilder}.java
 *
 * @author Jens Ebert
 *
 * @date 12.05.2018
 *
 */
package com.github.jmeta.library.dataformats.api.services.builder;

/**
 * {@link ContainerBuilder}
 *
 */
public interface ContainerBuilder<PB> extends DataBlockDescriptionBuilder<ContainerBuilder<PB>> {

   PB getPayload();

   HeaderBuilder<ContainerBuilder<PB>> addHeader(String localId, String name, String description);

   FooterBuilder<ContainerBuilder<PB>> addFooter(String localId, String name, String description);

   ContainerBasedPayloadBuilder finishContainer();

   ContainerBuilder<PB> asDefaultNestedContainer();
}
