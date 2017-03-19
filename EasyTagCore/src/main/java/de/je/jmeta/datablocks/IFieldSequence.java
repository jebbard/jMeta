/**
 *
 * {@link IFieldSequence}.java
 *
 * @author jebert
 *
 * @date 25.06.2011
 */
package de.je.jmeta.datablocks;

import java.util.List;

/**
 * {@link IFieldSequence}
 *
 */
public interface IFieldSequence {

   /**
    * Returns the {@link List} of {@link IField}s belonging to this {@link IPayload}. The returned {@link List} might be
    * empty if the {@link IPayload} does not contain {@link IField}s as children.
    *
    * @return the {@link List} of {@link IField}s that build this {@link IPayload}. Might return an empty {@link List}
    *         if there are no child {@link IField}s.
    */
   List<IField<?>> getFields();
}
