/**
 *
 * {@link Flags5ByteXXXTest}.java
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
 * {@link Flags5ByteXXXTest} tests the {@link Flags} class with 5 byte flags.
 */
public class Flags5ByteXXXTest extends Flags4ByteXXXTest {

   /**
    * @see de.je.util.javautil.common.flags.Flags2ByteXXXTest#getByteLength()
    */
   @Override
   protected int getByteLength() {
      return 5;
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

         m_enhancedDescriptions.add(new FlagDescription(RESERVED_5_FLAG, new BitAddress(4, 2), "", 1, null));
         m_enhancedDescriptions.add(new FlagDescription(NO_FLAG_YET, new BitAddress(4, 3), "", 1, null));
         m_enhancedDescriptions.add(new FlagDescription(ANGER_FLAG, new BitAddress(4, 6), "", 1, null));
         m_enhancedDescriptions.add(new FlagDescription(TRISYNCHRONISATION_FLAG, new BitAddress(4, 7), "", 1, null));
      }

      return m_enhancedDescriptions;
   }

   private final List<FlagDescription> m_enhancedDescriptions = new ArrayList<>();
   private final static String TRISYNCHRONISATION_FLAG = "Tri synchronsisation";
   private final static String NO_FLAG_YET = "no flag yet";
   private final static String ANGER_FLAG = "Anger";
   private final static String RESERVED_5_FLAG = "Reserved 5";
}
