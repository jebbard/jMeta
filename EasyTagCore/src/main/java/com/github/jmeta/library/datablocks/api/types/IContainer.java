/**
 * {@link IContainer}.java
 *
 * @author Jens Ebert
 * @date 31.12.10 19:47:11 (December 31, 2010)
 */

package com.github.jmeta.library.datablocks.api.types;

import java.awt.Container;
import java.util.List;

/**
 * An {@link IContainer} represents the integral parts of the so called container formats such as MP3, Ogg, TIFF and so
 * on. These parts are always called differently in any data format: Might it be <i>chunk</i>, <i>page</i>,
 * <i>packet</i>, <i>segment</i>, <i>frame</i> and so on - the basic principles are the same. Thereby, a container part
 * wraps payload data, usually multimedia data such as audio, image, textual or video data, but also metadata. It
 * contains metainformation required for parsing the payload. The structure of an {@link IContainer} in the same data
 * format is well-defined which usually allows extensions of the data format with new container parts,
 * "without breaking existing software". The latter sentence is an important phrase often written when describing the
 * advantages of such "chunky" formats. The idea is that software that does not know the type of a container part can
 * safely skip and ignore it.
 * 
 * Here, an {@link IContainer} consists of an arbitrary number of optional headers and footers, both represented by
 * {@link IHeader} instances and exactly one {@link IPayload} .
 */
public interface IContainer extends IDataBlock {

   /**
    * Returns the {@link IPayload} of this {@link Container}.
    * 
    * @return the {@link IPayload} of this {@link IContainer}.
    */
   public IPayload getPayload();

   /**
    * Returns the headers of this {@link IContainer}. This might return an empty {@link List} if there are no headers.
    * 
    * @return the {@link IHeader}s of this {@link IContainer}, maybe zero headers.
    */
   public List<IHeader> getHeaders();

   /**
    * Returns the footers of this {@link IContainer}. This might return an empty {@link List} if there are no footers.
    * 
    * @return the footers of this {@link IContainer}, maybe zero footers.
    */
   public List<IHeader> getFooters();

}
