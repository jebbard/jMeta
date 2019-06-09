package com.github.jmeta.utility.logging.api.services;

/**
 * {@link LoggingConstants} provides general string constants for logging
 * several classes of issues. Using it provides a uniform logging layout.
 * Furthermore, it allows to find problems in log files by applying standardized
 * search patterns.
 *
 * Constants starting with "PREFIX" are meant to be used as prefix to a string
 * to log, Constants starting with "SUFFIX" are meant to be used as suffix to a
 * string to log,
 */
public interface LoggingConstants {

	/**
	 * Used as prefix to a string that states that a specific task has changed its
	 * state.
	 */
	String PREFIX_TASK = " ...... ";

	/**
	 * Used as suffix to a string that states that a specific task has changed its
	 * state.
	 */
	String SUFFIX_TASK = " ...... ";

	/**
	 * States that an arbitrary task is starting
	 */
	String PREFIX_TASK_STARTING = LoggingConstants.PREFIX_TASK + " [STARTING]   ";

	/**
	 * States that an arbitrary task has been completed neutrally, without knowing
	 * its exact results (completed maybe with errors, warnings or without).
	 */
	String PREFIX_TASK_DONE_NEUTRAL = LoggingConstants.PREFIX_TASK + " [DONE]       ";

	/**
	 * States that an arbitrary task has been completed successfully.
	 */
	String PREFIX_TASK_DONE_SUCCESSFUL = LoggingConstants.PREFIX_TASK + " [SUCCESSFUL] ";

	/**
	 * States that an arbitrary task failed.
	 */
	String PREFIX_TASK_FAILED = LoggingConstants.PREFIX_TASK + " [FAILED]     ";

	/**
	 * The line separator.
	 */
	String LINE_SEPARATOR = System.getProperty("line.separator");

	/**
	 * Prefix for critical errors.
	 */
	String PREFIX_CRITICAL_ERROR = "CRITICAL ERROR: ";

	/**
	 * Prefix for runtime exceptions.
	 */
	String PREFIX_RUNTIME_EXCEPTION = "## RUNTIME EXCEPTION ##: ";

	/**
	 * Prefix for checked exceptions.
	 */
	String PREFIX_CHECKED_EXCEPTION = "## CHECKED EXCEPTION ##: ";

	/**
	 * Prefix for any other throwables.
	 */
	String PREFIX_THROWABLE = "## THROWABLE ##: ";

	/**
	 * For logging the event of a cache miss.
	 */
	String CACHE_MISS = "***** Cache miss *****";
}
