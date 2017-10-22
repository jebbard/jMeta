/**
 *
 * {@link FileMediumCacheTest}.java
 *
 * @author Jens Ebert
 *
 * @date 10.04.2011
 */
package com.github.jmeta.library.media.impl.OLD;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import com.github.jmeta.library.media.api.helper.MediaTestCaseConstants;
import com.github.jmeta.library.media.api.types.FileMedium;
import com.github.jmeta.library.media.api.types.IMedium;
import com.github.jmeta.library.media.api.types.IMediumReference;
import com.github.jmeta.library.media.impl.mediumAccessor.FileMediumAccessor;
import com.github.jmeta.library.media.impl.reference.StandardMediumReference;

/**
 * {@link FileMediumCacheTest} tests the {@link StandardMediumCache} implementation with a {@link FileMediumAccessor}.
 */
public class FileMediumCacheTest extends IMediumCacheTest {

   private static final int MEDIUM_LENGTH_IN_BYTES = 852;

   private static final int MAXIMUM_CACHE_BLOCK_SIZE = Integer.MAX_VALUE;

   private IMediumCache m_theCache = null;

   private FileMedium m_theMedium = null;

   private LinkedHashMap<IMediumReference, Integer> m_theTestCacheSizes;

   private LinkedHashMap<IMediumReference, Integer> m_cacheDistancesToEOM;

   private Map<IMediumReference, Integer> m_overlappingRegions;

   private Map<IMediumReference, byte[]> m_expectedData;

   /**
    * @see IMediumCacheTest#getExpectedData()
    */
   @Override
   protected Map<IMediumReference, byte[]> getExpectedData() {

      if (m_expectedData == null) {
         m_expectedData = new HashMap<>();

         IMedium<?> medium = getExpectedMedium();

         m_expectedData.put(new StandardMediumReference(medium, 0),
            new byte[] { '0', '0', '0', 'S', 't', 'a', 'l', 'k', 'e', 'r' });
         m_expectedData.put(new StandardMediumReference(medium, 15),
            new byte[] { 'B', 'i', 'o', 's', 'h', 'o', 'c', 'k', '\r', '\n',
               '0', '2', '8', 'T', 'h', 'e', ' ', 'W', 'i', 't', 'c', 'h', 'e',
               'r', '\r', '\n', '0', '4', '4', 'D', 'i', 'a', 'b', 'l', 'o',
               '\r', '\n', '0', '5', '5', 'D', 'i', 'a', 'b', 'l', 'o', ' ',
               'H', 'e', 'l' });
         m_expectedData.put(new StandardMediumReference(medium, 17),
            new byte[] { 'o', 's', 'h', 'o', 'c' });
         m_expectedData.put(new StandardMediumReference(medium, 65),
            new byte[] { 'l', 'f', 'i', 'r', 'e', '\r', '\n', '0', '7', '5',
               'D', 'i', 'a', 'b', 'l', 'o', ' ' });
         m_expectedData.put(new StandardMediumReference(medium, 70),
            new byte[] { '\r', '\n', '0', '7', '5', 'D', 'i', 'a', 'b', 'l',
               'o', ' ', '2', '\r', '\n', '0', '8' });
         m_expectedData.put(new StandardMediumReference(medium, 100),
            new byte[] { 'o', 'n', ' ', 'd', 'e', 's', ' ', 'B', 'a', 'a', 'l',
               '\r', '\n', '1', '1', '6', 'W', 'a', 'r', 'c' });
         m_expectedData.put(new StandardMediumReference(medium, 121),
            new byte[] { 'a' });
         m_expectedData.put(new StandardMediumReference(medium, 122),
            new byte[] { 'f', 't', ' ', '3', '\r', '\n', '1', '3', '1', 'W',
               'a', 'r', 'c', 'r', 'a', 'f', 't', ' ', '3', ' ', 'F', 'r', 'o',
               'z', 'e', 'n', ' ', 'T', 'h', 'r', 'o', 'n', 'e', '\r', '\n',
               '1', '4', '7', 'S', 't', 'a', 'r', 'c', 'r', 'a', 'f', 't', '\r',
               '\n', '1', '6', '4', 'S', 't', 'a', 'r', 'c', 'r', 'a', 'f', 't',
               ' ', 'B', 'r', 'o', 'o', 'd', ' ', 'W', 'a', 'r', '\r', '\n',
               '1', '8', '8', 'B', 'l', 'a', 'c', 'k', ' ', 'M', 'i', 'r', 'r',
               'o', 'r', '\r' });
         m_expectedData.put(new StandardMediumReference(medium, 250),
            new byte[] { 'l', 'a', 'n', 'd', ' ', '2', '\r', '\n', '2', '5',
               '1', 'I', 'n', 'd', 'i', 'a', 'n', 'a', ' ', 'J', 'o', 'n', 'e',
               's', ' ', '3', '\r', '\n', '2', '7', '1', 'I', 'n', 'd', 'i',
               'a', 'n', 'a', ' ', 'J', 'o', 'n', 'e', 's', ' ', 'F', 'a', 't',
               'e', ' ', 'o', 'f', ' ', 'A', 't', 'l', 'a', 'n', 't', 'i', 's',
               '\r', '\n', '3', '0', '6', 'E', 'a', 'r', 't', 'h', ' ', '2',
               '1', '4', '0', '\r', '\n', '3', '2', '1', 'S', 'i', 'l', 'v',
               'e', 'r', '\r', '\n', '3', '3', '2', 'G', 'o', 't', 'h', 'i',
               'c', '\r', '\n' });

      }

      return m_expectedData;
   }

