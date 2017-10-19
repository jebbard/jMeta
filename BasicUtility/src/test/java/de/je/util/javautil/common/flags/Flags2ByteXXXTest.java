/**
 *
 * {@link Flags1ByteXXXTest}.java
 *
 * @author Jens Ebert
 *
 * @date 24.01.2009
 *
 */
package de.je.util.javautil.common.flags;

import java.util.ArrayList;
import java.util.List;

/**
 * {@link Flags2ByteXXXTest} tests the {@link Flags} class with 2 byte flags.
 */
public class Flags2ByteXXXTest extends Flags1ByteXXXTest {

   /**
    * @see de.je.util.javautil.common.flags.Flags1ByteXXXTest#getByteLength()
    */
   @Override
   protected int getByteLength() {
      return 2;
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

         m_enhancedDescriptions.add(new FlagDescription(RESERVED_2_FLAG, new BitAddress(1, 1), "", 1, null));
         m_enhancedDescriptions.add(new FlagDescription(UNDO_FLAG, new BitAddress(1, 4), "", 1, null));
         m_enhancedDescriptions.add(new FlagDescription(STAGED_FLAG, new BitAddress(1, 6), "", 1, null));
         m_enhancedDescriptions.add(new FlagDescription(TERMINATION_FLAG, new BitAddress(1, 7), "", 1, null));
      }

      return m_enhancedDescriptions;
   }

   private final List<FlagDescription> m_enhancedDescriptions = new ArrayList<>();

   private final static String RESERVED_2_FLAG = "Reserved 2";
   private final static String UNDO_FLAG = "Undo possible";
   private final static String STAGED_FLAG = "Staged";
   private final static String TERMINATION_FLAG = "Termination";
}
