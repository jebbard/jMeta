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

   public MediumStore createMediumStore(Medium<?> medium);
}