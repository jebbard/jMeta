/**
 * {@link Payload}.java
 *
 * @author Jens Ebert
 * @date 31.12.10 19:47:11 (December 31, 2010)
 */

package com.github.jmeta.library.datablocks.api.types;

/**
 * The {@link Payload} is the part of the data of a {@link Container} that holds the information valuable for the user,
 * while {@link Header}s usually only store technical and version metadata information mainly required for parsing.
 *
 * The {@link Payload} might consist either of {@link Field}s or of child {@link Container}s, never both. See the
 * corresponding sub-interfaces for details.
 */
public interface Payload extends DataBlock {

}
