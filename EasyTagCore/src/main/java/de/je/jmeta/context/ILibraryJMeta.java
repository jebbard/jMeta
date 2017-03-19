/**
 *
 * {@link ILibraryJMeta}.java
 *
 * @author Jens
 *
 * @date 20.03.2016
 *
 */
package de.je.jmeta.context;

import de.je.jmeta.context.impl.LibraryJMeta;
import de.je.jmeta.datablocks.IDataBlockAccessor;
import de.je.jmeta.dataformats.IDataFormatRepository;

/**
 * {@link ILibraryJMeta} is the start interface when using the jMeta library, providing access to the overall library
 * functionality. It provides an instance of itself, that in turn you can use to retrieve all top-level interfaces the
 * library offers.
 */
public interface ILibraryJMeta {

   /**
    * @return The {@link ILibraryJMeta}
    */
   public static ILibraryJMeta getLibrary() {

      return new LibraryJMeta();
   }

   /**
    * Returns an {@link IDataBlockAccessor} for accessing data blocks of a specific medium.
    * 
    * @return an {@link IDataBlockAccessor} for accessing data blocks of a specific medium.
    */
   public IDataBlockAccessor getDataBlockAccessor();

   /**
    * Returns an {@link IDataFormatRepository} for accessing the properties of all supported data formats.
    * 
    * @return an {@link IDataFormatRepository} for accessing the properties of all supported data formats.
    */
   public IDataFormatRepository getDataFormatRepository();
}
