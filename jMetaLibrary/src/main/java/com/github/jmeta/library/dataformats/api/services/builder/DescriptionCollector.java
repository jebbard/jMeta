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

import java.util.Map;

import com.github.jmeta.library.dataformats.api.types.DataBlockDescription;
import com.github.jmeta.library.dataformats.api.types.DataBlockId;

/**
 * {@link DescriptionCollector}
 *
 */
public interface DescriptionCollector {

   public void addDataBlockDescription(DataBlockDescription newDescription, boolean isGeneric, boolean isTopLevel);

   public Map<DataBlockId, DataBlockDescription> getAllDescriptions();

   public Map<DataBlockId, DataBlockDescription> getGenericDescriptions();

   public Map<DataBlockId, DataBlockDescription> getTopLevelDescriptions();
}
