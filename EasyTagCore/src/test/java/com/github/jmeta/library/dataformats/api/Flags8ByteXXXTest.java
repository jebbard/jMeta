/**
 *
 * {@link Flags8ByteXXXTest}.java
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
 * {@link Flags8ByteXXXTest} tests the {@link Flags} class with 8 byte flags.
 */
public class Flags8ByteXXXTest extends Flags7ByteXXXTest {

   /**
    * @see com.github.jmeta.library.dataformats.api.Flags2ByteXXXTest#getByteLength()
    */
   @Override
   protected int getByteLength() {
      return 8;
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

         m_enhancedDescriptions.add(new FlagDescription(RESERVED_8_FLAG, new BitAddress(7, 0), "", 1, null));
         m_enhancedDescriptions.add(new FlagDescription(XWORLD, new BitAddress(7, 2), "", 1, null));
         m_enhancedDescriptions.add(new FlagDescription(ANGER_3_FLAG, new BitAddress(7, 4), "", 1, null));
         m_enhancedDescriptions.add(new FlagDescription(OCTSYNCHRONISATION_FLAG, new BitAddress(7, 6), "", 1, null));
      }

      return m_enhancedDescriptions;
   }

   private final List<FlagDescription> m_enhancedDescriptions = new ArrayList<>();
   private final static String ANGER_3_FLAG = "Anger3";
   private final static String OCTSYNCHRONISATION_FLAG = "Oct synchronsisation";
   private final static String RESERVED_8_FLAG = "Reserved 8";
   private final static String XWORLD = "Xworld";
}
