/**
 *
 * {@link DataFormatSpecificationBuilderFactory}.java
 *
 * @author Jens Ebert
 *
 * @date 23.05.2018
 *
 */
package com.github.jmeta.library.dataformats.api.services;

import com.github.jmeta.library.dataformats.api.types.ContainerDataFormat;

/**
 * {@link DataFormatSpecificationBuilderFactory}
 *
 */
public interface DataFormatSpecificationBuilderFactory {

	DataFormatSpecificationBuilder createDataFormatSpecificationBuilder(ContainerDataFormat dataFormat);
}
