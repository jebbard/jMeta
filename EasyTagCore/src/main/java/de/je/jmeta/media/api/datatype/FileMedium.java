/**
 *
 * {@link FileMedium}.java
 *
 * @author Jens
 *
 * @date 16.05.2015
 *
 */
package de.je.jmeta.media.api.datatype;

import java.io.File;

import de.je.util.javautil.common.configparams.AbstractConfigParam;
import de.je.util.javautil.common.configparams.BooleanConfigParam;
import de.je.util.javautil.common.configparams.IntegerConfigParam;
import de.je.util.javautil.common.configparams.LongConfigParam;

/**
 * {@link FileMedium} represents a {@link File} that is both readable and writable.
 */
public class FileMedium extends AbstractMedium<File> {

   /**
    * Parameter for enabling caching, default is: Caching is enabled. Disabling caching will empty current cache
    * contents.
    */
   public final static AbstractConfigParam<Boolean> ENABLE_CACHING = new BooleanConfigParam(
      FileMedium.class.getName() + ".ENABLE_CACHING", true);

   /**
    * Parameter for setting the maximum cache region size, default is: Unlimited cache region size. Changing the max
    * cache region size will not have any effect on existing cache regions
    */
   public final static AbstractConfigParam<Integer> MAX_CACHE_REGION_SIZE = new IntegerConfigParam(
      FileMedium.class.getName() + ".MAX_CACHE_REGION_SIZE", Integer.MAX_VALUE,
      1, Integer.MAX_VALUE);

   /**
    * Parameter for setting the maximum write block size, default is: 8192 bytes. It determines the maximum number of
    * bytes read or written during a flush operation.
    */
   public final static AbstractConfigParam<Integer> MAX_WRITE_BLOCK_SIZE = new IntegerConfigParam(
      FileMedium.class.getName() + ".MAX_WRITE_BLOCK_SIZE", 8192, 1,
      Integer.MAX_VALUE);

   /**
    * Parameter for setting the maximum cache size, default is: Unlimited cache size. Changing the max cache size
    * immediately frees up data if the new max cache size is exceeded by current cache contents.
    */
   public final static AbstractConfigParam<Long> MAX_CACHE_SIZE = new LongConfigParam(
      FileMedium.class.getName() + ".MAX_CACHE_SIZE", Long.MAX_VALUE, 1L,
      Long.MAX_VALUE);

   /**
    * Creates a new {@link FileMedium}.
    * 
    * @param medium
    *           The {@link File} this {@link FileMedium} refers to.
    * @param readOnly
    *           true to make this {@link FileMedium} read-only, false enables read and write.
    */
   public FileMedium(File medium, boolean readOnly) {

      super(medium, medium.getAbsolutePath(), true, readOnly,
         new AbstractConfigParam<?>[] { ENABLE_CACHING, MAX_CACHE_REGION_SIZE,
            MAX_WRITE_BLOCK_SIZE, MAX_CACHE_SIZE });
   }

   /**
    * @see de.je.jmeta.media.api.IMedium#exists()
    */
   @Override
   public boolean exists() {

      return getWrappedMedium().exists();
   }

   /**
    * @see de.je.jmeta.media.api.IMedium#getCurrentLength()
    */
   @Override
   public long getCurrentLength() {

      return getWrappedMedium().length();
   }
}
