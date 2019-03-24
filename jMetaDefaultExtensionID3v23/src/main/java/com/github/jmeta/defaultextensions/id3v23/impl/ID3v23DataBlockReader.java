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
import com.github.jmeta.library.datablocks.api.types.Container;
import com.github.jmeta.library.datablocks.api.types.ContainerContext;
import com.github.jmeta.library.datablocks.api.types.Payload;
import com.github.jmeta.library.datablocks.impl.ForwardDataBlockReader;
import com.github.jmeta.library.datablocks.impl.MediumDataProvider;
import com.github.jmeta.library.dataformats.api.services.DataFormatSpecification;
import com.github.jmeta.library.dataformats.api.types.ContainerDataFormat;
import com.github.jmeta.library.dataformats.api.types.DataBlockId;
import com.github.jmeta.library.media.api.types.MediumOffset;
import com.github.jmeta.utility.dbc.api.services.Reject;

/**
 * {@link ID3v23DataBlockReader}
 *
 */
public class ID3v23DataBlockReader extends ForwardDataBlockReader {

   private Map<ID3v2TransformationType, AbstractID3v2TransformationHandler> transformationsReadOrder = new LinkedHashMap<>();

   /**
    * Creates a new {@link ID3v23DataBlockReader}.
    *
    * @param spec
    */
   public ID3v23DataBlockReader(DataFormatSpecification spec) {
      super(spec);

      setCustomSizeProvider(new ID3v23ExtHeaderSizeProvider());

      transformationsReadOrder.put(ID3v2TransformationType.UNSYNCHRONIZATION,
         new UnsynchronisationHandler(getDataBlockFactory()));
      transformationsReadOrder.put(ID3v2TransformationType.COMPRESSION, new CompressionHandler(getDataBlockFactory()));
   }

   /**
    * @see com.github.jmeta.library.datablocks.impl.AbstractDataBlockReader#readContainerWithId(com.github.jmeta.library.media.api.types.MediumOffset,
    *      com.github.jmeta.library.dataformats.api.types.DataBlockId,
    *      com.github.jmeta.library.datablocks.api.types.Payload,
    *      com.github.jmeta.library.datablocks.api.types.FieldFunctionStack, long)
    */
   @Override
   public Container readContainerWithId(MediumOffset reference, DataBlockId id, Payload parent,
      long remainingDirectParentByteCount, ContainerContext containerContext, int sequenceNumber) {
      Container container = super.readContainerWithId(reference, id, parent, remainingDirectParentByteCount,
         containerContext, sequenceNumber);

      return applyTransformationsAfterRead(container, this);
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

   private Container applyTransformationsAfterRead(Container container, DataBlockReader reader) {
      Container transformedContainer = container;

      Iterator<AbstractID3v2TransformationHandler> handlerIterator = transformationsReadOrder.values().iterator();
      MediumDataProvider mediumDataProvider = getMediumDataProvider();

      while (handlerIterator.hasNext()) {
         AbstractID3v2TransformationHandler transformationHandler = handlerIterator.next();

         if (transformationHandler.requiresUntransform(transformedContainer)) {
            mediumDataProvider.bufferBeforeRead(transformedContainer.getMediumReference(),
               transformedContainer.getTotalSize());
            transformedContainer = transformationHandler.untransform(transformedContainer, reader);
         }
      }

      return transformedContainer;
   }
}
