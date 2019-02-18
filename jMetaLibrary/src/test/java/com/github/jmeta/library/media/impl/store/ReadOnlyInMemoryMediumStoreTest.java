/**
 *
 * {@link ReadOnlyInMemoryMediumStoreTest}.java
 *
 * @author Jens Ebert
 *
 * @date 31.10.2017
 *
 */
package com.github.jmeta.library.media.impl.store;

import java.io.IOException;

import com.github.jmeta.library.media.api.services.AbstractReadOnlyMediumStoreTest;
import com.github.jmeta.library.media.api.services.MediumStore;
import com.github.jmeta.library.media.api.types.InMemoryMedium;
import com.github.jmeta.library.media.impl.mediumAccessor.MediumAccessor;
import com.github.jmeta.library.media.impl.mediumAccessor.InMemoryMediumAccessor;

/**
 * {@link ReadOnlyInMemoryMediumStoreTest} tests a {@link MediumStore} backed by {@link InMemoryMedium} instances.
 */
public class ReadOnlyInMemoryMediumStoreTest extends AbstractReadOnlyMediumStoreTest<InMemoryMedium> {

   /**
    * @see com.github.jmeta.library.media.api.services.AbstractMediumStoreTest#createEmptyMedium(java.lang.String)
    */
   @Override
   protected InMemoryMedium createMedium() throws IOException {
      return new InMemoryMedium(new byte[100], "In-memory medium", true);
   }

   /**
    * @see com.github.jmeta.library.media.api.services.AbstractMediumStoreTest#createMediumAccessor(com.github.jmeta.library.media.api.types.Medium)
    */
   @Override
   protected MediumAccessor<InMemoryMedium> createMediumAccessor(InMemoryMedium mediumToUse) {
      return new InMemoryMediumAccessor(mediumToUse);
   }
}
