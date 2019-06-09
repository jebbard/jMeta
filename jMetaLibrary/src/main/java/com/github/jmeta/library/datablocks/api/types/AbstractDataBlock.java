/**
 * {@link AbstractDataBlock}.java
 *
 * @author Jens Ebert
 * @date 31.12.10 19:47:08 (December 31, 2010)
 */

package com.github.jmeta.library.datablocks.api.types;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import com.github.jmeta.library.datablocks.impl.MediumDataProvider;
import com.github.jmeta.library.datablocks.impl.events.DataBlockEvent;
import com.github.jmeta.library.datablocks.impl.events.DataBlockEventBus;
import com.github.jmeta.library.datablocks.impl.events.DataBlockEventListener;
import com.github.jmeta.library.dataformats.api.services.DataFormatSpecification;
import com.github.jmeta.library.dataformats.api.types.DataBlockDescription;
import com.github.jmeta.library.dataformats.api.types.DataBlockId;
import com.github.jmeta.library.media.api.types.MediumOffset;
import com.github.jmeta.utility.dbc.api.services.Reject;

/**
 *
 */
public abstract class AbstractDataBlock implements DataBlock, DataBlockEventListener {

	private int sequenceNumber;
	private MediumDataProvider mediumDataProvider;
	private DataBlockState state;
	private DataBlockEventBus eventBus;

	private ContainerContext containerContext;

	private DataBlock m_parent;
	private MediumOffset m_mediumReference;
	private final DataBlockId m_id;
	private final DataFormatSpecification spec;

	public AbstractDataBlock(DataBlockId id, DataFormatSpecification spec) {
		Reject.ifNull(spec, "spec");
		Reject.ifNull(id, "id");

		m_id = id;
		this.spec = spec;
	}

	/**
	 * Creates a new {@link AbstractDataBlock}.
	 *
	 * @param id
	 * @param sequenceNumber     TODO
	 * @param offset
	 * @param parent
	 * @param mediumDataProvider
	 * @param containerContext   TODO
	 * @param state              TODO
	 * @param eventBus           TODO
	 */
	public AbstractDataBlock(DataBlockId id, int sequenceNumber, MediumOffset offset, DataBlock parent,
		MediumDataProvider mediumDataProvider, ContainerContext containerContext, DataBlockState state,
		DataBlockEventBus eventBus) {
		Reject.ifNull(id, "id");
		Reject.ifNull(mediumDataProvider, "dataBlockReader");
		Reject.ifNull(offset, "reference");
		Reject.ifNull(state, "state");
		Reject.ifNull(eventBus, "eventBus");
		Reject.ifNegative(sequenceNumber, "sequenceNumber");

		m_id = id;
		this.state = state;
		this.eventBus = eventBus;
		this.mediumDataProvider = mediumDataProvider;
		m_mediumReference = offset;
		this.sequenceNumber = sequenceNumber;
		spec = null;

		this.containerContext = containerContext;

		this.eventBus.registerListener(this);

		if (parent != null) {
			initParent(parent);
		}
	}

	public void attachToMedium(MediumOffset offset, int sequenceNumber, MediumDataProvider mediumDataProvider,
		DataBlockEventBus eventBus, DataBlockState attachedState) {
		Reject.ifNull(mediumDataProvider, "dataBlockReader");
		Reject.ifNull(offset, "reference");
		Reject.ifNull(eventBus, "eventBus");
		Reject.ifNull(attachedState, "attachedState");
		Reject.ifNegative(sequenceNumber, "sequenceNumber");

		this.eventBus = eventBus;
		this.mediumDataProvider = mediumDataProvider;
		m_mediumReference = offset;
		this.sequenceNumber = sequenceNumber;
		state = attachedState;
		getEventBus().registerListener((DataBlockEventListener) containerContext);

		this.eventBus.registerListener(this);
	}

