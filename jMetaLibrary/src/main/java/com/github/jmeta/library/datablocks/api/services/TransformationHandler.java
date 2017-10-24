/**
 * {@link TransformationHandler}.java
 *
 * @author Jens Ebert
 * @date 31.12.10 19:47:10 (December 31, 2010)
 */

package com.github.jmeta.library.datablocks.api.services;

import com.github.jmeta.library.datablocks.api.types.Container;
import com.github.jmeta.library.dataformats.api.types.DataTransformationType;

/**
 *
 */
public interface TransformationHandler {

   /**
    * @param container
    * @return true if transformation is required
    */
   public boolean requiresTransform(Container container);

   /**
    * @param container
    * @return the transformed {@link Container}
    */
   public Container transform(Container container);

   /**
    * @param container
    * @return true if untransformation is required
    */
   public boolean requiresUntransform(Container container);

   /**
    * @param container
    * @return the untransformed {@link Container}
    */
   public Container untransform(Container container);

   /**
    * @return the {@link DataTransformationType}
    */
   public DataTransformationType getTransformationType();
}
