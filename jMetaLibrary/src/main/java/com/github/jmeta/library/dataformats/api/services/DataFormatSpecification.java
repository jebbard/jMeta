/**
 * {@link DataFormatSpecification}.java
 *
 * @author Jens Ebert
 * @date 31.12.10 19:47:10 (December 31, 2010)
 */

package com.github.jmeta.library.dataformats.api.services;

import java.nio.ByteOrder;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;

import com.github.jmeta.library.dataformats.api.types.AbstractFieldFunction;
import com.github.jmeta.library.dataformats.api.types.ContainerDataFormat;
import com.github.jmeta.library.dataformats.api.types.DataBlockDescription;
import com.github.jmeta.library.dataformats.api.types.DataBlockId;

/**
 * Represents the complete specification of a single {@link ContainerDataFormat}. The specification contains all
 * {@link DataBlockDescription}s for the {@link ContainerDataFormat}.
 */
public interface DataFormatSpecification {

   /**
    *
    */
   public static final String UNKNOWN_FIELD_ID = "<<++**UNKNOWN_FIELD**++>>";

   /**
    * @return the data format
    */
   public ContainerDataFormat getDataFormat();

   /**
    * @param id
    * @return the generic id
    */
   public DataBlockId getMatchingGenericId(DataBlockId id);

   /**
    * @return the top-level data blocks
    */
   public List<DataBlockDescription> getTopLevelDataBlockDescriptions();

   /**
    * @param id
    * @return the {@link DataBlockDescription} for the given id
    */
   public DataBlockDescription getDataBlockDescription(DataBlockId id);

   public DataBlockDescription getDefaultNestedContainerDescription();

   /**
    * @return the {@link Charset}s supported
    */
   public List<Charset> getSupportedCharacterEncodings();

   /**
    * @return the {@link ByteOrder}s supported
    */
   public List<ByteOrder> getSupportedByteOrders();

   /**
    * @return the default {@link Charset}
    */
   public Charset getDefaultCharacterEncoding();

   /**
    * @return the default {@link ByteOrder}
    */
   public ByteOrder getDefaultByteOrder();

   /**
    * @param id
    * @return true if the given {@link DataBlockId} is contained in the current data format, false otherwise
    */
   public boolean specifiesBlockWithId(DataBlockId id);

   /**
    * @return all {@link AbstractFieldFunction}s specified in this {@link DataFormatSpecification}, mapped to the target
    *         block id they reference
    */
   public Map<DataBlockId, List<AbstractFieldFunction<?>>> getAllFieldFunctionsByTargetId();
}
