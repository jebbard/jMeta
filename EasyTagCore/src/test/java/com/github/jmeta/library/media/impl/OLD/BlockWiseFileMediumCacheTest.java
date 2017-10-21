/**
 *
 * {@link BlockWiseFileMediumCacheTest}.java
 *
 * @author Jens Ebert
 *
 * @date 17.04.2011
 */
package com.github.jmeta.library.media.impl.OLD;

import com.github.jmeta.library.media.api.types.AbstractMedium;
import com.github.jmeta.library.media.impl.OLD.IMediumCache;
import com.github.jmeta.library.media.impl.OLD.StandardMediumCache;
import com.github.jmeta.library.media.impl.mediumAccessor.FileMediumAccessor;

/**
 * {@link BlockWiseFileMediumCacheTest} tests the {@link StandardMediumCache} with a {@link AbstractMedium}, thereby
 * giving a small maximum block-size for the medium cache regions to test block-wise caching.
 */
public class BlockWiseFileMediumCacheTest extends FileMediumCacheTest {

   private static final int MAXIMUM_CACHE_BLOCK_SIZE = 20;

   private IMediumCache m_theCache = null;

   /**
    * Creates a new {@BlockWiseFileMediumCacheTest }.
    */
   public BlockWiseFileMediumCacheTest() {

   }

   /**
    * @see IMediumCacheTest#getMaximumCacheSize()
    */
   @Override
   protected int getMaximumCacheSize() {

      return MAXIMUM_CACHE_BLOCK_SIZE;
   }

   /**
    * @see FileMediumCacheTest#getTestling()
    */
   @Override
   protected IMediumCache getTestling() {

      if (m_theCache == null) {
         final FileMediumAccessor accessor = new FileMediumAccessor(
            getExpectedMedium());

         m_theCache = new StandardMediumCache(accessor,
            MAXIMUM_CACHE_BLOCK_SIZE);
      }

      return m_theCache;
   }
}
