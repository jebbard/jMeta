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
import java.util.Map;

import com.github.jmeta.library.datablocks.api.services.DataBlockReader;
import com.github.jmeta.library.datablocks.api.services.ExtendedDataBlockFactory;
import com.github.jmeta.library.datablocks.api.types.Container;
import com.github.jmeta.library.datablocks.api.types.FieldFunctionStack;
import com.github.jmeta.library.datablocks.api.types.Payload;
import com.github.jmeta.library.datablocks.impl.StandardDataBlockReader;
import com.github.jmeta.library.dataformats.api.services.DataFormatSpecification;
import com.github.jmeta.library.dataformats.api.types.ContainerDataFormat;
import com.github.jmeta.library.dataformats.api.types.DataBlockId;
import com.github.jmeta.library.media.api.exceptions.EndOfMediumException;
import com.github.jmeta.library.media.api.types.MediumOffset;
import com.github.jmeta.utility.dbc.api.services.Reject;

/**
 * {@link ID3v23DataBlockReader}
 *
 */
public class ID3v23DataBlockReader extends StandardDataBlockReader implements DataBlockReader {

   private Map<ID3v2TransformationType, AbstractID3v2TransformationHandler> transformationsReadOrder = new LinkedHashMap<>();

   /**
    * Creates a new {@link ID3v23DataBlockReader}.
    * 
    * @param spec
    * @param maxFieldBlockSize
    */
   public ID3v23DataBlockReader(DataFormatSpecification spec, int maxFieldBlockSize) {
      super(spec, maxFieldBlockSize);
   }

   @Override
   public void initDataBlockFactory(ExtendedDataBlockFactory dataBlockFactory) {
      super.initDataBlockFactory(dataBlockFactory);

      transformationsReadOrder.put(ID3v2TransformationType.UNSYNCHRONIZATION,
         new UnsynchronisationHandler(getDataBlockFactory()));
      transformationsReadOrder.put(ID3v2TransformationType.COMPRESSION, new CompressionHandler(getDataBlockFactory()));
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
   public Map<ID3v2TransformationType, AbstractID3v2TransformationHandler> getTransformationHandlers() {

      return Collections.unmodifiableMap(transformationsReadOrder);
   }

   public void removeEncryptionHandler() {
      transformationsReadOrder.remove(ID3v2TransformationType.ENCRYPTION);
   }

   public void setEncryptionHandler(AbstractID3v2TransformationHandler handler) {
      Reject.ifNull(handler, "handler");

      transformationsReadOrder.put(ID3v2TransformationType.ENCRYPTION, handler);
   }

   private Container applyTransformationsAfterRead(Container container) {
      Container transformedContainer = container;

      Iterator<AbstractID3v2TransformationHandler> handlerIterator = transformationsReadOrder.values().iterator();

      while (handlerIterator.hasNext()) {
         AbstractID3v2TransformationHandler transformationHandler = handlerIterator.next();

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
