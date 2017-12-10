
package com.github.jmeta.library.datablocks.api.services;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.Map;

import com.github.jmeta.library.datablocks.api.types.Container;
import com.github.jmeta.library.datablocks.api.types.Field;
import com.github.jmeta.library.datablocks.api.types.FieldFunctionStack;
import com.github.jmeta.library.datablocks.api.types.Header;
import com.github.jmeta.library.datablocks.api.types.Payload;
import com.github.jmeta.library.dataformats.api.services.DataFormatSpecification;
import com.github.jmeta.library.dataformats.api.types.DataBlockId;
import com.github.jmeta.library.dataformats.api.types.DataTransformationType;
import com.github.jmeta.library.media.api.exceptions.EndOfMediumException;
import com.github.jmeta.library.media.api.services.MediumStore;
import com.github.jmeta.library.media.api.types.AbstractMedium;
import com.github.jmeta.library.media.api.types.MediumOffset;

/**
 * {@link DataBlockReader}
 *
 */
public interface DataBlockReader {

   /**
    * @param dataBlockFactory
    */
   public void initDataBlockFactory(ExtendedDataBlockFactory dataBlockFactory);

   /**
    * @param reference
    * @param id
    * @param parent
    * @param remainingDirectParentByteCount
    * @return true if it has, false otherwise
    */
   public boolean hasContainerWithId(MediumOffset reference, DataBlockId id, Payload parent,
      long remainingDirectParentByteCount);

   /**
    * Returns the next {@link Container} with the given {@link DataBlockId} assumed to be stored starting at the given
    * {@link MediumOffset} or null. If the {@link Container}s presence is optional, its actual presence is determined
    * 
    * @param reference
    * @param id
    * @param parent
    * @param context
    * @param remainingDirectParentByteCount
    * @return the {@link Container}
    */
   public Container readContainerWithId(MediumOffset reference, DataBlockId id, Payload parent,
      FieldFunctionStack context, long remainingDirectParentByteCount);

   /**
    * @param reference
    * @param id
    * @param parent
    * @param context
    * @param remainingDirectParentByteCount
    * @return the {@link Container}
    */
   public Container readContainerWithIdBackwards(MediumOffset reference, DataBlockId id, Payload parent,
      FieldFunctionStack context, long remainingDirectParentByteCount);

   /**
    * @param reference
    * @param id
    * @param parentId
    * @param footers
    * @param context
    * @param remainingDirectParentByteCount
    * @return the {@link Payload}
    */
   public Payload readPayloadBackwards(MediumOffset reference, DataBlockId id, DataBlockId parentId,
      List<Header> footers, FieldFunctionStack context, long remainingDirectParentByteCount);

   /**
    * @param maxFieldBlockSize
    */
   public void setMaxFieldBlockSize(int maxFieldBlockSize);

   /**
    * Returns the next {@link Header} with the given {@link DataBlockId} assumed to be stored starting at the given
    * {@link MediumOffset} or null. If the {@link Header}s presence is optional, its actual presence is determined using
    * the given previous {@link Header}s. The method returns null if no {@link Header} with the {@link DataBlockId} is
    * present at the given {@link MediumOffset}.
    *
    * @param reference
    *           The {@link MediumOffset} pointing to the location of the assumed {@link Header} in the
    *           {@link AbstractMedium}.
    * @param headerId
    *           The {@link DataBlockId} of the assumed {@link Header}.
    * @param parentId
    * @param previousHeaders
    *           The {@link List} of previous {@link Header}s belonging to the same {@link Container}. Have been already
    *           read beforehand. These {@link Header}s can be used to determine the presence of the currently requested
    *           {@link Header}. If there are no {@link Header}s that have been read beforehand, this {@link List} must
    *           be empty.
    * @param context
    * @return The {@link Header} with the given {@link DataBlockId} with its {@link Field}s read from the given
    *         {@link MediumOffset}.
    */
   public List<Header> readHeadersWithId(MediumOffset reference, DataBlockId headerId, DataBlockId parentId,
      List<Header> previousHeaders, FieldFunctionStack context);

   /**
    * @param reference
    * @param footerId
    * @param parentId
    * @param previousFooters
    * @param context
    * @return the list of {@link Header}s
    */
   public List<Header> readFootersWithId(MediumOffset reference, DataBlockId footerId, DataBlockId parentId,
      List<Header> previousFooters, FieldFunctionStack context);

   /**
    * @param reference
    * @param id
    * @param parentId
    * @param headers
    * @param context
    * @param remainingDirectParentByteCount
    * @return the {@link Payload}
    */
   public Payload readPayload(MediumOffset reference, DataBlockId id, DataBlockId parentId, List<Header> headers,
      FieldFunctionStack context, long remainingDirectParentByteCount);

   /**
    * @param reference
    * @param parentId
    * @param context
    * @param remainingDirectParentByteCount
    * @return the list of read {@link Field}s
    */
   public List<Field<?>> readFields(MediumOffset reference, DataBlockId parentId, FieldFunctionStack context,
      long remainingDirectParentByteCount);

   /**
    * @param cache
    */
   public void setMediumCache(MediumStore cache);

   /**
    * @param payloadId
    * @return the longest minimum header size
    */
   public long getLongestMinimumContainerHeaderSize(DataBlockId payloadId);

   /**
    * @param payloadId
    * @return the shortest minimum header size
    */
   public long getShortestMinimumContainerHeaderSize(DataBlockId payloadId);

   /**
    * @param reference
    * @return true if it identifies, false otherwise
    */
   public boolean identifiesDataFormat(MediumOffset reference);

   /**
    * @return the {@link DataFormatSpecification}
    */
   public DataFormatSpecification getSpecification();

   /**
    * @return the transformation handlers
    */
   public Map<DataTransformationType, TransformationHandler> getTransformationHandlers();

   /**
    * @param transformationType
    * @param handler
    */
   public void setTransformationHandler(DataTransformationType transformationType, TransformationHandler handler);

   /**
    * @param reference
    * @param size
    * @throws EndOfMediumException
    */
   public void cache(MediumOffset reference, long size) throws EndOfMediumException;

   /**
    * @param reference
    * @param size
    * @return the {@link ByteBuffer}
    */
   public ByteBuffer readBytes(MediumOffset reference, int size);

   /**
    * @param startReference
    * @param size
    */
   public void free(MediumOffset startReference, long size);
}