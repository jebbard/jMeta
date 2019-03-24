/**
 *
 * {@link ForwardDataBlockReader}.java
 *
 * @author Jens Ebert
 *
 * @date 24.03.2019
 *
 */
package com.github.jmeta.library.datablocks.impl;

import com.github.jmeta.library.dataformats.api.services.DataFormatSpecification;

/**
 * {@link ForwardDataBlockReader}
 *
 */
public class ForwardDataBlockReader extends AbstractDataBlockReader {

   /**
    * Creates a new {@link ForwardDataBlockReader}.
    *
    * @param spec
    */
   public ForwardDataBlockReader(DataFormatSpecification spec) {
      super(spec);
   }
}
