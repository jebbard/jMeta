/**
 *
 * {@link AbstractTransformationHandler}.java
 *
 * @author Jens Ebert
 *
 * @date 02.03.2011
 */
package de.je.jmeta.datablocks.export;

import de.je.jmeta.datablocks.IContainer;
import de.je.jmeta.datablocks.IDataBlockFactory;
import de.je.jmeta.datablocks.ITransformationHandler;
import de.je.jmeta.dataformats.DataTransformationType;
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
    * @see de.je.jmeta.datablocks.ITransformationHandler#getTransformationType()
    */
   @Override
   public DataTransformationType getTransformationType() {

      return m_type;
   }

   /**
    * @see de.je.jmeta.datablocks.ITransformationHandler#requiresTransform(de.je.jmeta.datablocks.IContainer)
    */
   @Override
   public boolean requiresTransform(IContainer container) {

      Reject.ifNull(container, "container");

      return getTransformationType().getAffectedContainers()
         .contains(container.getId());
   }

   /**
    * @see de.je.jmeta.datablocks.ITransformationHandler#requiresUntransform(de.je.jmeta.datablocks.IContainer)
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
