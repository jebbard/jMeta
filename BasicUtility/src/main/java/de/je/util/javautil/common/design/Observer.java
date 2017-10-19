/**
 *
 * {@link Observer}.java
 *
 * @author Jens Ebert
 *
 * @date 06.03.2010
 *
 */
package de.je.util.javautil.common.design;

/**
 * {@link Observer} represents an observer in the observer pattern.
 *
 * @param <T>
 *           The type of subject
 */
public interface Observer<T extends Subject> {

   /**
    * Updates the subject about an event.
    * 
    * @param subject
    *           The subject to update
    */
   public void update(T subject);
}
