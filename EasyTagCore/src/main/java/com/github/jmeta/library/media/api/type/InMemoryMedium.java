/**
 *
 * {@link InMemoryMedium}.java
 *
 * @author Jens
 *
 * @date 16.05.2015
 *
 */
package com.github.jmeta.library.media.api.type;

import java.io.InputStream;

/**
 * {@link InMemoryMedium} represents data stored already in memory.
 */
public class InMemoryMedium extends AbstractMedium<byte[]> {

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
      super(medium, name, true, readOnly, false);
   }

   /**
    * @see com.github.jmeta.library.media.api.type.IMedium#exists()
    */
   @Override
   public boolean exists() {

      return true;
   }

   /**
    * @see com.github.jmeta.library.media.api.type.IMedium#getCurrentLength()
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
