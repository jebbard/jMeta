/**
 *
 * {@link AbstractTransformationHandler}.java
 *
 * @author Jens Ebert
 *
 * @date 02.03.2011
 */
package com.github.jmeta.defaultextensions.id3v23.impl;

import com.github.jmeta.library.datablocks.api.services.DataBlockFactory;
import com.github.jmeta.library.datablocks.api.types.Container;
import com.github.jmeta.utility.dbc.api.services.Reject;

/**
 * {@link AbstractTransformationHandler}
 *
 */
public abstract class AbstractTransformationHandler
   implements TransformationHandler {

   /**
    * Creates a new {@link AbstractTransformationHandler}.
    * 
    * @param type
    * @param dbFactory
    */
   public AbstractTransformationHandler(DataTransformationType type,
      DataBlockFactory dbFactory) {
      Reject.ifNull(type, "dtt");
      Reject.ifNull(dbFactory, "dbFactory");

      m_type = type;
      m_dbFactory = dbFactory;
   }

   /**
    * @see com.github.jmeta.defaultextensions.id3v23.impl.TransformationHandler#getTransformationType()
    */
   @Override
   public DataTransformationType getTransformationType() {

      return m_type;
   }

   /**
    * @see com.github.jmeta.defaultextensions.id3v23.impl.TransformationHandler#requiresTransform(com.github.jmeta.library.datablocks.api.types.Container)
    */
   @Override
   public boolean requiresTransform(Container container) {

      Reject.ifNull(container, "container");

      return getTransformationType().getAffectedContainers()
         .contains(container.getId());
   }

   /**
    * @see com.github.jmeta.defaultextensions.id3v23.impl.TransformationHandler#requiresUntransform(com.github.jmeta.library.datablocks.api.types.Container)
    */
   @Override
   public boolean requiresUntransform(Container container) {

      Reject.ifNull(container, "container");

      return getTransformationType().getAffectedContainers()
         .contains(container.getId());
   }

   /**
    * Returns dbFactory
    *
    * @return dbFactory
    */
   protected DataBlockFactory getDataBlockFactory() {

      return m_dbFactory;
   }

   private final DataTransformationType m_type;

   private final DataBlockFactory m_dbFactory;
}
