package de.je.jmeta.media.impl;

import java.util.Map;

import de.je.jmeta.media.api.IMediumReference;
import de.je.jmeta.media.api.IMediumStore;

/**
 * {@link IMediumCache} is a cache extension of {@link IMediumStore} and provides fine grained access to pre-loaded data
 * regions.
 */
public interface IMediumCache extends IMediumStore {

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