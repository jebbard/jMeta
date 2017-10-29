package com.github.jmeta.tools.benchmark.api.types;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import com.github.jmeta.tools.benchmark.api.services.AbstractTimeProvider;
import com.github.jmeta.tools.benchmark.api.services.MeasurementSession;
import com.github.jmeta.utility.dbc.api.services.Reject;

/**
 * {@link MeasurementResult} represents all result information of a single measurement run of a
 * {@link MeasurementSession}.
 */
public class MeasurementResult {

   /**
    * Creates a new {@link MeasurementResult}.
    * 
    * @param measurementStartTime
    *           The total start time of the measurement in {@link TimeUnit}s of the {@link AbstractTimeProvider} used.
    * @param measurementStopTime
    *           The total stop time of the measurement in {@link TimeUnit}s of the {@link AbstractTimeProvider} used.
    * @param measurementUnit
    *           The {@link TimeUnit} of the {@link AbstractTimeProvider} used. All durations in this class are specified
    *           in this {@link TimeUnit}.
    * @param measurementSessionId
    *           The id of the {@link MeasurementSession} that produced this {@link MeasurementResult}.
    */
   public MeasurementResult(long measurementStartTime, long measurementStopTime, TimeUnit measurementUnit,
      long measurementSessionId) {
      Reject.ifNull(measurementUnit, "measurementUnit");

      this.commandCount = 0;
      this.repeatCount = 0;
      this.measurementStartTime = measurementStartTime;
      this.measurementStopTime = measurementStopTime;
      this.measurementUnit = measurementUnit;
      this.measurementSessionId = measurementSessionId;
   }

   /**
    * Creates a new {@link MeasurementResult}, setting start and stop time to -1.
    * 
    * @param measurementUnit
    *           The {@link TimeUnit} of the {@link AbstractTimeProvider} used. All durations in this class are specified
    *           in this {@link TimeUnit}.
    * @param measurementSessionId
    *           The id of the {@link MeasurementSession} that produced this {@link MeasurementResult}.
    * @param commandCount
    *           The number of executed {@link MeasuredCommand}s. May be 0 if an explicit measurement without
    *           {@link MeasuredCommand}s has been performed.
    * @param repeatCount
    *           The number of repeated executions of each {@link MeasuredCommand}. May be 0 if an explicit measurement
    *           without {@link MeasuredCommand}s has been performed.
    */
   public MeasurementResult(TimeUnit measurementUnit, long measurementSessionId, int commandCount, int repeatCount) {
      Reject.ifNull(measurementUnit, "measurementUnit");

      this.commandCount = commandCount;
      this.repeatCount = repeatCount;
      this.measurementStartTime = -1L;
      this.measurementStopTime = -1L;
      this.measurementUnit = measurementUnit;
      this.measurementSessionId = measurementSessionId;
   }

   /**
    * Returns the {@link MeasuredCommandExecution}s in this {@link MeasurementResult}.
    * 
    * @return the {@link MeasuredCommandExecution}s in this {@link MeasurementResult}.
    */
   public List<MeasuredCommandExecution> getCommandExecutions() {
      return Collections.unmodifiableList(commandExecutions);
   }

   /**
    * Returns the number of executed {@link MeasuredCommand}s. May be 0 if an explicit measurement without
    * {@link MeasuredCommand}s has been performed.
    * 
    * @return the number of executed {@link MeasuredCommand}s. May be 0 if an explicit measurement without
    *         {@link MeasuredCommand}s has been performed.
    */
   public int getCommandCount() {
      return commandCount;
   }

   /**
    * Returns the number of repeated executions of each {@link MeasuredCommand}. May be 0 if an explicit measurement
    * without {@link MeasuredCommand}s has been performed.
    * 
    * @return the number of repeated executions of each {@link MeasuredCommand}. May be 0 if an explicit measurement
    *         without {@link MeasuredCommand}s has been performed.
    */
   public int getRepeatCount() {
      return repeatCount;
   }

   /**
    * Returns the total start time of the measurement in {@link TimeUnit}s of the {@link AbstractTimeProvider} used.
    * 
    * @return the total start time of the measurement in {@link TimeUnit}s of the {@link AbstractTimeProvider} used.
    */
   public long getMeasurementStartTime() {
      return measurementStartTime;
   }

   /**
    * Returns the total stop time of the measurement in {@link TimeUnit}s of the {@link AbstractTimeProvider} used.
    * 
    * @return the total stop time of the measurement in {@link TimeUnit}s of the {@link AbstractTimeProvider} used.
    */
   public long getMeasurementStopTime() {
      return measurementStopTime;
   }

   /**
    * Returns the {@link TimeUnit} of the {@link AbstractTimeProvider} used. All durations in this class are specified
    * in this {@link TimeUnit}.
    * 
    * @return the {@link TimeUnit} of the {@link AbstractTimeProvider} used. All durations in this class are specified
    *         in this {@link TimeUnit}.
    */
   public TimeUnit getMeasurementUnit() {
      return measurementUnit;
   }

   /**
    * Returns the id of the {@link MeasurementSession} that produced this {@link MeasurementResult}.
    * 
    * @return the id of the {@link MeasurementSession} that produced this {@link MeasurementResult}.
    */
   public long getMeasurementSessionId() {
      return measurementSessionId;
   }

   /**
    * Sets the measurement total start time.
    * 
    * @param measurementStartTime
    *           the measurement total start time.
    */
   public void setMeasurementStartTime(long measurementStartTime) {
      this.measurementStartTime = measurementStartTime;
   }

   /**
    * Sets the measurement total stop time.
    * 
    * @param measurementStopTime
    *           the measurement total stop time.
    */
   public void setMeasurementStopTime(long measurementStopTime) {
      this.measurementStopTime = measurementStopTime;
   }

   /**
    * Adds a new single command measurement result.
    * 
    * @param command
    *           The {@link MeasuredCommand} that has been executed.
    * @param startTime
    *           The start time of the execution in {@link TimeUnit}s of the {@link AbstractTimeProvider} used.
    * @param stopTime
    *           The stop time of the execution in {@link TimeUnit}s of the {@link AbstractTimeProvider} used.
    * @param throwed
    *           Contains the {@link Throwable} that was thrown by the {@link MeasuredCommand#execute()} method, if any,
    *           or null if the method did not throw any {@link Throwable}.
    * @return the {@link MeasuredCommandExecution} of the command.
    */
   public MeasuredCommandExecution addExecutedCommand(MeasuredCommand command, long startTime, long stopTime,
      Throwable throwed) {
      Reject.ifNull(command, "command");

      MeasuredCommandExecution measuredCommandExecution = new MeasuredCommandExecution(command, startTime, stopTime,
         throwed);
      commandExecutions.add(measuredCommandExecution);
      return measuredCommandExecution;
   }

   private final List<MeasuredCommandExecution> commandExecutions = new ArrayList<>();
   private final int commandCount;
   private final int repeatCount;
   private long measurementStartTime;
   private long measurementStopTime;
   private final TimeUnit measurementUnit;
   private final long measurementSessionId;
}
