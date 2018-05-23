/**
 * {@link DataFormatRepository}.java
 *
 * @author Jens Ebert
 * @date 31.12.10 19:47:10 (December 31, 2010)
 */

package com.github.jmeta.library.dataformats.api.services;

import java.util.Set;

import com.github.jmeta.library.dataformats.api.types.ContainerDataFormat;

/**
 *
 */
public interface DataFormatRepository {

   /**
    * @return the supported {@link ContainerDataFormat}s
    */
   public Set<ContainerDataFormat> getSupportedDataFormats();

   /**
    * @param dataFormat
    * @return the {@link DataFormatSpecification} for the given {@link ContainerDataFormat}
    */
   public DataFormatSpecification getDataFormatSpecification(ContainerDataFormat dataFormat);
}
