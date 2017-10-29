package com.github.jmeta.tools.benchmark.api.types;

import com.github.jmeta.tools.benchmark.api.services.MeasurementSession;

/**
 * {@link MeasuredCommand} is a {@link Command} for measurement purposes. Application that want to use the
 * {@link MeasurementSession#runMeasurement(MeasuredCommand[], int)} method must implement the {@link MeasuredCommand}
 * interface and call the the code to be measured from its execute method.
 */
public interface MeasuredCommand extends Command {

   /**
    * Should return a unique name of the {@link MeasuredCommand} for identification purposes, i.e. for readable
    * measurement reports.
    * 
    * @return a unique name of the {@link MeasuredCommand} for identification purposes, i.e. for readable measurement
    *         reports.
    */
   public String getUniqueName();
}
