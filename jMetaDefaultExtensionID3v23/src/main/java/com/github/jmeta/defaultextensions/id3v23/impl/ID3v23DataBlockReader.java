/**
 *
 * {@link ID3v23DataBlockReader}.java
 *
 * @author Jens Ebert
 *
 * @date 11.03.2018
 *
 */
package com.github.jmeta.defaultextensions.id3v23.impl;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.github.jmeta.library.datablocks.api.services.DataBlockReader;
import com.github.jmeta.library.datablocks.api.types.Container;
import com.github.jmeta.library.datablocks.api.types.FieldFunctionStack;
import com.github.jmeta.library.datablocks.api.types.Payload;
import com.github.jmeta.library.datablocks.impl.StandardDataBlockReader;
import com.github.jmeta.library.dataformats.api.services.DataFormatSpecification;
import com.github.jmeta.library.dataformats.api.types.ContainerDataFormat;
import com.github.jmeta.library.dataformats.api.types.DataBlockId;
import com.github.jmeta.library.dataformats.api.types.FieldFunctionType;
import com.github.jmeta.library.media.api.exceptions.EndOfMediumException;
import com.github.jmeta.library.media.api.types.MediumOffset;
import com.github.jmeta.utility.dbc.api.services.Reject;

/**
 * {@link ID3v23DataBlockReader}
 *
 */
public class ID3v23DataBlockReader extends StandardDataBlockReader implements DataBlockReader {

   private Map<DataTransformationType, TransformationHandler> m_transformationsReadOrder = new LinkedHashMap<>();

   /**
    * Creates a new {@link ID3v23DataBlockReader}.
    * 
    * @param spec
    * @param transformationHandlers
    * @param maxFieldBlockSize
    */
   public ID3v23DataBlockReader(DataFormatSpecification spec,
      Map<DataTransformationType, TransformationHandler> transformationHandlers, int maxFieldBlockSize,
      List<DataTransformationType> trafos) {
      super(spec, maxFieldBlockSize);

      // Determine order of the transformations
      if (!transformationHandlers.isEmpty())
         addTransformationHandlers(transformationHandlers, spec, trafos);
   }

   /**
    * @see com.github.jmeta.library.datablocks.impl.StandardDataBlockReader#readContainerWithId(com.github.jmeta.library.media.api.types.MediumOffset,
    *      com.github.jmeta.library.dataformats.api.types.DataBlockId,
    *      com.github.jmeta.library.datablocks.api.types.Payload,
    *      com.github.jmeta.library.datablocks.api.types.FieldFunctionStack, long)
    */
   @Override
   public Container readContainerWithId(MediumOffset reference, DataBlockId id, Payload parent,
      FieldFunctionStack context, long remainingDirectParentByteCount) {
      Container container = super.readContainerWithId(reference, id, parent, context, remainingDirectParentByteCount);

      // It is the tag
      if (parent == null && context.hasFieldFunction(id, FieldFunctionType.TRANSFORMATION_OF)) {
         // DO unynchronize
      } else {
         // do compress etc.
      }

      return applyTransformationsAfterRead(container);
   }

   /**
    * @see com.github.jmeta.library.datablocks.impl.StandardDataBlockReader#readContainerWithIdBackwards(com.github.jmeta.library.media.api.types.MediumOffset,
    *      com.github.jmeta.library.dataformats.api.types.DataBlockId,
    *      com.github.jmeta.library.datablocks.api.types.Payload,
    *      com.github.jmeta.library.datablocks.api.types.FieldFunctionStack, long)
    */
   @Override
   public Container readContainerWithIdBackwards(MediumOffset reference, DataBlockId id, Payload parent,
      FieldFunctionStack context, long remainingDirectParentByteCount) {
      return applyTransformationsAfterRead(
         super.readContainerWithIdBackwards(reference, id, parent, context, remainingDirectParentByteCount));
   }

   /**
    * @see com.github.jmeta.library.datablocks.api.services.DataBlockAccessor#getTransformationHandlers(ContainerDataFormat)
    */
   public Map<DataTransformationType, TransformationHandler> getTransformationHandlers() {

      return Collections.unmodifiableMap(m_transformationsReadOrder);
   }

   /**
    * @see com.github.jmeta.library.datablocks.api.services.DataBlockAccessor#setTransformationHandler(ContainerDataFormat,
    *      DataTransformationType, com.github.jmeta.defaultextensions.id3v23.impl.TransformationHandler)
    */
   public void setTransformationHandler(DataTransformationType transformationType, TransformationHandler handler) {

      Reject.ifFalse(m_transformationsReadOrder.containsKey(transformationType),
         "m_transformationsReadOrder.containsKey(transformationType)");

      if (handler != null)
         Reject.ifFalse(transformationType.equals(handler.getTransformationType()),
            "transformationType.equals(handler.getTransformationType())");

      // Set the handler
      if (handler != null)
         m_transformationsReadOrder.put(transformationType, handler);

      // Remove an already set handler
      else
         m_transformationsReadOrder.remove(transformationType);
   }

   private void addTransformationHandlers(Map<DataTransformationType, TransformationHandler> transformationHandlers,
      DataFormatSpecification spec, List<DataTransformationType> trafos) {

      Map<Integer, DataTransformationType> transformationsReadOrder = new TreeMap<>();

      for (int i = 0; i < trafos.size(); ++i) {
         DataTransformationType dtt = trafos.get(i);

         transformationsReadOrder.put(dtt.getReadOrder(), dtt);
      }

      // Add data transformation handlers
      Iterator<DataTransformationType> readOrderIterator = transformationsReadOrder.values().iterator();

      while (readOrderIterator.hasNext()) {
         DataTransformationType nextTransformationType = readOrderIterator.next();

         if (transformationHandlers.containsKey(nextTransformationType))
            m_transformationsReadOrder.put(nextTransformationType, transformationHandlers.get(nextTransformationType));

         else
            m_transformationsReadOrder.put(nextTransformationType, null);
      }
   }

   private Container applyTransformationsAfterRead(Container container) {

      // TODO: if id3v23.payload: Alles lesen, dann Unsynchro
      // else if id3v2.frame.payload: Andere Trafos

      Reject.ifNull(container, "container");

      Container transformedContainer = container;

      Iterator<TransformationHandler> handlerIterator = m_transformationsReadOrder.values().iterator();

      while (handlerIterator.hasNext()) {
         TransformationHandler transformationHandler = handlerIterator.next();

         if (transformationHandler.requiresUntransform(transformedContainer)) {
            if (m_cache.getCachedByteCountAt(transformedContainer.getMediumReference()) < transformedContainer
               .getTotalSize())
               try {
                  m_cache.cache(transformedContainer.getMediumReference(), (int) transformedContainer.getTotalSize());
               } catch (EndOfMediumException e) {
                  throw new IllegalStateException("Unexpected end of medium", e);
               }

            transformedContainer = transformationHandler.untransform(transformedContainer);
         }
      }

      return transformedContainer;
   }
}
