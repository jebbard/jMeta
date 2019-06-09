/**
 *
 * {@link MeasurementSession}.java
 *
 * @author Jens Ebert
 *
 * @date 25.04.2010
 *
 */
package com.github.jmeta.tools.benchmark.api.services;

import com.github.jmeta.tools.benchmark.api.types.MeasuredCommand;
import com.github.jmeta.tools.benchmark.api.types.MeasuredCommandExecution;
import com.github.jmeta.tools.benchmark.api.types.MeasurementCommandListener;
import com.github.jmeta.tools.benchmark.api.types.MeasurementResult;
import com.github.jmeta.utility.dbc.api.services.Reject;

/**
 * {@link MeasurementSession} performs a time performance measurement. It can
 * operate in two modes:
 * <ul>
 * <li>Explicitly call {@link MeasurementSession#startMeasurement()}, then
 * execute the code to be measured (without any help of
 * {@link MeasurementSession}, and afterwards explicitly call
 * {@link MeasurementSession#stopMeasurement()}.</li>
 * <li>Use {@link MeasurementSession#runMeasurement(MeasuredCommand[], int)},
 * passing {@link MeasuredCommand}s to be executed and measured.</li>
 * </ul>
 * The first option is useful if you don't want to or cannot make the code to be
 * measured be implemented in the {@link MeasuredCommand} interface's execute()
 * method. It is slightly more work to do here and you cannot have fine-grained
 * measurement results per run oder {@link MeasuredCommand}.
 *
 * The second option is easier to use, because the measurement itself is
 * included in the method call and is more fine-grained. Any
 * {@link MeasuredCommand} is executed the given number of times and measured.
 */
public class MeasurementSession {

	/**
	 * Indicates that currently no measurement is running or there has no yet been a
	 * measurement.
	 */
	public static final long NO_MEASUREMENT = -1L;

	private final long sessionId;

	private final String machineInfo;

	private final AbstractTimeProvider timeProvider;

	private long measurementStartTime = MeasurementSession.NO_MEASUREMENT;

	private MeasurementCommandListener listener = null;

	/**
	 * Creates a new {@link MeasurementSession}.
	 * 
	 * @param timeProvider the {@link AbstractTimeProvider} to use for obtaining
	 *                     current time stamps.
	 * @param machineInfo  A string containing information about the test machine,
	 *                     may be null.
	 */
	public MeasurementSession(AbstractTimeProvider timeProvider, String machineInfo) {
		Reject.ifNull(timeProvider, "timeProvider");

		this.machineInfo = machineInfo;
		sessionId = System.currentTimeMillis();
		this.timeProvider = timeProvider;
	}

	/**
	 * Returns a String containing information about the current test machine.
	 *
	 * @return a String containing information about the current test machine.
	 */
	public String getMachineInfo() {
		return machineInfo;
	}

	/**
	 * Returns a unique session id.
	 *
	 * @return a unique session id.
	 */
	public long getSessionId() {
		return sessionId;
	}

	/**
	 * Returns the start time of the current measurement in units of the current
	 * {@link AbstractTimeProvider}, or {@value #NO_MEASUREMENT} if currently no
	 * measurement runs.
	 * 
	 * @return the start time of the current measurement in units of the current
	 *         {@link AbstractTimeProvider}, or {@value #NO_MEASUREMENT} if
	 *         currently no measurement runs.
	 */
	public long getStartTime() {
		return measurementStartTime;
	}

	/**
	 * Returns the {@link AbstractTimeProvider} used by this
	 * {@link MeasurementSession}.
	 * 
	 * @return the {@link AbstractTimeProvider} used by this
	 *         {@link MeasurementSession}.
	 */
	public AbstractTimeProvider getTimeProvider() {
		return timeProvider;
	}

	private boolean isMeasurementRunning() {
		return measurementStartTime != MeasurementSession.NO_MEASUREMENT;
	}

