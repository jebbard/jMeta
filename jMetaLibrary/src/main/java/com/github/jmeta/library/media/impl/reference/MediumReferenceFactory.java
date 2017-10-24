/**
 *
 * MediumReferenceFactory.java
 *
 * @author Jens
 *
 * @date 20.05.2016
 *
 */
package com.github.jmeta.library.media.impl.reference;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.github.jmeta.library.media.api.types.Medium;
import com.github.jmeta.library.media.api.types.MediumReference;
import com.github.jmeta.library.media.api.types.MediumAction;
import com.github.jmeta.library.media.api.types.MediumActionType;
import com.github.jmeta.library.media.api.types.MediumRegion;
import com.github.jmeta.utility.dbc.api.services.Reject;

/**
 * {@link MediumReferenceFactory} is a class to create and manage all created {@link MediumReference} instances. Due to
 * this, it has a strong coupling to the {@link MediumReference} and {@link StandardMediumReference} classes.
 * 
 * When creating an instance of {@link MediumReference}, it automatically passes a reference to itself to the instance,
 * to ensure any call to {@link MediumReference#advance(long)} can use it to create advanced {@link MediumReference}
 * instance.
 * 
 * It not only creates {@link MediumReference} instances, but it also maintains all created instances in an internal
 * data structure. It provides corresponding methods to retrieve all {@link MediumReference}s in a specific range.
 * Furthermore, it implements update functionality to automatically update all {@link MediumReference} instances due to
 * an action that was done on the current {@link Medium}.
 */
public class MediumReferenceFactory {

   private final List<MediumReference> references = new ArrayList<>();

   private final Medium<?> medium;

   /**
    * Creates a new {@link MediumReferenceFactory}.
    * 
    * @param medium
    *           The {@link Medium} this {@link MediumReferenceFactory} is working on.
    */
   public MediumReferenceFactory(Medium<?> medium) {
      Reject.ifNull(medium, "medium");
      this.medium = medium;
   }

   /**
    * The factory method to create a new {@link MediumReference} instance.
    * 
    * @param absoluteMediumOffset
    *           The absolute, positive and null-origin byte offset of the new {@link MediumReference} instance.
    * @return The created {@link MediumReference} instance.
    */
   public MediumReference createMediumReference(long absoluteMediumOffset) {

      StandardMediumReference newReference = new StandardMediumReference(medium, absoluteMediumOffset);

      // To ensure IMediumReference.advance() can add the advanced references to "this"
      newReference.setMediumReferenceRepository(this);

      references.add(newReference);

      return newReference;
   }

   /**
    * Returns the {@link Medium} this {@link MediumReferenceFactory} is working on.
    * 
    * @return medium the {@link Medium} this {@link MediumReferenceFactory} is working on.
    */
   public Medium<?> getMedium() {

      return medium;
   }

   /**
    * Updates all {@link MediumReference}s currently managed by this {@link MediumReferenceFactory} according to the
    * given {@link MediumAction}. Only {@link MediumAction}s of types {@link MediumActionType#INSERT} and
    * {@link MediumActionType#REMOVE} are supported, because only these actions move objects (in terms of sequences of
    * bytes) that are stored behind the location of the change. In case of {@link MediumActionType#INSERT}, bytes behind
    * the insert location must be moved back by the number of inserted bytes. In case of {@link MediumActionType#REMOVE}
    * , bytes behind the remove location must be moved forward by the number of removed bytes. In either case, the idea
    * of this method is that bytes/objects on the {@link Medium} get addressed by their {@link MediumReference}. And
    * if an insertion or removal happens before these objects, their {@link MediumReference} addresses must be changed
    * accordingly. Otherwise these {@link MediumReference}s would no longer point to the same objects/bytes.
    * 
    * @param action
    *           The {@link MediumAction} to apply. Only {@link MediumAction}s of types {@link MediumActionType#INSERT}
    *           and {@link MediumActionType#REMOVE} are supported. Must refer to the same {@link Medium} as this
    *           {@link MediumReferenceFactory}.
    */
   public void updateReferences(MediumAction action) {

      Reject.ifNull(action, "action");

      Reject.ifTrue(
         action.getActionType() != MediumActionType.INSERT && action.getActionType() != MediumActionType.REMOVE
            && action.getActionType() != MediumActionType.REPLACE,
         "action.getActionType() != MediumActionType.INSERT && action.getActionType() != MediumActionType.REMOVE && action.getActionType() != MediumActionType.REPLACE");

      MediumReference startReference = action.getRegion().getStartReference();

      Reject.ifFalse(startReference.getMedium().equals(getMedium()),
	   	         "startReference.getMedium().equals(getMedium())");

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

      List<MediumReference> referencesBehindOrEqual = getAllReferencesBehindOrEqual(startReference);

      long y = startReference.getAbsoluteMediumOffset();

      for (MediumReference updatedReference : referencesBehindOrEqual) {
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
    * Returns all {@link MediumReference}s currently maintained in this factory.
    * 
    * @return all {@link MediumReference}s currently maintained in this factory.
    */
   public List<MediumReference> getAllReferences() {

      return Collections.unmodifiableList(references);
   }

   /**
    * Returns all {@link MediumReference}s currently maintained in this factory that lie within the provided
    * {@link MediumRegion}.
    * 
    * @param region
    *           The {@link MediumRegion}
    * @return all {@link MediumReference}s currently maintained in this factory that lie within the provided
    *         {@link MediumRegion}.
    */
   public List<MediumReference> getAllReferencesInRegion(MediumRegion region) {

      Reject.ifNull(region, "region");
      Reject.ifFalse(region.getStartReference().getMedium().equals(getMedium()),
	   	         "region.getStartReference().getMedium().equals(getMedium())");

      List<MediumReference> allReferencesInRegion = new ArrayList<>();

      for (MediumReference mediumReference : references) {
         if (region.contains(mediumReference)) {
            allReferencesInRegion.add(mediumReference);
         }
      }

      return allReferencesInRegion;
   }

   /**
    * Returns all {@link MediumReference}s currently maintained in this factory that lie behind or are equal to the
    * provided {@link MediumReference}.
    * 
    * @param reference
    *           The {@link MediumReference}, must belong to the same {@link Medium}.
    * @return all {@link MediumReference}s currently maintained in this factory that lie behind or are equal to the
    *         provided {@link MediumReference}.
    */
   public List<MediumReference> getAllReferencesBehindOrEqual(MediumReference reference) {

      Reject.ifNull(reference, "reference");
      Reject.ifFalse(reference.getMedium().equals(getMedium()),
	   	         "reference.getMedium().equals(getMedium())");

      List<MediumReference> allReferencesBehindOrEqual = new ArrayList<>();

      for (MediumReference mediumReference : references) {
         if (mediumReference.behindOrEqual(reference)) {
            allReferencesBehindOrEqual.add(mediumReference);
         }
      }

      return allReferencesBehindOrEqual;
   }
}
