/**
 *
 * {@link Subject}.java
 *
 * @author Jens Ebert
 *
 * @date 06.03.2010
 *
 */
package de.je.util.javautil.common.design;

/**
 * {@link Subject} represents a subject in the observer pattern.
 */
public interface Subject {

   /**
    * Returns true if the subject has the given object as an observer, false otherwise
    * 
    * @param observer
    *           The {@link Observer} to check
    * @return true if the subject has the given object as an observer, false otherwise
    */
   public boolean hasObserver(Observer<Subject> observer);

   /**
    * Adds the given object as observer
    * 
    * @param observer
    *           The observer to add
    */
   public void addObserver(Observer<Subject> observer);

   /**
    * Removes the given object as observer
    * 
    * @param observer
    *           The observer to remove
    */
   public void removeObserver(Observer<Subject> observer);

   /**
    * Notifies all observers
    */
   public void notifyObservers();
}
