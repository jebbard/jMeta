/**
 *
 * {@link Flags6ByteXXXTest}.java
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
 * {@link Flags6ByteXXXTest} tests the {@link Flags} class with 6 byte flags.
 */
public class Flags6ByteXXXTest extends Flags5ByteXXXTest {

   /**
    * @see com.github.jmeta.library.dataformats.api.Flags2ByteXXXTest#getByteLength()
    */
   @Override
   protected int getByteLength() {
      return 6;
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

         m_enhancedDescriptions.add(new FlagDescription(RESERVED_6_FLAG, new BitAddress(5, 0), "", 1, null));
         m_enhancedDescriptions.add(new FlagDescription(OTHERWORLD, new BitAddress(5, 2), "", 1, null));
         m_enhancedDescriptions.add(new FlagDescription(FURIOUS_ANGER_FLAG, new BitAddress(5, 4), "", 1, null));
         m_enhancedDescriptions.add(new FlagDescription(QUADSYNCHRONISATION_FLAG, new BitAddress(5, 6), "", 1, null));
      }

      return m_enhancedDescriptions;
   }

   private final List<FlagDescription> m_enhancedDescriptions = new ArrayList<>();
   private final static String FURIOUS_ANGER_FLAG = "FurtosdaAnger";
   private final static String OTHERWORLD = "otherworld";
   private final static String RESERVED_6_FLAG = "Reserved 6";
   private final static String QUADSYNCHRONISATION_FLAG = "Quad synchronsisation";
}
