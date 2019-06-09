/**
 *
 * {@link Flags1ByteTest}.java
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
 * {@link Flags3ByteTest} tests the {@link Flags} class with 3 byte flags.
 */
public class Flags3ByteTest extends Flags2ByteTest {

	private final static String REDO_FLAG = "Redo possible";

	private final static String RESERVED_3_FLAG = "Reserved 3";

	private final static String STAFFED_FLAG = "Staffed";
	private final static String SYNCHRONISATION_FLAG = "Synchronsisation";
	private final List<FlagDescription> m_enhancedDescriptions = new ArrayList<>();

	/**
	 * @see com.github.jmeta.library.dataformats.api.types.Flags2ByteTest#getByteLength()
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

			m_enhancedDescriptions
				.add(new FlagDescription(Flags3ByteTest.RESERVED_3_FLAG, new BitAddress(2, 0), "", 1, null));
			m_enhancedDescriptions
				.add(new FlagDescription(Flags3ByteTest.REDO_FLAG, new BitAddress(2, 1), "", 1, null));
			m_enhancedDescriptions
				.add(new FlagDescription(Flags3ByteTest.STAFFED_FLAG, new BitAddress(2, 2), "", 1, null));
			m_enhancedDescriptions
				.add(new FlagDescription(Flags3ByteTest.SYNCHRONISATION_FLAG, new BitAddress(2, 3), "", 1, null));
		}

		return m_enhancedDescriptions;
	}
}