   /**
    * @see IMediumCacheTest#getExpectedDataBoundaryCases()
    */
   @Override
   protected byte[] getExpectedDataBoundaryCases() {

      return new byte[] { '0', '0', '0', 'S', 't', 'a', 'l', 'k', 'e', 'r',
         '\r', '\n', '0', '1', '5', 'B', 'i', 'o', 's', 'h', 'o', 'c', 'k',
         '\r', '\n', '0', '2', '8', 'T', 'h', 'e', ' ', 'W', 'i', 't', 'c', 'h',
         'e', 'r', '\r', '\n', '0', '4', '4', 'D', 'i', 'a', 'b', 'l', 'o',
         '\r', '\n', '0', '5', '5', 'D', 'i', 'a', 'b', 'l', 'o', ' ', 'H', 'e',
         'l' };
   }

   /**
    * @see IMediumCacheTest#getExpectedMedium()
    */
   @Override
   protected FileMedium getExpectedMedium() {

      if (m_theMedium == null)
         m_theMedium = new FileMedium(MediaTestCaseConstants.STANDARD_TEST_FILE,
            true);
      return m_theMedium;
   }

   /**
    * @see IMediumCacheTest#getExpectedRandomAccess()
    */
   @Override
   protected boolean getExpectedRandomAccess() {

      return true;
   }

   /**
    * @see IMediumCacheTest#getMaximumCacheSize()
    */
   @Override
   protected int getMaximumCacheSize() {

      return MAXIMUM_CACHE_BLOCK_SIZE;
   }

   /**
    * @see IMediumCacheTest#getMediumReferencesAndDistToEOM()
    */
   @Override
   protected Map<IMediumReference, Integer> getMediumReferencesAndDistToEOM() {

      if (m_cacheDistancesToEOM == null) {
         m_cacheDistancesToEOM = new LinkedHashMap<>();

         IMedium<?> medium = getExpectedMedium();

         // Must be put in order into this map!
         m_cacheDistancesToEOM.put(new StandardMediumReference(medium, 0),
            MEDIUM_LENGTH_IN_BYTES);
         m_cacheDistancesToEOM.put(new StandardMediumReference(medium, 15),
            MEDIUM_LENGTH_IN_BYTES - 15);
         m_cacheDistancesToEOM.put(new StandardMediumReference(medium, 17),
            MEDIUM_LENGTH_IN_BYTES - 17);
         m_cacheDistancesToEOM.put(new StandardMediumReference(medium, 65),
            MEDIUM_LENGTH_IN_BYTES - 65);
         m_cacheDistancesToEOM.put(new StandardMediumReference(medium, 70),
            MEDIUM_LENGTH_IN_BYTES - 70);
         m_cacheDistancesToEOM.put(new StandardMediumReference(medium, 100),
            MEDIUM_LENGTH_IN_BYTES - 100);
         m_cacheDistancesToEOM.put(new StandardMediumReference(medium, 121),
            MEDIUM_LENGTH_IN_BYTES - 121);
         m_cacheDistancesToEOM.put(new StandardMediumReference(medium, 122),
            MEDIUM_LENGTH_IN_BYTES - 122);
         m_cacheDistancesToEOM.put(new StandardMediumReference(medium, 250),
            MEDIUM_LENGTH_IN_BYTES - 250);
         // One byte before EOM
         m_cacheDistancesToEOM.put(
            new StandardMediumReference(medium, MEDIUM_LENGTH_IN_BYTES - 1), 1);
      }

      return m_cacheDistancesToEOM;
   }

   /**
    * @see IMediumCacheTest#getOverlappingRegions()
    */
   @Override
   protected Map<IMediumReference, Integer> getOverlappingRegions() {

      if (m_overlappingRegions == null) {
         m_overlappingRegions = new HashMap<>();

         IMedium<?> medium = getExpectedMedium();

         m_overlappingRegions.put(new StandardMediumReference(medium, 15), 72);
         m_overlappingRegions.put(new StandardMediumReference(medium, 121), 90);
      }

      return m_overlappingRegions;
   }

   /**
    * @see IMediumCacheTest#getTestCacheSizes()
    */
   @Override
   protected LinkedHashMap<IMediumReference, Integer> getTestCacheSizes() {

      if (m_theTestCacheSizes == null) {
         m_theTestCacheSizes = new LinkedHashMap<>();

         IMedium<?> medium = getExpectedMedium();

         // Must be put in order into this map!
         m_theTestCacheSizes.put(new StandardMediumReference(medium, 0), 10);
         // --> Start of first overlapping region (size: 72)
         m_theTestCacheSizes.put(new StandardMediumReference(medium, 15), 50);
         // Important: A region totally contained within another one
         m_theTestCacheSizes.put(new StandardMediumReference(medium, 17), 5);
         m_theTestCacheSizes.put(new StandardMediumReference(medium, 65), 17);
         m_theTestCacheSizes.put(new StandardMediumReference(medium, 70), 17);
         // --> End of first overlapping region (size: 72)
         m_theTestCacheSizes.put(new StandardMediumReference(medium, 100), 20);

         // --> Start of second overlapping region (size: 90)
         m_theTestCacheSizes.put(new StandardMediumReference(medium, 121), 1);
         m_theTestCacheSizes.put(new StandardMediumReference(medium, 122), 89);
         // --> End of second overlapping region (size: 90)

         m_theTestCacheSizes.put(new StandardMediumReference(medium, 250), 100);
      }

      return m_theTestCacheSizes;
   }

   /**
    * @see IMediumCacheTest#getTestling()
    */
   @Override
   protected IMediumCache getTestling() {

      if (m_theCache == null) {
         final FileMediumAccessor accessor = new FileMediumAccessor(
            getExpectedMedium());

         m_theCache = new StandardMediumCache(accessor,
            MAXIMUM_CACHE_BLOCK_SIZE);
      }

      return m_theCache;
   }
}
