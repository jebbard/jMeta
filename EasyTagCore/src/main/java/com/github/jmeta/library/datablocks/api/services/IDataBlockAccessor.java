/**
 * {@link IDataBlockAccessor}.java
 *
 * @author Jens Ebert
 * @date 31.12.10 19:47:11 (December 31, 2010)
 */

package com.github.jmeta.library.datablocks.api.services;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.github.jmeta.library.datablocks.api.type.IContainer;
import com.github.jmeta.library.datablocks.api.type.IDataBlock;
import com.github.jmeta.library.dataformats.api.type.DataFormat;
import com.github.jmeta.library.dataformats.api.type.DataTransformationType;
import com.github.jmeta.library.media.api.type.AbstractMedium;
import com.github.jmeta.library.media.api.type.IMedium;

/**
 * This interface is the starting point when accessing the {@link IDataBlock}s of a {@link AbstractMedium}. For any
 * existing {@link AbstractMedium}, it returns an {@link Iterator} that allows to iterate the top-level
 * {@link IDataBlock}s of that {@link AbstractMedium}.
 */
public interface IDataBlockAccessor {

   /**
    * Returns the {@link Iterator} for retrieving all the top-level {@link IDataBlock}s in the given
    * {@link AbstractMedium}. Optionally, a {@link List} of expected {@link DataFormat}s can be specified to support the
    * library to identify the {@link DataFormat}s faster.
    *
    * @param medium
    *           the {@link AbstractMedium} for which to get the top-level {@link IDataBlock}s.
    * @param dataFormatHints
    *           a {@link List} containing {@link DataFormat}s expected by the user in the {@link AbstractMedium} in
    *           their given order. This ensures best performance by facilitating the users knowledge. The {@link List}
    *           may be empty if there are no concrete hints.
    * @param forceMediumReadOnly
    *           true if the {@link AbstractMedium} should be treated as read-only {@link AbstractMedium} in any case,
    *           false if the default behavior should be chosen.
    * @return the {@link Iterator} for iterating all the top-level {@link IDataBlock}s of the {@link AbstractMedium}.
    */
   public AbstractDataBlockIterator<IContainer> getContainerIterator(IMedium<?> medium,
      List<DataFormat> dataFormatHints, boolean forceMediumReadOnly);

   /**
    * @param medium
    * @param dataFormatHints
    * @param forceMediumReadOnly
    * @return a reverse {@link AbstractDataBlockIterator}
    */
   public AbstractDataBlockIterator<IContainer> getReverseContainerIterator(IMedium<?> medium,
      List<DataFormat> dataFormatHints, boolean forceMediumReadOnly);

   /**
    * Returns an {@link IDataBlockFactory} that is used for creating {@link IDataBlock} instances.
    * 
    * @param dataFormat
    *
    * @return the {@link IDataBlockFactory}.
    */
   public IDataBlockFactory getDataBlockFactory(DataFormat dataFormat);

   /**
    * @param lazyFieldSize
    */
   public void setLazyFieldSize(int lazyFieldSize);

   /**
    * @param medium
    */
   public void closeMedium(IMedium<?> medium);

   /**
    *
    *
    * @param dataFormat
    * @return TODO
    */
   public Map<DataTransformationType, ITransformationHandler> getTransformationHandlers(DataFormat dataFormat);

   /**
    *
    *
    * @param dataFormat
    * @param transformationType
    * @param handler
    * @pre {@link DataTransformationType#isBuiltIn()} == false
    */
   public void setTransformationHandler(DataFormat dataFormat, DataTransformationType transformationType,
      ITransformationHandler handler);

}
