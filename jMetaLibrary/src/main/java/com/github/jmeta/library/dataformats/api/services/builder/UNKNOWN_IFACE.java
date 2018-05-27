/**
 *
 * {@link UNKNOWN_IFACE}.java
 *
 * @author Jens Ebert
 *
 * @date 27.05.2018
 *
 */
package com.github.jmeta.library.dataformats.api.services.builder;

import com.github.jmeta.library.dataformats.api.types.ContainerDataFormat;
import com.github.jmeta.library.dataformats.api.types.DataBlockDescription;

/**
 * {@link UNKNOWN_IFACE}
 *
 */
public interface UNKNOWN_IFACE {

   public String getGlobalId();

   public void addChildDescription(DataBlockDescription childDesc);

   public ContainerDataFormat getDataFormat();

   public DescriptionCollector getDescriptionCollector();

}
