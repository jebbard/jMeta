/**
 *
 * {@link ByteArrayUtils}.java
 *
 * @author Jens Ebert
 *
 * @date 02.03.2009
 *
 */
package com.github.jmeta.utility.byteutils.api.services;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.github.jmeta.utility.byteutils.api.exceptions.InvalidArrayStringFormatException;
import com.github.jmeta.utility.dbc.api.services.Reject;

/**
 * {@link ByteArrayUtils} provides further array algorithms specifically for
 * byte arrays not contained in {@link Arrays} .
 */
public class ByteArrayUtils {

	/**
	 * The pattern to match for a array as string expression as required by
	 * {@link #parseArray(String)}.
	 */
	public static final Pattern ARRAY_AS_STRING_PATTERN = Pattern
		.compile("\\{((?:-?1?[0-9]?[0-9]\\, ?)*?)\\}(?:\\*([1-9][0-9]{0,9}))?");

	/**
	 * Copies the specified range of the specified array into a new array. The
	 * initial index of the range (from) must lie between zero and original.length,
	 * inclusive. The value at original[from] is placed into the initial element of
	 * the copy (unless from == original.length or from == to). Values from
	 * subsequent elements in the original array are placed into subsequent elements
	 * in the copy. The final index of the range (to), which must be greater than or
	 * equal to from, may be greater than original.length, in which case (byte)0 is
	 * placed in all elements of the copy whose index is greater than or equal to
	 * original.length - from. The length of the returned array will be to - from.
	 *
	 * @param original The array from which a range is to be copied.
	 * @param from     The initial index of the range to be copied, inclusive.
	 * @param to       The final index of the range to be copied, exclusive. (This
	 *                 index may lie outside the array.)
	 *
	 * @return A new array containing the specified range from the original array,
	 *         truncated or padded with zeros to obtain the required length.
	 *
	 * @throws ArrayIndexOutOfBoundsException {@literal if from < 0  or from > original.length()}
	 */
	public static byte[] copyOfRange(byte[] original, int from, int to) {
		Reject.ifNull(original, "original");
		Reject.ifTrue(from > to, "from > to");

		byte[] rangeCopy = new byte[to - from];

		int index = 0;

		for (int i = from; i < to; ++i) {
			rangeCopy[index++] = original[i];
		}

		return rangeCopy;
	}

	/**
	 * Determines the offset of the first occurrence of a sub-array within a given
	 * array of the same type.
	 * 
	 * @param array       The array to search in. Its length must be equal or longer
	 *                    than the length of the array to find plus the given start
	 *                    offset.
	 * @param subArray    The array to find. Its length must be equal or shorter
	 *                    than the length of the array to search in minus the given
	 *                    start offset.
	 * @param startOffset The offset in the array to start searching for the
	 *                    sub-array. Must not be negative. Must not be greater or
	 *                    equal to the array's length.
	 * @param increase    The count the offset for searching the sub-array in the
	 *                    given array is increased. E.g. if this parameter is 2,
	 *                    this method will find occurrences of the given sub-array
	 *                    at offsets 0, 2, 4, 6 .. It will not find occurrences at
	 *                    offsets 1, 3, 5, 7 .. This is useful if sub-arrays are
	 *                    required to start at word or dword offsets, e.g. for
	 *                    finding byte representations of multi-byte characters.
	 *                    Must not be smaller than 1.
	 *
	 * @return The offset of the first occurrence of toFind within searchIn or -1 if
	 *         the array to find is not contained in the searched array.
	 */
	public static int findFirst(byte[] array, byte[] subArray, int startOffset, int increase) {
		Reject.ifNull(subArray, "subArray");
		Reject.ifNull(array, "array");
		Reject.ifTrue(increase < 1, "increase < 1");
		Reject.ifNotInInterval(startOffset, 0, array.length - 1, "startOffset");

		if ((subArray.length + startOffset) > array.length) {
			return -1;
		}

		int offset = -1;

		// Equal objects
		if (array == subArray) {
			offset = 0;
		} else {
			// Equal length and hashes
			if (array.length == subArray.length) {
				if (Arrays.hashCode(array) == Arrays.hashCode(subArray)) {
					offset = 0;
				}
			}

			// Find inside
			else {
				for (int i = startOffset; (offset == -1) && ((i + subArray.length) <= array.length); i += increase) {
					int elementsEqual = 0;

					// Count equal elements in both arrays for each index
					for (int k = 0; (k < subArray.length) && (subArray[k] == array[i + k]); k++) {
						elementsEqual++;
					}

					if (elementsEqual == subArray.length) {
						offset = i;
					}
				}
			}
		}

		return offset;
	}

