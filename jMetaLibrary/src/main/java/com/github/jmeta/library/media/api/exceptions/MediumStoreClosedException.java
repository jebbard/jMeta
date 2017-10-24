/**
 *
 * {@link MediumStoreClosedException}.java
 *
 * @author Jens Ebert
 *
 * @date 16.10.2017
 *
 */
package com.github.jmeta.library.media.api.exceptions;

import com.github.jmeta.library.media.api.services.MediumStore;

/**
 * {@link MediumStoreClosedException} is thrown whenever an access method of an {@link MediumStore} is used on an
 * already closed {@link MediumStore} which is not allowed.
 */
public class MediumStoreClosedException extends RuntimeException {

   private static final long serialVersionUID = 7481214216659870533L;

}
