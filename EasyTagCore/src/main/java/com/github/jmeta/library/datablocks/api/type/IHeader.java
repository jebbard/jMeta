/**
 * {@link IHeader}.java
 *
 * @author Jens Ebert
 * @date 31.12.10 19:47:11 (December 31, 2010)
 */

package com.github.jmeta.library.datablocks.api.type;

/**
 * Represents both <i>headers</i> and <i>footers</i>. These two types of {@link IDataBlock}s have the purpose to ease
 * parsing of a chunk of data. Most data formats define their data to be preceded or followed by a well-known, often
 * fixed-size header or footer. Such an {@link IDataBlock} is used to identify the data chunk to belong to a given data
 * format or to have a specific type.
 *
 * An {@link IHeader} may only consist of {@link IField}s.
 */
public interface IHeader extends IDataBlock, IFieldSequence {

   /**
    * Returns whether this {@link IHeader} instance represents a footer (= true) or a header (=false).
    *
    * @return true if this {@link IHeader} logically is a footer, false otherwise.
    */
   public boolean isFooter();

}
