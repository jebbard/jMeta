/**
 *
 * {@link CommandExecutedCallback}.java
 *
 * @author Jens Ebert
 *
 * @date 25.04.2010
 *
 */
package com.github.jmeta.tools.benchmark.api.types;

import com.github.jmeta.tools.benchmark.api.services.MeasurementSession;

/**
 * {@link MeasurementCommandListener} is notified whenever a specific {@link Command} has been executed within a given
 * {@link MeasurementSession}.
 */
public interface MeasurementCommandListener {

   /**
    * Notifies a user that the given {@link Command} has been executed within the given {@link MeasurementSession}.
    *
    * @param command
    *           The {@link Command} that has been executed. It provides measurement information on the execution time
    *           and encapsulates the real executed command.
    * @param session
    *           The {@link MeasurementSession} that executed the command.
    * @param measuredCommandExceution
    *           The {@link MeasuredCommandExecution} of the command.
    */
   void commandExecuted(MeasuredCommand command, MeasurementSession session,
      MeasuredCommandExecution measuredCommandExceution);

   /**
    * Notifies a user that the given {@link Command} is being executed directly after this method call within the given
    * {@link MeasurementSession}.
    *
    * @param command
    *           The {@link Command} that is being executed.
    * @param session
    *           The {@link MeasurementSession} that will execute the command.
    */
   void aboutToExecuteCommand(MeasuredCommand command, MeasurementSession session);
}
