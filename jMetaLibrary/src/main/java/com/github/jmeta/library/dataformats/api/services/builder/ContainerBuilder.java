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
public interface ContainerBuilder<PayloadBuilder>
   extends DataFormatSpecificationBuilder, DataBlockDescriptionModifier<ContainerBuilder<PayloadBuilder>> {

   PayloadBuilder getPayload();

   HeaderBuilder<PayloadBuilder> addHeader(String localId, String name, String description);

   FooterBuilder<PayloadBuilder> addFooter(String localId, String name, String description);

   ContainerBasedPayloadBuilder finishContainer();
}
