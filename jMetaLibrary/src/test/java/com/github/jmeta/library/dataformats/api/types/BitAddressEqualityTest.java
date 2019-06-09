/**
 *
 * {@link BitAddressEqualityTest}.java
 *
 * @author Jens Ebert
 *
 * @date 09.01.2009
 *
 */
package com.github.jmeta.library.dataformats.api.types;

import java.util.ArrayList;
import java.util.List;

import com.github.jmeta.utility.equalstest.api.services.AbstractEqualsTest;

/**
 * {@link BitAddressEqualityTest}
 *
 */
public class BitAddressEqualityTest extends AbstractEqualsTest<BitAddress> {

	private List<BitAddress> objects;
	private List<BitAddress> equalObjects;
	private List<BitAddress> thirdEqualObjects;
	private List<BitAddress> differentObjects;

	/**
	 * @see com.github.jmeta.utility.equalstest.api.services.AbstractEqualsTest#getDifferentObjects()
	 */
	@Override
	protected List<BitAddress> getDifferentObjects() {
		if (differentObjects == null) {
			differentObjects = new ArrayList<>();

			differentObjects.add(new BitAddress(0, 0));
			differentObjects.add(new BitAddress(6, 0));
			differentObjects.add(new BitAddress(200, 1));
		}

		return differentObjects;
	}

	/**
	 * @see com.github.jmeta.utility.equalstest.api.services.AbstractEqualsTest#getEqualObjects()
	 */
	@Override
	protected List<BitAddress> getEqualObjects() {
		if (equalObjects == null) {
			equalObjects = new ArrayList<>();

			equalObjects.add(new BitAddress(6, 0));
			equalObjects.add(new BitAddress(7, 0));
			equalObjects.add(new BitAddress(200, 0));
		}

		return equalObjects;
	}

	/**
	 * @see com.github.jmeta.utility.equalstest.api.services.AbstractEqualsTest#getObjects()
	 */
	@Override
	protected List<BitAddress> getObjects() {
		if (objects == null) {
			objects = new ArrayList<>();

			objects.add(new BitAddress(6, 0));
			objects.add(new BitAddress(7, 0));
			objects.add(new BitAddress(200, 0));
		}

		return objects;
	}

	/**
	 * @see com.github.jmeta.utility.equalstest.api.services.AbstractEqualsTest#getThirdEqualObjects()
	 */
	@Override
	protected List<BitAddress> getThirdEqualObjects() {
		if (thirdEqualObjects == null) {
			thirdEqualObjects = new ArrayList<>();

			thirdEqualObjects.add(new BitAddress(6, 0));
			thirdEqualObjects.add(new BitAddress(7, 0));
			thirdEqualObjects.add(new BitAddress(200, 0));
		}

		return thirdEqualObjects;
	}
}
