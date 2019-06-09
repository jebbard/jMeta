package com.github.jmeta.library.datablocks.api.services;

import com.github.jmeta.library.datablocks.api.types.DataBlock;
import com.github.jmeta.library.dataformats.api.types.DataBlockId;
import com.github.jmeta.library.dataformats.api.types.PhysicalDataBlockType;
import com.github.jmeta.library.media.api.types.MediumOffset;
import com.github.jmeta.utility.dbc.api.services.Reject;

/**
 * {@link DataBlockInstanceId} represents and uniquely determines a single
 * occurrence of a {@link DataBlock} with a given {@link DataBlockId} in the
 * current medium.
 */
public class DataBlockInstanceId {

	private final DataBlockId m_id;

	private final DataBlockInstanceId m_parentInstanceId;

	private final int m_sequenceNumber;

	/**
	 * Creates a new {@link DataBlockInstanceId}.
	 * 
	 * @param id               the {@link DataBlockId} of the data block.
	 * @param parentInstanceId The {@link DataBlockInstanceId} of the parent data
	 *                         block or null if there is no parent and this
	 *                         {@link DataBlockInstanceId} refers to a top level
	 *                         block.
	 * @param sequenceNumber   A zero based number that corresponds to the absolute
	 *                         occurrence index of the block in its parent block.
	 *                         The first child block (regarding its
	 *                         {@link MediumOffset} and irrespective of its
	 *                         {@link DataBlockId} or {@link PhysicalDataBlockType})
	 *                         gets sequence number 0, the second one gets sequence
	 *                         number 1, and so on.
	 */
	public DataBlockInstanceId(DataBlockId id, DataBlockInstanceId parentInstanceId, int sequenceNumber) {
		Reject.ifNull(id, "id");
		Reject.ifNegative(sequenceNumber, "sequenceNumber");

		m_id = id;
		m_parentInstanceId = parentInstanceId;
		m_sequenceNumber = sequenceNumber;
	}

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
		DataBlockInstanceId other = (DataBlockInstanceId) obj;
		if (m_id == null) {
			if (other.m_id != null) {
				return false;
			}
		} else if (!m_id.equals(other.m_id)) {
			return false;
		}
		if (m_parentInstanceId == null) {
			if (other.m_parentInstanceId != null) {
				return false;
			}
		} else if (!m_parentInstanceId.equals(other.m_parentInstanceId)) {
			return false;
		}
		if (m_sequenceNumber != other.m_sequenceNumber) {
			return false;
		}
		return true;
	}

	/**
	 * Returns the {@link DataBlockId} of this {@link DataBlockInstanceId}.
	 *
	 * @return the {@link DataBlockId} of this {@link DataBlockInstanceId}.
	 */
	public DataBlockId getId() {

		return m_id;
	}

	/**
	 * Returns the {@link DataBlockInstanceId} of the parent data block or null if
	 * there is no parent and this {@link DataBlockInstanceId} refers to a top level
	 * block.
	 *
	 * @return the {@link DataBlockInstanceId} of the parent data block or null if
	 *         there is no parent and this {@link DataBlockInstanceId} refers to a
	 *         top level block.
	 */
	public DataBlockInstanceId getParentInstanceId() {

		return m_parentInstanceId;
	}

	/**
	 * Returns the sequence number of this {@link DataBlockInstanceId}. The sequence
	 * number is a zero based number that corresponds to the absolute occurrence
	 * index of the block in its parent block. The first child block (regarding its
	 * {@link MediumOffset} and irrespective of its {@link DataBlockId} or
	 * {@link PhysicalDataBlockType}) gets sequence number 0, the second one gets
	 * sequence number 1, and so on.
	 *
	 * @return the sequence number of this {@link DataBlockInstanceId}.
	 */
	public int getSequenceNumber() {

		return m_sequenceNumber;
	}

	@Override
	public int hashCode() {

		final int prime = 31;
		int result = 1;
		result = (prime * result) + ((m_id == null) ? 0 : m_id.hashCode());
		result = (prime * result) + ((m_parentInstanceId == null) ? 0 : m_parentInstanceId.hashCode());
		result = (prime * result) + m_sequenceNumber;
		return result;
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {

		return "DataBlockInstanceId [m_id=" + m_id + ", m_parentInstanceId=" + m_parentInstanceId
			+ ", m_sequenceNumber=" + m_sequenceNumber + "]";
	}
}
