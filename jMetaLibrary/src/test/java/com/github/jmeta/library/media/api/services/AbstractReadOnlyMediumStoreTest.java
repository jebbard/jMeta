/**
 *
 * {@link AbstractReadOnlyMediumStoreTest}.java
 *
 * @author Jens Ebert
 *
 * @date 31.10.2017
 *
 */
package com.github.jmeta.library.media.api.services;

import com.github.jmeta.library.media.api.types.Medium;

/**
 * {@link AbstractReadOnlyMediumStoreTest} contains negative tests for {@link MediumStore} write method on read-only
 * {@link Medium} instances.
 *
 * @param <T>
 *           The type of {@link Medium} to test
 */
public abstract class AbstractReadOnlyMediumStoreTest<T extends Medium<?>> extends AbstractMediumStoreTest<T> {
}
