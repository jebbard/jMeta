/**
 *
 * {@link AbstractTransformationHandler}.java
 *
 * @author Jens Ebert
 *
 * @date 02.03.2011
 */
package com.github.jmeta.library.datablocks.api.services;

import com.github.jmeta.library.datablocks.api.type.IContainer;
import com.github.jmeta.library.dataformats.api.type.DataTransformationType;

import de.je.util.javautil.common.err.Reject;

/**
 * {@link AbstractTransformationHandler}
 *
 */
public abstract class AbstractTransformationHandler
   implements ITransformationHandler {

   /**
    * Creates a new {@link AbstractTransformationHandler}.
    * 
    * @param type
    * @param dbFactory
    */
   public AbstractTransformationHandler(DataTransformationType type,
      IDataBlockFactory dbFactory) {
      Reject.ifNull(type, "dtt");
      Reject.ifNull(dbFactory, "dbFactory");

      m_type = type;
      m_dbFactory = dbFactory;
   }

   /**
    * @see com.github.jmeta.library.datablocks.api.services.ITransformationHandler#getTransformationType()
    */
   @Override
   public DataTransformationType getTransformationType() {

      return m_type;
   }

   /**
    * @see com.github.jmeta.library.datablocks.api.services.ITransformationHandler#requiresTransform(com.github.jmeta.library.datablocks.api.type.IContainer)
    */
   @Override
   public boolean requiresTransform(IContainer container) {

      Reject.ifNull(container, "container");

      return getTransformationType().getAffectedContainers()
         .contains(container.getId());
   }

   /**
    * @see com.github.jmeta.library.datablocks.api.services.ITransformationHandler#requiresUntransform(com.github.jmeta.library.datablocks.api.type.IContainer)
    */
   @Override
   public boolean requiresUntransform(IContainer container) {

      Reject.ifNull(container, "container");

      return getTransformationType().getAffectedContainers()
         .contains(container.getId());
   }

   /**
    * Returns dbFactory
    *
    * @return dbFactory
    */
   protected IDataBlockFactory getDataBlockFactory() {

      return m_dbFactory;
   }

   private final DataTransformationType m_type;

   private final IDataBlockFactory m_dbFactory;
}
