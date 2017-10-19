/**
 *
 * {@link IMediumEqualityTest}.java
 *
 * @author Jens Ebert
 *
 * @date 10.04.2011
 */
package com.github.jmeta.library.media.api.type;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import com.github.jmeta.library.media.api.helper.DummyMediumCreator;
import com.github.jmeta.library.media.api.type.IMedium;
import com.github.jmeta.library.media.api.type.InputStreamMedium;

import de.je.util.javautil.testUtil.equa.AbstractEqualsTest;

/**
 * {@link IMediumEqualityTest} tests the {@link IMedium} interface and its implementations for {@link #equals(Object)}
 * and {@link #hashCode()}.
 */
public class IMediumEqualityTest extends AbstractEqualsTest<IMedium<?>> {

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
   private List<IMedium<?>> createMedia() {

      List<IMedium<?>> media = new ArrayList<>();

      media.add(DummyMediumCreator.createDummyInMemoryMedium(BYTE_ARRAY_4, "Hallo4", true));
      media.add(DummyMediumCreator.createDummyInMemoryMedium(BYTE_ARRAY_3, "Hallo3", false));
      media.add(DummyMediumCreator.createDummyInMemoryMedium(BYTE_ARRAY_2, "Hallo2", true));
      media.add(DummyMediumCreator.createDummyInMemoryMedium(BYTE_ARRAY_1, "Hallo1", false));
      media.add(new InputStreamMedium(CENTRAL_BYTE_ARRAY_INPUT_STREAM, "Hallo"));
      media.add(new InputStreamMedium(CENTRAL_BUFFERED_INPUT_STREAM, "Hallo"));
      media.add(DummyMediumCreator.createDefaultDummyFileMedium());

      return media;
   }

   /**
    * Creates the media equal to what {@link #createMedia()} returns for equality testing.
    */
   private List<IMedium<?>> createEqualMedia() {

      List<IMedium<?>> media = new ArrayList<>();

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
      media.add(DummyMediumCreator.createDummyFileMedium(Paths.get("."), true));

      return media;
   }

   /**
    * Creates the media different to what {@link #createMedia()} returns for equality testing.
    */
   private List<IMedium<?>> createDifferentMedia() {

      List<IMedium<?>> media = new ArrayList<>();

      // Note that the order of media (byte arrays etc.) has changed
      // corresponding to the createMedia()
      // method
      media.add(DummyMediumCreator.createDummyInMemoryMedium(BYTE_ARRAY_3, "different name", true));
      media.add(DummyMediumCreator.createDummyInMemoryMedium(BYTE_ARRAY_4, "Hallo3", false));
      media.add(DummyMediumCreator.createDummyInMemoryMedium(BYTE_ARRAY_1, "Hallo2", true));
      media.add(DummyMediumCreator.createDummyInMemoryMedium(BYTE_ARRAY_2, "Hallo1", false));
      media.add(new InputStreamMedium(CENTRAL_BUFFERED_INPUT_STREAM, "bye"));
      media.add(new InputStreamMedium(CENTRAL_BYTE_ARRAY_INPUT_STREAM, "Tschau"));
      media.add(DummyMediumCreator.createDummyFileMedium(Paths.get("../test"), false));

      return media;
   }

   /**
    * @see de.je.util.javautil.testUtil.equa.AbstractEqualsTest#getObjects()
    */
   @Override
   protected List<IMedium<?>> getObjects() {

      return createMedia();
   }

   /**
    * @see de.je.util.javautil.testUtil.equa.AbstractEqualsTest#getEqualObjects()
    */
   @Override
   protected List<IMedium<?>> getEqualObjects() {

      return createEqualMedia();
   }

   /**
    * @see de.je.util.javautil.testUtil.equa.AbstractEqualsTest#getDifferentObjects()
    */
   @Override
   protected List<IMedium<?>> getDifferentObjects() {

      return createDifferentMedia();
   }

   /**
    * @see de.je.util.javautil.testUtil.equa.AbstractEqualsTest#getThirdEqualObjects()
    */
   @Override
   protected List<IMedium<?>> getThirdEqualObjects() {

      return createMedia();
   }
}