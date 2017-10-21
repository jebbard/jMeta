/**
 *
 * MediumChangeManagerTestData.java
 *
 * @author Jens
 *
 * @date 24.05.2016
 *
 */
package com.github.jmeta.library.media.impl.changeManager;

import java.nio.ByteBuffer;

import com.github.jmeta.library.media.api.types.MediumAction;
import com.github.jmeta.library.media.api.types.MediumActionType;
import com.github.jmeta.library.media.api.types.MediumRegion;

/**
 * {@link MediumChangeManagerTestData} represents both test input data as well as test expectations for checking
 * {@link MediumChangeManager}s schedule methods. Every {@link MediumChangeManagerTestData} instance is associated with
 * one newly created {@link MediumAction} instance.
 */
public class MediumChangeManagerTestData {

   private final ByteBuffer actionBytesToUse;

   private final int expectedSequenceNumber;

   private final MediumRegion regionToUse;

   private final MediumActionType typeToUse;

   /**
    * Creates a new {@link MediumChangeManagerTestData}.
    * 
    * @param testling
    *           The {@link MediumChangeManager} to test.
    * @param typeToUse
    *           Represents the {@link MediumActionType} which is used to determine which schedule method to call from
    *           {@link MediumChangeManager}. Furthermore, it represents the {@link MediumActionType} expected for the
    *           new {@link MediumAction} returned by the checked schedule method.
    * @param regionToUse
    *           Represents the {@link MediumRegion} which is used when calling the schedule method of
    *           {@link MediumChangeManager}. Furthermore, it represents the {@link MediumRegion} expected for the new
    *           {@link MediumAction} returned by the checked schedule method.
    * @param actionBytesToUse
    *           Represents the action bytes which are used when calling the schedule method of
    *           {@link MediumChangeManager}. Furthermore, it represents the action bytes expected for the new
    *           {@link MediumAction} returned by the checked schedule method.
    * @param expectedSequenceNumber
    *           The sequence number expected for the new {@link MediumAction} returned by the checked schedule method.
    */
   public MediumChangeManagerTestData(MediumChangeManager testling, MediumActionType typeToUse,
      MediumRegion regionToUse, ByteBuffer actionBytesToUse, int expectedSequenceNumber) {
      this.expectedSequenceNumber = expectedSequenceNumber;
      this.actionBytesToUse = actionBytesToUse;
      this.regionToUse = regionToUse;
      this.typeToUse = typeToUse;
   }

   /**
    * Represents the {@link MediumActionType} which is used to determine which schedule method to call from
    * {@link MediumChangeManager}. Furthermore, it represents the {@link MediumActionType} expected for the new
    * {@link MediumAction} returned by the checked schedule method.
    * 
    * @return see method description
    */
   public MediumActionType getTypeToUse() {

      return typeToUse;
   }

   /**
    * Represents the {@link MediumRegion} which is used when calling the schedule method of {@link MediumChangeManager}.
    * Furthermore, it represents the {@link MediumRegion} expected for the new {@link MediumAction} returned by the
    * checked schedule method.
    * 
    * @return see method description
    */
   public MediumRegion getRegionToUse() {

      return regionToUse;
   }

   /**
    * Represents the action bytes which are used when calling the schedule method of {@link MediumChangeManager}.
    * Furthermore, it represents the action bytes expected for the new {@link MediumAction} returned by the checked
    * schedule method.
    * 
    * @return see method description
    */
   public ByteBuffer getActionBytesToUse() {
      return actionBytesToUse;
   }

   /**
    * Returns the sequence number expected for the new {@link MediumAction} returned by the checked schedule method.
    * 
    * @return The sequence number expected for the new {@link MediumAction} returned by the checked schedule method.
    */
   public int getExpectedSequenceNumber() {

      return expectedSequenceNumber;
   }

   /**
    * @see java.lang.Object#toString()
    */
   @Override
   public String toString() {
      return "MediumChangeManagerTestData [typeToUse=" + typeToUse + ", regionToUse=" + regionToUse
         + ", actionBytesToUse=" + actionBytesToUse + ", expectedSequenceNumber=" + expectedSequenceNumber + "]";
   }
}
