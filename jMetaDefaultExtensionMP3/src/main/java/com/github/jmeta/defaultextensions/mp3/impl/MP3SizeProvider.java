/**
 *
 * {@link MP3DataBlockReader}.java
 *
 * @author Jens Ebert
 *
 * @date 09.10.2011
 */
package com.github.jmeta.defaultextensions.mp3.impl;

import com.github.jmeta.library.datablocks.api.exceptions.BinaryValueConversionException;
import com.github.jmeta.library.datablocks.api.services.SizeProvider;
import com.github.jmeta.library.datablocks.api.types.ContainerContext;
import com.github.jmeta.library.datablocks.api.types.Field;
import com.github.jmeta.library.datablocks.api.types.Header;
import com.github.jmeta.library.dataformats.api.types.DataBlockDescription;
import com.github.jmeta.library.dataformats.api.types.DataBlockId;
import com.github.jmeta.library.dataformats.api.types.Flags;

/**
 * {@link MP3SizeProvider}
 *
 */
public class MP3SizeProvider implements SizeProvider {

	private int samplingRateFrequencies[][] = {
		// MPEG Version 1 sampling frequencies (-1 = reserved)
		{ 44100, 48000, 32000, -1 },
		// MPEG Version 2 sampling frequencies (-1 = reserved)
		{ 22050, 24000, 16000, -1 },
		// MPEG Version 2.5 sampling frequencies (-1 = reserved)
		{ 11025, 12000, 8000, -1 }, };

	private int bitRates[][][] = {
		// MPEG Version 1 bitrates (0 = free, -1 = bad)
		{
			// Layer 1
			{ 0, 32, 64, 96, 128, 160, 192, 224, 256, 288, 320, 352, 384, 416, 448, -1 },
			// Layer 2
			{ 0, 32, 48, 56, 64, 80, 96, 112, 128, 160, 192, 224, 256, 320, 384, -1 },
			// Layer 3
			{ 0, 32, 40, 48, 56, 64, 80, 96, 112, 128, 160, 192, 224, 256, 320, -1 }, },
		// MPEG Version 2 and 2.5 bitrates
		{
			// Layer 1
			{ 0, 32, 48, 56, 64, 80, 96, 112, 128, 144, 160, 176, 192, 224, 256, -1 },
			// Layer 2
			{ 0, 8, 16, 24, 32, 40, 48, 56, 64, 80, 96, 112, 128, 144, 160, -1 },
			// Layer 3
			{ 0, 8, 16, 24, 32, 40, 48, 56, 64, 80, 96, 112, 128, 144, 160, -1 }, }, };

	/**
	 * @see com.github.jmeta.library.datablocks.api.services.SizeProvider#getSizeOf(com.github.jmeta.library.dataformats.api.types.DataBlockId,
	 *      int, com.github.jmeta.library.datablocks.api.types.ContainerContext)
	 */
	@Override
	public long getSizeOf(DataBlockId id, int sequenceNumber, ContainerContext containerContext) {

		if (id.equals(MP3Extension.REF_PAYLOAD.getId())) {
			Header header = containerContext.getContainer().getHeaders().get(0);

			// System.out.println("########################");
			// System.out.println("######### Next frame");
			// System.out.println("########################");
			// System.out.println();
			//
			// System.out.println("Header bytes: " + Arrays.toString(bytes));

			@SuppressWarnings("unchecked")
			Field<Flags> flagsHeaderField = (Field<Flags>) header.getFields().get(0);

			long totalPayloadSize = DataBlockDescription.UNDEFINED;
			try {
				Flags flags = flagsHeaderField.getInterpretedValue();

				int mpegAudioVersionIdBits = flags.getFlagIntegerValue(MP3Extension.HEADER_FLAGS_ID);
				int layerBits = flags.getFlagIntegerValue(MP3Extension.HEADER_FLAGS_LAYER);
				int bitRateBits = flags.getFlagIntegerValue(MP3Extension.HEADER_FLAGS_BITRATE_INDEX);
				int sampleRateBits = flags.getFlagIntegerValue(MP3Extension.HEADER_FLAGS_SAMPLING_FREQUENCY);
				int paddingBits = flags.getFlagIntegerValue(MP3Extension.HEADER_FLAGS_PADDING_BIT);
				int protectionBit = flags.getFlagIntegerValue(MP3Extension.HEADER_FLAGS_NO_PROTECTION_BIT);

				// System.out.println(flags.getFlagValueString("Id"));
				// System.out.println(flags.getFlagValueString("Layer"));

				int bitRateVersionIndex = mpegAudioVersionIdBits == 3 ? 0 : 1;
				int bitRateLayerIndex = 0;

				if (layerBits == 1) {
					bitRateLayerIndex = 2;
				} else if (layerBits == 2) {
					bitRateLayerIndex = 1;
				} else if (layerBits == 3) {
					bitRateLayerIndex = 0;
				}

				int bitRate = bitRates[bitRateVersionIndex][bitRateLayerIndex][bitRateBits];

				// System.out.println("Bit rate index: " + bitRateBits);
				// System.out.println("Bit rate bits: " + Integer.toBinaryString(bitRateBits));
				// System.out.println("Bit rate: " + bitRate);

				int samplingFreqVersionIndex = 0;

				if (mpegAudioVersionIdBits == 0) {
					samplingFreqVersionIndex = 2;
				} else if (mpegAudioVersionIdBits == 2) {
					samplingFreqVersionIndex = 1;
				} else if (mpegAudioVersionIdBits == 3) {
					samplingFreqVersionIndex = 0;
				}

				int samplingRateFrequency = samplingRateFrequencies[samplingFreqVersionIndex][sampleRateBits];

				// System.out.println("Sampling freq index: " + sampleRateBits);
				// System.out.println("Sampling freq bits: " +
				// Integer.toBinaryString(sampleRateBits));
				// System.out.println("Sampling freq: " + samplingRateFrequency);

				if (layerBits == 3) {
					totalPayloadSize = (((12 * bitRate * 1000) / samplingRateFrequency) + paddingBits) * 4;
				} else if ((layerBits == 1) || (layerBits == 2)) {
					totalPayloadSize = ((144 * bitRate * 1000) / samplingRateFrequency) + paddingBits;
				}

				totalPayloadSize -= 4; // Minus header size
				if (protectionBit == 0) {
					totalPayloadSize += 2;
				}

				// System.out.println("Padding bit: " + paddingBits);
				// System.out.println("CRC NOT present: " + protectionBit);
				// System.out.println("Total size = " + totalPayloadSize);

				return totalPayloadSize;
			} catch (BinaryValueConversionException e) {
				throw new RuntimeException("No conversion possible", e);
			}
		}

		return DataBlockDescription.UNDEFINED;
	}
}
