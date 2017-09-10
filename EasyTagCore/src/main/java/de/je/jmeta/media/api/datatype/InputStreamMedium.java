/**
 *
 * {@link InputStreamMedium}.java
 *
 * @author Jens
 *
 * @date 16.05.2015
 *
 */
package de.je.jmeta.media.api.datatype;

import java.io.InputStream;
import java.nio.ByteBuffer;

import de.je.jmeta.media.api.IMedium;
import de.je.jmeta.media.api.OLD.IMediumStore_OLD;
import de.je.jmeta.media.api.exception.ReadTimedOutException;
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
      InputStreamMedium.class.getName() + ".MAX_CACHE_REGION_SIZE",
      Integer.MAX_VALUE, 1, Integer.MAX_VALUE);

   /**
    * Parameter for setting the maximum cache size, default is: Unlimited cache size. Changing the max cache size
    * immediately frees up data if the new max cache size is exceeded by current cache contents.
    */
   public final static AbstractConfigParam<Long> MAX_CACHE_SIZE = new LongConfigParam(
      InputStreamMedium.class.getName() + ".MAX_CACHE_SIZE", Long.MAX_VALUE, 1L,
      Long.MAX_VALUE);

   /**
    * Parameter for enabling that bytes are skipped when reading forward, default is: false. This parameter influences
    * the behavior of read operations on {@link InputStreamMedium}s. If this parameter is set to true, then any bytes
    * between the last read offset and the read offset of the next read operation on an {@link InputStreamMedium} (if
    * any) are skipped instead of being read to the cache.
    */
   public final static AbstractConfigParam<Boolean> SKIP_ON_FORWARD_READ = new BooleanConfigParam(
      InputStreamMedium.class.getName() + ".SKIP_ON_FORWARD_READ", false);

   /**
    * Parameter for setting the maximum read timeout in milliseconds, default is: 0 milliseconds, i.e. no timeout, but
    * the read calls are expected to return immediately on first call. This parameter influences the behavior of read
    * operations on {@link InputStreamMedium}s. If this parameter is set to true, then any bytes between the last read
    * offset and the read offset of the next read operation on an {@link InputStreamMedium} (if any) are skipped instead
    * of being read to the cache. Note that the timeout value, i.e. the time read is blocking if no data arrives, is
    * only a rough guideline that is not guaranteed to be matched exactly. Especially when the timeout value is small,
    * it might be more inaccurate. Typically, for timeout values >= 500 ms, it will be accurate to a few milliseconds.
    */
   public final static AbstractConfigParam<Integer> READ_TIMEOUT_MILLIS = new IntegerConfigParam(
      InputStreamMedium.class.getName() + ".READ_TIMEOUT_MILLIS", 0, 0,
      Integer.MAX_VALUE);

   /**
    * Timeout value to be used for "no blocking timeout" leading to reads that do not timeout when blocking, blocking
    * maybe infinitely.
    */
   public static final int NO_TIMEOUT = 0;

   private int timeoutMillis = NO_TIMEOUT;

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
         new AbstractConfigParam<?>[] { ENABLE_CACHING, MAX_CACHE_REGION_SIZE,
            MAX_CACHE_SIZE, SKIP_ON_FORWARD_READ, READ_TIMEOUT_MILLIS });
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

      return UNKNOWN_LENGTH;
   }

   /**
    * Returns the timeout value in milliseconds used for blocking read operations.
    *
    * @see #setReadTimeout(int) for details.
    *
    * @return The timeout value in milliseconds. {@link #NO_TIMEOUT} for no timeout.
    */
   public int getReadTimeout() {

      return timeoutMillis;
   }

   /**
    * Sets the timeout value in milliseconds used for blocking read operations. If set to a positive value other than
    * {@link #NO_TIMEOUT}, the {@link IMediumStore_OLD} read operations will throw an {@link ReadTimedOutException} during
    * reading, if if the set timeout has expired and the full number of bytes requested for reading has not yet been
    * read. In case of an expired timeout, the {@link ByteBuffer} given to the operation contains all the bytes read
    * already as the remaining bytes until the timeout has expired.
    *
    * If the timeout is set to {@link #NO_TIMEOUT}, the blocking methods block until new content is there without timing
    * out.
    *
    * Note that the timeout value, i.e. the time read is blocking if no data arrives, is only a rough guideline that is
    * not guaranteed to be matched exactly. Especially when the timeout value is small, it might be more inaccurate.
    * Typically, for timeout values >= 500 ms, it will be accurate to a few milliseconds
    *
    * @param timeoutMillis
    *           The timeout value given in milliseconds. {@link #NO_TIMEOUT} for no timeout. Must not be negative.
    *
    * @pre timeoutMillis >= 0
    */
   public void setReadTimeout(int timeoutMillis) {

      this.timeoutMillis = timeoutMillis;
   }
}
