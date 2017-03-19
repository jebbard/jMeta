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
import de.je.jmeta.media.api.IMediumStore;
import de.je.jmeta.media.api.datatype.FileMedium;
import de.je.jmeta.media.api.datatype.InMemoryMedium;
import de.je.jmeta.media.api.datatype.InputStreamMedium;
import de.je.jmeta.media.api.exception.MediumAccessException;
import de.je.util.javautil.common.err.Contract;
import de.je.util.javautil.common.err.Reject;
import de.je.util.javautil.simpleregistry.AbstractComponentImplementation;
import de.je.util.javautil.simpleregistry.ComponentDescription;
import de.je.util.javautil.simpleregistry.ISimpleComponentRegistry;

/**
 * {@link MediaAPI} is the default implementation of the {@link IMediaAPI} interface.
 */
public class MediaAPI extends AbstractComponentImplementation<IMediaAPI>
   implements IMediaAPI {

   private static final Set<Class<? extends IMedium<?>>> SUPORTED_MEDIA_CLASSES = new HashSet<>();

   private static final ComponentDescription<IMediaAPI> COMPONENT_DESCRIPTION = new ComponentDescription<>(
      "Media", IMediaAPI.class, "Jens Ebert", "v0.1",
      "Component for low-level access to physical media");

   static {
      SUPORTED_MEDIA_CLASSES.add(FileMedium.class);
      SUPORTED_MEDIA_CLASSES.add(InMemoryMedium.class);
      SUPORTED_MEDIA_CLASSES.add(InputStreamMedium.class);
   }

   private final Map<IMedium<?>, IMediumCache> m_alreadyCreatedCaches = new HashMap<>();

   /**
    * Creates a new {@link MediaAPI}.
    * 
    * @param registry
    *           The {@link ISimpleComponentRegistry} instance
    */
   public MediaAPI(ISimpleComponentRegistry registry) {
      super(COMPONENT_DESCRIPTION, IMediaAPI.class, registry);
   }

   /**
    * @see de.je.jmeta.media.api.IMediaAPI#getMediumStore(de.je.jmeta.media.api.IMedium)
    */
   @Override
   public IMediumStore getMediumStore(IMedium<?> medium) {

      Reject.ifNull(medium, "medium");

      Contract.checkPrecondition(
         SUPORTED_MEDIA_CLASSES.contains(medium.getClass()),
         "The given medium <" + medium
            + "> does not have one of the supported media types: <"
            + SUPORTED_MEDIA_CLASSES + ">.");

      if (!medium.exists())
         throw new MediumAccessException(
            "Medium <" + medium + "> does not exist.", null);

      if (!m_alreadyCreatedCaches.containsKey(medium)) {
         int maxCacheRegionSize = Integer.MAX_VALUE;

         IMediumAccessor<?> mediumAccessor = null;

         if (medium.getClass() == FileMedium.class)
            mediumAccessor = new FileMediumAccessor((FileMedium) medium);

         else if (medium.getClass() == InMemoryMedium.class)
            mediumAccessor = new MemoryMediumAccessor((InMemoryMedium) medium);

         else if (medium.getClass() == InputStreamMedium.class)
            mediumAccessor = new StreamMediumAccessor(
               (InputStreamMedium) medium);

         m_alreadyCreatedCaches.put(medium,
            new StandardMediumCache(mediumAccessor, maxCacheRegionSize));
      }

      return m_alreadyCreatedCaches.get(medium);
   }

}
