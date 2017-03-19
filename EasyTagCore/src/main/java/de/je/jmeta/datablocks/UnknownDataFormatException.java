/**
 * {@link UnknownDataFormatException}.java
 *
 * @author Jens Ebert
 * @date 31.12.10 19:47:07 (December 31, 2010)
 */

package de.je.jmeta.datablocks;

import de.je.jmeta.dataformats.DataFormat;
import de.je.jmeta.media.api.IMediumReference;
import de.je.jmeta.media.api.datatype.AbstractMedium;

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
