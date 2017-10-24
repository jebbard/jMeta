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

import com.github.jmeta.library.media.api.helper.DummyMediumCreator;
import com.github.jmeta.library.media.api.helper.MediaTestCaseConstants;
import com.github.jmeta.utility.equalstest.api.services.AbstractEqualsTest;

/**
 * {@link MediumEqualityTest} tests the {@link Medium} interface and its implementations for {@link #equals(Object)}
 * and {@link #hashCode()}.
 */
public class MediumEqualityTest extends AbstractEqualsTest<Medium<?>> {

   private static final byte[] BYTE_ARRAY_4 = new byte[] { 1, 2, 3 };

   private static final byte[] BYTE_ARRAY_3 = new byte[] { 1 };

   private static final byte[] BYTE_ARRAY_2 = new byte[] {};

   private static final byte[] BYTE_ARRAY_1 = new byte[] { 5, -5, 10 };

   private static final ByteArrayInputStream CENTRAL_BYTE_ARRAY_INPUT_STREAM = new ByteArrayInputStream(BYTE_ARRAY_1);

   private static final BufferedInputStream CENTRAL_BUFFERED_INPUT_STREAM = new BufferedInputStream(
      CENTRAL_BYTE_ARRAY_INPUT_STREAM);

   /**
    * Creates the media for equality testing.
    */
   private List<Medium<?>> createMedia() {

      List<Medium<?>> media = new ArrayList<>();

      media.add(DummyMediumCreator.createDummyInMemoryMedium(BYTE_ARRAY_4, "Hallo4", true));
      media.add(DummyMediumCreator.createDummyInMemoryMedium(BYTE_ARRAY_3, "Hallo3", false));
      media.add(DummyMediumCreator.createDummyInMemoryMedium(BYTE_ARRAY_2, "Hallo2", true));
      media.add(DummyMediumCreator.createDummyInMemoryMedium(BYTE_ARRAY_1, "Hallo1", false));
      media.add(new InputStreamMedium(CENTRAL_BYTE_ARRAY_INPUT_STREAM, "Hallo"));
      media.add(new InputStreamMedium(CENTRAL_BUFFERED_INPUT_STREAM, "Hallo"));
      media.add(DummyMediumCreator.createDummyFileMedium(MediaTestCaseConstants.STANDARD_TEST_FILE, false));

      return media;
   }

   /**
    * Creates the media equal to what {@link #createMedia()} returns for equality testing.
    */
   private List<Medium<?>> createEqualMedia() {

      List<Medium<?>> media = new ArrayList<>();

      // We must actually pass the IDENTICAL byte arrays and streams, because
      // they are compared bases on object identity
      media.add(DummyMediumCreator.createDummyInMemoryMedium(BYTE_ARRAY_4, "different name", true));
      media.add(DummyMediumCreator.createDummyInMemoryMedium(BYTE_ARRAY_3, "Hallo3", true));
      media.add(DummyMediumCreator.createDummyInMemoryMedium(BYTE_ARRAY_2, "Hallo2", false));
      media.add(DummyMediumCreator.createDummyInMemoryMedium(BYTE_ARRAY_1, "Hallo1", false));
      media.add(new InputStreamMedium(CENTRAL_BYTE_ARRAY_INPUT_STREAM, "Tschau"));
      media.add(new InputStreamMedium(CENTRAL_BUFFERED_INPUT_STREAM, "bye"));
      // For file media, equality is simply that the internal file object points
      // to the same file
      media.add(DummyMediumCreator.createDummyFileMedium(MediaTestCaseConstants.STANDARD_TEST_FILE, false));

      return media;
   }

   /**
    * Creates the media different to what {@link #createMedia()} returns for equality testing.
    */
   private List<Medium<?>> createDifferentMedia() {

      List<Medium<?>> media = new ArrayList<>();

      // Note that the order of media (byte arrays etc.) has changed
      // corresponding to the createMedia()
      // method
      media.add(DummyMediumCreator.createDummyInMemoryMedium(BYTE_ARRAY_3, "different name", true));
      media.add(DummyMediumCreator.createDummyInMemoryMedium(BYTE_ARRAY_4, "Hallo3", false));
      media.add(DummyMediumCreator.createDummyInMemoryMedium(BYTE_ARRAY_1, "Hallo2", true));
      media.add(DummyMediumCreator.createDummyInMemoryMedium(BYTE_ARRAY_2, "Hallo1", false));
      media.add(new InputStreamMedium(CENTRAL_BUFFERED_INPUT_STREAM, "bye"));
      media.add(new InputStreamMedium(CENTRAL_BYTE_ARRAY_INPUT_STREAM, "Tschau"));
      media.add(DummyMediumCreator.createDummyFileMedium(MediaTestCaseConstants.SECOND_TEST_FILE, false));

      return media;
   }

   /**
    * @see de.je.util.javautil.testUtil.equa.AbstractEqualsTest#getObjects()
    */
   @Override
   protected List<Medium<?>> getObjects() {

      return createMedia();
   }

   /**
    * @see de.je.util.javautil.testUtil.equa.AbstractEqualsTest#getEqualObjects()
    */
   @Override
   protected List<Medium<?>> getEqualObjects() {

      return createEqualMedia();
   }

   /**
    * @see de.je.util.javautil.testUtil.equa.AbstractEqualsTest#getDifferentObjects()
    */
   @Override
   protected List<Medium<?>> getDifferentObjects() {

      return createDifferentMedia();
   }

   /**
    * @see de.je.util.javautil.testUtil.equa.AbstractEqualsTest#getThirdEqualObjects()
    */
   @Override
   protected List<Medium<?>> getThirdEqualObjects() {

      return createMedia();
   }
}