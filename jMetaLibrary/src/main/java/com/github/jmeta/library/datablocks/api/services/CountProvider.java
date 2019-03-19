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
 * {@link CountProvider} allows extensions to implement their own way of determining the number of occurrences of the
 * given {@link DataBlockId}.
 */
@FunctionalInterface
public interface CountProvider {

   /**
    * Returns the number of occurrences of the given {@link DataBlockId} in its parent or
    * {@link DataBlockDescription#UNDEFINED} if it cannot determine a count for it.
    *
    * @param id
    *           The {@link DataBlockId}, must not be null
    * @param containerContext
    *           The {@link ContainerContext} of the currently parsed container, must not be null
    * @return the size of the given {@link DataBlockId} in its parent or {@link DataBlockDescription#UNDEFINED} if it
    *         cannot determine a count for it
    */
   public long getCountOf(DataBlockId id, ContainerContext containerContext);
}
