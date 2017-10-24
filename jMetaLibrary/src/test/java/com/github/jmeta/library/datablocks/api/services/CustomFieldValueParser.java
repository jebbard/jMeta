package com.github.jmeta.library.datablocks.api.services;

import java.util.Set;

import com.github.jmeta.library.dataformats.api.types.DataBlockId;
import com.github.jmeta.library.dataformats.api.types.FieldType;

/**
 * {@link CustomFieldValueParser} represents a class that can parse a fields expected value from a given string and
 * convert it to an arbitrary data type.
 *
 * This is necessary for field types {@link FieldType#ENUMERATED} and {@link FieldType#ANY}.
 */
public interface CustomFieldValueParser {

   /**
    * Returns the {@link DataBlockId}s for which this {@link CustomFieldValueParser} must be used.
    *
    * @return the {@link DataBlockId}s for which this {@link CustomFieldValueParser} must be used.
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
