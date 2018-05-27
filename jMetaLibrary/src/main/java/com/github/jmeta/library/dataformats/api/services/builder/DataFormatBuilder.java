/**
 *
 * {@link DataFormatBuilder}.java
 *
 * @author Jens Ebert
 *
 * @date 27.05.2018
 *
 */
package com.github.jmeta.library.dataformats.api.services.builder;

import com.github.jmeta.library.dataformats.api.services.DataFormatSpecificationBuilder;
import com.github.jmeta.library.dataformats.api.types.ContainerDataFormat;
import com.github.jmeta.library.dataformats.api.types.DataBlockDescription;

/**
 * {@link DataFormatBuilder}
 *
 */
public interface DataFormatBuilder {

   public String getGlobalId();

   public void addChildDescription(DataBlockDescription childDesc);

   public ContainerDataFormat getDataFormat();

   public DataFormatSpecificationBuilder getRootBuilder();

}
