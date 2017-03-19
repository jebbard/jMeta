package de.je.jmeta.datablocks.impl;

import java.util.Set;

import de.je.jmeta.dataformats.DataBlockId;
import de.je.jmeta.dataformats.FieldType;

/**
 * {@link ICustomFieldValueParser} represents a class that can parse a fields expected value from a given string and
 * convert it to an arbitrary data type.
 *
 * This is necessary for field types {@link FieldType#ENUMERATED} and {@link FieldType#ANY}.
 */
public interface ICustomFieldValueParser {

   /**
    * Returns the {@link DataBlockId}s for which this {@link ICustomFieldValueParser} must be used.
    *
    * @return the {@link DataBlockId}s for which this {@link ICustomFieldValueParser} must be used.
    */
   public Set<DataBlockId> getFieldIds();

   /**
    * Returns an {@link Object} of arbitrary data type that has been parsed from the given string representation.
    *
    * @param fieldValueAsString
    *           The string representation of the field value that is to be converted to an arbitray typed {@link Object}
    *           .
    *
    * @return an {@link Object} of arbitrary data type that has been parsed from the given string representation.
    */
   public Object parse(String fieldValueAsString);
}
