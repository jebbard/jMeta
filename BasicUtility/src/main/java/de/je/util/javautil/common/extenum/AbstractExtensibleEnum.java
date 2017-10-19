/**
 *
 * {@link AbstractExtendableEnum}.java
 *
 * @author Jens Ebert
 *
 * @date 18.06.2009
 *
 */
package de.je.util.javautil.common.extenum;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import de.je.util.javautil.common.err.Reject;

/**
 * {@link AbstractExtensibleEnum} represents an extensible enumeration base class that provides type safety like Java's
 * "enum" keyword, but does not add the drawback of a highly static nature. Thanks to polymorphism, defining an argument
 * or member with a concrete {@link AbstractExtensibleEnum} always allows to later use an extended enumeration instance
 * of a later added sub-class.
 *
 * A class extending from this class may however also prohibit further extension of the enum values by just specifying a
 * private constructor and the class itself as final.
 * 
 * Every class directly derived from {@link AbstractExtensibleEnum} is called <i>base enum class</i> here. A base enum
 * class spans up a new hierarchy of enumeration constants which must be unique, called a <i>domain</i>. Every instance
 * of a type derived from the base enum class or typed as the base enum class itself must have a unique id in its
 * domain. This is already ensured at creation time. If the same id already exists in the domain, an
 * {@link EnumException} is thrown.
 * 
 * Of course you can define different domains that are fully unrelated, i.e. which can use their own set of ids. Of
 * course, different domains can use the same ids, as long as the ids are unique within each domain itself.
 *
 * @param <T>
 *           The concrete type of class derived from {@link AbstractExtensibleEnum}.
 */
public abstract class AbstractExtensibleEnum<T extends AbstractExtensibleEnum<T>> {

   /**
    * Initializes the {@link AbstractExtensibleEnum}.
    *
    * @param id
    *           The id to use. Must be unique in the domain of the corresponding base enum class.
    *
    * @throws EnumException
    *            If the given id is already defined in the same domain.
    */
   protected AbstractExtensibleEnum(String id) {
      Reject.ifNull(id, "id");

      synchronized (this) {
         @SuppressWarnings("unchecked")
         Class<T> classToAddId = (Class<T>) determineBaseEnumClass(getClass());

         BASE_ENUM_CLASSES.put(getClass(), classToAddId);

         if (ALL_INSTANCES.containsKey(classToAddId)) {
            if (ALL_INSTANCES.get(classToAddId).containsKey(id))
               throw new EnumException(
                  "The enum with id <" + id + "> is already defined for class <" + classToAddId.getSimpleName() + ">.",
                  id, classToAddId);
         }

         else
            ALL_INSTANCES.put(classToAddId, new HashMap<String, AbstractExtensibleEnum<?>>());

         m_id = id;

         ALL_INSTANCES.get(classToAddId).put(id, this);
      }
   }

   /**
    * Returns the unique id of the enum within its id. This id is not necessarily globally unique among all instances of
    * {@link AbstractExtensibleEnum}.
    *
    * @return A unique id of the enum value within its domain.
    */
   public String getId() {
      return m_id;
   }

   /**
    * @see java.lang.Object#toString()
    */
   @Override
   public String toString() {
      return getClass().getName() + "[id=" + getId() + "]";
   }

   /**
    * Returns all currently defined instances of domain the given concrete class belongs to. The return value of this
    * method can only be assigned to {@literal Set<U>}-typed variables, where U is the base enum class.
    *
    * @param concreteEnumClass
    *           The concrete enumeration class.
    * @param <U>
    *           The base enum class
    * @param <T>
    *           The concrete enum class
    * @return all currently defined instances of domain the given concrete class belongs to.
    * @throws EnumException
    *            if the given class is not registered, which should never happen.
    */
   @SuppressWarnings("unchecked")
   public static <U extends AbstractExtensibleEnum<U>, T extends U> Set<U> values(Class<T> concreteEnumClass) {
      Class<?> enumBaseClass = BASE_ENUM_CLASSES.get(concreteEnumClass);

      Set<U> returnedSet = new HashSet<>();

      returnedSet.addAll((Collection<U>) ALL_INSTANCES.get(enumBaseClass).values());

      return returnedSet;
   }

   /**
    * Returns the instance of the given enum class with the given id.
    * 
    * @param concreteEnumClass
    *           The concrete enum class belonging to a specific domain.
    * @param id
    *           The id of the instance to retrieve. The id must exist within the domain of the given class.
    * @param <T>
    *           The concrete enum class
    * @return The instance of the domain with the given id.
    * @throws EnumException
    *            if the given class is not registered, which should never happen. This exception is also thrown if the
    *            given id does not exist within the domain denoted by the given class.
    */
   @SuppressWarnings("unchecked")
   public static <T extends AbstractExtensibleEnum<? super T>> T valueOf(Class<T> concreteEnumClass, String id) {
      Reject.ifNull(concreteEnumClass, "concreteEnumClass");
      Reject.ifNull(id, "id");

      Class<?> enumBaseClass = getBaseEnumClass(concreteEnumClass);

      if (!ALL_INSTANCES.get(enumBaseClass).containsKey(id))
         throw new EnumException("Enum id <" + id + "> is undefined.", id, concreteEnumClass);

      return (T) ALL_INSTANCES.get(enumBaseClass).get(id);
   }

   /**
    * Returns the base enum class for the given class.
    * 
    * @param concreteEnumClass
    *           The concrete enum class. This class belongs to a single base enum class which is its base class that
    *           directly derives from {@link AbstractExtensibleEnum} (could be the concrete enum class itself).
    * @param <T>
    *           The concrete enum class
    * @return the base enum class for the given class.
    */
   @SuppressWarnings("unchecked")
   public static <T extends AbstractExtensibleEnum<? super T>> Class<? super T> getBaseEnumClass(
      Class<T> concreteEnumClass) {
      Reject.ifNull(concreteEnumClass, "concreteEnumClass");

      if (!BASE_ENUM_CLASSES.containsKey(concreteEnumClass))
         throw new EnumException(
            "Class <" + concreteEnumClass.getSimpleName() + "> is not a class derived from AbstractExtensibleEnum.",
            null, concreteEnumClass);

      return (Class<T>) BASE_ENUM_CLASSES.get(concreteEnumClass);
   }

   /**
    * Determines the base enum class for the given class, i.e. the base class of the given class which directly inherits
    * from {@link AbstractExtensibleEnum}.
    * 
    * @param clazz
    *           The class.
    * @return The base enum class.
    */
   private static Class<?> determineBaseEnumClass(Class<?> clazz) {
      if (clazz.getSuperclass() != AbstractExtensibleEnum.class)
         return determineBaseEnumClass(clazz.getSuperclass());

      return clazz;
   }

   private final String m_id;

   private static final Map<Class<?>, Class<? extends AbstractExtensibleEnum<?>>> BASE_ENUM_CLASSES = new HashMap<>();
   private static final Map<Class<? extends AbstractExtensibleEnum<?>>, Map<String, AbstractExtensibleEnum<?>>> ALL_INSTANCES = new HashMap<>();
}
