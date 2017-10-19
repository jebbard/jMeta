package com.github.jmeta.library.media.impl.OLD;

import java.util.Map;

import com.github.jmeta.library.media.api.OLD.IMediumStore_OLD;
import com.github.jmeta.library.media.api.type.IMediumReference;

/**
 * {@link IMediumCache} is a cache extension of {@link IMediumStore_OLD} and provides fine grained access to pre-loaded data
 * regions.
 */
public interface IMediumCache extends IMediumStore_OLD {

   /**
    * Returns the regions currently pre-loaded by this {@link IMediumCache}, i.e. each {@link IMediumReference} and
    * cached size for that reference.
    * 
    * @return the regions currently pre-loaded by this {@link IMediumCache}, i.e. each {@link IMediumReference} and
    *         cached size for that reference. No particular order of the returned {@link IMediumReference}s is
    *         guaranteed.
    */
   public Map<IMediumReference, Integer> getBufferedRegions();
}