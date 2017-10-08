/**
 *
 * {@link IMedium}.java
 *
 * @author Jens Ebert
 *
 * @date 16.05.2015
 *
 */
package de.je.jmeta.media.api;

/**
 * {@link IMedium} represents a source of binary data storage.
 * 
 * @param <T>
 *           The concrete type of wrapped medium object.
 */
public interface IMedium<T> {

   /**
    * Refers to an unknown length of this {@link IMedium}.
    */
   public static final long UNKNOWN_LENGTH = -1;

   /**
    * Returns whether this {@link IMedium} supports random-access or not.
    * 
    * @return whether this {@link IMedium} supports random-access or not.
    */
   public boolean isRandomAccess();

   /**
    * Returns whether this {@link IMedium} already exists or not.
    * 
    * @return whether this {@link IMedium} already exists or not.
    */
   public boolean exists();

   /**
    * Returns whether this {@link IMedium} supports only read-access or not.
    * 
    * @return whether this {@link IMedium} supports only read-access or not.
    */
   public boolean isReadOnly();

   /**
    * @return the current length of this {@link IMedium}, if it is a random-access medium, or {@link #UNKNOWN_LENGTH} if
    *         it is not. Also returns {@link #UNKNOWN_LENGTH} if the medium does not exist or its length cannot be
    *         determined due to any other reason.
    */
   public long getCurrentLength();

   /**
    * Returns the name of the {@link IMedium}, if any, or null.
    * 
    * @return the name of the {@link IMedium}, if any, or null.
    */
   public String getName();

   /**
    * Returns the wrapped medium object.
    * 
    * @return the wrapped medium object.
    */
   public T getWrappedMedium();

   /**
    * @see java.lang.Object#equals(java.lang.Object)
    * 
    *      Two {@link IMedium} implementations are equal if and only if they refer to the same (i.e. an equal) wrapped
    *      medium object.
    */
   @Override
   public boolean equals(Object obj);

   /**
    * @see java.lang.Object#hashCode()
    */
   @Override
   public int hashCode();

   /**
    * @see java.lang.Object#toString()
    */
   @Override
   public String toString();
}
