/**
 *
 * {@link MediaAPI}.java
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
import com.github.jmeta.library.media.api.exception.MediumAccessException;
import com.github.jmeta.library.media.api.services.IMediaAPI;
import com.github.jmeta.library.media.api.type.FileMedium;
import com.github.jmeta.library.media.api.type.IMedium;
import com.github.jmeta.library.media.api.type.InMemoryMedium;
import com.github.jmeta.library.media.api.type.InputStreamMedium;
import com.github.jmeta.library.media.impl.OLD.IMediumCache;
import com.github.jmeta.library.media.impl.OLD.StandardMediumCache;
import com.github.jmeta.library.media.impl.mediumAccessor.FileMediumAccessor;
import com.github.jmeta.library.media.impl.mediumAccessor.IMediumAccessor;
import com.github.jmeta.library.media.impl.mediumAccessor.MemoryMediumAccessor;
import com.github.jmeta.library.media.impl.mediumAccessor.StreamMediumAccessor;

import de.je.util.javautil.common.err.Reject;

/**
 * {@link MediaAPI} is the default implementation of the {@link IMediaAPI} interface.
 */
public class MediaAPI implements IMediaAPI {

   private static final Set<Class<? extends IMedium<?>>> SUPORTED_MEDIA_CLASSES = new HashSet<>();

   static {
      SUPORTED_MEDIA_CLASSES.add(FileMedium.class);
      SUPORTED_MEDIA_CLASSES.add(InMemoryMedium.class);
      SUPORTED_MEDIA_CLASSES.add(InputStreamMedium.class);
   }

   private final Map<IMedium<?>, IMediumCache> m_alreadyCreatedCaches = new HashMap<>();

   /**
    * @see com.github.jmeta.library.media.api.services.IMediaAPI#getMediumStore(com.github.jmeta.library.media.api.type.IMedium)
    */
   @Override
   public IMediumStore_OLD getMediumStore(IMedium<?> medium) {

      Reject.ifNull(medium, "medium");

      Reject.ifFalse(SUPORTED_MEDIA_CLASSES.contains(medium.getClass()),
         "SUPORTED_MEDIA_CLASSES.contains(medium.getClass())");

      if (!medium.exists())
         throw new MediumAccessException("Medium <" + medium + "> does not exist.", null);

      if (!m_alreadyCreatedCaches.containsKey(medium)) {
         int maxCacheRegionSize = Integer.MAX_VALUE;

         IMediumAccessor<?> mediumAccessor = null;

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
