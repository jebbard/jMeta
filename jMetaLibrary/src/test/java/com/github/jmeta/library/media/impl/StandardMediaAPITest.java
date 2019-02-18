/**
 *
 * {@link StandardMediaAPITest}.java
 *
 * @author Jens Ebert
 *
 * @date 09.12.2017
 *
 */
package com.github.jmeta.library.media.impl;

import java.io.FileInputStream;
import java.io.FileNotFoundException;

import org.junit.Assert;
import org.junit.Test;

import com.github.jmeta.library.media.api.helper.TestMedia;
import com.github.jmeta.library.media.api.services.MediaAPI;
import com.github.jmeta.library.media.api.services.MediumStore;
import com.github.jmeta.library.media.api.types.AbstractMedium;
import com.github.jmeta.library.media.api.types.FileMedium;
import com.github.jmeta.library.media.api.types.InMemoryMedium;
import com.github.jmeta.library.media.api.types.InputStreamMedium;
import com.github.jmeta.utility.dbc.api.exceptions.PreconditionUnfullfilledException;

/**
 * {@link StandardMediaAPITest} tests the {@link StandardMediaAPI} class.
 */
public class StandardMediaAPITest {

   /**
    * {@link FakeMediumClass} is a fake class for testing
    * {@link MediaAPI#createMediumStore(com.github.jmeta.library.media.api.types.Medium)}
    */
   private class FakeMediumClass extends AbstractMedium<Object> {

      /**
       * @see com.github.jmeta.library.media.api.types.Medium#getCurrentLength()
       */
      @Override
      public long getCurrentLength() {
         return 0;
      }

      /**
       * Creates a new {@link FakeMediumClass}.
       *
       * @param medium
       *           The fake medium
       */
      public FakeMediumClass(Object medium) {
         super(medium, "Fake test medium", false, false, false, 0, 0);
      }
   }

   /**
    * Tests {@link MediaAPI#createMediumStore(com.github.jmeta.library.media.api.types.Medium)}.
    */
   @Test
   public void createMediumStore_forFileMedium_returnsProperStoreInstance() {
      MediaAPI mediaAPI = new StandardMediaAPI();

      FileMedium mediumDefinition = new FileMedium(TestMedia.EMPTY_TEST_FILE_PATH, false,
         MediumStore.MIN_CACHE_SIZE_IN_BYTES, 500);

      MediumStore store = mediaAPI.createMediumStore(mediumDefinition);

      Assert.assertNotNull(store);
      Assert.assertEquals(mediumDefinition, store.getMedium());
   }

   /**
    * Tests {@link MediaAPI#createMediumStore(com.github.jmeta.library.media.api.types.Medium)}.
    */
   @Test
   public void createMediumStore_forInMemoryMedium_returnsProperStoreInstance() {
      MediaAPI mediaAPI = new StandardMediaAPI();

      InMemoryMedium mediumDefinition = new InMemoryMedium(new byte[] { 100 }, "My Medium", false,
         MediumStore.MIN_CACHE_SIZE_IN_BYTES, 500);

      MediumStore store = mediaAPI.createMediumStore(mediumDefinition);

      Assert.assertNotNull(store);
      Assert.assertEquals(mediumDefinition, store.getMedium());
   }

   /**
    * Tests {@link MediaAPI#createMediumStore(com.github.jmeta.library.media.api.types.Medium)}.
    */
   @Test
   public void createMediumStore_forInputStreamMedium_returnsProperStoreInstance() {
      MediaAPI mediaAPI = new StandardMediaAPI();

      InputStreamMedium mediumDefinition;
      try {
         mediumDefinition = new InputStreamMedium(new FileInputStream(TestMedia.EMPTY_TEST_FILE_PATH.toFile()),
            "My Medium", MediumStore.MIN_CACHE_SIZE_IN_BYTES, 500);
      } catch (FileNotFoundException e) {
         throw new RuntimeException("Unexpected exception", e);
      }

      MediumStore store = mediaAPI.createMediumStore(mediumDefinition);

      Assert.assertNotNull(store);
      Assert.assertEquals(mediumDefinition, store.getMedium());
   }

   /**
    * Tests {@link MediaAPI#createMediumStore(com.github.jmeta.library.media.api.types.Medium)}.
    */
   @Test(expected = PreconditionUnfullfilledException.class)
   public void createMediumStore_forUnsupportedMediumType_throwsException() {
      MediaAPI mediaAPI = new StandardMediaAPI();

      mediaAPI.createMediumStore(new FakeMediumClass("test"));
   }
}
