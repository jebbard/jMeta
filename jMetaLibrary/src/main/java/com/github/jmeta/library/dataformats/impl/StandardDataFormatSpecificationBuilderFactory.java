/**
 *
 * {@link StandardDataFormatSpecificationBuilderFactory}.java
 *
 * @author Jens Ebert
 *
 * @date 23.05.2018
 *
 */
package com.github.jmeta.library.dataformats.impl;

import com.github.jmeta.library.dataformats.api.services.DataFormatSpecification;
import com.github.jmeta.library.dataformats.api.services.DataFormatSpecificationBuilder;
import com.github.jmeta.library.dataformats.api.services.DataFormatSpecificationBuilderFactory;
import com.github.jmeta.library.dataformats.api.types.ContainerDataFormat;

/**
 * {@link StandardDataFormatSpecificationBuilderFactory} provides an instance of a
 * {@link DataFormatSpecificationBuilder} to extensions that need to build a {@link DataFormatSpecification}.
 */
public class StandardDataFormatSpecificationBuilderFactory implements DataFormatSpecificationBuilderFactory {

   /**
    * @see com.github.jmeta.library.dataformats.api.services.DataFormatSpecificationBuilderFactory#createDataFormatSpecificationBuilder(com.github.jmeta.library.dataformats.api.types.ContainerDataFormat)
    */
   @Override
   public DataFormatSpecificationBuilder createDataFormatSpecificationBuilder(ContainerDataFormat dataFormat) {
      return new TopLevelContainerSequenceBuilder(dataFormat);
   }
}
