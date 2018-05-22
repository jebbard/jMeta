/**
 *
 * {@link DataBlockDescriptionBuilder}.java
 *
 * @author Jens Ebert
 *
 * @date 12.05.2018
 *
 */
package com.github.jmeta.library.dataformats.api.services.builder;

import com.github.jmeta.library.dataformats.api.types.ContainerDataFormat;
import com.github.jmeta.library.dataformats.api.types.DataBlockDescription;
import com.github.jmeta.library.dataformats.api.types.DataBlockId;

/**
 * {@link DataBlockDescriptionBuilder}
 *
 */
public interface DataBlockDescriptionBuilder<C extends DataBlockDescriptionBuilder<C>> {

   public C withStaticLengthOf(long staticByteLength);

   public C withLengthOf(long minimumByteLength, long maximumByteLength);

   public C withOccurrences(int minimumOccurrences, int maximumOccurrences);

   public C withOverriddenId(DataBlockId overriddenId);

   public C withDescription(String name, String description);

   public String getGlobalId();

   public void addChildDescription(DataBlockDescription childDesc);

   public ContainerDataFormat getDataFormat();

   public DescriptionCollector getDescriptionCollector();
}
