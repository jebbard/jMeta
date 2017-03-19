/**
 * {@link IDataFormatRepository}.java
 *
 * @author Jens Ebert
 * @date 31.12.10 19:47:10 (December 31, 2010)
 */

package de.je.jmeta.dataformats;

import java.util.List;
import java.util.Set;

import de.je.util.javautil.simpleregistry.IComponentInterface;

/**
 *
 */
public interface IDataFormatRepository extends IComponentInterface {

   /**
    * @return the supported {@link DataFormat}s
    */
   public Set<DataFormat> getSupportedDataFormats();

   /**
    * @param dataFormat
    * @return the {@link IDataFormatSpecification} for the given {@link DataFormat}
    * @pre {@link #getSupportedDataFormats()} {@link List#contains(Object)} dataFormat
    */
   public IDataFormatSpecification getDataFormatSpecification(
      DataFormat dataFormat);

}
