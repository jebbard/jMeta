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
import com.github.jmeta.library.dataformats.api.types.DataBlockId;

/**
 * {@link SizeProvider}
 *
 */
public interface SizeProvider {

   public long getSizeOf(DataBlockId id, int sequenceNumber, ContainerContext containerContext);
}
