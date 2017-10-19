package de.je.util.javautil.common.time;

import java.util.concurrent.TimeUnit;

/**
 * {@link SystemNanosTimeProvider} uses {@link System#nanoTime()} and {@link TimeUnit#NANOSECONDS}.
 */
public class SystemNanosTimeProvider extends AbstractTimeProvider {

   /**
    * Creates a new {@link SystemNanosTimeProvider}.
    */
   public SystemNanosTimeProvider() {
      super(TimeUnit.NANOSECONDS);
   }

   /**
    * @see AbstractTimeProvider#getCurrentTime()
    */
   @Override
   public long getCurrentTime() {
      return System.nanoTime();
   }
}
