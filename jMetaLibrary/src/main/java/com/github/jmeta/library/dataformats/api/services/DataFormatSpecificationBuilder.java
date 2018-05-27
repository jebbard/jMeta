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
import java.util.List;

import com.github.jmeta.library.dataformats.api.services.builder.ContainerSequenceBuilder;
import com.github.jmeta.library.dataformats.api.services.builder.DescriptionCollector;

/**
 * {@link DataFormatSpecificationBuilder}
 *
 */
public interface DataFormatSpecificationBuilder
   extends ContainerSequenceBuilder<DataFormatSpecificationBuilder>, DescriptionCollector {

   public DataFormatSpecification createDataFormatSpecification(List<ByteOrder> supportedByteOrders,
      List<Charset> supportedCharacterEncodings);

}
