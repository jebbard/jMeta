/**
 *
 * {@link InputStreamMedium}.java
 *
 * @author Jens
 *
 * @date 16.05.2015
 *
 */
package com.github.jmeta.library.media.api.type;

import java.io.InputStream;

/**
 * {@link InputStreamMedium} represents an {@link InputStream} as read-only {@link IMedium}. It supports configuring a
 * read timeout in millisecond. This timeout will be used for every read operation on the {@link IMedium}. Use
 * {@value #NO_TIMEOUT} for using no read timeout.
 * 
 * It is not recommended to use {@link InputStreamMedium} for accessing files because it imposes possibly significant
 * performance drawbacks compared to using {@link FileMedium}.
 */
public class InputStreamMedium extends AbstractMedium<InputStream> {

   /**
    * Creates a new {@link InputStreamMedium}.
    * 
    * @param medium
    *           The {@link InputStream} to use.
    * @param name
    *           A name of the {@link InputStream} to be able to identify it. Optional, null may be passed.
    */
   public InputStreamMedium(InputStream medium, String name) {

      super(medium, name, false, true, true);
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

      return UNKNOWN_LENGTH;
   }
}
