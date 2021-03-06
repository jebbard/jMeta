/**
 *
 * {@link Flags7ByteTest}.java
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
 * {@link Flags7ByteTest} tests the {@link Flags} class with 7 byte flags.
 */
public class Flags7ByteTest extends Flags6ByteTest {

	private final static String ANGER_2_FLAG = "Anger2";

	private final static String THISWORLD = "thisworld";

	private final static String QUINSYNCHRONISATION_FLAG = "Quin synchronsisation";
	private final static String RESERVED_7_FLAG = "Reserved 7";
	private final List<FlagDescription> m_enhancedDescriptions = new ArrayList<>();

	/**
	 * @see com.github.jmeta.library.dataformats.api.types.Flags2ByteTest#getByteLength()
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

			m_enhancedDescriptions
				.add(new FlagDescription(Flags7ByteTest.RESERVED_7_FLAG, new BitAddress(6, 0), "", 1, null));
			m_enhancedDescriptions
				.add(new FlagDescription(Flags7ByteTest.THISWORLD, new BitAddress(6, 1), "", 1, null));
			m_enhancedDescriptions
				.add(new FlagDescription(Flags7ByteTest.ANGER_2_FLAG, new BitAddress(6, 3), "", 1, null));
			m_enhancedDescriptions
				.add(new FlagDescription(Flags7ByteTest.QUINSYNCHRONISATION_FLAG, new BitAddress(6, 5), "", 1, null));
		}

		return m_enhancedDescriptions;
	}
}
