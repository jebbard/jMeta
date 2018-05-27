/**
 *
 * {@link FieldBasedPayloadBuilder}.java
 *
 * @author Jens Ebert
 *
 * @date 12.05.2018
 *
 */
package com.github.jmeta.library.dataformats.api.services.builder;

/**
 * {@link FieldBasedPayloadBuilder}
 *
 */
public interface FieldBasedPayloadBuilder<P extends ContainerSequenceBuilder<P>>
   extends FieldSequenceBuilder<FieldBasedPayloadBuilder<P>> {

   FieldBasedPayloadBuilder<P> withDescription(String name, String description);

   ContainerBuilder<P, FieldBasedPayloadBuilder<P>> finishFieldBasedPayload();
}
