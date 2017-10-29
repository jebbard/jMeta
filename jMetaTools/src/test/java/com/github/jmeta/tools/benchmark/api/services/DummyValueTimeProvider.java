package com.github.jmeta.tools.benchmark.api.services;

import java.util.concurrent.TimeUnit;

import com.github.jmeta.tools.benchmark.api.services.AbstractTimeProvider;
import com.github.jmeta.utility.dbc.api.services.Reject;

/**
 * {@link DummyValueTimeProvider} is an {@link AbstractTimeProvider} for testing purposes. It returns a sequence of
 * predefined times each time {@link #getCurrentTime()} is called.
 * 
 * The sequence is repeated infinitely after all elements have been returned by {@link #getCurrentTime()}.
 */
public class DummyValueTimeProvider extends AbstractTimeProvider {

   /**
    * Creates a new {@link DummyValueTimeProvider}. Initializes unit only. The created {@link DummyValueTimeProvider}
    * does not return any value other than 0.
    * 
    * @param unit
    *           The {@link TimeUnit} of this {@link DummyValueTimeProvider}.
    */
   public DummyValueTimeProvider(TimeUnit unit) {
      super(unit);

      this.returnedTimes = new long[] { 0 };
   }

   /**
    * Creates a new {@link DummyValueTimeProvider} with the given sequence of values.
    * 
    * @param unit
    *           The {@link TimeUnit} of this {@link DummyValueTimeProvider}.
    * @param returnedTimeDiffs
    *           An array of time differences (NOT absolute) times in the specified {@link TimeUnit}. These are summed up
    *           to return a new time.
    * @param timeOffset
    *           The start time offset in the specified {@link TimeUnit} taken as a basis for all times returned by
    *           {@link #getCurrentTime()}.
    */
   public DummyValueTimeProvider(TimeUnit unit, long[] returnedTimeDiffs, long timeOffset) {
      super(unit);

      Reject.ifNull(returnedTimeDiffs, "returnedTimeDiffs");
      Reject.ifTrue(returnedTimeDiffs.length == 0, "The given time diff array must at least contain 1 element");
      Reject.ifTrue(timeOffset < 0, "The given time offset must not be negative");

      this.returnedTimes = new long[returnedTimeDiffs.length];

      this.returnedTimes[0] = timeOffset + returnedTimeDiffs[0];

      // Accumulate times to return
      for (int i = 1; i < returnedTimeDiffs.length; i++)
         this.returnedTimes[i] = returnedTimeDiffs[i] + this.returnedTimes[i - 1];
   }

   /**
    * Returns the current time in millis
    * 
    * @return The current time in millis
    */
   @Override
   public long getCurrentTime() {
      if (callCounter == returnedTimes.length)
         callCounter = 0;

      return returnedTimes[callCounter++];
   }

   private final long[] returnedTimes;
   private int callCounter = 0;
}
