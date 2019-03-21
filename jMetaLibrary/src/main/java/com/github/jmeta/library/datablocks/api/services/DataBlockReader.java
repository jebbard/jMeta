
package com.github.jmeta.library.datablocks.api.services;

import java.nio.ByteBuffer;
import java.util.List;

import com.github.jmeta.library.datablocks.api.types.Container;
import com.github.jmeta.library.datablocks.api.types.ContainerContext;
import com.github.jmeta.library.datablocks.api.types.Field;
import com.github.jmeta.library.datablocks.api.types.Header;
import com.github.jmeta.library.datablocks.api.types.Payload;
import com.github.jmeta.library.datablocks.impl.StandardField;
import com.github.jmeta.library.dataformats.api.services.DataFormatSpecification;
import com.github.jmeta.library.dataformats.api.types.DataBlockId;
import com.github.jmeta.library.media.api.services.MediumStore;
import com.github.jmeta.library.media.api.types.AbstractMedium;
import com.github.jmeta.library.media.api.types.MediumOffset;

/**
 * {@link DataBlockReader}
 *
 */
public interface DataBlockReader {

   /**
    * @param reference
    * @param id
    * @param parent
    * @param remainingDirectParentByteCount
    * @param forwardRead
    *           TODO
    * @return true if it has, false otherwise
    */
   public boolean hasContainerWithId(MediumOffset reference, DataBlockId id, Payload parent,
      long remainingDirectParentByteCount, boolean forwardRead);

   /**
    * Returns the next {@link Container} with the given {@link DataBlockId} assumed to be stored starting at the given
    * {@link MediumOffset} or null. If the {@link Container}s presence is optional, its actual presence is determined
    *
    * @param reference
    * @param id
    * @param parent
    * @param context
    * @param remainingDirectParentByteCount
    * @param containerContext
    *           TODO
    * @param sequenceNumber
    *           TODO
    * @return the {@link Container}
    */
   public Container readContainerWithId(MediumOffset reference, DataBlockId id, Payload parent,
      long remainingDirectParentByteCount, ContainerContext containerContext, int sequenceNumber);

   /**
    * @param reference
    * @param id
    * @param parent
    * @param context
    * @param remainingDirectParentByteCount
    * @param containerContext
    *           TODO
    * @param sequenceNumber
    *           TODO
    * @return the {@link Container}
    */
   public Container readContainerWithIdBackwards(MediumOffset reference, DataBlockId id, Payload parent,
      long remainingDirectParentByteCount, ContainerContext containerContext, int sequenceNumber);

   /**
    * @param reference
    * @param id
    * @param parentId
    * @param footers
    * @param context
    * @param containerContext
    * @return the {@link Payload}
    */
   public Payload readPayloadBackwards(MediumOffset reference, DataBlockId id, DataBlockId parentId,
      List<Header> footers, long remainingDirectParentByteCount, ContainerContext containerContext);

   /**
    * Returns the next {@link Header} instance with the given {@link DataBlockId} assumed to be stored starting at the
    * given {@link MediumOffset} or null. If the {@link Header}s presence is optional, its actual presence is determined
    * using the given previous {@link Header}s. The method returns null if no {@link Header} with the
    * {@link DataBlockId} is present at the given {@link MediumOffset}. Note that this can either refer to headers or
    * footers which is only determined by the isFooter parameter.
    *
    * @param reference
    *           The {@link MediumOffset} pointing to the location of the assumed {@link Header} in the
    *           {@link AbstractMedium}.
    * @param headerOrFooterId
    *           The {@link DataBlockId} of the assumed {@link Header}.
    * @param parentId
    * @param previousHeadersOrFooters
    *           The {@link List} of previous {@link Header}s belonging to the same {@link Container}. Have been already
    *           read beforehand. These {@link Header}s can be used to determine the presence of the currently requested
    *           {@link Header}. If there are no {@link Header}s that have been read beforehand, this {@link List} must
    *           be empty.
    * @param isFooter
    *           Indicates if this refers to headers (false) or footers (true)
    * @param containerContext
    *           TODO
    * @return The {@link Header} with the given {@link DataBlockId} with its {@link StandardField}s read from the given
    *         {@link MediumOffset}.
    */
   public List<Header> readHeadersOrFootersWithId(MediumOffset reference, DataBlockId headerOrFooterId,
      DataBlockId parentId, List<Header> previousHeadersOrFooters, boolean isFooter, ContainerContext containerContext);

   /**
    * @param reference
    * @param id
    * @param parentId
    * @param headers
    * @param context
    * @param remainingDirectParentByteCount
    * @param containerContext
    *           TODO
    * @return the {@link Payload}
    */
   public Payload readPayload(MediumOffset reference, DataBlockId id, DataBlockId parentId, List<Header> headers,
      long remainingDirectParentByteCount, ContainerContext containerContext);

   /**
    * @param reference
    * @param parentId
    * @param context
    * @param remainingDirectParentByteCount
    * @param containerContext
    *           TODO
    * @return the list of read {@link Field}s
    */
   public List<Field<?>> readFields(MediumOffset reference, DataBlockId parentId, long remainingDirectParentByteCount,
      ContainerContext containerContext);

   /**
    * @param cache
    */
   public void setMediumCache(MediumStore cache);

   /**
    * @param reference
    * @param forwardRead
    *           TODO
    * @return true if it identifies, false otherwise
    */
   public boolean identifiesDataFormat(MediumOffset reference, boolean forwardRead);

   /**
    * @return the {@link DataFormatSpecification}
    */
   public DataFormatSpecification getSpecification();

   /**
    * @param reference
    * @param size
    * @return the {@link ByteBuffer}
    */
   public ByteBuffer readBytes(MediumOffset reference, int size);

   public void setCustomSizeProvider(SizeProvider sizeProvider);

   public void setCustomCountProvider(CountProvider countProvider);
}