/**
 *
 * {@link StandardMediaAPI}.java
 *
 * @author Jens
 *
 * @date 18.05.2015
 *
 */
package com.github.jmeta.library.media.impl;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.github.jmeta.library.media.api.OLD.IMediumStore_OLD;
import com.github.jmeta.library.media.api.services.MediaAPI;
import com.github.jmeta.library.media.api.types.FileMedium;
import com.github.jmeta.library.media.api.types.InMemoryMedium;
import com.github.jmeta.library.media.api.types.InputStreamMedium;
import com.github.jmeta.library.media.api.types.Medium;
import com.github.jmeta.library.media.impl.OLD.MediumCache;
import com.github.jmeta.library.media.impl.OLD.StandardMediumCache;
import com.github.jmeta.library.media.impl.mediumAccessor.FileMediumAccessor;
import com.github.jmeta.library.media.impl.mediumAccessor.MediumAccessor;
import com.github.jmeta.library.media.impl.mediumAccessor.MemoryMediumAccessor;
import com.github.jmeta.library.media.impl.mediumAccessor.StreamMediumAccessor;
import com.github.jmeta.utility.dbc.api.services.Reject;

/**
 * {@link StandardMediaAPI} is the default implementation of the {@link StandardMediaAPI} interface.
 */
public class StandardMediaAPI implements MediaAPI {

   private static final Set<Class<? extends Medium<?>>> SUPORTED_MEDIA_CLASSES = new HashSet<>();

   static {
      SUPORTED_MEDIA_CLASSES.add(FileMedium.class);
      SUPORTED_MEDIA_CLASSES.add(InMemoryMedium.class);
      SUPORTED_MEDIA_CLASSES.add(InputStreamMedium.class);
   }

   private final Map<Medium<?>, MediumCache> m_alreadyCreatedCaches = new HashMap<>();

   /**
    * @see com.github.jmeta.library.media.api.services.MediaAPI#getMediumStore(com.github.jmeta.library.media.api.types.Medium)
    */
   @Override
   public IMediumStore_OLD getMediumStore(Medium<?> medium) {

      Reject.ifNull(medium, "medium");

      Reject.ifFalse(SUPORTED_MEDIA_CLASSES.contains(medium.getClass()),
         "SUPORTED_MEDIA_CLASSES.contains(medium.getClass())");

      if (!m_alreadyCreatedCaches.containsKey(medium)) {
         int maxCacheRegionSize = Integer.MAX_VALUE;

         MediumAccessor<?> mediumAccessor = null;

         if (medium.getClass() == FileMedium.class)
            mediumAccessor = new FileMediumAccessor((FileMedium) medium);

         else if (medium.getClass() == InMemoryMedium.class)
            mediumAccessor = new MemoryMediumAccessor((InMemoryMedium) medium);

         else if (medium.getClass() == InputStreamMedium.class)
            mediumAccessor = new StreamMediumAccessor((InputStreamMedium) medium);

         m_alreadyCreatedCaches.put(medium, new StandardMediumCache(mediumAccessor, maxCacheRegionSize));
      }

      return m_alreadyCreatedCaches.get(medium);
   }

}
