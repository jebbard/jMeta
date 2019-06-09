/**
 * {@link MagicKey}.java
 *
 * @author Jens Ebert
 * @date 31.12.10 19:47:06 (December 31, 2010)
 */

package com.github.jmeta.library.dataformats.api.types;

import java.nio.ByteBuffer;
import java.util.Arrays;

import com.github.jmeta.utility.charset.api.services.Charsets;
import com.github.jmeta.utility.dbc.api.services.Reject;

/**
 * This class represents a magic key used for identifying the data format of a
 * given container.
 */
// TODO add testcase class for class
public class MagicKey {

	private final byte[] magicKeyBytes;

	private final int bitLength;

	private final long deltaOffset;

	private final DataBlockId fieldId;

	private final String stringRepresentation;

	/**
	 * Creates a new {@link MagicKey}. Use this constructor if the magic key has no
	 * human-readable string representation but only covers full bytes.
	 * 
	 * @param magicKeyBytes The magic key's bytes indicating presence of a container
	 *                      if found or not found, depending on the concrete
	 *                      subclass; the string representation of the magic key is
	 *                      just containing the raw bytes as array, e.g. "[1, 2,
	 *                      144]"
	 * @param fieldId       The id of the field this magic key corresponds to
	 * @param deltaOffset   The delta offset of the first byte of the magic key from
	 *                      start of header (positive offset) or from the end of the
	 *                      footer (negative offset) it is contained in
	 */
	public MagicKey(byte[] magicKeyBytes, DataBlockId fieldId, long deltaOffset) {
		this(magicKeyBytes, magicKeyBytes != null ? magicKeyBytes.length * Byte.SIZE : 0, fieldId, deltaOffset);
	}

	/**
	 * Creates a new {@link MagicKey}. Use this constructor if the magic key has no
	 * human-readable string representation and an odd length, i.e. only covers
	 * bytes partially.
	 * 
	 * @param magicKeyBytes The magic key's bytes indicating presence of a container
	 *                      if found or not found, depending on the concrete
	 *                      subclass; the string representation of the magic key is
	 *                      just containing the raw bytes as array, e.g. "[1, 2,
	 *                      144]"
	 * @param bitLength     The length of the magic key in bits
	 * @param fieldId       The id of the field this magic key corresponds to
	 * @param deltaOffset   The delta offset of the first byte of the magic key from
	 *                      start of header (positive offset) or from the end of the
	 *                      footer (negative offset) it is contained in
	 */
	public MagicKey(byte[] magicKeyBytes, int bitLength, DataBlockId fieldId, long deltaOffset) {
		Reject.ifNull(magicKeyBytes, "magicKeyBytes");
		Reject.ifNull(fieldId, "fieldId");
		Reject.ifNegativeOrZero(bitLength, "bitLength");
		Reject.ifFalse(bitLength <= (magicKeyBytes.length * Byte.SIZE),
			"bitLength <= magicKeyBytes.length * Byte.SIZE");

		this.magicKeyBytes = magicKeyBytes.clone();
		this.bitLength = bitLength;
		stringRepresentation = Arrays.toString(magicKeyBytes);
		this.fieldId = fieldId;
		this.deltaOffset = deltaOffset;
	}

	/**
	 * Creates a new {@link MagicKey}. Use this constructor if the magic key has a
	 * human-readable ASCII string representation and only covers full bytes.
	 * 
	 * @param asciiKey    A string containing only 7 bit standard ASCII characters
	 *                    which is the human-readable magic key
	 * @param fieldId     The id of the field this magic key corresponds to
	 * @param deltaOffset The delta offset of the first byte of the magic key from
	 *                    start of header (positive offset) or from the end of the
	 *                    footer (negative offset) it is contained in
	 */
	public MagicKey(String asciiKey, DataBlockId fieldId, long deltaOffset) {
		this(asciiKey != null ? asciiKey.getBytes(Charsets.CHARSET_ASCII) : null, fieldId, deltaOffset);
	}

	/**
	 * @return the bit length
	 */
	public int getBitLength() {
		return bitLength;
	}

	/**
	 * @return the byte length, i.e. the number of fully or partly covered bytes
	 */
	public int getByteLength() {
		return (getBitLength() / Byte.SIZE) + ((getBitLength() % Byte.SIZE) != 0 ? 1 : 0);
	}

	/**
	 * @return delta offset from start of header or end of footer
	 */
	public long getDeltaOffset() {
		return deltaOffset;
	}

	/**
	 * @return the id of the field this magic key corresponds to
	 */
	public DataBlockId getFieldId() {
		return fieldId;
	}

	/**
	 * @return a clone of the magic key bytes
	 */
	public byte[] getMagicKeyBytes() {
		return magicKeyBytes.clone();
	}

	/**
	 * @return the string representation of the magic key bytes
	 */
	public String getStringRepresentation() {
		return stringRepresentation;
	}

	/**
	 * Indicates whether the bytes of this magic key are present at the beginning of
	 * the given bytes or not. The bytes are NOT scanned for the magic key. Either
	 * the magic key is found at the beginning of the bytes, or not.
	 * 
	 * @param readBytes The bytes to look for the magic key
	 * @return true if the magic key's presence is indicated by the given bytes,
	 *         false otherwise
	 */
	public boolean isPresentIn(ByteBuffer readBytes) {
		Reject.ifNull(readBytes, "readBytes");

		int comparedBits = 0;

		if (readBytes.remaining() < magicKeyBytes.length) {
			return false;
		}

		for (int i = 0; i < magicKeyBytes.length; i++) {
			final byte magicKeyByte = magicKeyBytes[i];
			final byte readByte = readBytes.get();

			if ((getBitLength() - comparedBits) < Byte.SIZE) {
				byte bitMask = 0;

				for (int j = 1; j <= (getBitLength() % Byte.SIZE); ++j) {
					bitMask |= (1 << (Byte.SIZE - j));
				}

				if ((bitMask & readByte) != magicKeyByte) {
					return false;
				}
			}

			else if (magicKeyByte != readByte) {
				return false;
			}

			comparedBits += Byte.SIZE;
		}

		return true;
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return getClass().getName() + "[" + "magicKeyBytes=" + Arrays.toString(magicKeyBytes) + ", bitLength="
			+ bitLength + ", byte length=" + getByteLength() + ", deltaOffset=" + deltaOffset + ", fieldId=" + fieldId
			+ ", stringRepresentation=" + stringRepresentation + "]";
	}

}
