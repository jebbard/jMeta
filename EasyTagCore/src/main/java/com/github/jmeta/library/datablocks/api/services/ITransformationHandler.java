/**
 * {@link ITransformationHandler}.java
 *
 * @author Jens Ebert
 * @date 31.12.10 19:47:10 (December 31, 2010)
 */

package com.github.jmeta.library.datablocks.api.services;

import com.github.jmeta.library.datablocks.api.type.IContainer;
import com.github.jmeta.library.dataformats.api.type.DataTransformationType;

/**
 *
 */
public interface ITransformationHandler {

   /**
    * @param container
    * @return true if transformation is required
    */
   public boolean requiresTransform(IContainer container);

   /**
    * @param container
    * @return the transformed {@link IContainer}
    */
   public IContainer transform(IContainer container);

   /**
    * @param container
    * @return true if untransformation is required
    */
   public boolean requiresUntransform(IContainer container);

   /**
    * @param container
    * @return the untransformed {@link IContainer}
    */
   public IContainer untransform(IContainer container);

   /**
    * @return the {@link DataTransformationType}
    */
   public DataTransformationType getTransformationType();
}
