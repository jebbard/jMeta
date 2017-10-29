package com.github.jmeta.tools.benchmark.api.services;

import java.util.concurrent.TimeUnit;

/**
 * {@link SystemMillisTimeProvider} uses {@link System#currentTimeMillis()} and {@link TimeUnit#MILLISECONDS}.
 */
public class SystemMillisTimeProvider extends AbstractTimeProvider {

   /**
    * Creates a new {@link SystemMillisTimeProvider}.
    */
   public SystemMillisTimeProvider() {
      super(TimeUnit.MILLISECONDS);
   }

   /**
    * @see AbstractTimeProvider#getCurrentTime()
    */
   @Override
   public long getCurrentTime() {
      return System.currentTimeMillis();
   }
}
