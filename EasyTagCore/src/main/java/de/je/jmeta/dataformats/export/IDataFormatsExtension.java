/**
 *
 * {@link IDataFormatsExtension}.java
 *
 * @author Jens Ebert
 *
 * @date 28.04.2011
 */
package de.je.jmeta.dataformats.export;

import de.je.jmeta.dataformats.DataFormat;
import de.je.jmeta.dataformats.IDataFormatSpecification;
import de.je.jmeta.extmanager.export.IExtensionPoint;

/**
 * {@link IDataFormatsExtension}
 *
 */
public interface IDataFormatsExtension extends IExtensionPoint {

   /**
    * @return the {@link DataFormat}
    */
   public DataFormat getDataFormat();

   /**
    * @return the {@link IDataFormatSpecification}
    */
   public IDataFormatSpecification getSpecification();
}
