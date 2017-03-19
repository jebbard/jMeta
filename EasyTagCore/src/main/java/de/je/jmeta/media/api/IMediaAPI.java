/**
 *
 * {@link IMediaAPI}.java
 *
 * @author Jens
 *
 * @date 16.05.2015
 *
 */
package de.je.jmeta.media.api;

import de.je.util.javautil.simpleregistry.IComponentInterface;

/**
 * {@link IMediaAPI} provides access to all functionality of the component Media.
 */
public interface IMediaAPI extends IComponentInterface {

   /**
    * Returns a suitable {@link IMediumStore} for the given readable {@link IMedium}.
    * 
    * @param medium
    *           The {@link IMedium} to use. Must be a supported implementation of {@link IMedium}. You must not use own
    *           implementations of this interface here.
    * @return an {@link IMediumStore} for the given {@link IMedium}.
    * 
    * @pre medium must be a supported {@link IMedium} implementation.
    */
   public IMediumStore getMediumStore(IMedium<?> medium);
}