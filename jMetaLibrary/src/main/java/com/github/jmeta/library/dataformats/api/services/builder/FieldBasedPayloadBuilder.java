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
public interface FieldBasedPayloadBuilder extends FieldSequenceBuilder<FieldBasedPayloadBuilder> {

   FieldBasedPayloadBuilder withDescription(String name, String description);

   ContainerBuilder<FieldBasedPayloadBuilder> finishFieldBasedPayload();
}
