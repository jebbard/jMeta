/**
 * {@link AbstractDataBlockIterator}.java
 *
 * @author Jens Ebert
 * @date 31.12.10 19:47:07 (December 31, 2010)
 */

package de.je.jmeta.datablocks;

import java.util.Iterator;

/**
 * An {@link Iterator} used for iterating over {@link IDataBlock}s. This {@link Iterator} will throw an
 * {@link UnsupportedOperationException} when the #remove() method is called.
 * 
 * The #next() method might throw an {@link UnknownDataFormatException} which is a {@link RuntimeException}.
 *
 * @param <T>
 */
public abstract class AbstractDataBlockIterator<T extends IDataBlock>
   implements Iterator<T> {

   /**
    * @see java.util.Iterator#remove()
    */
   @Override
   public void remove() {

      throw new UnsupportedOperationException(
         "remove operation is unsupported");
   }
}
