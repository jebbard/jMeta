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

import com.github.jmeta.library.dataformats.api.types.DataBlockCrossReference;
import com.github.jmeta.library.dataformats.api.types.DataBlockId;

/**
 * {@link ContainerBuilder} allows to add child data blocks to a container. As every container must have exactly one
 * child payload data block, it is implicitly created and not added by the user. If the user needs to work with this
 * payload, he can use {@link #getPayload()} to retrieve it.
 *
 * @param <P>
 *           The concrete parent builder interface
 * @param <PB>
 *           The payload builder type
 */
public interface ContainerBuilder<P extends ContainerSequenceBuilder<P>, PB>
   extends DataBlockDescriptionBuilder<ContainerBuilder<P, PB>> {

   /**
    * Provides the implicitly created payload builder of this {@link ContainerBuilder}, such that e.g. its description
    * can be changed or children can be added to it.
    * 
    * @return the implicitly created payload builder of this {@link ContainerBuilder}
    */
   PB getPayload();

   /**
    * Adds a header to this container.
    * 
    * @param localId
    *           The local id of the new data block, must not be null, must not contain the character
    *           {@link DataBlockId#SEGMENT_SEPARATOR}
    * @param name
    *           The human-readable specification name of the data block or null
    * @param description
    *           A description of the data block preferably taken from the data format specification or null
    * @return The child builder for the new data block
    */
   HeaderBuilder<ContainerBuilder<P, PB>> addHeader(String localId, String name, String description);

   /**
    * Adds a footer to this container.
    * 
    * @param localId
    *           The local id of the new data block, must not be null, must not contain the character
    *           {@link DataBlockId#SEGMENT_SEPARATOR}
    * @param name
    *           The human-readable specification name of the data block or null
    * @param description
    *           A description of the data block preferably taken from the data format specification or null
    * @return The child builder for the new data block
    */
   FooterBuilder<ContainerBuilder<P, PB>> addFooter(String localId, String name, String description);

   /**
    * Finishes building the container
    * 
    * @return The parent builder
    */
   P finishContainer();

   /**
    * Tags the container built by this {@link ContainerBuilder} as default nested container.
    * 
    * @return the current builder
    */
   ContainerBuilder<P, PB> asDefaultNestedContainer();

   /**
    * Clones all properties of the given container id into this container. This method implicitly validates the the
    * given container exists and has the same type, and that all of its children are valid. If this is not the case,
    * this method directly throws an {@link IllegalArgumentException}.
    * 
    * @param existingContainerRef
    *           The existing container {@link DataBlockCrossReference}, must indeed refer to a container of the same
    *           type (field-based or container-based payload) and must exist in the root builder already, i.e. it must
    *           have been already finished prior to calling this method
    * @return the current builder
    */
   ContainerBuilder<P, PB> cloneFrom(DataBlockCrossReference existingContainerRef);
}
