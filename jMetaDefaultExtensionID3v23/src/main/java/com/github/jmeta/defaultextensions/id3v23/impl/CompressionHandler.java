/**
 *
 * {@link CompressionHandler}.java
 *
 * @author Jens Ebert
 *
 * @date 02.03.2011
 */
package com.github.jmeta.defaultextensions.id3v23.impl;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.jmeta.library.datablocks.api.exceptions.BinaryValueConversionException;
import com.github.jmeta.library.datablocks.api.services.ExtendedDataBlockFactory;
import com.github.jmeta.library.datablocks.api.types.Container;
import com.github.jmeta.library.datablocks.api.types.Field;
import com.github.jmeta.library.datablocks.api.types.Header;
import com.github.jmeta.library.dataformats.api.types.Flags;
import com.github.jmeta.utility.dbc.api.services.Reject;

/**
 * {@link CompressionHandler} compresses ID3v2 frames
 */
public class CompressionHandler extends AbstractID3v2TransformationHandler {

	private static final Logger LOGGER = LoggerFactory.getLogger(CompressionHandler.class);

	private static final int BLOCK_SIZE = 1024;

	/**
	 * Creates a new {@link CompressionHandler}.
	 *
	 * @param dbFactory The {@link ExtendedDataBlockFactory}
	 */
	public CompressionHandler(ExtendedDataBlockFactory dbFactory) {
		super(ID3v2TransformationType.COMPRESSION, dbFactory);
	}

	@Override
	public boolean requiresTransform(Container container) {
		Reject.ifNull(container, "container");

		if (container.getHeaders().size() == 0) {
			return false;
		}

		Header firstHeader = container.getHeaders().get(0);

		for (int i = 0; i < firstHeader.getFields().size(); ++i) {
			Field<?> field = firstHeader.getFields().get(i);

			if (field.getId().equals(ID3v23Extension.REF_GENERIC_FRAME_HEADER_FLAGS.getId())) {
				try {
					Flags flags = (Flags) field.getInterpretedValue();

					return flags.getFlag(ID3v23Extension.FRAME_FLAGS_COMPRESSION);
				} catch (BinaryValueConversionException e) {
					CompressionHandler.LOGGER.warn(
						"Field conversion from binary to interpreted value failed for field id <%1$s>. Exception see below.",
						field.getId());
					CompressionHandler.LOGGER.error("requiresTransform", e);
					return false;
				}
			}
		}

		return false;
	}

	@Override
	public boolean requiresUntransform(Container container) {

		Reject.ifNull(container, "container");

		return requiresTransform(container);
	}

	@Override
	protected byte[][] transformRawBytes(ByteBuffer payloadBytes) {

		final Deflater compressor = new Deflater();
		compressor.setInput(payloadBytes.array());

		final ByteArrayOutputStream bos = new ByteArrayOutputStream(payloadBytes.remaining());
		byte[] buf = new byte[CompressionHandler.BLOCK_SIZE];

		try {
			int count = 0;

			while ((count = compressor.deflate(buf)) > 0) {
				bos.write(buf, 0, count);
			}

			if (!compressor.finished()) {
				throw new RuntimeException("Bad zip data, size:" + payloadBytes.remaining());
			}
		} finally {
			compressor.end();
		}

		return new byte[][] { bos.toByteArray() };
	}

	@Override
	protected byte[][] untransformRawBytes(ByteBuffer payloadBytes) {

		// The case where the output gets longer than Integer.MAX is not handled
		// anywhere
		// An OutOfMemoryError is expected whenever this happens during writing to the
		// ByteArrayOutputStream. However, this should not happen as ID3v2 also has a
		// decompressed size field of int size
		final Inflater decompressor = new Inflater();
		decompressor.setInput(payloadBytes.array());

		// This output stream will grow, if necessary
		final ByteArrayOutputStream bos = new ByteArrayOutputStream(payloadBytes.remaining());
		byte[] buf = new byte[CompressionHandler.BLOCK_SIZE];

		try {
			int count = 0;

			while ((count = decompressor.inflate(buf)) > 0) {
				bos.write(buf, 0, count);
			}

			if (!decompressor.finished()) {
				throw new RuntimeException("Bad zip data, size:" + payloadBytes.remaining());
			}
		} catch (DataFormatException t) {
			throw new RuntimeException(t);
		} finally {
			decompressor.end();
		}

		return new byte[][] { bos.toByteArray() };
	}
}
