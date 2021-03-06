/**
 *
 * {@link MediumEqualityTest}.java
 *
 * @author Jens Ebert
 *
 * @date 10.04.2011
 */
package com.github.jmeta.library.media.api.types;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;

import com.github.jmeta.library.media.api.helper.TestMedia;
import com.github.jmeta.utility.equalstest.api.services.AbstractEqualsTest;

/**
 * {@link MediumEqualityTest} tests the {@link Medium} interface and its implementations for {@link #equals(Object)} and
 * {@link #hashCode()}.
 */
public class MediumEqualityTest extends AbstractEqualsTest<Medium<?>> {

   private static final byte[] BYTE_ARRAY_4 = new byte[] { 1, 2, 3 };

   private static final byte[] BYTE_ARRAY_3 = new byte[] { 1 };

   private static final byte[] BYTE_ARRAY_2 = new byte[] {};

   private static final byte[] BYTE_ARRAY_1 = new byte[] { 5, -5, 10 };

   private static final ByteArrayInputStream CENTRAL_BYTE_ARRAY_INPUT_STREAM = new ByteArrayInputStream(
      MediumEqualityTest.BYTE_ARRAY_1);

   private static final BufferedInputStream CENTRAL_BUFFERED_INPUT_STREAM = new BufferedInputStream(
      MediumEqualityTest.CENTRAL_BYTE_ARRAY_INPUT_STREAM);

   /**
    * Creates the media different to what {@link #createMedia()} returns for equality testing.
    * 
    * @return The differing media
    */
   private List<Medium<?>> createDifferentMedia() {

      List<Medium<?>> media = new ArrayList<>();

      // Note that the order of media (byte arrays etc.) has changed
      // corresponding to the createMedia()
      // method
      media.add(new InMemoryMedium(MediumEqualityTest.BYTE_ARRAY_3, "different name", MediumAccessType.READ_ONLY));
      media.add(new InMemoryMedium(MediumEqualityTest.BYTE_ARRAY_4, "Hallo3", MediumAccessType.READ_WRITE));
      media.add(new InMemoryMedium(MediumEqualityTest.BYTE_ARRAY_1, "Hallo2", MediumAccessType.READ_ONLY));
      media.add(new InMemoryMedium(MediumEqualityTest.BYTE_ARRAY_2, "Hallo1", MediumAccessType.READ_WRITE));
      media.add(new InputStreamMedium(MediumEqualityTest.CENTRAL_BUFFERED_INPUT_STREAM, "bye"));
      media.add(new InputStreamMedium(MediumEqualityTest.CENTRAL_BYTE_ARRAY_INPUT_STREAM, "Tschau"));
      media.add(new FileMedium(TestMedia.SECOND_TEST_FILE_PATH, MediumAccessType.READ_WRITE));

      return media;
   }

   /**
    * Creates the media equal to what {@link #createMedia()} returns for equality testing.
    * 
    * @return the equal media
    */
   private List<Medium<?>> createEqualMedia() {

      List<Medium<?>> media = new ArrayList<>();

      // We must actually pass the IDENTICAL byte arrays and streams, because
      // they are compared bases on object identity
      media.add(new InMemoryMedium(MediumEqualityTest.BYTE_ARRAY_4, "different name", MediumAccessType.READ_ONLY));
      media.add(new InMemoryMedium(MediumEqualityTest.BYTE_ARRAY_3, "Hallo3", MediumAccessType.READ_ONLY));
      media.add(new InMemoryMedium(MediumEqualityTest.BYTE_ARRAY_2, "Hallo2", MediumAccessType.READ_WRITE));
      media.add(new InMemoryMedium(MediumEqualityTest.BYTE_ARRAY_1, "Hallo1", MediumAccessType.READ_WRITE));
      media.add(new InputStreamMedium(MediumEqualityTest.CENTRAL_BYTE_ARRAY_INPUT_STREAM, "Tschau"));
      media.add(new InputStreamMedium(MediumEqualityTest.CENTRAL_BUFFERED_INPUT_STREAM, "bye"));
      // For file media, equality is simply that the internal file object points
      // to the same file
      media.add(new FileMedium(TestMedia.FIRST_TEST_FILE_PATH, MediumAccessType.READ_WRITE));

      return media;
   }

   /**
    * Creates the media for equality testing.
    * 
    * @return The media
    */
   private List<Medium<?>> createMedia() {

      List<Medium<?>> media = new ArrayList<>();

      media.add(new InMemoryMedium(MediumEqualityTest.BYTE_ARRAY_4, "Hallo4", MediumAccessType.READ_ONLY));
      media.add(new InMemoryMedium(MediumEqualityTest.BYTE_ARRAY_3, "Hallo3", MediumAccessType.READ_WRITE));
      media.add(new InMemoryMedium(MediumEqualityTest.BYTE_ARRAY_2, "Hallo2", MediumAccessType.READ_ONLY));
      media.add(new InMemoryMedium(MediumEqualityTest.BYTE_ARRAY_1, "Hallo1", MediumAccessType.READ_WRITE));
      media.add(new InputStreamMedium(MediumEqualityTest.CENTRAL_BYTE_ARRAY_INPUT_STREAM, "Hallo"));
      media.add(new InputStreamMedium(MediumEqualityTest.CENTRAL_BUFFERED_INPUT_STREAM, "Hallo"));
      media.add(new FileMedium(TestMedia.FIRST_TEST_FILE_PATH, MediumAccessType.READ_WRITE));

      return media;
   }

   /**
    * @see com.github.jmeta.utility.equalstest.api.services.AbstractEqualsTest#getDifferentObjects()
    */
   @Override
   protected List<Medium<?>> getDifferentObjects() {

      return createDifferentMedia();
   }

   /**
    * @see com.github.jmeta.utility.equalstest.api.services.AbstractEqualsTest#getEqualObjects()
    */
   @Override
   protected List<Medium<?>> getEqualObjects() {

      return createEqualMedia();
   }

   /**
    * @see com.github.jmeta.utility.equalstest.api.services.AbstractEqualsTest#getObjects()
    */
   @Override
   protected List<Medium<?>> getObjects() {

      return createMedia();
   }

   /**
    * @see com.github.jmeta.utility.equalstest.api.services.AbstractEqualsTest#getThirdEqualObjects()
    */
   @Override
   protected List<Medium<?>> getThirdEqualObjects() {

      return createMedia();
   }
}