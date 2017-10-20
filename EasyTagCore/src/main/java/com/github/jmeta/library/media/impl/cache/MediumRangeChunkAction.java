package com.github.jmeta.library.media.impl.cache;

import java.util.ArrayList;
import java.util.List;

import com.github.jmeta.library.media.api.type.IMedium;
import com.github.jmeta.library.media.api.type.IMediumReference;
import com.github.jmeta.library.media.api.type.MediumRegion;
import com.github.jmeta.utility.dbc.api.services.Reject;

/**
 * Represents an action to perform for a given chunk of a range within an {@link IMedium}. A range is not a formal term,
 * but you can see it as a pre-stage of a {@link MediumRegion}, just referring to an {@link IMediumReference} as start
 * of the range and a size in bytes as the size of the range.
 * 
 * It is a frequently occurring task to split an arbitrary range into regular equally-sized chunk and perform arbitrary
 * actions on each chunk. Despite writing the same integer division and modulo as well as looping code over and over
 * again, this function interface offers the static method
 * {@link #walkDividedRange(Class, IMediumReference, int, int, MediumRangeChunkAction)} that standardizes the split
 * process and delegates to an arbitrary {@link #perform(IMediumReference, int)} method implementing this interface. The
 * method {@link #perform(IMediumReference, int)} can be a implemented as lambda, or reference to a an arbitrary private
 * or static method with the same signature. It gets the corresponding chunk start and size parameters.
 *
 * @param <T>
 *           The return type of {@link MediumRangeChunkAction#perform(IMediumReference, int)}
 */
@FunctionalInterface
public interface MediumRangeChunkAction<T> {

   /**
    * Performs an arbitrary user-defined action based on the given start {@link IMediumReference} and size of the
    * current chunk. It can have an arbitrary return value which is collected and returned by
    * {@link #walkDividedRange(Class, IMediumReference, int, int, MediumRangeChunkAction)}.
    * 
    * {@link #walkDividedRange(Class, IMediumReference, int, int, MediumRangeChunkAction)} calls this method in strict
    * ascending sequence for each chunk of the given chunk size.
    * 
    * @param chunkStartReference
    *           The start {@link IMediumReference} of the current chunk.
    * @param chunkSize
    *           The current chunk's size in bytes
    * @return An arbitrary result of the action
    */
   public T perform(IMediumReference chunkStartReference, int chunkSize);

   /**
    * Implements the division of a given range into zero to N equally sized chunks of a given fixed chunk size and zero
    * or one remainder chunks of a size smaller than the fixed chunk size. For each chunk, it calls the method
    * {@link #perform(IMediumReference, int)} on the given last parameter. It takes the return value of this method and
    * adds it into a result list that is returned. Thus the list contains totalRangeSize / chunkSize elements (in
    * ascending order) if the totalRange size is a multiple of the chunk size, or totalRangeSize / chunkSize + 1
    * elements if the totalRange size is not a multiple of the chunk size (including that case that it is smaller than
    * the chunk size).
    * 
    * @param resultClass
    *           The class of the return value
    * @param rangeStartReference
    *           The start {@link IMediumReference} of the range to divide into chunks
    * @param totalRangeSize
    *           The total size of the range to divide into chunks, must be bigger than 1
    * @param chunkSize
    *           The fixed chunk size to use for dividing the range, must be bigger than 1. Might be bigger than the
    *           total range size which results in just one chunk covering the whole range, or it might be smaller than
    *           the total range size, thus more than one chunk covers the range.
    * @param action
    *           The action to perform, the method {@link #perform(IMediumReference, int)} is called on this reference,
    *           which usually is a lambda expression or a method reference.
    * @return The list of all result values returned by each invocation of the {@link #perform(IMediumReference, int)}
    *         method for each chunk, in strict call sequence, i.e. starting with the first chunk processing result as
    *         first element, going on with the second chunk processing result and so on.
    */
   public static <T> List<T> walkDividedRange(Class<T> resultClass, IMediumReference rangeStartReference,
      int totalRangeSize, int chunkSize, MediumRangeChunkAction<T> action) {

      Reject.ifNull(action, "action");
      Reject.ifNull(rangeStartReference, "rangeStartReference");
      Reject.ifNull(resultClass, "resultClass");
      Reject.ifNegativeOrZero(totalRangeSize, "totalRangeSize");
      Reject.ifNegativeOrZero(chunkSize, "chunkSize");

      int fullChunkCount = totalRangeSize / chunkSize;

      ArrayList<T> resultList = new ArrayList<>(fullChunkCount + 1);

      IMediumReference currentChunkStartReference = rangeStartReference;

      for (int chunkIndex = 0; chunkIndex < fullChunkCount; chunkIndex++) {
         resultList.add(action.perform(currentChunkStartReference, chunkSize));

         currentChunkStartReference = currentChunkStartReference.advance(chunkSize);
      }

      if (totalRangeSize % chunkSize > 0) {
         resultList.add(action.perform(currentChunkStartReference, totalRangeSize % chunkSize));
      }

      return resultList;
   }
}
