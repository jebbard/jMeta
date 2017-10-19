/**
 * {@link IDataBlockFactory}.java
 *
 * @author Jens Ebert
 * @date 31.12.10 19:47:11 (December 31, 2010)
 */

package com.github.jmeta.library.datablocks.api.services;

import java.nio.ByteOrder;
import java.nio.charset.Charset;
import java.util.List;

import com.github.jmeta.library.datablocks.api.type.IContainer;
import com.github.jmeta.library.datablocks.api.type.IDataBlock;
import com.github.jmeta.library.datablocks.api.type.IField;
import com.github.jmeta.library.datablocks.api.type.IHeader;
import com.github.jmeta.library.datablocks.api.type.IPayload;
import com.github.jmeta.library.dataformats.api.service.IDataFormatSpecification;
import com.github.jmeta.library.dataformats.api.type.BinaryValue;
import com.github.jmeta.library.dataformats.api.type.DataBlockId;
import com.github.jmeta.library.media.api.type.AbstractMedium;
import com.github.jmeta.library.media.api.type.IMediumReference;

/**
 * A factory used for creating {@link IDataBlock} instances of the various types.
 */
public interface IDataBlockFactory {

   /**
    * Creates an {@link IContainer} instance.
    *
    * @param id
    *           the {@link DataBlockId}.
    * @param parent
    *           the parent {@link IDataBlock} or null if there is not parent {@link IDataBlock}.
    * @param reference
    *           the {@link AbstractMedium} of the {@link IContainer}.
    * @param headers
    *           the headers building this {@link IContainer}.
    * @param footers
    *           the footers building this {@link IContainer}.
    * @param payload
    *           the {@link IPayload} building this {@link IContainer}.
    * @return the created {@link IContainer}.
    */
   public IContainer createContainer(DataBlockId id, IDataBlock parent,
      IMediumReference reference, List<IHeader> headers, IPayload payload,
      List<IHeader> footers);

   /**
    * Creates an {@link IPayload} instance.
    *
    * @param id
    *           the {@link DataBlockId}.
    * @param parent
    *           the parent {@link IDataBlock}.
    * @param containers
    *           the {@link IContainer}s building this {@link IPayload}. May be null if there are no {@link IContainer}s
    *           in the {@link IPayload} instance. If this is the case, the fields parameter must be non-null.
    * @param fields
    *           the {@link IField}s building this {@link IPayload}. May be null if there are no {@link IContainer}s in
    *           the {@link IPayload} instance. If this is the case, the containers parameter must be non-null.
    * @return the created {@link IPayload}.
    */
   public IPayload createPayload(DataBlockId id, IDataBlock parent,
      List<IContainer> containers, List<IField<?>> fields);

   /**
    * @param id
    * @param spec
    * @param reference
    * @param fieldBytes
    * @param byteOrder
    * @param characterEncoding
    * @return the {@link IField}
    */
   public <T> IField<T> createFieldFromBytes(DataBlockId id,
      IDataFormatSpecification spec, IMediumReference reference,
      BinaryValue fieldBytes, ByteOrder byteOrder, Charset characterEncoding);

   /**
    * @param fieldId
    * @param value
    * @return the {@link IField}
    */
   public <T> IField<T> createFieldForWriting(DataBlockId fieldId, T value);

   /**
    * @param id
    * @param reference
    * @param fields
    * @param isFooter
    * @return the {@link IHeader}
    */
   public IHeader createHeader(DataBlockId id, IMediumReference reference,
      List<IField<?>> fields, boolean isFooter);

}
