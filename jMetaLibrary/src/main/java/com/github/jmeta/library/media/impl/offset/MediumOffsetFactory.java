/**
 *
 * MediumReferenceFactory.java
 *
 * @author Jens
 *
 * @date 20.05.2016
 *
 */
package com.github.jmeta.library.media.impl.offset;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.github.jmeta.library.media.api.types.Medium;
import com.github.jmeta.library.media.api.types.MediumAction;
import com.github.jmeta.library.media.api.types.MediumActionType;
import com.github.jmeta.library.media.api.types.MediumOffset;
import com.github.jmeta.library.media.api.types.MediumRegion;
import com.github.jmeta.utility.dbc.api.services.Reject;

/**
 * {@link MediumOffsetFactory} is a class to create and manage all created {@link MediumOffset} instances. Due to this,
 * it has a strong coupling to the {@link MediumOffset} and {@link StandardMediumOffset} classes.
 * 
 * When creating an instance of {@link MediumOffset}, it automatically passes a reference to itself to the instance, to
 * ensure any call to {@link MediumOffset#advance(long)} can use it to create advanced {@link MediumOffset} instance.
 * 
 * It not only creates {@link MediumOffset} instances, but it also maintains all created instances in an internal data
 * structure. It provides corresponding methods to retrieve all {@link MediumOffset}s in a specific range. Furthermore,
 * it implements update functionality to automatically update all {@link MediumOffset} instances due to an action that
 * was done on the current {@link Medium}.
 */
public class MediumOffsetFactory {

   private final List<MediumOffset> offsets = new ArrayList<>();

   private final Medium<?> medium;

   /**
    * Creates a new {@link MediumOffsetFactory}.
    * 
    * @param medium
    *           The {@link Medium} this {@link MediumOffsetFactory} is working on.
    */
   public MediumOffsetFactory(Medium<?> medium) {
      Reject.ifNull(medium, "medium");
      this.medium = medium;
   }

   /**
    * The factory method to create a new {@link MediumOffset} instance.
    * 
    * @param absoluteMediumOffset
    *           The absolute, positive and null-origin byte offset of the new {@link MediumOffset} instance.
    * @return The created {@link MediumOffset} instance.
    */
   public MediumOffset createMediumOffset(long absoluteMediumOffset) {

      StandardMediumOffset newOffset = new StandardMediumOffset(medium, absoluteMediumOffset);

      // To ensure IMediumReference.advance() can add the advanced references to "this"
      newOffset.setMediumReferenceRepository(this);

      offsets.add(newOffset);

      return newOffset;
   }

   /**
    * Returns the {@link Medium} this {@link MediumOffsetFactory} is working on.
    * 
    * @return medium the {@link Medium} this {@link MediumOffsetFactory} is working on.
    */
   public Medium<?> getMedium() {

      return medium;
   }

