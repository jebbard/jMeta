/**
 *
 * {@link AbstractMedium}.java
 *
 * @author Jens Ebert
 *
 * @date 24.12.2010
 *
 */

package com.github.jmeta.library.media.api.type;

import de.je.util.javautil.common.configparams.AbstractConfigParam;
import de.je.util.javautil.common.configparams.ConfigParamHandler;
import de.je.util.javautil.common.err.Reject;

/**
 * {@link AbstractMedium} represents a physical medium where binary data can be stored.
 * 
 * @param <T>
 *           The concrete type of wrapped medium object.
 */
public abstract class AbstractMedium<T> extends ConfigParamHandler
   implements IMedium<T> {

   private T medium;

   private final String name;

   private final boolean isRandomAccess;

   private final boolean isReadOnly;

   /**
    * Creates a new {@link AbstractMedium}.
    * 
    * @param medium
    *           The medium to store.
    * @param name
    *           An externally known name of the {@link AbstractMedium} that must be used as medium name. May be null to
    *           indicate that only the physical name of the {@link AbstractMedium} is relevant.
    * @param isRandomAccess
    *           true if the medium is random-access, false otherwise
    * @param isReadOnly
    *           true to make this a read-only {@link IMedium}, false otherwise.
    * @param supportedParameters
    *           The supported {@link AbstractConfigParam}s. For ease of specification, it is an array. The array must
    *           not contain two {@link AbstractConfigParam} instances with the same name, especially they must not
    *           contain the same instance twice. The specified array must contain at least one
    *           {@link AbstractConfigParam}.
    */
   public AbstractMedium(T medium, String name, boolean isRandomAccess,
      boolean isReadOnly,
      AbstractConfigParam<? extends Comparable<?>>[] supportedParameters) {
      super(supportedParameters);

      Reject.ifNull(medium, "medium");

      this.medium = medium;
      this.name = name;
      this.isRandomAccess = isRandomAccess;
      this.isReadOnly = isReadOnly;
   }

   /**
    * @see java.lang.Object#equals(java.lang.Object)
    */
   @Override
   public boolean equals(Object obj) {

      if (this == obj) {
         return true;
      }
      if (obj == null) {
         return false;
      }
      if (getClass() != obj.getClass()) {
         return false;
      }
      AbstractMedium<?> other = (AbstractMedium<?>) obj;
      if (medium == null) {
         if (other.medium != null) {
            return false;
         }
      } else if (!medium.equals(other.medium)) {
         return false;
      }
      return true;
   }

   /**
    * @see com.github.jmeta.library.media.api.type.IMedium#getName()
    */
   @Override
   public String getName() {

      return name;
   }

   /**
    * @see com.github.jmeta.library.media.api.type.IMedium#getWrappedMedium()
    */
   @Override
   public T getWrappedMedium() {

      return medium;
   }

   /**
    * @see java.lang.Object#hashCode()
    */
   @Override
   public int hashCode() {

      final int prime = 31;
      int result = 1;
      result = prime * result + ((medium == null) ? 0 : medium.hashCode());
      return result;
   }

   @Override
   public boolean isRandomAccess() {

      return isRandomAccess;
   }

   /**
    * @see com.github.jmeta.library.media.api.type.IMedium#isReadOnly()
    */
   @Override
   public boolean isReadOnly() {

      return isReadOnly;
   }

   /**
    * Provides the possibility to overwrite the wrapped medium.
    * 
    * @param newMedium
    *           The new medium content.
    */
   protected void setNewMediumContent(T newMedium) {

      Reject.ifNull(newMedium, "medium");

      this.medium = newMedium;
   }

   /**
    * @see java.lang.Object#toString()
    */
   @Override
   public String toString() {

      return getClass().getSimpleName() + "[medium=" + medium
         + ", name=" + name + ", isRandomAccess="
         + isRandomAccess + ", isReadOnly=" + isReadOnly + "]";
   }
}
