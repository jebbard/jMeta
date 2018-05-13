/**
 *
 * {@link DescriptionCollector}.java
 *
 * @author Jens Ebert
 *
 * @date 13.05.2018
 *
 */
package com.github.jmeta.library.dataformats.api.services.builder;

import java.util.List;

import com.github.jmeta.library.dataformats.api.types.DataBlockDescription;

/**
 * {@link DescriptionCollector}
 *
 */
public interface DescriptionCollector {

   public void addDataBlockDescription(DataBlockDescription newDescription);

   public List<DataBlockDescription> getAllDescriptions();
}