   /**
    * Updates all {@link MediumOffset}s currently managed by this {@link MediumOffsetFactory} according to the given
    * {@link MediumAction}. Only {@link MediumAction}s of types {@link MediumActionType#INSERT},
    * {@link MediumActionType#REMOVE} and {@link MediumActionType#REPLACE} are supported, because only these actions
    * move objects (in terms of sequences of bytes) that are stored behind the location of the change.
    * <ul>
    * <li>For {@link MediumActionType#INSERT}s, bytes at offsets equal to or behind the insert offset must be moved back
    * by the number of inserted bytes.</li>
    * <li>For {@link MediumActionType#REMOVE}s, bytes behind the removed region must be moved forward by the number of
    * removed bytes. Any offsets <i>within</i> the removed region fall back to the remove offset.</li>
    * <li>For {@link MediumActionType#REPLACE}s, bytes behind the replaced region must be moved forward by the number of
    * removed bytes (if it is a removing replace) or must be moved backward by the number of inserted bytes (if it is an
    * inserting replace), or must not be moved at all (if it is an overwriting replace). Any offsets <i>within</i> the
    * replaced region are not changed at all.</li>
    * </ul>
    * 
    * @param action
    *           The {@link MediumAction} to apply. Only {@link MediumAction}s of types {@link MediumActionType#INSERT},
    *           {@link MediumActionType#REMOVE} and {@link MediumActionType#REPLACE} are supported. Must refer to the
    *           same {@link Medium} as this {@link MediumOffsetFactory}.
    */
   public void updateOffsets(MediumAction action) {

      Reject.ifNull(action, "action");

      Reject.ifTrue(
         action.getActionType() != MediumActionType.INSERT && action.getActionType() != MediumActionType.REMOVE
            && action.getActionType() != MediumActionType.REPLACE,
         "action.getActionType() != MediumActionType.INSERT && action.getActionType() != MediumActionType.REMOVE && action.getActionType() != MediumActionType.REPLACE");

      MediumOffset startReference = action.getRegion().getStartOffset();

      Reject.ifFalse(startReference.getMedium().equals(getMedium()), "startReference.getMedium().equals(getMedium())");

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

      List<MediumOffset> referencesBehindOrEqual = getAllOffsetsBehindOrEqual(startReference);

      long y = startReference.getAbsoluteMediumOffset();

      for (MediumOffset offsetToUpdate : referencesBehindOrEqual) {
         long x = offsetToUpdate.getAbsoluteMediumOffset();

         StandardMediumOffset ref = (StandardMediumOffset) offsetToUpdate;

         // For INSERTS, we only update all medium offsets EXCEPT the causing action's start offset which must remain
         // stable
         // Please also note the comment in ShiftedMediumBlock.initStartReference() where a specific workaround is
         // necessary due to this...
         if ((action.getActionType() == MediumActionType.INSERT
            && offsetToUpdate != action.getRegion().getStartOffset()) || insertingReplace) {
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

      offsets.clear();
   }

   /**
    * Returns all {@link MediumOffset}s currently maintained in this factory.
    * 
    * @return all {@link MediumOffset}s currently maintained in this factory.
    */
   public List<MediumOffset> getAllOffsets() {

      return Collections.unmodifiableList(offsets);
   }

   /**
    * Returns all {@link MediumOffset}s currently maintained in this factory that lie within the provided
    * {@link MediumRegion}.
    * 
    * @param region
    *           The {@link MediumRegion}
    * @return all {@link MediumOffset}s currently maintained in this factory that lie within the provided
    *         {@link MediumRegion}.
    */
   public List<MediumOffset> getAllOffsetsInRegion(MediumRegion region) {

      Reject.ifNull(region, "region");
      Reject.ifFalse(region.getStartOffset().getMedium().equals(getMedium()),
         "region.getStartOffset().getMedium().equals(getMedium())");

      List<MediumOffset> allOffsetsInRegion = new ArrayList<>();

      for (MediumOffset offset : offsets) {
         if (region.contains(offset)) {
            allOffsetsInRegion.add(offset);
         }
      }

      return allOffsetsInRegion;
   }

   /**
    * Returns all {@link MediumOffset}s currently maintained in this factory that lie behind or are equal to the
    * provided {@link MediumOffset}.
    * 
    * @param offset
    *           The {@link MediumOffset}, must belong to the same {@link Medium}.
    * @return all {@link MediumOffset}s currently maintained in this factory that lie behind or are equal to the
    *         provided {@link MediumOffset}.
    */
   public List<MediumOffset> getAllOffsetsBehindOrEqual(MediumOffset offset) {

      Reject.ifNull(offset, "offset");
      Reject.ifFalse(offset.getMedium().equals(getMedium()), "offset.getMedium().equals(getMedium())");

      List<MediumOffset> allOffsetsBehindOrEqual = new ArrayList<>();

      for (MediumOffset offsetBehindOrEqual : offsets) {
         if (offsetBehindOrEqual.behindOrEqual(offset)) {
            allOffsetsBehindOrEqual.add(offsetBehindOrEqual);
         }
      }

      return allOffsetsBehindOrEqual;
   }
}
