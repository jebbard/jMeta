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

import com.github.jmeta.library.dataformats.api.services.DataFormatSpecificationBuilder;
import com.github.jmeta.library.dataformats.api.services.DataFormatSpecificationBuilderFactory;
import com.github.jmeta.library.dataformats.api.types.ContainerDataFormat;
import com.github.jmeta.library.dataformats.impl.builder.TopLevelContainerSequenceBuilder;

/**
 * {@link StandardDataFormatSpecificationBuilderFactory}
 *
 */
public class StandardDataFormatSpecificationBuilderFactory implements DataFormatSpecificationBuilderFactory {

   @Override
   public DataFormatSpecificationBuilder createDataFormatSpecificationBuilder(ContainerDataFormat dataFormat) {
      return new TopLevelContainerSequenceBuilder(dataFormat);
   }

}
