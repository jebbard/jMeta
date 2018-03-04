/**
 * {@link ContainerDataFormat}.java
 *
 * @author Jens Ebert
 * @date 31.12.10 19:47:06 (December 31, 2010)
 */

package com.github.jmeta.library.dataformats.api.types;

import java.util.Date;
import java.util.List;
import java.util.Set;

/**
 * Represents a container data format.
 */
public class ContainerDataFormat extends AbstractDataFormat {

   /**
    * Creates a new {@link ContainerDataFormat}.
    */
   public ContainerDataFormat(String id, Set<String> fileExtensions, Set<String> mimeTypes,
      List<String> specificationLinks, String author, Date revisionDate) {
      super(id, fileExtensions, mimeTypes, specificationLinks, author, revisionDate);
   }
}
