/**
 *
 * {@link Flags4ByteTest}.java
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
 * {@link Flags4ByteTest} tests the {@link Flags} class with 4 byte flags.
 */
public class Flags4ByteTest extends Flags3ByteTest {

	private final static String RESERVED_4_FLAG = "Reserved 4";

	private final static String NO_FLAG = "no";

	private final static String RAGE_FLAG = "Rage";
	private final static String BISYNCHRONISATION_FLAG = "Bi synchronsisation";
	private final List<FlagDescription> m_enhancedDescriptions = new ArrayList<>();

	/**
	 * @see com.github.jmeta.library.dataformats.api.types.Flags2ByteTest#getByteLength()
	 */
	@Override
	protected int getByteLength() {
		return 4;
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

			m_enhancedDescriptions
				.add(new FlagDescription(Flags4ByteTest.RESERVED_4_FLAG, new BitAddress(3, 1), "", 1, null));
			m_enhancedDescriptions.add(new FlagDescription(Flags4ByteTest.NO_FLAG, new BitAddress(3, 3), "", 1, null));
			m_enhancedDescriptions
				.add(new FlagDescription(Flags4ByteTest.RAGE_FLAG, new BitAddress(3, 5), "", 1, null));
			m_enhancedDescriptions
				.add(new FlagDescription(Flags4ByteTest.BISYNCHRONISATION_FLAG, new BitAddress(3, 6), "", 1, null));
		}

		return m_enhancedDescriptions;
	}
}
