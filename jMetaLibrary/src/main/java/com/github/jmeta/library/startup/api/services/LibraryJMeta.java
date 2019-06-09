/**
 *
 * {@link StandardLibraryJMeta}.java
 *
 * @author Jens
 *
 * @date 20.03.2016
 *
 */
package com.github.jmeta.library.startup.api.services;

import com.github.jmeta.library.datablocks.api.services.DataBlockAccessor;
import com.github.jmeta.library.dataformats.api.services.DataFormatRepository;
import com.github.jmeta.library.startup.impl.StandardLibraryJMeta;

/**
 * {@link LibraryJMeta} is the start interface when using the jMeta library,
 * providing access to the overall library functionality. It provides an
 * instance of itself, that in turn you can use to retrieve all top-level
 * interfaces the library offers.
 */
public interface LibraryJMeta {

	/**
	 * @return The {@link LibraryJMeta}
	 */
	static LibraryJMeta getLibrary() {

		return new StandardLibraryJMeta();
	}

	/**
	 * Returns an {@link DataBlockAccessor} for accessing data blocks of a specific
	 * medium.
	 * 
	 * @return an {@link DataBlockAccessor} for accessing data blocks of a specific
	 *         medium.
	 */
	DataBlockAccessor getDataBlockAccessor();

	/**
	 * Returns an {@link DataFormatRepository} for accessing the properties of all
	 * supported data formats.
	 * 
	 * @return an {@link DataFormatRepository} for accessing the properties of all
	 *         supported data formats.
	 */
	DataFormatRepository getDataFormatRepository();
}