	/**
	 * Merges a list of arrays in the order as given in the corresponding parameter.
	 * The total length of the resulting merged array must be available before
	 * calling this method due to performance issues - it safes one iteration
	 * through the whole list. When filling the list with the fragment arrays, the
	 * total length can be calculated.
	 *
	 * @param arrayList   A list of arrays that are fragments and are to be merged
	 *                    to a resulting array.
	 * @param totalLength The overall total length of the destination array.
	 *
	 * @return A merged array containing all array fragments in the given order.
	 *
	 * @throws IndexOutOfBoundsException If the given total length does not match
	 *                                   the sum of all array fragment lengths.
	 */
	public static byte[] merge(List<byte[]> arrayList, int totalLength) {
		Reject.ifNull(arrayList, "arrayList");
		Reject.ifTrue(totalLength < 1, "totalLength < 1");

		byte[] mergedBytes = new byte[totalLength];

		int currentLength = 0;

		for (byte[] bytes : arrayList) {
			System.arraycopy(bytes, 0, mergedBytes, currentLength, bytes.length);

			currentLength += bytes.length;
		}

		return mergedBytes;
	}

	/**
	 * Parses a byte array from a string representation. The string representation
	 * is expected to have a format very similar to a Java byte array. See the
	 * complex value of the regular expression pattern
	 * {@link #ARRAY_AS_STRING_PATTERN}.
	 *
	 * The pattern allows for specification of a multiplicity using the '*' symbol.
	 * If the specified multiplicity times the number of array elements in the
	 * string exceeds {@link Integer#MAX_VALUE}, the
	 *
	 * Valid string representations are:
	 * <table summary="hallo">
	 * <tr>
	 * <td>String expression</td>
	 * <td>Corresponding Java array</td>
	 * </tr>
	 * <tr>
	 * <td><code>"{1,2,3,}"</code></td>
	 * <td><code>{1,2,3}</code></td>
	 * </tr>
	 * <tr>
	 * <td><code>"{1, 2,3,}"</code></td>
	 * <td><code>{1,2,3}</code></td>
	 * </tr>
	 * <tr>
	 * <td><code>"{-123,}"</code></td>
	 * <td><code>{-123}</code></td>
	 * </tr>
	 * <tr>
	 * <td><code>"{-123,1,5, 10, -32, 5,}"</code></td>
	 * <td><code>{-123,1,5,10,-32,5}</code></td>
	 * </tr>
	 * <tr>
	 * <td><code>"{1,}"</code></td>
	 * <td><code>{1}</code></td>
	 * </tr>
	 * <tr>
	 * <td><code>"{1,}*4"</code></td>
	 * <td><code>{1,1,1,1}</code></td>
	 * </tr>
	 * <tr>
	 * <td><code>"{1,2,3,}*2"</code></td>
	 * <td><code>{1,2,3,1,2,3}</code></td>
	 * </tr>
	 * <tr>
	 * <td><code>"{}"</code></td>
	 * <td><code>{}</code></td>
	 * </tr>
	 * <tr>
	 * <td><code>"{}*4"</code></td>
	 * <td><code>{}</code></td>
	 * </tr>
	 * </table>
	 * <p>
	 * Invalid string representations are:
	 * <table summary="hallo">
	 * <tr>
	 * <td>String expression</td>
	 * <td>Why invalid?</td>
	 * </tr>
	 * <tr>
	 * <td><code>"{1,2,3}"</code></td>
	 * <td>The array contents must end with a comma.</td>
	 * </tr>
	 * <tr>
	 * <td><code>"{234, 2,3,}"</code></td>
	 * <td>One element is bigger than {@link Byte#MAX_VALUE}.</td>
	 * </tr>
	 * <tr>
	 * <td><code>"{--123,}"</code></td>
	 * <td>Invalid character.</td>
	 * </tr>
	 * <tr>
	 * <td><code>"{xa}"</code></td>
	 * <td>Invalid character.</td>
	 * </tr>
	 * <tr>
	 * <td><code>"{,}"</code></td>
	 * <td>Number expected before comma.</td>
	 * </tr>
	 * <tr>
	 * <td><code>"{'a',}"</code></td>
	 * <td>Character expressions are not supported.</td>
	 * </tr>
	 * <tr>
	 * <td><code>"{-123,1,5, 10, -32, 5,"</code></td>
	 * <td>Missing closing bracket.</td>
	 * </tr>
	 * <tr>
	 * <td><code>"-123,1,5}"</code></td>
	 * <td>Missing opening bracket.</td>
	 * </tr>
	 * <tr>
	 * <td><code>"{+123,1,5}"</code></td>
	 * <td>Positive sign is unsupported.</td>
	 * </tr>
	 * <tr>
	 * <td><code>"{1,  2,}"</code></td>
	 * <td>At most one blank is allowed after a comma.</td>
	 * </tr>
	 * <tr>
	 * <td><code>"{1, 2,}*"</code></td>
	 * <td>Number must follow after '*'.</td>
	 * </tr>
	 * <tr>
	 * <td><code>"{1, 2,}*-1"</code></td>
	 * <td>Must be a positive number bigger than 0.</td>
	 * </tr>
	 * <tr>
	 * <td><code>"{1, 2,}*0"</code></td>
	 * <td>Must be a positive number bigger than 0.</td>
	 * </tr>
	 * <tr>
	 * <td><code>"{1, 2,}*214748364700"</code></td>
	 * <td>Number of digits in multiplicity must not be bigger than 10.</td>
	 * </tr>
	 * </table>
	 * 
	 * @param arrayAsString A String representation of the array
	 * @return A byte array corresponding to the given array string representation.
	 * @throws InvalidArrayStringFormatException If the given array string
	 *                                           representation does not match the
	 *                                           regular expression @link
	 *                                           #ARRAY_AS_STRING_PATTERN}.
	 */
	public static byte[] parseArray(String arrayAsString) throws InvalidArrayStringFormatException {
		Reject.ifNull(arrayAsString, "arrayAsString");

		Matcher matcher = ByteArrayUtils.ARRAY_AS_STRING_PATTERN.matcher(arrayAsString);

		if (!matcher.matches()) {
			throw new InvalidArrayStringFormatException("Array string must " + "match regular expression <  "
				+ ByteArrayUtils.ARRAY_AS_STRING_PATTERN.pattern() + "  >.");
		}

		String arrayContents = matcher.group(1);
		String multiplicity = matcher.group(2);

		// Empty array
		if ((arrayContents == null) || arrayContents.isEmpty()) {
			return new byte[0];
		}

		String[] splitArrayContents = arrayContents.split("\\,");

		int times = 1;

		if ((multiplicity != null) && !multiplicity.isEmpty()) {
			times = Integer.parseInt(multiplicity);
		}

		int arrayElementCount = splitArrayContents.length * times;

		byte[] returnedBytes = new byte[arrayElementCount];

		for (int j = 0; j < times; ++j) {
			for (int i = 0; i < splitArrayContents.length; i++) {
				byte arrayItem = Byte.parseByte(splitArrayContents[i].trim());

				returnedBytes[(j * splitArrayContents.length) + i] = arrayItem;
			}
		}

		return returnedBytes;
	}

	/**
	 * Converts a {@link Collection} of the wrapper type {@link Byte} to an array of
	 * byte.
	 *
	 * @param byteCollection The list of {@link Byte}s to convert.
	 * @return array of byte, always equal to the size of the given collection.
	 */
	public static byte[] toArray(Collection<Byte> byteCollection) {
		Reject.ifNull(byteCollection, "bytes");

		byte[] returnedBytes = new byte[byteCollection.size()];

		final Iterator<Byte> iterator = byteCollection.iterator();

		int currentIndex = 0;

		while (iterator.hasNext()) {
			returnedBytes[currentIndex++] = iterator.next();
		}

		return returnedBytes;
	}

	/**
	 * Cannot be instantiated.
	 */
	private ByteArrayUtils() {
	}
}
