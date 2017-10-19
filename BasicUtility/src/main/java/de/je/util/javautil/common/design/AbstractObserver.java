/**
 *
 * {@link AbstractObserver}.java
 *
 * @author Jens Ebert
 *
 * @date 06.03.2010
 *
 */
package de.je.util.javautil.common.design;

import de.je.util.javautil.common.err.Reject;

/**
 * {@link AbstractObserver} is an abstract observer base class according to the observer pattern.
 */
public class AbstractObserver implements Observer<Subject> {

   /**
    * Creates a new {@link AbstractObserver}.
    * 
    * @param subject
    *           The subject
    */
   public AbstractObserver(Subject subject) {
      Reject.ifNull(subject, "subject");
      subject.addObserver(this);
   }

   /**
    * @see Observer#update
    */
   @Override
   public void update(Subject subject) {
      // Does nothing by default
   }
}
