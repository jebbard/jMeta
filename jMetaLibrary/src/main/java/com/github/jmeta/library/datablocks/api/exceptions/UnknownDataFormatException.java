/**
 * {@link UnknownDataFormatException}.java
 *
 * @author Jens Ebert
 * @date 31.12.10 19:47:07 (December 31, 2010)
 */

package com.github.jmeta.library.datablocks.api.exceptions;

import com.github.jmeta.library.datablocks.api.types.IDataBlock;
import com.github.jmeta.library.dataformats.api.types.DataFormat;
import com.github.jmeta.library.media.api.types.AbstractMedium;
import com.github.jmeta.library.media.api.types.IMediumReference;

/**
 * Thrown whenever the {@link DataFormat} of a top-level {@link IDataBlock} within a {@link AbstractMedium} is unknown.
 * I.e. whenever a {@link AbstractMedium} contains at least one top-level data block whose {@link DataFormat} could not
 * be identified, this exception is thrown.
 */
public class UnknownDataFormatException extends RuntimeException {

   private static final long serialVersionUID = 1L;

   /**
    * Creates a new {@link UnknownDataFormatException}.
    * 
    * @param reference
    * @param message
    */
   public UnknownDataFormatException(IMediumReference reference,
      String message) {
      super(message);

      m_mediumReference = reference;
   }

   /**
    * Returns medium
    *
    * @return medium
    */
   public IMediumReference getMediumReference() {

      return m_mediumReference;
   }

   private final IMediumReference m_mediumReference;
}