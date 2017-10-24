/**
 *
 * {@link StandardMediaAPI}.java
 *
 * @author Jens
 *
 * @date 16.05.2015
 *
 */
package com.github.jmeta.library.media.api.services;

import com.github.jmeta.library.media.api.OLD.IMediumStore_OLD;
import com.github.jmeta.library.media.api.types.Medium;

/**
 * {@link MediaAPI} provides access to all functionality of the component Media.
 */
public interface MediaAPI {

   /**
    * Returns a suitable {@link IMediumStore_OLD} for the given readable {@link Medium}.
    * 
    * @param medium
    *           The {@link Medium} to use. Must be a supported implementation of {@link Medium}. You must not use own
    *           implementations of this interface here.
    * @return an {@link IMediumStore_OLD} for the given {@link Medium}.
    * 
    * @pre medium must be a supported {@link Medium} implementation.
    */
   public IMediumStore_OLD getMediumStore(Medium<?> medium);
}