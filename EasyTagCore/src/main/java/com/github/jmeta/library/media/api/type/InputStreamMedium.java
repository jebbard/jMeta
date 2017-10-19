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

import de.je.util.javautil.common.configparams.AbstractConfigParam;
import de.je.util.javautil.common.configparams.BooleanConfigParam;
import de.je.util.javautil.common.configparams.IntegerConfigParam;
import de.je.util.javautil.common.configparams.LongConfigParam;

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
    * Parameter for enabling caching, default is: Caching is enabled. Disabling caching will empty current cache
    * contents.
    */
   public final static AbstractConfigParam<Boolean> ENABLE_CACHING = new BooleanConfigParam(
      InputStreamMedium.class.getName() + ".ENABLE_CACHING", true);

   /**
    * Parameter for setting the maximum cache region size, default is: Unlimited cache region size. Changing the max
    * cache region size will not have any effect on existing cache regions
    */
   public final static AbstractConfigParam<Integer> MAX_CACHE_REGION_SIZE = new IntegerConfigParam(
      InputStreamMedium.class.getName() + ".MAX_CACHE_REGION_SIZE", Integer.MAX_VALUE, 1, Integer.MAX_VALUE);

   /**
    * Parameter for setting the maximum cache size, default is: Unlimited cache size. Changing the max cache size
    * immediately frees up data if the new max cache size is exceeded by current cache contents.
    */
   public final static AbstractConfigParam<Long> MAX_CACHE_SIZE = new LongConfigParam(
      InputStreamMedium.class.getName() + ".MAX_CACHE_SIZE", Long.MAX_VALUE, 1L, Long.MAX_VALUE);

   /**
    * Parameter for enabling that bytes are skipped when reading forward, default is: false. This parameter influences
    * the behavior of read operations on {@link InputStreamMedium}s. If this parameter is set to true, then any bytes
    * between the last read offset and the read offset of the next read operation on an {@link InputStreamMedium} (if
    * any) are skipped instead of being read to the cache.
    */
   public final static AbstractConfigParam<Boolean> SKIP_ON_FORWARD_READ = new BooleanConfigParam(
      InputStreamMedium.class.getName() + ".SKIP_ON_FORWARD_READ", false);

   /**
    * Creates a new {@link InputStreamMedium}.
    * 
    * @param medium
    *           The {@link InputStream} to use.
    * @param name
    *           A name of the {@link InputStream} to be able to identify it. Optional, null may be passed.
    */
   public InputStreamMedium(InputStream medium, String name) {

      super(medium, name, false, true,
         new AbstractConfigParam<?>[] { ENABLE_CACHING, MAX_CACHE_REGION_SIZE, MAX_CACHE_SIZE, SKIP_ON_FORWARD_READ, });
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
