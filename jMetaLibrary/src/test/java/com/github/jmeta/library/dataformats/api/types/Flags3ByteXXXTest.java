/**
 *
 * {@link Flags1ByteXXXTest}.java
 *
 * @author Jens Ebert
 *
 * @date 24.01.2009
 *
 */
package com.github.jmeta.library.dataformats.api.types;

import java.util.ArrayList;
import java.util.List;

/**
 * {@link Flags3ByteXXXTest} tests the {@link Flags} class with 3 byte flags.
 */
public class Flags3ByteXXXTest extends Flags2ByteXXXTest {

   /**
    * @see com.github.jmeta.library.dataformats.api.types.Flags2ByteXXXTest#getByteLength()
    */
   @Override
   protected int getByteLength() {
      return 3;
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

         m_enhancedDescriptions.add(new FlagDescription(RESERVED_3_FLAG, new BitAddress(2, 0), "", 1, null));
         m_enhancedDescriptions.add(new FlagDescription(REDO_FLAG, new BitAddress(2, 1), "", 1, null));
         m_enhancedDescriptions.add(new FlagDescription(STAFFED_FLAG, new BitAddress(2, 2), "", 1, null));
         m_enhancedDescriptions.add(new FlagDescription(SYNCHRONISATION_FLAG, new BitAddress(2, 3), "", 1, null));
      }

      return m_enhancedDescriptions;
   }

   private final List<FlagDescription> m_enhancedDescriptions = new ArrayList<>();
   private final static String REDO_FLAG = "Redo possible";
   private final static String RESERVED_3_FLAG = "Reserved 3";
   private final static String STAFFED_FLAG = "Staffed";
   private final static String SYNCHRONISATION_FLAG = "Synchronsisation";
}
