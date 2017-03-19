/**
 *
 * {@link InMemoryMedium}.java
 *
 * @author Jens
 *
 * @date 16.05.2015
 *
 */
package de.je.jmeta.media.api.datatype;

import java.io.InputStream;

import de.je.util.javautil.common.configparams.AbstractConfigParam;
import de.je.util.javautil.common.configparams.IntegerConfigParam;

/**
 * {@link InMemoryMedium} represents data stored already in memory.
 */
public class InMemoryMedium extends AbstractMedium<byte[]> {

   /**
    * Parameter for setting the maximum write block size, default is: 8192 bytes. It determines the maximum number of
    * bytes read or written during a flush operation.
    */
   public final static AbstractConfigParam<Integer> MAX_WRITE_BLOCK_SIZE = new IntegerConfigParam(
      InMemoryMedium.class.getName() + ".MAX_WRITE_BLOCK_SIZE", 8192, 1,
      Integer.MAX_VALUE);

   /**
    * Creates a new {@link InMemoryMedium}.
    * 
    * @param medium
    *           The data store.
    * @param name
    *           A name of the {@link InputStream} to be able to identify it. Optional, i.e. null may be passed.
    * @param readOnly
    *           true to make this {@link InMemoryMedium} read-only, false enables read and write.
    */
   public InMemoryMedium(byte[] medium, String name, boolean readOnly) {

      super(medium, name, true, readOnly,
         new AbstractConfigParam<?>[] { MAX_WRITE_BLOCK_SIZE });
   }

   /**
    * @see de.je.jmeta.media.api.IMedium#exists()
    */
   @Override
   public boolean exists() {

      return true;
   }

   /**
    * @see de.je.jmeta.media.api.IMedium#getCurrentLength()
    */
   @Override
   public long getCurrentLength() {

      return getWrappedMedium().length;
   }

   /**
    * Provides the possibility to reset the current medium bytes.
    * 
    * @param mediumBytes
    *           The bytes to set
    */
   public void setBytes(byte[] mediumBytes) {

      setNewMediumContent(mediumBytes);
   }
}
