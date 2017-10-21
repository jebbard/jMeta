/**
 *
 * {@link Flags7ByteXXXTest}.java
 *
 * @author Jens Ebert
 *
 * @date 24.01.2009
 *
 */
package com.github.jmeta.library.dataformats.api;

import java.util.ArrayList;
import java.util.List;

import com.github.jmeta.library.dataformats.api.types.BitAddress;
import com.github.jmeta.library.dataformats.api.types.FlagDescription;
import com.github.jmeta.library.dataformats.api.types.FlagSpecification;
import com.github.jmeta.library.dataformats.api.types.Flags;

/**
 * {@link Flags7ByteXXXTest} tests the {@link Flags} class with 7 byte flags.
 */
public class Flags7ByteXXXTest extends Flags6ByteXXXTest {

   /**
    * @see com.github.jmeta.library.dataformats.api.Flags2ByteXXXTest#getByteLength()
    */
   @Override
   protected int getByteLength() {
      return 7;
   }

   /**
    * Returns the flag mapping to use for both {@link FlagSpecification}s.
    *
    * @return The flag mapping to use for both {@link FlagSpecification}s.
    */
   @Override
   protected List<FlagDescription> getFlagDescriptions() {
      if (m_enhancedDescriptions.isEmpty()) {
         m_enhancedDescriptions.addAll(super.getFlagDescriptions());

         m_enhancedDescriptions.add(new FlagDescription(RESERVED_7_FLAG, new BitAddress(6, 0), "", 1, null));
         m_enhancedDescriptions.add(new FlagDescription(THISWORLD, new BitAddress(6, 1), "", 1, null));
         m_enhancedDescriptions.add(new FlagDescription(ANGER_2_FLAG, new BitAddress(6, 3), "", 1, null));
         m_enhancedDescriptions.add(new FlagDescription(QUINSYNCHRONISATION_FLAG, new BitAddress(6, 5), "", 1, null));
      }

      return m_enhancedDescriptions;
   }

   private final List<FlagDescription> m_enhancedDescriptions = new ArrayList<>();
   private final static String ANGER_2_FLAG = "Anger2";
   private final static String THISWORLD = "thisworld";
   private final static String QUINSYNCHRONISATION_FLAG = "Quin synchronsisation";
   private final static String RESERVED_7_FLAG = "Reserved 7";
}
