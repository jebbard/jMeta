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

import com.github.jmeta.library.media.api.types.Medium;

/**
 * {@link MediaAPI} provides access to all functionality of the component Media.
 */
public interface MediaAPI {

   /**
    * Creates a new {@link MediumStore} instance.
    * 
    * @param medium
    *           The {@link Medium} to use, must not be null ans must be one of the supported media types
    * @return The {@link MediumStore} newly created
    */
   public MediumStore createMediumStore(Medium<?> medium);
}