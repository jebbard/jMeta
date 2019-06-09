/**
 *
 * {@link SizeOf}.java
 *
 * @author Jens Ebert
 *
 * @date 25.02.2019
 *
 */
package com.github.jmeta.library.dataformats.api.types;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.github.jmeta.library.dataformats.api.exceptions.InvalidSpecificationException;
import com.github.jmeta.library.dataformats.api.services.DataFormatSpecification;

/**
 * {@link SummedSizeOf} is a field function expressing that the field it refers
 * to contains the summed size of two or more consecutive other data blocks.
 */
public class SummedSizeOf extends SizeOf {

	/**
	 * Creates a new {@link SummedSizeOf} field function.
	 *
	 * @param referencedBlocks The referenced {@link DataBlockCrossReference}s, must
	 *                         not be null and must at least contain one entry
	 */
	public SummedSizeOf(DataBlockCrossReference... referencedBlocks) {
		super(referencedBlocks);
	}

	private void ensureAtMostOneTargetBlockHasNoSize(DataBlockDescription fieldDesc, DataFormatSpecification spec,
		List<DataBlockId> targetDataBlockIds) {
		Map<DataBlockId, List<AbstractFieldFunction<?>>> fieldFunctionsByTargetId = spec
			.getAllFieldFunctionsByTargetId();

		Set<DataBlockId> targetBlocksWithoutSize = new HashSet<>();

		for (DataBlockId targetId : targetDataBlockIds) {
			DataBlockDescription targetDescc = spec.getDataBlockDescription(targetId);

			boolean hasSuitableFieldFunction = fieldFunctionsByTargetId.containsKey(targetId)
				&& fieldFunctionsByTargetId.get(targetId).stream().anyMatch(ff -> ff.getClass().equals(SizeOf.class));

			if (!targetDescc.hasFixedSize() && !hasSuitableFieldFunction) {
				targetBlocksWithoutSize.add(targetId);
			}
		}

		if (targetBlocksWithoutSize.size() > 1) {
			throw new InvalidSpecificationException(InvalidSpecificationException.VLD_FIELD_FUNC_INVALID_SUMMED_SIZE,
				fieldDesc, targetBlocksWithoutSize);
		}
	}

	private void ensureConsecutiveTargetBlocks(DataBlockDescription fieldDesc, DataFormatSpecification spec,
		List<DataBlockId> targetDataBlockIds) {

		List<DataBlockId> distinctParents = targetDataBlockIds.stream().map(targetId -> targetId.getParentId())
			.distinct().collect(Collectors.toList());

		if (distinctParents.size() > 1) {
			throw new InvalidSpecificationException(
				InvalidSpecificationException.VLD_FIELD_FUNC_SUMMED_SIZE_DIFFERENT_PARENT, fieldDesc,
				targetDataBlockIds, distinctParents);
		}

		DataBlockDescription parentDesc = spec.getDataBlockDescription(distinctParents.get(0));

		Iterator<DataBlockId> targetIdIterator = targetDataBlockIds.iterator();

		List<DataBlockDescription> children = getChildrenStartingWithId(parentDesc, targetIdIterator.next());

		Iterator<DataBlockDescription> childIterator = children.iterator();

		// Skip the first child itself
		childIterator.next();

		while (targetIdIterator.hasNext()) {
			if (!childIterator.hasNext() || !childIterator.next().getId().equals(targetIdIterator.next())) {
				throw new InvalidSpecificationException(
					InvalidSpecificationException.VLD_FIELD_FUNC_SUMMED_SIZE_NON_SIBLING_CHILDREN, fieldDesc,
					targetDataBlockIds, parentDesc.getId(),
					children.stream().map(DataBlockDescription::getId).collect(Collectors.toList()));
			}
		}
	}

	private List<DataBlockDescription> getChildrenStartingWithId(DataBlockDescription parentDesc, DataBlockId firstId) {
		List<DataBlockDescription> children = parentDesc.getOrderedChildren();

		int childIndexWithId = children.stream().map(DataBlockDescription::getId).collect(Collectors.toList())
			.indexOf(firstId);

		if (childIndexWithId == -1) {
			throw new IllegalStateException(
				"Child id of parent is not in list of child DataBlockDescription which must never happen");
		}

		return children.subList(childIndexWithId, children.size());
	}

	/**
	 * @see com.github.jmeta.library.dataformats.api.types.AbstractFieldFunction#validate(com.github.jmeta.library.dataformats.api.types.DataBlockDescription,
	 *      com.github.jmeta.library.dataformats.api.services.DataFormatSpecification)
	 */
	@Override
	public void validate(DataBlockDescription fieldDesc, DataFormatSpecification spec) {
		super.validate(fieldDesc, spec);

		List<DataBlockId> targetDataBlockIds = getReferencedBlocks().stream().map(DataBlockCrossReference::getId)
			.collect(Collectors.toList());

		ensureAtMostOneTargetBlockHasNoSize(fieldDesc, spec, targetDataBlockIds);
		ensureConsecutiveTargetBlocks(fieldDesc, spec, targetDataBlockIds);
	}

	/**
	 * @see com.github.jmeta.library.dataformats.api.types.SizeOf#withReplacedReferences(com.github.jmeta.library.dataformats.api.types.DataBlockCrossReference[])
	 */
	@Override
	public AbstractFieldFunction<Long> withReplacedReferences(DataBlockCrossReference... replacedReferences) {
		return new SummedSizeOf(replacedReferences);
	}
}
