package com.github.jmeta.tools.benchmark.api.services;

import java.util.concurrent.TimeUnit;

/**
 * {@link AbstractTimeProvider} simply provides a current time stamp in a predefined, fixed {@link TimeUnit}. There may
 * be multiple {@link AbstractTimeProvider} implementations with different precision, i.e. {@link TimeUnit}s.
 */
public abstract class AbstractTimeProvider {

   /**
    * Creates a new {@link AbstractTimeProvider}.
    * 
    * @param unit
    *           The {@link TimeUnit} the time stamps this {@link AbstractTimeProvider} returns are given in.
    */
   public AbstractTimeProvider(TimeUnit unit) {
      this.unit = unit;
   }

   /**
    * Retrieves the current time stamp in the {@link TimeUnit} of this {@link AbstractTimeProvider}.
    * 
    * @return the current time stamp in the {@link TimeUnit} of this {@link AbstractTimeProvider}.
    */
   public abstract long getCurrentTime();

   /**
    * Returns the {@link TimeUnit} the time stamps this {@link AbstractTimeProvider} returns are given in.
    * 
    * @return The {@link TimeUnit} the time stamps this {@link AbstractTimeProvider} returns are given in.
    */
   public TimeUnit getUnit() {
      return unit;
   }

   private final TimeUnit unit;
}