	/**
	 * Performs a measurement of the given {@link MeasuredCommand}s, where each
	 * {@link MeasuredCommand} is executed the given number of times. E.g. if times
	 * = 10 and there are two {@link MeasuredCommand}s, this method runs the first
	 * {@link MeasuredCommand} ten times, and afterwards the second
	 * {@link MeasuredCommand} ten times.
	 * 
	 * The method records the duration of each single
	 * {@link MeasuredCommand#execute()} call in the unit of the current
	 * {@link AbstractTimeProvider} and returns these in execution order as a
	 * {@link MeasurementResult}. The returned {@link MeasurementResult} contains
	 * commands.size() * times results.
	 * 
	 * If a measurement is running already, the method returns null and performs no
	 * further actions.
	 * 
	 * @param commands The {@link MeasuredCommand}s to execute in the given order.
	 *                 Each {@link MeasuredCommand} is executed the given number of
	 *                 times, then the next {@link MeasuredCommand} follows and so
	 *                 on.
	 * @param times    The number of times each {@link MeasuredCommand} is to be
	 *                 executed.
	 * 
	 * @return The {@link MeasurementResult} containing duration information for
	 *         each single {@link MeasuredCommand#execute()} call in the unit of the
	 *         current {@link AbstractTimeProvider}. It contains these in execution
	 *         order.
	 */
	public MeasurementResult runMeasurement(MeasuredCommand[] commands, int times) {
		Reject.ifNull(commands, "commands");
		Reject.ifTrue(times < 0, "times must not be negative");

		if (isMeasurementRunning()) {
			return null;
		}

		int commandCount = commands.length;

		MeasurementResult result = new MeasurementResult(timeProvider.getUnit(), sessionId, commandCount, times);

		startMeasurement();

		for (int i = 0; i < commandCount; i++) {
			for (int n = 0; n < times; n++) {
				if (listener != null) {
					listener.aboutToExecuteCommand(commands[i], this);
				}

				long start = timeProvider.getCurrentTime();
				long stop = MeasurementSession.NO_MEASUREMENT;
				Throwable throwed = null;

				try {
					commands[i].execute();
				} catch (Throwable t) {
					throwed = t;
				}

				stop = timeProvider.getCurrentTime();

				MeasuredCommandExecution execution = result.addExecutedCommand(commands[i], start, stop, throwed);

				if (listener != null) {
					listener.commandExecuted(commands[i], this, execution);
				}
			}
		}

		MeasurementResult explicitResult = stopMeasurement();

		result.setMeasurementStartTime(explicitResult.getMeasurementStartTime());
		result.setMeasurementStopTime(explicitResult.getMeasurementStopTime());

		return result;
	}

	/**
	 * @param listener The listener to set.
	 */
	public void setMeasurementCommandListener(MeasurementCommandListener listener) {
		this.listener = listener;
	}

	/**
	 * Starts a measurement explicitly. The measurement needs to also be stopped
	 * explicitly by calling {@link #stopMeasurement()}. The call has no effect if a
	 * measurement is running already, either started with
	 * {@link #startMeasurement()} or
	 * {@link #runMeasurement(MeasuredCommand[], int)}. It then returns the start
	 * time of the previously started measurement.
	 * 
	 * @return The measurement start time in the unit of the
	 *         {@link AbstractTimeProvider} used.
	 */
	public long startMeasurement() {
		if (isMeasurementRunning()) {
			return measurementStartTime;
		}

		measurementStartTime = timeProvider.getCurrentTime();

		return measurementStartTime;
	}

	/**
	 * Stops a measurement explicitly. The call has no effect if a measurement has
	 * not been started. It then returns null.
	 * 
	 * @return The {@link MeasurementResult} or null if no measurement was running.
	 */
	public MeasurementResult stopMeasurement() {
		if (!isMeasurementRunning()) {
			return null;
		}

		long measurementStopTime = timeProvider.getCurrentTime();
		long measurementStartTimeTemp = measurementStartTime;
		measurementStartTime = MeasurementSession.NO_MEASUREMENT;

		return new MeasurementResult(measurementStartTimeTemp, measurementStopTime, timeProvider.getUnit(), sessionId);
	}
}
