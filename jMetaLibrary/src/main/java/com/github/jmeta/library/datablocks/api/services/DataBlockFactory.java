/**
 * {@link DataBlockFactory}.java
 *
 * @author Jens Ebert
 * @date 31.12.10 19:47:11 (December 31, 2010)
 */

package com.github.jmeta.library.datablocks.api.services;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.Charset;
import java.util.List;

import com.github.jmeta.library.datablocks.api.types.Container;
import com.github.jmeta.library.datablocks.api.types.DataBlock;
import com.github.jmeta.library.datablocks.api.types.Field;
import com.github.jmeta.library.datablocks.api.types.Header;
import com.github.jmeta.library.datablocks.api.types.Payload;
import com.github.jmeta.library.dataformats.api.services.DataFormatSpecification;
import com.github.jmeta.library.dataformats.api.types.DataBlockId;
import com.github.jmeta.library.media.api.types.AbstractMedium;
import com.github.jmeta.library.media.api.types.MediumOffset;

/**
 * A factory used for creating {@link DataBlock} instances of the various types.
 */
public interface DataBlockFactory {

   /**
    * Creates an {@link Container} instance.
    *
    * @param id
    *           the {@link DataBlockId}.
    * @param parent
    *           the parent {@link DataBlock} or null if there is not parent {@link DataBlock}.
    * @param reference
    *           the {@link AbstractMedium} of the {@link Container}.
    * @param headers
    *           the headers building this {@link Container}.
    * @param footers
    *           the footers building this {@link Container}.
    * @param payload
    *           the {@link Payload} building this {@link Container}.
    * @return the created {@link Container}.
    */
   public Container createContainer(DataBlockId id, DataBlock parent, MediumOffset reference, List<Header> headers,
      Payload payload, List<Header> footers);

   /**
    * @param id
    * @param spec
    * @param reference
    * @param fieldBytes
    * @param byteOrder
    * @param characterEncoding
    * @return the {@link Field}
    */
   public <T> Field<T> createFieldFromBytes(DataBlockId id, DataFormatSpecification spec, MediumOffset reference,
      ByteBuffer fieldBytes, ByteOrder byteOrder, Charset characterEncoding);

   /**
    * @param fieldId
    * @param value
    * @return the {@link Field}
    */
   public <T> Field<T> createFieldForWriting(DataBlockId fieldId, T value);

   /**
    * @param id
    * @param reference
    * @param fields
    * @param isFooter
    * @return the {@link Header}
    */
   public Header createHeaderOrFooter(DataBlockId id, MediumOffset reference, List<Field<?>> fields, boolean isFooter);

}
