package de.je.util.javautil.common.time;

import java.util.concurrent.TimeUnit;

/**
 * {@link MeasuredCommandExecution} records one execution of a {@link MeasuredCommand}.
 */
public class MeasuredCommandExecution {

   /**
    * Creates a new {@link MeasuredCommandExecution}.
    * 
    * @param executedCommand
    *           The {@link MeasuredCommand} that has been executed.
    * @param startTime
    *           The start time of the execution in {@link TimeUnit}s of the {@link AbstractTimeProvider} used.
    * @param stopTime
    *           The stop time of the execution in {@link TimeUnit}s of the {@link AbstractTimeProvider} used.
    * @param throwed
    *           Contains the {@link Throwable} that was thrown by the {@link MeasuredCommand#execute()} method, if any,
    *           or null if the method did not throw any {@link Throwable}.
    */
   public MeasuredCommandExecution(MeasuredCommand executedCommand, long startTime, long stopTime, Throwable throwed) {
      this.executedCommand = executedCommand;
      this.startTime = startTime;
      this.stopTime = stopTime;
      this.throwed = throwed;
   }

   /**
    * Returns the {@link Throwable} that was thrown by the {@link MeasuredCommand#execute()} method, if any, or null if
    * the method did not throw any {@link Throwable}.
    * 
    * @return the {@link Throwable} that was thrown by the {@link MeasuredCommand#execute()} method, if any, or null if
    *         the method did not throw any {@link Throwable}.
    */
   public Throwable getThrowed() {
      return throwed;
   }

   /**
    * Returns the executed {@link MeasuredCommand}.
    * 
    * @return the executed {@link MeasuredCommand}
    */
   public MeasuredCommand getExecutedCommand() {
      return executedCommand;
   }

   /**
    * Returns the start time of the execution in {@link TimeUnit}s of the {@link AbstractTimeProvider} used.
    * 
    * @return the start time of the execution in {@link TimeUnit}s of the {@link AbstractTimeProvider} used.
    */
   public long getStartTime() {
      return startTime;
   }

   /**
    * Returns the stop time of the execution in {@link TimeUnit}s of the {@link AbstractTimeProvider} used.
    * 
    * @return the stop time of the execution in {@link TimeUnit}s of the {@link AbstractTimeProvider} used.
    */
   public long getStopTime() {
      return stopTime;
   }

   private final MeasuredCommand executedCommand;
   private final long startTime;
   private final long stopTime;
   private final Throwable throwed;
}
