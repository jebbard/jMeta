
package de.je.jmeta.datablocks.export;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.Map;

import de.je.jmeta.datablocks.IContainer;
import de.je.jmeta.datablocks.IField;
import de.je.jmeta.datablocks.IHeader;
import de.je.jmeta.datablocks.IPayload;
import de.je.jmeta.datablocks.ITransformationHandler;
import de.je.jmeta.dataformats.DataBlockId;
import de.je.jmeta.dataformats.DataTransformationType;
import de.je.jmeta.dataformats.IDataFormatSpecification;
import de.je.jmeta.media.api.IMediumReference;
import de.je.jmeta.media.api.IMediumStore;
import de.je.jmeta.media.api.datatype.AbstractMedium;
import de.je.jmeta.media.api.exception.EndOfMediumException;

/**
 * {@link IDataBlockReader}
 *
 */
public interface IDataBlockReader {

   /**
    * @param dataBlockFactory
    */
   public void initDataBlockFactory(IExtendedDataBlockFactory dataBlockFactory);

   /**
    * @param reference
    * @param id
    * @param parent
    * @param remainingDirectParentByteCount
    * @return true if it has, false otherwise
    */
   public boolean hasContainerWithId(IMediumReference reference, DataBlockId id,
      IPayload parent, long remainingDirectParentByteCount);

   /**
    * Returns the next {@link IContainer} with the given {@link DataBlockId} assumed to be stored starting at the given
    * {@link IMediumReference} or null. If the {@link IContainer}s presence is optional, its actual presence is
    * determined
    * 
    * @param reference
    * @param id
    * @param parent
    * @param context
    * @param remainingDirectParentByteCount
    * @return the {@link IContainer}
    */
   public IContainer readContainerWithId(IMediumReference reference,
      DataBlockId id, IPayload parent, FieldFunctionStack context,
      long remainingDirectParentByteCount);

   /**
    * @param reference
    * @param id
    * @param parent
    * @param context
    * @param remainingDirectParentByteCount
    * @return the {@link IContainer}
    */
   public IContainer readContainerWithIdBackwards(IMediumReference reference,
      DataBlockId id, IPayload parent, FieldFunctionStack context,
      long remainingDirectParentByteCount);

   /**
    * @param reference
    * @param id
    * @param parentId
    * @param footers
    * @param context
    * @param remainingDirectParentByteCount
    * @return the {@link IPayload}
    */
   public IPayload readPayloadBackwards(IMediumReference reference,
      DataBlockId id, DataBlockId parentId, List<IHeader> footers,
      FieldFunctionStack context, long remainingDirectParentByteCount);

   /**
    * @param maxFieldBlockSize
    */
   public void setMaxFieldBlockSize(int maxFieldBlockSize);

   /**
    * Returns the next {@link IHeader} with the given {@link DataBlockId} assumed to be stored starting at the given
    * {@link IMediumReference} or null. If the {@link IHeader}s presence is optional, its actual presence is determined
    * using the given previous {@link IHeader}s. The method returns null if no {@link IHeader} with the
    * {@link DataBlockId} is present at the given {@link IMediumReference}.
    *
    * @param reference
    *           The {@link IMediumReference} pointing to the location of the assumed {@link IHeader} in the
    *           {@link AbstractMedium}.
    * @param headerId
    *           The {@link DataBlockId} of the assumed {@link IHeader}.
    * @param parentId
    * @param previousHeaders
    *           The {@link List} of previous {@link IHeader}s belonging to the same {@link IContainer}. Have been
    *           already read beforehand. These {@link IHeader}s can be used to determine the presence of the currently
    *           requested {@link IHeader}. If there are no {@link IHeader}s that have been read beforehand, this
    *           {@link List} must be empty.
    * @param context
    * @return The {@link IHeader} with the given {@link DataBlockId} with its {@link IField}s read from the given
    *         {@link IMediumReference}.
    */
   public List<IHeader> readHeadersWithId(IMediumReference reference,
      DataBlockId headerId, DataBlockId parentId, List<IHeader> previousHeaders,
      FieldFunctionStack context);

   /**
    * @param reference
    * @param footerId
    * @param parentId
    * @param previousFooters
    * @param context
    * @return the list of {@link IHeader}s
    */
   public List<IHeader> readFootersWithId(IMediumReference reference,
      DataBlockId footerId, DataBlockId parentId, List<IHeader> previousFooters,
      FieldFunctionStack context);

   /**
    * @param reference
    * @param id
    * @param parentId
    * @param headers
    * @param context
    * @param remainingDirectParentByteCount
    * @return the {@link IPayload}
    */
   public IPayload readPayload(IMediumReference reference, DataBlockId id,
      DataBlockId parentId, List<IHeader> headers, FieldFunctionStack context,
      long remainingDirectParentByteCount);

   /**
    * @param reference
    * @param parentId
    * @param context
    * @param remainingDirectParentByteCount
    * @return the list of read {@link IField}s
    */
   public List<IField<?>> readFields(IMediumReference reference,
      DataBlockId parentId, FieldFunctionStack context,
      long remainingDirectParentByteCount);

   /**
    * @param cache
    */
   public void setMediumCache(IMediumStore cache);

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
   public boolean identifiesDataFormat(IMediumReference reference);

   /**
    * @return the {@link IDataFormatSpecification}
    */
   public IDataFormatSpecification getSpecification();

   /**
    * @return the transformation handlers
    */
   public Map<DataTransformationType, ITransformationHandler> getTransformationHandlers();

   /**
    * @param transformationType
    * @param handler
    */
   public void setTransformationHandler(
      DataTransformationType transformationType,
      ITransformationHandler handler);

   /**
    * @param reference
    * @param size
    * @throws EndOfMediumException
    */
   public void cache(IMediumReference reference, long size)
      throws EndOfMediumException;

   /**
    * @param reference
    * @param size
    * @return the {@link ByteBuffer}
    */
   public ByteBuffer readBytes(IMediumReference reference, int size);

   /**
    * @param startReference
    * @param size
    */
   public void free(IMediumReference startReference, long size);
}