/**
 *
 * {@link SizeProvider}.java
 *
 * @author Jens Ebert
 *
 * @date 02.03.2019
 *
 */
package com.github.jmeta.library.datablocks.api.services;

import com.github.jmeta.library.datablocks.api.types.ContainerContext;
import com.github.jmeta.library.dataformats.api.types.DataBlockDescription;
import com.github.jmeta.library.dataformats.api.types.DataBlockId;

/**
 * {@link SizeProvider} allows extensions to implement their own way of determining the size of the given
 * {@link DataBlockId}.
 */
@FunctionalInterface
public interface SizeProvider {

   /**
    * Returns the size of the given {@link DataBlockId} with the given sequence number in its parent or
    * {@link DataBlockDescription#UNDEFINED} if it cannot determine a size for it.
    *
    * @param id
    *           The {@link DataBlockId}, must not be null
    * @param sequenceNumber
    *           The sequence number, must be positive
    * @param containerContext
    *           The {@link ContainerContext} of the currently parsed container, must not be null
    * @return the size of the given {@link DataBlockId} with the given sequence number in its parent or
    *         {@link DataBlockDescription#UNDEFINED} if it cannot determine a size for it
    */
   public long getSizeOf(DataBlockId id, int sequenceNumber, ContainerContext containerContext);
}
