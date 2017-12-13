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
import java.util.Set;

import com.github.jmeta.library.dataformats.api.types.DataBlockDescription;
import com.github.jmeta.library.dataformats.api.types.DataBlockId;
import com.github.jmeta.library.dataformats.api.types.DataFormat;
import com.github.jmeta.library.dataformats.api.types.DataTransformationType;

/**
 * Represents the complete specification of a single {@link DataFormat}. The specification contains all
 * {@link DataBlockDescription}s for the {@link DataFormat}.
 */
public interface DataFormatSpecification {

   /**
    *
    */
   public static final String UNKNOWN_FIELD_ID = "<<++**UNKNOWN_FIELD**++>>";

   /**
    * @return the data format
    */
   public DataFormat getDataFormat();

   /**
    * @param id
    * @return the generic id
    */
   public DataBlockId getMatchingGenericId(DataBlockId id);

   /**
    * @return the top-level data blocks
    */
   public Set<DataBlockId> getTopLevelDataBlockIds();

   /**
    * @param id
    * @return the {@link DataBlockDescription} for the given id
    */
   public DataBlockDescription getDataBlockDescription(DataBlockId id);

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
    * @return the {@link DataTransformationType}s of this data format
    */
   public List<DataTransformationType> getDataTransformations();

   /**
    * @return All {@link DataBlockId}s used for padding
    */
   public Set<DataBlockId> getPaddingBlockIds();

   /**
    * @param id
    * @return true if it is a generic block, false otherwise
    */
   public boolean isGeneric(DataBlockId id);

   /**
    * @param id
    * @return true if the given {@link DataBlockId} is contained in the current data format, false otherwise
    */
   public boolean specifiesBlockWithId(DataBlockId id);

}
