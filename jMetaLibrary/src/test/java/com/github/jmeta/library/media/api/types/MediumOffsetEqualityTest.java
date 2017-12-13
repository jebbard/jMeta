/**
 *
 * {@link StandardMediumOffsetTest}.java
 *
 * @author Jens Ebert
 *
 * @date 10.04.2011
 */
package com.github.jmeta.library.media.api.types;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;

import com.github.jmeta.library.media.api.helper.TestMedia;
import com.github.jmeta.library.media.impl.offset.StandardMediumOffset;
import com.github.jmeta.library.media.impl.offset.StandardMediumOffsetTest;
import com.github.jmeta.utility.equalstest.api.services.AbstractEqualsTest;

/**
 * {@link MediumOffsetEqualityTest} tests the {@link StandardMediumOffset} class (and its interface {@link MediumOffset}
 * for its implementation of {@link #equals(Object)} and {@link #hashCode()}.
 */
public class MediumOffsetEqualityTest extends AbstractEqualsTest<MediumOffset> {

   private static final byte[] BYTES = new byte[] { 1, 2, 3 };

   private static final ByteArrayInputStream STREAM_MEDIUM = new ByteArrayInputStream(BYTES);

   /**
    * @see AbstractEqualsTest#getObjects()
    */
   @Override
   protected List<MediumOffset> getObjects() {

      return createMediumOffsets(createMedia(), 0);
   }

   /**
    * @see AbstractEqualsTest#getEqualObjects()
    */
   @Override
   protected List<MediumOffset> getEqualObjects() {

      return createMediumOffsets(createMedia(), 0);
   }

   /**
    * @see AbstractEqualsTest#getDifferentObjects()
    */
   @Override
   protected List<MediumOffset> getDifferentObjects() {

      return createMediumOffsets(createDifferentMedia(), 88);
   }

   /**
    * @see AbstractEqualsTest#getThirdEqualObjects()
    */
   @Override
   protected List<MediumOffset> getThirdEqualObjects() {

      return createMediumOffsets(createMedia(), 0);
   }

   /**
    * Creates a {@link List} of {@link MediumOffset}s for the given {@link List} of {@link Medium} instances.
    * 
    * @param media
    *           The media to use
    * @param baseOffset
    *           The base offset of the absolute medium offsets.
    * 
    * @return a {@link List} of {@link MediumOffset}s for the given {@link List} of {@link Medium} instances.
    */
   private List<MediumOffset> createMediumOffsets(List<Medium<?>> media, long baseOffset) {

      List<MediumOffset> mediumReferences = new ArrayList<>();

      // Create the references different due to different media, other properties are the same
      for (int i = 0; i < media.size(); ++i) {
         Medium<?> medium = media.get(i);

         mediumReferences.add(new StandardMediumOffset(medium, i * 10));
      }

      // Create some more references with equal medium, using the passed offset
      for (int i = 0; i < 3; ++i) {
         mediumReferences
            .add(new StandardMediumOffset(new FileMedium(TestMedia.FIRST_TEST_FILE_PATH, false), baseOffset + i * 10));
      }

      return mediumReferences;
   }

   /**
    * Creates the media for equality testing.
    * 
    * @return the equal media
    */
   private List<Medium<?>> createMedia() {

      List<Medium<?>> media = new ArrayList<>();

      // We must actually pass the IDENTICAL byte arrays and streams, because
      // they are compared bases on object identity
      media.add(new InMemoryMedium(BYTES, "Hallo4", true));
      media.add(new InputStreamMedium(STREAM_MEDIUM, "Hallo"));
      media.add(new FileMedium(TestMedia.FIRST_TEST_FILE_PATH, false));

      return media;
   }

   /**
    * Creates the media different to what {@link #createMedia()} returns for equality testing.
    * 
    * @return The different media
    */
   private List<Medium<?>> createDifferentMedia() {

      List<Medium<?>> media = new ArrayList<>();

      media.add(new InMemoryMedium(new byte[] { 1 }, "Hallo4", true));
      // We must create a new input stream here to achieve it is different
      media.add(new InputStreamMedium(new ByteArrayInputStream(BYTES), "Hallo"));
      media.add(new FileMedium(TestMedia.SECOND_TEST_FILE_PATH, false));

      return media;
   }
}