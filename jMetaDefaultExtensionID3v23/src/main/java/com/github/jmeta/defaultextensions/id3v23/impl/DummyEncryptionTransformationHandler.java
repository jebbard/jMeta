package com.github.jmeta.defaultextensions.id3v23.impl;

/**
 * {@link DummyEncryptionTransformationHandler}
 *
 */
public class DummyEncryptionTransformationHandler {
   // REMINDER: Such an implementation first must read the encryption method
   // and based on that call the "other" handler, who does the encryption
   // declared by the method byte.
   // Due to compatibility reasons, a direct support of multiple encryption
   // handlers has been INTENTIONALLY avoided! This is a rare special cases,
   // that needs to be and can be covered by a corresponding implementation.
}
