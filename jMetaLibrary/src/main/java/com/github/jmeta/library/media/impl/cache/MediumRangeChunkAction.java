package com.github.jmeta.library.media.impl.cache;

import java.util.ArrayList;
import java.util.List;

import com.github.jmeta.library.media.api.types.Medium;
import com.github.jmeta.library.media.api.types.MediumOffset;
import com.github.jmeta.library.media.api.types.MediumRegion;
import com.github.jmeta.utility.dbc.api.services.Reject;

/**
 * Represents an action to perform for a given chunk of a range within an {@link Medium}. A range is not a formal term,
 * but you can see it as a pre-stage of a {@link MediumRegion}, just referring to an {@link MediumOffset} as start of
 * the range and a size in bytes as the size of the range.
 * 
 * It is a frequently occurring task to split an arbitrary range into regular equally-sized chunk and perform arbitrary
 * actions on each chunk. Despite writing the same integer division and modulo as well as looping code over and over
 * again, this function interface offers the static method
 * {@link #performActionOnChunksInRange(Class, MediumOffset, int, int, MediumRangeChunkAction)} that standardizes the
 * split process and delegates to an arbitrary {@link #perform(MediumOffset, int)} method implementing this interface.
 * The method {@link #perform(MediumOffset, int)} can be a implemented as lambda, or reference to a an arbitrary private
 * or static method with the same signature. It gets the corresponding chunk start and size parameters.
 *
 * @param <T>
 *           The return type of {@link MediumRangeChunkAction#perform(MediumOffset, int)}
 * @param <E>
 *           The type of exception thrown by the action, use RuntimeException if it does not throw anything
 */
@FunctionalInterface
public interface MediumRangeChunkAction<T, E extends Throwable> {

   /**
    * Performs an arbitrary user-defined action based on the given start {@link MediumOffset} and size of the current
    * chunk. It can have an arbitrary return value which is collected and returned by
    * {@link #performActionOnChunksInRange(Class, MediumOffset, int, int, MediumRangeChunkAction)}.
    * 
    * {@link #performActionOnChunksInRange(Class, MediumOffset, int, int, MediumRangeChunkAction)} calls this method in
    * strict ascending sequence for each chunk of the given chunk size.
    * 
    * @param chunkStartReference
    *           The start {@link MediumOffset} of the current chunk.
    * @param chunkSizeInBytes
    *           The current chunk's size in bytes
    * @return An arbitrary result of the action
    * 
    * @throws E
    *            An arbitrary checked or unchecked exception the action might throw
    */
   public T perform(MediumOffset chunkStartReference, int chunkSizeInBytes) throws E;

   /**
    * Implements the division of a given range into zero to N equally sized chunks of a given fixed chunk size and zero
    * or one remainder chunks of a size smaller than the fixed chunk size. For each chunk, it calls the method
    * {@link #perform(MediumOffset, int)} on the given last parameter. It takes the return value of this method and adds
    * it into a result list that is returned. Thus the list contains totalRangeSize / chunkSize elements (in ascending
    * order) if the totalRange size is a multiple of the chunk size, or totalRangeSize / chunkSize + 1 elements if the
    * totalRange size is not a multiple of the chunk size (including that case that it is smaller than the chunk size).
    * 
    * @param resultClass
    *           The class of the return value
    * @param rangeStartReference
    *           The start {@link MediumOffset} of the range to divide into chunks
    * @param totalRangeSizeInBytes
    *           The total size of the range to divide into chunks, must be bigger than 1
    * @param chunkSizeInBytes
    *           The fixed chunk size to use for dividing the range, must be bigger than 1. Might be bigger than the
    *           total range size which results in just one chunk covering the whole range, or it might be smaller than
    *           the total range size, thus more than one chunk covers the range.
    * @param action
    *           The action to perform, the method {@link #perform(MediumOffset, int)} is called on this reference, which
    *           usually is a lambda expression or a method reference.
    * @return The list of all result values returned by each invocation of the {@link #perform(MediumOffset, int)}
    *         method for each chunk, in strict call sequence, i.e. starting with the first chunk processing result as
    *         first element, going on with the second chunk processing result and so on.
    */
   public static <T> List<T> performActionOnChunksInRange(Class<T> resultClass, MediumOffset rangeStartReference,
      int totalRangeSizeInBytes, int chunkSizeInBytes, MediumRangeChunkAction<T, RuntimeException> action) {
      return performActionOnChunksInRange(resultClass, RuntimeException.class, rangeStartReference,
         totalRangeSizeInBytes, chunkSizeInBytes, action);
   }

