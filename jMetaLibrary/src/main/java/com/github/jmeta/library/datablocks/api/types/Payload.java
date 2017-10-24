/**
 * {@link Payload}.java
 *
 * @author Jens Ebert
 * @date 31.12.10 19:47:11 (December 31, 2010)
 */

package com.github.jmeta.library.datablocks.api.types;

import java.util.Iterator;

import com.github.jmeta.library.datablocks.api.services.AbstractDataBlockIterator;

/**
 * The {@link Payload} is the part of the data of an {@link Container} that holds the information valuable for the
 * user, while {@link Header}s usually only store technical and version information mainly required for parsing.
 *
 * The {@link Payload} might both consist of {@link Field}s or child {@link Container}s. Usually, it is an either or.
 * E.g. an ID3v2.3 tag has a payload that usually contains child {@link Container}s only while these {@link Container}
 * children are the metadata attributes that usually only contain {@link Field}s as children of their {@link Payload}.
 *
 * However, there might be cases where an {@link Payload} both contains {@link Container} and {@link Field} children,
 * so always both should be considered.
 */
public interface Payload extends DataBlock, FieldSequence {

   /**
    * Returns the child {@link Container}s contained in this {@link Payload}. If there are no {@link Container}
    * children, the returned {@link Iterator} will not return any {@link Container}s.
    *
    * @return an {@link Iterator} for retrieving the child {@link Container}s of this {@link Payload}. Might return an
    *         {@link Iterator} that does not return any children if this {@link Payload} has none.
    */
   public AbstractDataBlockIterator<Container> getContainerIterator();
}
