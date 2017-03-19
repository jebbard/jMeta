package de.je.jmeta.common;

/**
 * {@link ILoggingMessageConstants} provides general string constants for logging several classes of issues. Using it
 * provides a uniform logging layout. Furthermore, it allows to find problems in log files by applying standardized
 * search patterns.
 *
 * Constants starting with "PREFIX" are meant to be used as prefix to a string to log, Constants starting with "SUFFIX"
 * are meant to be used as suffix to a string to log,
 */
public interface ILoggingMessageConstants {

   /**
    * Used as prefix to a string that states that a specific task has changed its state.
    */
   public static final String PREFIX_TASK = " ...... ";

   /**
    * Used as suffix to a string that states that a specific task has changed its state.
    */
   public static final String SUFFIX_TASK = " ...... ";

   /**
    * States that an arbitrary task is starting
    */
   public static final String PREFIX_TASK_STARTING = PREFIX_TASK
      + " [STARTING]   ";

   /**
    * States that an arbitrary task has been completed neutrally, without knowing its exact results (completed maybe
    * with errors, warnings or without).
    */
   public static final String PREFIX_TASK_DONE_NEUTRAL = PREFIX_TASK
      + " [DONE]       ";

   /**
    * States that an arbitrary task has been completed successfully.
    */
   public static final String PREFIX_TASK_DONE_SUCCESSFUL = PREFIX_TASK
      + " [SUCCESSFUL] ";

   /**
    * States that an arbitrary task failed.
    */
   public static final String PREFIX_TASK_FAILED = PREFIX_TASK
      + " [FAILED]     ";

   /**
    * The line separator.
    */
   public static final String LINE_SEPARATOR = System
      .getProperty("line.separator");

   /**
    * Prefix for critical errors.
    */
   public static final String PREFIX_CRITICAL_ERROR = "CRITICAL ERROR: ";

   /**
    * Prefix for runtime exceptions.
    */
   public static final String PREFIX_RUNTIME_EXCEPTION = "## RUNTIME EXCEPTION ##: ";

   /**
    * Prefix for checked exceptions.
    */
   public static final String PREFIX_CHECKED_EXCEPTION = "## CHECKED EXCEPTION ##: ";

   /**
    * Prefix for any other throwables.
    */
   public static final String PREFIX_THROWABLE = "## THROWABLE ##: ";

   /**
    * For logging the event of a cache miss.
    */
   public static final String CACHE_MISS = "***** Cache miss *****";
}
