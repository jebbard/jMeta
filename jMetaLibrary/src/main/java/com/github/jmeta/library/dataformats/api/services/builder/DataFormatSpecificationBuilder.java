/**
 *
 * {@link DataFormatSpecificationBuilder}.java
 *
 * @author Jens Ebert
 *
 * @date 12.05.2018
 *
 */
package com.github.jmeta.library.dataformats.api.services.builder;

import com.github.jmeta.library.dataformats.api.types.ContainerDataFormat;
import com.github.jmeta.library.dataformats.api.types.DataBlockDescription;

/**
 * {@link DataFormatSpecificationBuilder}
 *
 */
public interface DataFormatSpecificationBuilder {

   public String getGlobalId();

   public void addChildDescription(DataBlockDescription childDesc);

   public ContainerDataFormat getDataFormat();

   public DescriptionCollector getDescriptionCollector();
}
