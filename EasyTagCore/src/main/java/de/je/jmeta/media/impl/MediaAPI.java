/**
 *
 * {@link MediaAPI}.java
 *
 * @author Jens
 *
 * @date 18.05.2015
 *
 */
package de.je.jmeta.media.impl;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import de.je.jmeta.media.api.IMediaAPI;
import de.je.jmeta.media.api.IMedium;
import de.je.jmeta.media.api.OLD.IMediumStore_OLD;
import de.je.jmeta.media.api.datatype.FileMedium;
import de.je.jmeta.media.api.datatype.InMemoryMedium;
import de.je.jmeta.media.api.datatype.InputStreamMedium;
import de.je.jmeta.media.api.exception.MediumAccessException;
import de.je.jmeta.media.impl.OLD.IMediumCache;
import de.je.jmeta.media.impl.OLD.StandardMediumCache;
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
    * @see de.je.jmeta.media.api.IMediaAPI#getMediumStore(de.je.jmeta.media.api.IMedium)
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