	/**
	 * @see com.github.jmeta.library.datablocks.impl.events.DataBlockEventListener#dataBlockEventOccurred(com.github.jmeta.library.datablocks.impl.events.DataBlockEvent)
	 */
	@Override
	public void dataBlockEventOccurred(DataBlockEvent event) {
		// TODO implement
	}

	@Override
	public ByteBuffer getBytes(MediumOffset offset, int size) {
		Reject.ifNegative(size, "size");
		Reject.ifNull(offset, "offset");
		Reject.ifTrue(offset.before(getOffset()), "offset.before(getOffset())");

		if (getSize() != DataBlockDescription.UNDEFINED) {
			Reject.ifFalse(
				(offset.getAbsoluteMediumOffset() + size) <= (getOffset().getAbsoluteMediumOffset() + getSize()),
				"offset.getAbsoluteMediumOffset() + size <= getOffset().getAbsoluteMediumOffset() + getSize()");
		}

		return mediumDataProvider.getData(offset, size);
	}

	/**
	 * @see com.github.jmeta.library.datablocks.api.types.Container#getContainerContext()
	 */
	@Override
	public ContainerContext getContainerContext() {
		return containerContext;
	}

	/**
	 * Returns the attribute {@link #eventBus}.
	 *
	 * @return the attribute {@link #eventBus}
	 */
	public DataBlockEventBus getEventBus() {
		return eventBus;
	}

	/**
	 * @see com.github.jmeta.library.datablocks.api.types.DataBlock#getId()
	 */
	@Override
	public DataBlockId getId() {

		return m_id;
	}

	/**
	 * @see com.github.jmeta.library.datablocks.api.types.DataBlock#getOffset()
	 */
	@Override
	public MediumOffset getOffset() {

		return m_mediumReference;
	}

	/**
	 * @see com.github.jmeta.library.datablocks.api.types.DataBlock#getParent()
	 */
	@Override
	public DataBlock getParent() {

		return m_parent;
	}

	/**
	 * @see com.github.jmeta.library.datablocks.api.types.DataBlock#getSequenceNumber()
	 */
	@Override
	public int getSequenceNumber() {
		return sequenceNumber;
	}

	/**
	 * Returns the attribute {@link #spec}.
	 *
	 * @return the attribute {@link #spec}
	 */
	public DataFormatSpecification getSpec() {
		return spec;
	}

	/**
	 * @see com.github.jmeta.library.datablocks.api.types.DataBlock#getState()
	 */
	@Override
	public DataBlockState getState() {
		return state;
	}

	public void initContainerContext(ContainerContext containerContext) {
		Reject.ifNull(containerContext, "containerContext");

		this.containerContext = containerContext;
	}

	/**
	 * @see com.github.jmeta.library.datablocks.api.types.DataBlock#initParent(com.github.jmeta.library.datablocks.api.types.DataBlock)
	 */
	@Override
	public void initParent(DataBlock parent) {

		Reject.ifNull(parent, "parent");
		Reject.ifFalse(getParent() == null, "getParent() == null");

		m_parent = parent;
		initContainerContext(parent.getContainerContext());
	}

	public void initSequenceNumber(int sequenceNumber) {
		this.sequenceNumber = sequenceNumber;
	}

	/**
	 * @see com.github.jmeta.library.datablocks.api.types.DataBlock#setBytes(byte[][])
	 */
	@Override
	public void setBytes(byte[][] bytes) {

		List<ByteBuffer> cache = new ArrayList<>(bytes.length);

		for (int i = 0; i < bytes.length; i++) {
			cache.add(ByteBuffer.wrap(bytes[i]));
		}

		// m_mediumReference.setCache(cache);
	}

	/**
	 * Sets the attribute {@link #state}.
	 */
	protected void setState(DataBlockState state) {
		Reject.ifNull(state, "state");

		this.state = state;
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {

		return getClass().getName() + "[id=" + getId() + ", parentId="
			+ (getParent() == null ? getParent() : getParent().getId()) + ", medium=" + getOffset() + ", totalSize="
			+ getSize() + "]";
	}
}
