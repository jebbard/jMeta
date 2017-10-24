package com.github.jmeta.library.media.impl.OLD;

import java.util.Map;

import com.github.jmeta.library.media.api.OLD.IMediumStore_OLD;
import com.github.jmeta.library.media.api.types.MediumReference;

/**
 * {@link MediumCache} is a cache extension of {@link IMediumStore_OLD} and provides fine grained access to pre-loaded data
 * regions.
 */
public interface MediumCache extends IMediumStore_OLD {

   /**
    * Returns the regions currently pre-loaded by this {@link MediumCache}, i.e. each {@link MediumReference} and
    * cached size for that reference.
    * 
    * @return the regions currently pre-loaded by this {@link MediumCache}, i.e. each {@link MediumReference} and
    *         cached size for that reference. No particular order of the returned {@link MediumReference}s is
    *         guaranteed.
    */
   public Map<MediumReference, Integer> getBufferedRegions();
}