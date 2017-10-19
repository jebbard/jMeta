/**
 * {@link IPayload}.java
 *
 * @author Jens Ebert
 * @date 31.12.10 19:47:11 (December 31, 2010)
 */

package com.github.jmeta.library.datablocks.api.type;

import java.util.Iterator;

import com.github.jmeta.library.datablocks.api.services.AbstractDataBlockIterator;

/**
 * The {@link IPayload} is the part of the data of an {@link IContainer} that holds the information valuable for the
 * user, while {@link IHeader}s usually only store technical and version information mainly required for parsing.
 *
 * The {@link IPayload} might both consist of {@link IField}s or child {@link IContainer}s. Usually, it is an either or.
 * E.g. an ID3v2.3 tag has a payload that usually contains child {@link IContainer}s only while these {@link IContainer}
 * children are the metadata attributes that usually only contain {@link IField}s as children of their {@link IPayload}.
 *
 * However, there might be cases where an {@link IPayload} both contains {@link IContainer} and {@link IField} children,
 * so always both should be considered.
 */
public interface IPayload extends IDataBlock, IFieldSequence {

   /**
    * Returns the child {@link IContainer}s contained in this {@link IPayload}. If there are no {@link IContainer}
    * children, the returned {@link Iterator} will not return any {@link IContainer}s.
    *
    * @return an {@link Iterator} for retrieving the child {@link IContainer}s of this {@link IPayload}. Might return an
    *         {@link Iterator} that does not return any children if this {@link IPayload} has none.
    */
   public AbstractDataBlockIterator<IContainer> getContainerIterator();
}
