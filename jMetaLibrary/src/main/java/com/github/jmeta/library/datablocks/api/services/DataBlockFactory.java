/**
 * {@link DataBlockFactory}.java
 *
 * @author Jens Ebert
 * @date 31.12.10 19:47:11 (December 31, 2010)
 */

package com.github.jmeta.library.datablocks.api.services;

import java.nio.ByteBuffer;
import java.util.List;

import com.github.jmeta.library.datablocks.api.types.Container;
import com.github.jmeta.library.datablocks.api.types.ContainerContext;
import com.github.jmeta.library.datablocks.api.types.DataBlock;
import com.github.jmeta.library.datablocks.api.types.Field;
import com.github.jmeta.library.datablocks.api.types.FieldSequence;
import com.github.jmeta.library.datablocks.api.types.Footer;
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
    * @param payload
    *           the {@link Payload} building this {@link Container}.
    * @param footers
    *           the footers building this {@link Container}.
    * @param reader
    *           TODO
    * @param containerContext
    *           TODO
    * @param sequenceNumber
    *           TODO
    * @return the created {@link Container}.
    */
   public Container createContainer(DataBlockId id, DataBlock parent, MediumOffset reference, List<Header> headers,
      Payload payload, List<Footer> footers, DataBlockReader reader, ContainerContext containerContext,
      int sequenceNumber);

   public Container createContainer(DataBlockId id, DataBlock parent, MediumOffset reference, DataBlockReader reader,
      ContainerContext containerContext, int sequenceNumber);

   /**
    * @param id
    * @param spec
    * @param reference
    * @param fieldBytes
    * @param sequenceNumber
    *           TODO
    * @param containerContext
    *           TODO
    * @return the {@link Field}
    */
   public <T> Field<T> createFieldFromBytes(DataBlockId id, DataFormatSpecification spec, MediumOffset reference,
      ByteBuffer fieldBytes, int sequenceNumber, ContainerContext containerContext);

   /**
    * @param id
    * @param reference
    * @param fields
    * @param reader
    *           TODO
    * @param sequenceNumber
    *           TODO
    * @param containerContext
    *           TODO
    * @return the {@link Header}
    */
   public <T extends FieldSequence> T createHeaderOrFooter(Class<T> fieldSequenceClass, DataBlockId id,
      MediumOffset reference, List<Field<?>> fields, DataBlockReader reader, int sequenceNumber,
      ContainerContext containerContext);

   /**
    * @param id
    * @param reference
    * @param totalSize
    * @param reader
    * @param context
    * @param containerContext
    *           TODO
    * @return the {@link Payload}
    */
   public Payload createPayloadAfterRead(DataBlockId id, MediumOffset reference, long totalSize, DataBlockReader reader,
      ContainerContext containerContext);
}
