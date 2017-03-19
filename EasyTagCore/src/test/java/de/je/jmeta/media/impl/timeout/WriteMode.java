/**
 *
 * {@link WriteMode}.java
 *
 * @author Jens Ebert
 *
 * @date 25.04.2011
 */
package de.je.jmeta.media.impl.timeout;

/**
 * {@link WriteMode} defines write modes for the {@link BlockedInputStreamSimulator} test class.
 */
public enum WriteMode {
   /**
    * All content is written at once.
    */
   WM_AT_ONCE,
   /**
    * Content is written piece-wise with a sleep delay within each write. Writing is nevertheless finished before
    * expiration of the timeout.
    */
   WM_BEFORE_TIMEOUT,
   /**
    * Content is written piece-wise with a sleep delay within each write. Writing is not finished until expiration of
    * the timeout.
    */
   WM_EXPIRE_TIMEOUT,
}
