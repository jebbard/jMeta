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
 * {@link Flags2ByteTest} tests the {@link Flags} class with 2 byte flags.
 */
public class Flags2ByteTest extends Flags1ByteTest {

	private final static String RESERVED_2_FLAG = "Reserved 2";

	private final static String UNDO_FLAG = "Undo possible";

	private final static String STAGED_FLAG = "Staged";

	private final static String TERMINATION_FLAG = "Termination";
	private final List<FlagDescription> m_enhancedDescriptions = new ArrayList<>();

	/**
	 * @see com.github.jmeta.library.dataformats.api.types.Flags1ByteTest#getByteLength()
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

			m_enhancedDescriptions
				.add(new FlagDescription(Flags2ByteTest.RESERVED_2_FLAG, new BitAddress(1, 1), "", 1, null));
			m_enhancedDescriptions
				.add(new FlagDescription(Flags2ByteTest.UNDO_FLAG, new BitAddress(1, 4), "", 1, null));
			m_enhancedDescriptions
				.add(new FlagDescription(Flags2ByteTest.STAGED_FLAG, new BitAddress(1, 6), "", 1, null));
			m_enhancedDescriptions
				.add(new FlagDescription(Flags2ByteTest.TERMINATION_FLAG, new BitAddress(1, 7), "", 1, null));
		}

		return m_enhancedDescriptions;
	}
}
