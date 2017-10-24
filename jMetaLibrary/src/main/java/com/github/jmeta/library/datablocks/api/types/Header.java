/**
 * {@link Header}.java
 *
 * @author Jens Ebert
 * @date 31.12.10 19:47:11 (December 31, 2010)
 */

package com.github.jmeta.library.datablocks.api.types;

/**
 * Represents both <i>headers</i> and <i>footers</i>. These two types of {@link DataBlock}s have the purpose to ease
 * parsing of a chunk of data. Most data formats define their data to be preceded or followed by a well-known, often
 * fixed-size header or footer. Such an {@link DataBlock} is used to identify the data chunk to belong to a given data
 * format or to have a specific type.
 *
 * An {@link Header} may only consist of {@link Field}s.
 */
public interface Header extends DataBlock, FieldSequence {

   /**
    * Returns whether this {@link Header} instance represents a footer (= true) or a header (=false).
    *
    * @return true if this {@link Header} logically is a footer, false otherwise.
    */
   public boolean isFooter();

}