   /**
    * Behaves the same way as
    * {@link #performActionOnChunksInRange(Class, MediumOffset, int, int, MediumRangeChunkAction)}, but supports actions
    * that declare exactly one checked exception of type E that they might throw. If this is needed, you must use this
    * method and specify the type of exception that might be thrown. If it is thrown, this method just redeclares it
    * such that it is also thrown to the outside world as soon as it occurs.
    * 
    * @param resultClass
    *           The class of the return value
    * @param exceptionClass
    *           This is just a class token to be able to perform actions that might throw a checked exception
    * @param rangeStartReference
    *           The start {@link MediumOffset} of the range to divide into chunks
    * @param totalRangeSizeInBytes
    *           The total size of the range to divide into chunks, must be bigger than 1
    * @param chunkSizeInBytes
    *           The fixed chunk size to use for dividing the range, must be bigger than 1. Might be bigger than the
    *           total range size which results in just one chunk covering the whole range, or it might be smaller than
    *           the total range size, thus more than one chunk covers the range.
    * @param action
    *           The action to perform, the method {@link #perform(MediumOffset, int)} is called on this reference, which
    *           usually is a lambda expression or a method reference.
    * @return The list of all result values returned by each invocation of the {@link #perform(MediumOffset, int)}
    *         method for each chunk, in strict call sequence, i.e. starting with the first chunk processing result as
    *         first element, going on with the second chunk processing result and so on.
    * @throws E
    *            In case that any of the chunk actions threw such an exception
    */
   public static <T, E extends Throwable> List<T> performActionOnChunksInRange(Class<T> resultClass,
      Class<E> exceptionClass, MediumOffset rangeStartReference, long totalRangeSizeInBytes, int chunkSizeInBytes,
      MediumRangeChunkAction<T, E> action) throws E {

      Reject.ifNull(action, "action");
      Reject.ifNull(rangeStartReference, "rangeStartReference");
      Reject.ifNull(resultClass, "resultClass");
      Reject.ifNull(exceptionClass, "exceptionClass");
      Reject.ifNegativeOrZero(totalRangeSizeInBytes, "totalRangeSizeInBytes");
      Reject.ifNegativeOrZero(chunkSizeInBytes, "chunkSizeInBytes");

      long fullChunkCountLong = totalRangeSizeInBytes / chunkSizeInBytes;

      if (fullChunkCountLong > Integer.MAX_VALUE) {
         throw new RuntimeException(
            "Overflow in total range size, as the chunk count is bigger than " + Integer.MAX_VALUE);
      }

      int fullChunkCount = (int) fullChunkCountLong;

      ArrayList<T> resultList = new ArrayList<>(fullChunkCount + 1);

      MediumOffset currentChunkStartReference = rangeStartReference;

      for (int chunkIndex = 0; chunkIndex < fullChunkCount; chunkIndex++) {
         resultList.add(action.perform(currentChunkStartReference, chunkSizeInBytes));

         currentChunkStartReference = currentChunkStartReference.advance(chunkSizeInBytes);
      }

      if (totalRangeSizeInBytes % chunkSizeInBytes > 0) {
         resultList.add(action.perform(currentChunkStartReference, (int) (totalRangeSizeInBytes % chunkSizeInBytes)));
      }

      return resultList;
   }
}
