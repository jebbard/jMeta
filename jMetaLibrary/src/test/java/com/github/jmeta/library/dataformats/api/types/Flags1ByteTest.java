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

import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;

/**
 * {@link Flags1ByteTest} tests the {@link Flags} class with single byte flags.
 */
public class Flags1ByteTest extends FlagsTest {

	private final static String UNSYNCHRONISATION_FLAG = "Unsynchronisation";

	private final static String RESERVED_FLAG = "Reserved";

	private final static String HAS_PADDING_FLAG = "Has padding";

	private final static String HAS_FOOTER_FLAG = "Has footer";

	private List<FlagDescription> m_flagDescriptions;

	/**
	 * @see com.github.jmeta.library.dataformats.api.types.FlagsTest#getBigEndianFlagSpecification()
	 */
	@Override
	protected FlagSpecification getBigEndianFlagSpecification() {
		return new FlagSpecification(getFlagDescriptions(), getByteLength(), ByteOrder.BIG_ENDIAN,
			new byte[getByteLength()]);
	}

	/**
	 * Returns the byte length to use for this test.
	 *
	 * @return The byte length to use for this test.
	 */
	protected int getByteLength() {
		return 1;
	}

	/**
	 * Returns the flag mapping to use for both {@link FlagSpecification}s.
	 *
	 * @return The flag mapping to use for both {@link FlagSpecification}s.
	 */
	protected List<FlagDescription> getFlagDescriptions() {
		if (m_flagDescriptions == null) {
			m_flagDescriptions = new ArrayList<>();

			m_flagDescriptions
				.add(new FlagDescription(Flags1ByteTest.HAS_FOOTER_FLAG, new BitAddress(0, 1), "", 1, null));
			m_flagDescriptions
				.add(new FlagDescription(Flags1ByteTest.RESERVED_FLAG, new BitAddress(0, 2), "", 1, null));
			m_flagDescriptions
				.add(new FlagDescription(Flags1ByteTest.UNSYNCHRONISATION_FLAG, new BitAddress(0, 5), "", 1, null));
			m_flagDescriptions
				.add(new FlagDescription(Flags1ByteTest.HAS_PADDING_FLAG, new BitAddress(0, 7), "", 1, null));
		}

		return m_flagDescriptions;
	}

	/**
	 * @see com.github.jmeta.library.dataformats.api.types.FlagsTest#getLittleEndianFlagSpecification()
	 */
	@Override
	protected FlagSpecification getLittleEndianFlagSpecification() {
		return new FlagSpecification(getFlagDescriptions(), getByteLength(), ByteOrder.LITTLE_ENDIAN,
			new byte[getByteLength()]);
	}
}
