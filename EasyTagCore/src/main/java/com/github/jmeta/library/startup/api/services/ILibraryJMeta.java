/**
 *
 * {@link ILibraryJMeta}.java
 *
 * @author Jens
 *
 * @date 20.03.2016
 *
 */
package com.github.jmeta.library.startup.api.services;

import com.github.jmeta.library.datablocks.api.services.IDataBlockAccessor;
import com.github.jmeta.library.dataformats.api.service.IDataFormatRepository;
import com.github.jmeta.library.startup.impl.LibraryJMeta;

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
