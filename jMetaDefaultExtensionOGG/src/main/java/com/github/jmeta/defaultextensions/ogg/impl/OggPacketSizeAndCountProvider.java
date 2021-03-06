/**
 *
 * {@link OggPacketSizeAndCountProvider}.java
 *
 * @author Jens Ebert
 *
 * @date 02.03.2019
 *
 */
package com.github.jmeta.defaultextensions.ogg.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.github.jmeta.library.datablocks.api.exceptions.BinaryValueConversionException;
import com.github.jmeta.library.datablocks.api.services.CountProvider;
import com.github.jmeta.library.datablocks.api.services.SizeProvider;
import com.github.jmeta.library.datablocks.api.types.ContainerContext;
import com.github.jmeta.library.datablocks.api.types.Field;
import com.github.jmeta.library.datablocks.api.types.Header;
import com.github.jmeta.library.dataformats.api.types.DataBlockDescription;
import com.github.jmeta.library.dataformats.api.types.DataBlockId;

/**
 * {@link OggPacketSizeAndCountProvider} implements the very special ogg way of
 * determining the size of a packet and the number of segments it contains.
 */
public class OggPacketSizeAndCountProvider implements SizeProvider, CountProvider {

	/**
	 * @see com.github.jmeta.library.datablocks.api.services.CountProvider#getCountOf(com.github.jmeta.library.dataformats.api.types.DataBlockId,
	 *      com.github.jmeta.library.datablocks.api.types.ContainerContext)
	 */
	@Override
	public long getCountOf(DataBlockId id, ContainerContext containerContext) {
		if (id.equals(OggExtension.REF_OGG_SEGMENT.getId())) {
			return getSegmentSizesForPacket(containerContext).stream().filter(s -> s > 0).count();
		}

		return DataBlockDescription.UNDEFINED;
	}

	private List<Long> getSegmentSizesForPacket(ContainerContext containerContext) {
		Header oggPageHeader = containerContext.getParentContainerContext().getContainer().getHeaders().get(0);

		List<List<Long>> segmentSizesPerPacket = getSegmentSizesPerPacket(oggPageHeader);

		int containerSequenceNumber = containerContext.getContainer().getSequenceNumber();

		return segmentSizesPerPacket.get(containerSequenceNumber);
	}

	private List<List<Long>> getSegmentSizesPerPacket(Header oggPageHeader) {
		List<List<Long>> segmentSizesPerPacket = new ArrayList<>();

		List<Long> currentPacketSegmentSizes = new ArrayList<>();

		// Ogg segment sizes start with ogg page header field with index 8
		for (int fieldIndex = 8; fieldIndex < oggPageHeader.getFields().size(); ++fieldIndex) {
			Field<?> segmentTableEntry = oggPageHeader.getFields().get(fieldIndex);

			try {
				long segmentSize = (Long) segmentTableEntry.getInterpretedValue();

				if ((segmentSize < 0xFF)
					|| ((segmentSize == 0xFF) && (fieldIndex == (oggPageHeader.getFields().size() - 1)))) {
					currentPacketSegmentSizes.add(segmentSize);
					segmentSizesPerPacket.add(currentPacketSegmentSizes);
					currentPacketSegmentSizes = new ArrayList<>();
				} else {
					currentPacketSegmentSizes.add(segmentSize);
				}
			} catch (BinaryValueConversionException e) {
				throw new RuntimeException("Unexpected field conversion exception", e);
			}
		}

		return segmentSizesPerPacket;
	}

	/**
	 * @see com.github.jmeta.library.datablocks.api.services.SizeProvider#getSizeOf(com.github.jmeta.library.dataformats.api.types.DataBlockId,
	 *      int, com.github.jmeta.library.datablocks.api.types.ContainerContext)
	 */
	@Override
	public long getSizeOf(DataBlockId id, int sequenceNumber, ContainerContext containerContext) {
		if (id.equals(OggExtension.REF_OGG_PAYLOAD.getId())) {
			Header oggPageHeader = containerContext.getContainer().getHeaders().get(0);

			List<List<Long>> segmentSizesPerPacket = getSegmentSizesPerPacket(oggPageHeader);
			return segmentSizesPerPacket.stream().flatMap(segmentSizesForPacket -> segmentSizesForPacket.stream())
				.collect(Collectors.summingLong(size -> size));
		}

		if (id.equals(OggExtension.REF_OGG_PACKET_PAYLOAD.getId())) {
			return getSegmentSizesForPacket(containerContext).stream().collect(Collectors.summingLong(size -> size));
		}
		if (id.equals(OggExtension.REF_OGG_SEGMENT.getId())) {
			return getSegmentSizesForPacket(containerContext).get(sequenceNumber);
		}

		return DataBlockDescription.UNDEFINED;
	}
}
