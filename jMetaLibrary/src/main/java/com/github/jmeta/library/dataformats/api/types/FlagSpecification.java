/**
 *
 * {@link FlagSpecification}.java
 *
 * @author Jens Ebert
 *
 * @date 09.01.2009
 *
 */
package com.github.jmeta.library.dataformats.api.types;

import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.github.jmeta.utility.dbc.api.services.Reject;

/**
 * {@link FlagSpecification} defines the structure of {@link Flags} belonging
 * together. Defines length, positions and names of the flags for easy access.
 */
public class FlagSpecification {

	/** The maximum length of {@link Flags} in bytes. */
	public static final int MAXIMUM_BYTE_COUNT = Long.SIZE / Byte.SIZE;

	private final Map<String, FlagDescription> flagDescriptions = new HashMap<>();
	private final int m_byteLength;
	private final ByteOrder byteOrdering;
	private final byte[] defaultFlagBytes;

	/**
	 * Creates the specification.
	 *
	 * @param flagDescriptions The {@link FlagDescription}s of all flags comprising
	 *                         the flag bytes. The {@link FlagDescription}s must be
	 *                         ordered by {@link BitAddress} from lowest to highest
	 *                         significant bit, thereby having bit order left =
	 *                         highest, right = lowest and byte order little endian
	 *                         (left byte = lowest significance, right byte =
	 *                         highest significance). The list must not contain
	 *                         {@link FlagDescription}s whose {@link BitAddress}es
	 *                         are identical or overlap each other.
	 * @param byteLength       The length of the flags in bytes. Must be greater
	 *                         than 0 and smaller than {@link #MAXIMUM_BYTE_COUNT}.
	 * @param byteOrdering     The byte ordering of the {@link Flags}.
	 * @param defaultFlagBytes The default flags to be used when initializing new
	 *                         flag bytes. Must have the same length as the
	 *                         specified byte length.
	 */
	public FlagSpecification(List<FlagDescription> flagDescriptions, int byteLength, ByteOrder byteOrdering,
		byte[] defaultFlagBytes) {
		Reject.ifNull(byteOrdering, "byteOrdering");
		Reject.ifNull(defaultFlagBytes, "defaultFlags");
		Reject.ifNull(flagDescriptions, "flagDescriptions");
		Reject.ifNotInInterval(byteLength, 1, FlagSpecification.MAXIMUM_BYTE_COUNT, "byteLength");
		Reject.ifTrue(defaultFlagBytes.length != byteLength, "defaultFlagBytes.length must equal given byteLength");

		Map<BitAddress, Integer> bitAddressesWithSizes = new HashMap<>(flagDescriptions.size());

		for (int i = 0; i < flagDescriptions.size(); ++i) {
			FlagDescription flagDescription = flagDescriptions.get(i);

			BitAddress address = flagDescription.getStartBitAddress();
			final String flagName = flagDescription.getFlagName();

			Reject.ifTrue(address.getByteAddress() > byteLength, "The given byte adress " + address.getByteAddress()
				+ " is greater than the given byte length " + byteLength);
			Reject.ifTrue(this.flagDescriptions.containsKey(flagName),
				"The flag name " + flagName + " is multiply defined in the given list. Flag names must be unique");
			Reject.ifTrue(bitAddressesWithSizes.containsKey(address), "The given bit address " + address
				+ " is multiply defined in the given list. Bit addresses must be unique");

			// TODO make the code for detecting overlaps correct (bit 0 = lowest bit)
			// for (Iterator<BitAddress> iterator =
			// bitAddressesWithSizes.keySet().iterator(); iterator
			// .hasNext();)
			// {
			// BitAddress nextAddress = iterator.next();
			// int size = bitAddressesWithSizes.get(nextAddress);
			//
			// TestReject.ifTrue(address.smallerThan(nextAddress), "Bit addresses given in
			// the list of flag descriptions
			// must be ordered from lowest to hightest. Address " + address + " found to be
			// smaller then the previous
			// address " + nextAddress + ".");
			// TestReject.ifTrue(address.smallerThan(nextAddress.advance(size)), "Bit
			// address " + address + " found to be
			// overlapping with the previous address " + nextAddress + ".");
			// }

			bitAddressesWithSizes.put(address, flagDescription.getBitSize());

			this.flagDescriptions.put(flagName, flagDescription);
		}

		this.byteOrdering = byteOrdering;
		m_byteLength = byteLength;
		this.defaultFlagBytes = defaultFlagBytes;
	}

	/**
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}

		if (obj == null) {
			return false;
		}

		if (getClass() != obj.getClass()) {
			return false;
		}

		FlagSpecification other = (FlagSpecification) obj;

		if (m_byteLength != other.m_byteLength) {
			return false;
		}

		if (byteOrdering == null) {
			if (other.byteOrdering != null) {
				return false;
			}
		}

		else if (!byteOrdering.equals(other.byteOrdering)) {
			return false;
		}

		if (!Arrays.equals(defaultFlagBytes, other.defaultFlagBytes)) {
			return false;
		}

		if (flagDescriptions == null) {
			if (other.flagDescriptions != null) {
				return false;
			}
		}

		else if (!flagDescriptions.equals(other.flagDescriptions)) {
			return false;
		}

		return true;
	}

	/**
	 * Returns the byte length of this {@link Flags}.
	 *
	 * @return The byte length of this {@link Flags}.
	 */
	public int getByteLength() {
		return m_byteLength;
	}

	/**
	 * Returns the byte ordering of this {@link FlagSpecification}.
	 *
	 * @return The byte ordering of this {@link FlagSpecification}.
	 */
	public ByteOrder getByteOrdering() {
		return byteOrdering;
	}

	/**
	 * Returns the default flag bytes used when initializing new flag bytes.
	 *
	 * @return the default flag bytes used when initializing new flag bytes.
	 */
	public byte[] getDefaultFlagBytes() {
		return defaultFlagBytes.clone();
	}

	/**
	 * Returns the {@link BitAddress} of the flag with the given name.
	 *
	 * @param flagName The name of the flag, must exist.
	 * @return The {@link BitAddress} of the flag with the given name.
	 */
	public BitAddress getFlagAddress(String flagName) {
		Reject.ifNull(flagName, "flagName");

		return flagDescriptions.get(flagName).getStartBitAddress();
	}

	/**
	 * Returns all flag names and their {@link FlagDescription}s as specified by
	 * this {@link FlagSpecification}.
	 *
	 * @return All flag names and their {@link FlagDescription}s as specified by
	 *         this {@link FlagSpecification}.
	 */
	public Map<String, FlagDescription> getFlagDescriptions() {
		return Collections.unmodifiableMap(flagDescriptions);
	}

	/**
	 * Determines if the {@link FlagSpecification} knows a flag of the given name.
	 *
	 * @param flagName The name of the flag.
	 *
	 * @return true if the {@link FlagSpecification} knows a flag of the given name,
	 *         false otherwise.
	 */
	public boolean hasFlag(String flagName) {
		Reject.ifNull(flagName, "flagName");

		return flagDescriptions.containsKey(flagName);
	}

	/**
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = (prime * result) + m_byteLength;
		result = (prime * result) + ((byteOrdering == null) ? 0 : byteOrdering.hashCode());
		result = (prime * result) + Arrays.hashCode(defaultFlagBytes);
		result = (prime * result) + ((flagDescriptions == null) ? 0 : flagDescriptions.hashCode());

		return result;
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "FlagSpecification [m_byteOrdering=" + byteOrdering + ", m_flagPositions=" + flagDescriptions + "]";
	}
}
