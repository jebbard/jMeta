/**
 *
 * MediumReferenceFactory.java
 *
 * @author Jens
 *
 * @date 20.05.2016
 *
 */
package de.je.jmeta.media.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import de.je.jmeta.media.api.IMedium;
import de.je.jmeta.media.api.IMediumReference;
import de.je.jmeta.media.api.datatype.MediumAction;
import de.je.jmeta.media.api.datatype.MediumActionType;
import de.je.jmeta.media.api.datatype.MediumRegion;
import de.je.util.javautil.common.err.Contract;
import de.je.util.javautil.common.err.Reject;

/**
 * {@link MediumReferenceFactory} is a class to create and manage all created {@link IMediumReference} instances. Due to
 * this, it has a strong coupling to the {@link IMediumReference} and {@link StandardMediumReference} classes.
 * 
 * When creating an instance of {@link IMediumReference}, it automatically passes a reference to itself to the instance,
 * to ensure any call to {@link IMediumReference#advance(long)} can use it to create advanced {@link IMediumReference}
 * instance.
 * 
 * It not only creates {@link IMediumReference} instances, but it also maintains all created instances in an internal
 * data structure. It provides corresponding methods to retrieve all {@link IMediumReference}s in a specific range.
 * Furthermore, it implements update functionality to automatically update all {@link IMediumReference} instances due to
 * an action that was done on the current {@link IMedium}.
 */
public class MediumReferenceFactory {

   private final List<IMediumReference> references = new ArrayList<>();

   private final IMedium<?> medium;

   /**
    * Creates a new {@link MediumReferenceFactory}.
    * 
    * @param medium
    *           The {@link IMedium} this {@link MediumReferenceFactory} is working on.
    */
   public MediumReferenceFactory(IMedium<?> medium) {
      Reject.ifNull(medium, "medium");
      this.medium = medium;
   }

   /**
    * The factory method to create a new {@link IMediumReference} instance.
    * 
    * @param absoluteMediumOffset
    *           The absolute, positive and null-origin byte offset of the new {@link IMediumReference} instance.
    * @return The created {@link IMediumReference} instance.
    */
   public IMediumReference createMediumReference(long absoluteMediumOffset) {

      StandardMediumReference newReference = new StandardMediumReference(medium, absoluteMediumOffset);

      // To ensure IMediumReference.advance() can add the advanced references to "this"
      newReference.setMediumReferenceRepository(this);

      references.add(newReference);

      return newReference;
   }

   /**
    * Returns the {@link IMedium} this {@link MediumReferenceFactory} is working on.
    * 
    * @return medium the {@link IMedium} this {@link MediumReferenceFactory} is working on.
    */
   public IMedium<?> getMedium() {

      return medium;
   }

