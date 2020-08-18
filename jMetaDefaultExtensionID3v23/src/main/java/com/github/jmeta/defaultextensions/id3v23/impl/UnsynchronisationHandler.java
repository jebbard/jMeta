/**
 *
 * {@link UnsynchronisationHandler}.java
 *
 * @author Jens Ebert
 *
 * @date 08.03.2011
 */
package com.github.jmeta.defaultextensions.id3v23.impl;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.jmeta.library.datablocks.api.exceptions.BinaryValueConversionException;
import com.github.jmeta.library.datablocks.api.services.ExtendedDataBlockFactory;
import com.github.jmeta.library.datablocks.api.types.Container;
import com.github.jmeta.library.datablocks.api.types.Field;
import com.github.jmeta.library.datablocks.api.types.Header;
import com.github.jmeta.library.dataformats.api.types.Flags;
import com.github.jmeta.utility.byteutils.api.services.ByteArrayUtils;

/**
 * {@link UnsynchronisationHandler} performs the ID3v2 unsynchronization scheme.
 */
public class UnsynchronisationHandler extends AbstractID3v2TransformationHandler {

	private static final Logger LOGGER = LoggerFactory.getLogger(UnsynchronisationHandler.class);

	/**
	 * Creates a new {@link UnsynchronisationHandler}.
	 *
	 * @param dbFactory The {@link ExtendedDataBlockFactory}
	 */
	public UnsynchronisationHandler(ExtendedDataBlockFactory dbFactory) {
		super(ID3v2TransformationType.UNSYNCHRONIZATION, dbFactory);
	}

	@Override
	public boolean requiresTransform(Container container) {

		if (container.getHeaders().size() == 0) {
			return false;
		}

		Header id3v2Header = container.getHeaders().get(0);

		for (int i = 0; i < id3v2Header.getFields().size(); ++i) {
			Field<?> field = id3v2Header.getFields().get(i);

			if (field.getId().equals(ID3v23Extension.REF_TAG_HEADER_FLAGS.getId())) {
				try {
					Flags flags = (Flags) field.getInterpretedValue();

					return flags.getFlag(ID3v23Extension.TAG_FLAGS_UNSYNCHRONIZATION);
				} catch (BinaryValueConversionException e) {
					UnsynchronisationHandler.LOGGER.warn(
						"Field conversion from binary to interpreted value failed for field id <%1$s>. Exception see below.",
						field.getId());
					UnsynchronisationHandler.LOGGER.error("requiresTransform", e);
					return false;
				}
			}
		}

		return false;
	}

	@Override
	public boolean requiresUntransform(Container container) {

		return requiresTransform(container);
	}

	@Override
	protected byte[][] transformRawBytes(ByteBuffer payloadBytes) {

		if (payloadBytes.remaining() < 1) {
			return new byte[][] { payloadBytes.array() };
		}

		List<Byte> unsynchronisedByteList = new ArrayList<>(payloadBytes.remaining());

		for (int i = 0; i < payloadBytes.remaining() - 1; i++) {
			byte firstByte = payloadBytes.get(i);
			byte secondByte = payloadBytes.get(i + 1);

			unsynchronisedByteList.add(firstByte);

			if (firstByte == 0xFF && (secondByte >= 0xE0 || secondByte == 0)) {
				unsynchronisedByteList.add((byte) 0);
			}
		}

		unsynchronisedByteList.add(payloadBytes.get(payloadBytes.remaining() - 1));

		return new byte[][] { ByteArrayUtils.toArray(unsynchronisedByteList) };
	}

	@Override
	protected byte[][] untransformRawBytes(ByteBuffer payloadBytes) {

		if (payloadBytes.remaining() < 1) {
			return new byte[][] { payloadBytes.array() };
		}

		List<Byte> synchronisedByteList = new ArrayList<>(payloadBytes.remaining());

		for (int i = payloadBytes.remaining() - 1; i > 0; i--) {
			byte previousByte = payloadBytes.get(i - 1);
			byte nextByte = payloadBytes.get(i);

			if (previousByte == 0xFF && nextByte == 0x00) {
				synchronisedByteList.add(previousByte);
			} else {
				synchronisedByteList.add(nextByte);
			}
		}

		synchronisedByteList.add(payloadBytes.get(payloadBytes.remaining() - 1));

		Collections.reverse(synchronisedByteList);

		return new byte[][] { ByteArrayUtils.toArray(synchronisedByteList) };
	}
}
