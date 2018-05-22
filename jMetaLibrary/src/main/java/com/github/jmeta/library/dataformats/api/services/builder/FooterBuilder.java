/**
 *
 * {@link HeaderBuilder}.java
 *
 * @author Jens Ebert
 *
 * @date 12.05.2018
 *
 */
package com.github.jmeta.library.dataformats.api.services.builder;

/**
 * {@link FooterBuilder}
 *
 */
public interface FooterBuilder<PayloadBuilder> extends FieldSequenceBuilder<FooterBuilder<PayloadBuilder>> {

   ContainerBuilder<PayloadBuilder> finishFooter();
}
