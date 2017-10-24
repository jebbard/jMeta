/**
 * {@link DataFormatRepository}.java
 *
 * @author Jens Ebert
 * @date 31.12.10 19:47:10 (December 31, 2010)
 */

package com.github.jmeta.library.dataformats.api.services;

import java.util.List;
import java.util.Set;

import com.github.jmeta.library.dataformats.api.types.DataFormat;

/**
 *
 */
public interface DataFormatRepository {

   /**
    * @return the supported {@link DataFormat}s
    */
   public Set<DataFormat> getSupportedDataFormats();

   /**
    * @param dataFormat
    * @return the {@link DataFormatSpecification} for the given {@link DataFormat}
    * @pre {@link #getSupportedDataFormats()} {@link List#contains(Object)} dataFormat
    */
   public DataFormatSpecification getDataFormatSpecification(DataFormat dataFormat);

}
