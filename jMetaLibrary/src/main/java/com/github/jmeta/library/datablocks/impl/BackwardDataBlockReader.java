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
 * {@link BackwardDataBlockReader}
 *
 */
public class BackwardDataBlockReader extends AbstractDataBlockReader {

   /**
    * Creates a new {@link BackwardDataBlockReader}.
    *
    * @param spec
    */
   public BackwardDataBlockReader(DataFormatSpecification spec) {
      super(spec);
   }
}
