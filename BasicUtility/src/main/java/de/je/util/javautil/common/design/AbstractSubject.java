/**
 *
 * {@link AbstractSubject}.java
 *
 * @author Jens Ebert
 *
 * @date 06.03.2010
 *
 */
package de.je.util.javautil.common.design;

import java.util.Iterator;
import java.util.List;

import de.je.util.javautil.common.err.Reject;

/**
 * {@link AbstractSubject} is a base class for a subject in the observer pattern.
 */
public abstract class AbstractSubject implements Subject {

   /**
    * @see Subject#addObserver(Observer)
    */
   @Override
   public void addObserver(Observer<Subject> observer) {
      Reject.ifNull(observer, "observer");
      Reject.ifTrue(hasObserver(observer), "hasObserver(observer)");

      m_observers.add(observer);
   }

   /**
    * @see Subject#notifyObservers()
    */
   @Override
   public void notifyObservers() {
      for (Iterator<Observer<Subject>> iterator = m_observers.iterator(); iterator.hasNext();) {
         Observer<Subject> observer = iterator.next();

         observer.update(this);
      }
   }

   /**
    * @see Subject#hasObserver(Observer)
    */
   @Override
   public boolean hasObserver(Observer<Subject> observer) {
      Reject.ifNull(observer, "observer");

      return m_observers.contains(observer);
   }

   /**
    * @see Subject#removeObserver(Observer)
    */
   @Override
   public void removeObserver(Observer<Subject> observer) {
      Reject.ifNull(observer, "observer");
      Reject.ifFalse(hasObserver(observer), "hasObserver(observer)");

      m_observers.remove(observer);
   }

   private List<Observer<Subject>> m_observers;
}
