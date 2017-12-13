/**
 *
 * {@link FlagSpecificationTest}.java
 *
 * @author Jens Ebert
 *
 * @date 09.01.2009
 *
 */
package com.github.jmeta.library.dataformats.api.types;

import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.github.jmeta.utility.dbc.api.exceptions.PreconditionUnfullfilledException;

/**
 * {@link FlagSpecificationTest} tests the {@link FlagSpecification} class.
 */
public class FlagSpecificationTest {

   /**
    * Test {@link FlagSpecification#FlagSpecification(List, int, ByteOrder, byte[])} for
    * {@link IllegalArgumentException}.
    */
   @Test
   public void test_constructor_exception() {
      // (1) Must not contain the same BitAddresses twice
      try {
         List<FlagDescription> wrongDescriptions = new ArrayList<>();

         wrongDescriptions.add(new FlagDescription("Flag1", new BitAddress(3, 5), "", 1, null));
         wrongDescriptions.add(new FlagDescription("Flag2", new BitAddress(0, 1), "", 1, null));
         wrongDescriptions.add(new FlagDescription("Flag3", new BitAddress(3, 5), "", 1, null));

         FlagSpecification flagSpec = new FlagSpecification(wrongDescriptions, 6, ByteOrder.BIG_ENDIAN, new byte[6]);

         Assert.fail("Exception expected: " + PreconditionUnfullfilledException.class.getName() + " for " + flagSpec);
      } catch (PreconditionUnfullfilledException e) {
         Assert.assertNotNull("Exception as expected", e);
      }

      // (2) Must not contain ByteAddresses bigger than specified byte length
      try {
         List<FlagDescription> wrongDescriptions = new ArrayList<>();

         wrongDescriptions.add(new FlagDescription("Flag1", new BitAddress(3, 5), "", 1, null));
         wrongDescriptions.add(new FlagDescription("Flag2", new BitAddress(0, 1), "", 1, null));
         wrongDescriptions.add(new FlagDescription("Flag3", new BitAddress(9, 5), "", 1, null));

         FlagSpecification flagSpec = new FlagSpecification(wrongDescriptions, 6, ByteOrder.BIG_ENDIAN, new byte[6]);

         Assert.fail("Exception expected: " + PreconditionUnfullfilledException.class.getName() + " for " + flagSpec);
      } catch (PreconditionUnfullfilledException e) {
         Assert.assertNotNull("Exception as expected", e);
      }
   }

   /**
    * Tests {@link FlagDescription#isMultibitFlag()}.
    */
   @Test
   public void test_isMultibitFlag() {
      FlagDescription desc = new FlagDescription("HALLO", new BitAddress(0, 0), "", 3, null);
      FlagDescription desc2 = new FlagDescription("HALLO2", new BitAddress(0, 0), "", 1, null);

      Assert.assertTrue(desc.isMultibitFlag());
      Assert.assertFalse(desc2.isMultibitFlag());
   }
}