   /**
    * Updates all {@link IMediumReference}s currently managed by this {@link MediumReferenceFactory} according to the
    * given {@link MediumAction}. Only {@link MediumAction}s of types {@link MediumActionType#INSERT} and
    * {@link MediumActionType#REMOVE} are supported, because only these actions move objects (in terms of sequences of
    * bytes) that are stored behind the location of the change. In case of {@link MediumActionType#INSERT}, bytes behind
    * the insert location must be moved back by the number of inserted bytes. In case of {@link MediumActionType#REMOVE}
    * , bytes behind the remove location must be moved forward by the number of removed bytes. In either case, the idea
    * of this method is that bytes/objects on the {@link IMedium} get addressed by their {@link IMediumReference}. And
    * if an insertion or removal happens before these objects, their {@link IMediumReference} addresses must be changed
    * accordingly. Otherwise these {@link IMediumReference}s would no longer point to the same objects/bytes.
    * 
    * @param action
    *           The {@link MediumAction} to apply. Only {@link MediumAction}s of types {@link MediumActionType#INSERT}
    *           and {@link MediumActionType#REMOVE} are supported. Must refer to the same {@link IMedium} as this
    *           {@link MediumReferenceFactory}.
    */
   public void updateReferences(MediumAction action) {

      Reject.ifNull(action, "action");

      Contract.checkPrecondition(
         action.getActionType() == MediumActionType.INSERT || action.getActionType() == MediumActionType.REMOVE
            || action.getActionType() == MediumActionType.REPLACE,
         "Only " + MediumActionType.class.getSimpleName() + MediumActionType.REMOVE.toString() + ", "
            + MediumActionType.class.getSimpleName() + MediumActionType.REPLACE.toString() + " and "
            + MediumActionType.class.getSimpleName() + MediumActionType.INSERT.toString() + " are allowed"

      );

      IMediumReference startReference = action.getRegion().getStartReference();

      IMediumReference.validateSameMedium(startReference, getMedium());

      boolean insertingReplace = false;
      boolean removingReplace = false;

      int k = action.getRegion().getSize();

      if (action.getActionType() == MediumActionType.REPLACE) {

         if (action.getActionBytes().remaining() > action.getRegion().getSize()) {
            // N bytes are replaced by M > N bytes => It is an insert
            insertingReplace = true;

            startReference = startReference.advance(action.getRegion().getSize());

            k = action.getActionBytes().remaining() - action.getRegion().getSize();

         } else if (action.getActionBytes().remaining() < action.getRegion().getSize()) {
            // N bytes are replaced by M < N bytes => It is a remove
            removingReplace = true;

            startReference = startReference.advance(action.getActionBytes().remaining());

            k = action.getRegion().getSize() - action.getActionBytes().remaining();
         }
      }

      List<IMediumReference> referencesBehindOrEqual = getAllReferencesBehindOrEqual(startReference);

      long y = startReference.getAbsoluteMediumOffset();

      for (IMediumReference updatedReference : referencesBehindOrEqual) {
         long x = updatedReference.getAbsoluteMediumOffset();

         StandardMediumReference ref = (StandardMediumReference) updatedReference;

         if (action.getActionType() == MediumActionType.INSERT || insertingReplace) {
            ref.setAbsoluteMediumOffset(x + k);
         } else if (action.getActionType() == MediumActionType.REMOVE || removingReplace) {

            if (y + k > x) {
               ref.setAbsoluteMediumOffset(y);
            } else {
               ref.setAbsoluteMediumOffset(x - k);
            }
         }
      }
   }

   /**
    * Clears the entire factory of all contained references already created.
    */
   public void clear() {

      references.clear();
   }

   /**
    * Returns all {@link IMediumReference}s currently maintained in this factory.
    * 
    * @return all {@link IMediumReference}s currently maintained in this factory.
    */
   public List<IMediumReference> getAllReferences() {

      return Collections.unmodifiableList(references);
   }

   /**
    * Returns all {@link IMediumReference}s currently maintained in this factory that lie within the provided
    * {@link MediumRegion}.
    * 
    * @param region
    *           The {@link MediumRegion}
    * @return all {@link IMediumReference}s currently maintained in this factory that lie within the provided
    *         {@link MediumRegion}.
    */
   public List<IMediumReference> getAllReferencesInRegion(MediumRegion region) {

      Reject.ifNull(region, "region");
      IMediumReference.validateSameMedium(region.getStartReference(), getMedium());

      List<IMediumReference> allReferencesInRegion = new ArrayList<>();

      for (IMediumReference mediumReference : references) {
         if (region.contains(mediumReference)) {
            allReferencesInRegion.add(mediumReference);
         }
      }

      return allReferencesInRegion;
   }

   /**
    * Returns all {@link IMediumReference}s currently maintained in this factory that lie behind or are equal to the
    * provided {@link IMediumReference}.
    * 
    * @param reference
    *           The {@link IMediumReference}, must belong to the same {@link IMedium}.
    * @return all {@link IMediumReference}s currently maintained in this factory that lie behind or are equal to the
    *         provided {@link IMediumReference}.
    */
   public List<IMediumReference> getAllReferencesBehindOrEqual(IMediumReference reference) {

      Reject.ifNull(reference, "reference");
      IMediumReference.validateSameMedium(reference, getMedium());

      List<IMediumReference> allReferencesBehindOrEqual = new ArrayList<>();

      for (IMediumReference mediumReference : references) {
         if (mediumReference.behindOrEqual(reference)) {
            allReferencesBehindOrEqual.add(mediumReference);
         }
      }

      return allReferencesBehindOrEqual;
   }
}
