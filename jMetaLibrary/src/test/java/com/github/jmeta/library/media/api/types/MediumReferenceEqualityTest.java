/**
 *
 * {@link StandardMediumReferenceTest}.java
 *
 * @author Jens Ebert
 *
 * @date 10.04.2011
 */
package com.github.jmeta.library.media.api.types;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;

import com.github.jmeta.library.media.api.helper.DummyMediumCreator;
import com.github.jmeta.library.media.api.helper.MediaTestCaseConstants;
import com.github.jmeta.library.media.impl.reference.StandardMediumReference;
import com.github.jmeta.utility.equalstest.api.services.AbstractEqualsTest;

/**
 * {@link MediumReferenceEqualityTest} tests the {@link StandardMediumReference} class (and its interface
 * {@link MediumReference} for its implementation of {@link #equals(Object)} and {@link #hashCode()}.
 */
public class MediumReferenceEqualityTest extends AbstractEqualsTest<MediumReference> {

   private static final byte[] BYTES = new byte[] { 1, 2, 3 };

   private static final ByteArrayInputStream STREAM_MEDIUM = new ByteArrayInputStream(BYTES);

   /**
    * @see AbstractEqualsTest#getObjects()
    */
   @Override
   protected List<MediumReference> getObjects() {

      return createMediumReferences(createMedia(), 0);
   }

   /**
    * @see AbstractEqualsTest#getEqualObjects()
    */
   @Override
   protected List<MediumReference> getEqualObjects() {

      return createMediumReferences(createMedia(), 0);
   }

   /**
    * @see AbstractEqualsTest#getDifferentObjects()
    */
   @Override
   protected List<MediumReference> getDifferentObjects() {

      return createMediumReferences(createDifferentMedia(), 88);
   }

   /**
    * @see AbstractEqualsTest#getThirdEqualObjects()
    */
   @Override
   protected List<MediumReference> getThirdEqualObjects() {

      return createMediumReferences(createMedia(), 0);
   }

   /**
    * Creates a {@link List} of {@link MediumReference}s for the given {@link List} of {@link Medium} instances.
    * 
    * @param baseOffset
    *           The base offset of the absolute medium offsets.
    * 
    * @return a {@link List} of {@link MediumReference}s for the given {@link List} of {@link Medium} instances.
    */
   private List<MediumReference> createMediumReferences(List<Medium<?>> media, long baseOffset) {

      List<MediumReference> mediumReferences = new ArrayList<>();

      // Create the references different due to different media, other properties are the same
      for (int i = 0; i < media.size(); ++i) {
         Medium<?> medium = media.get(i);

         mediumReferences.add(new StandardMediumReference(medium, i * 10));
      }

      // Create some more references with equal medium, using the passed offset
      for (int i = 0; i < 3; ++i) {
         mediumReferences.add(new StandardMediumReference(
            DummyMediumCreator.createDummyFileMedium(MediaTestCaseConstants.STANDARD_TEST_FILE, false),
            baseOffset + i * 10));
      }

      return mediumReferences;
   }

   /**
    * Creates the media for equality testing.
    */
   private List<Medium<?>> createMedia() {

      List<Medium<?>> media = new ArrayList<>();

      // We must actually pass the IDENTICAL byte arrays and streams, because
      // they are compared bases on object identity
      media.add(DummyMediumCreator.createDummyInMemoryMedium(BYTES, "Hallo4", true));
      media.add(new InputStreamMedium(STREAM_MEDIUM, "Hallo"));
      media.add(DummyMediumCreator.createDummyFileMedium(MediaTestCaseConstants.STANDARD_TEST_FILE, false));

      return media;
   }

   /**
    * Creates the media different to what {@link #createMedia()} returns for equality testing.
    */
   private List<Medium<?>> createDifferentMedia() {

      List<Medium<?>> media = new ArrayList<>();

      media.add(DummyMediumCreator.createDummyInMemoryMedium(new byte[] { 1 }, "Hallo4", true));
      // We must create a new input stream here to achieve it is different
      media.add(new InputStreamMedium(new ByteArrayInputStream(BYTES), "Hallo"));
      media.add(DummyMediumCreator.createDummyFileMedium(MediaTestCaseConstants.SECOND_TEST_FILE, false));

      return media;
   }
}