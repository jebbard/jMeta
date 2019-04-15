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
 * An {@link Footer} may only consist of {@link Field}s.
 */
public interface Footer extends FieldSequence {
}
