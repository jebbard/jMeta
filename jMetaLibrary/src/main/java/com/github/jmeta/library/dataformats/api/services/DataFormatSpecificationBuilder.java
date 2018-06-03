/**
 *
 * {@link DataFormatSpecificationBuilder}.java
 *
 * @author Jens Ebert
 *
 * @date 23.05.2018
 *
 */
package com.github.jmeta.library.dataformats.api.services;

import java.nio.ByteOrder;
import java.nio.charset.Charset;

import com.github.jmeta.library.dataformats.api.services.builder.ContainerSequenceBuilder;
import com.github.jmeta.library.dataformats.api.services.builder.DataFormatBuilder;
import com.github.jmeta.library.dataformats.api.types.DataBlockDescription;
import com.github.jmeta.library.dataformats.api.types.DataBlockId;
import com.github.jmeta.library.dataformats.api.types.PhysicalDataBlockType;

/**
 * {@link DataFormatSpecificationBuilder} provides the entry point for a chained API that allows to build a new
 * {@link DataFormatSpecification}. A {@link DataFormatSpecification} is a complex hierarchical set of so-called data
 * blocks which have different {@link PhysicalDataBlockType}s. These types are arranged according to specific rules,
 * e.g. a {@link PhysicalDataBlockType#CONTAINER}-typed data block must only have {@link PhysicalDataBlockType#HEADER}s,
 * {@link PhysicalDataBlockType#FOOTERs} and {@link PhysicalDataBlockType#FIELD_BASED_PAYLOAD} or
 * {@link PhysicalDataBlockType#CONTAINER_BASED_PAYLOAD} data blocks as children. In addition, each data block has some
 * general properties such as its length and number of occurrences, as well as properties that might only occur for
 * specific data block types, such as a default value which is only valid for {@link PhysicalDataBlockType#FIELD}s.
 *
 * To hide this complexity and at the same time provide the user a possibility to create a new
 * {@link DataFormatSpecification} easily - both easy to write and easier to read - this builder API was created.
 * 
 * As a starting point, you add top-level containers to the specification, then you add headers, footers and payload to
 * each of those containers in nested calls etc. This is possible because each method returns the same builder instance
 * again, allowing chained method invocation which we highly recommend. The basic rules of the builder API are as
 * follows:
 * <ol>
 * <li>All methods starting with "add" are used to add a new child data block to the current builder. They return the
 * corresponding child builder, allowing to build the child data block</li>
 * <li>Every builder instance started by calling any of the "add*" methods needs to be finished by calling the child
 * builder's corresponding "finish*" method. This is necessary to be able to build hierarchically and obtain the parent
 * builder instance again by calling "finish*".</li>
 * <li>There are a few methods that are not intended to be called by the end-user but are just needed internally. They
 * do not return the builder itself but something different, e.g. void. In addition, they are not using with or add in
 * their names to ensure the API end-user does not unintentionally pick them during auto-completion.</li>
 * <li>In contrast to this, any methods named like "with*" allow to modify a property of the current data block, i.e.
 * they will not return the child builder but the current builder only.</li>
 * <li>Any methods named like "as*" allow to tag the current data block with a special function, e.g. that it is a magic
 * key.</li>
 * <li>In the end, if the last top-level container has been finished, you must call the {@link #build()} method to
 * finally create a {@link DataFormatSpecification} from the built content.</li>
 * </ol>
 * 
 * Validation of the additions you made is done during the "finish*" methods. A runtime {@link IllegalArgumentException}
 * is thrown if any of the provided input is invalid or inconsistent. A final validation of the whole
 * {@link DataFormatSpecification} is also done during the {@link #build()} method call which might also throw an
 * {@link IllegalArgumentException} in case of any inconsistencies detected.
 */
public interface DataFormatSpecificationBuilder
   extends ContainerSequenceBuilder<DataFormatSpecificationBuilder>, DataFormatBuilder {

   public DataFormatSpecification build();

   /**
    * Allows builders in the hierarchy to add a new {@link DataBlockDescription} to the root builder. Should not be
    * called by end-users which is highlighted by having a return type of void.
    * 
    * Note that this method REPLACES any previously added {@link DataBlockDescription} for the same {@link DataBlockId}.
    * 
    * @param newDescription
    *           The new {@link DataBlockDescription} to add
    * @param isTopLevel
    *           true if this is a top-level {@link DataBlockDescription}, false otherwise
    * @param isDefaultNestedContainer
    *           true if this {@link DataBlockDescription} is the default nested container, false otherwise
    */
   public void putDataBlockDescription(DataBlockDescription newDescription, boolean isTopLevel,
      boolean isDefaultNestedContainer);

   /**
    * Allows builders in the hierarchy to remove an existing {@link DataBlockDescription} from the root builder. Should
    * not be called by end-users which is highlighted by having a return type of void.
    * 
    * @param dataBlockId
    *           The {@link DataBlockId} to remove, must not be null
    */
   public void removeDataBlockDescription(DataBlockId dataBlockId);

   /**
    * Sets all supported byte orders for the {@link DataFormatSpecification}, where the first parameter is mandatory and
    * is the default {@link ByteOrder}
    * 
    * @param defaultByteOrder
    *           The default {@link ByteOrder} used whenever no other byte order can be determined
    * @param furtherSupportedByteOrders
    *           Any additionally supported {@link ByteOrder}s
    * @return this builder instance, allowing to chain further calls
    */
   public DataFormatSpecificationBuilder withByteOrders(ByteOrder defaultByteOrder,
      ByteOrder... furtherSupportedByteOrders);

   /**
    * Sets all supported charsets for the {@link DataFormatSpecification}, where the first parameter is mandatory and is
    * the default {@link Charset}
    * 
    * @param defaultCharset
    *           The default {@link Charset} used whenever no other byte order can be determined
    * @param furtherSupportedCharsets
    *           Any additionally supported {@link Charset}s
    * @return this builder instance, allowing to chain further calls
    */
   public DataFormatSpecificationBuilder withCharsets(Charset defaultCharset, Charset... furtherSupportedCharsets);

   /**
    * Allows builders in the hierarchy to obtain a {@link DataBlockDescription} already added for the given
    * {@link DataBlockId}.
    * 
    * @param dataBlockId
    *           The {@link DataBlockId} to request, must not be null
    * @return The {@link DataBlockDescription} for the given {@link DataBlockId} or null if there is none for the given
    *         id
    */
   public DataBlockDescription getDataBlockDescription(DataBlockId dataBlockId);
}
