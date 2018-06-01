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

import com.github.jmeta.library.dataformats.api.types.DataBlockId;

/**
 * {@link ContainerBuilder}
 *
 */
public interface ContainerBuilder<P extends ContainerSequenceBuilder<P>, PB>
   extends DataBlockDescriptionBuilder<ContainerBuilder<P, PB>> {

   PB getPayload();

   HeaderBuilder<ContainerBuilder<P, PB>> addHeader(String localId, String name, String description);

   FooterBuilder<ContainerBuilder<P, PB>> addFooter(String localId, String name, String description);

   P finishContainer();

   ContainerBuilder<P, PB> asDefaultNestedContainer();

   ContainerBuilder<P, PB> cloneFrom(DataBlockId existingContainerId);
}
