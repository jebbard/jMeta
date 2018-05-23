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
 * {@link HeaderBuilder}
 *
 */
public interface HeaderBuilder<PB> extends FieldSequenceBuilder<HeaderBuilder<PB>> {

   ContainerBuilder<PB> finishHeader();
}
