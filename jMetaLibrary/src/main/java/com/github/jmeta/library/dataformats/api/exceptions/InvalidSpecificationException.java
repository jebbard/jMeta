/**
 *
 * {@link InvalidSpecificationException}.java
 *
 * @author Jens Ebert
 *
 * @date 04.06.2018
 *
 */
package com.github.jmeta.library.dataformats.api.exceptions;

import com.github.jmeta.library.dataformats.api.types.DataBlockDescription;
import com.github.jmeta.library.dataformats.api.types.FieldFunctionType;
import com.github.jmeta.library.dataformats.api.types.FieldType;
import com.github.jmeta.library.dataformats.api.types.PhysicalDataBlockType;
import com.github.jmeta.utility.dbc.api.services.Reject;

/**
 * {@link InvalidSpecificationException}
 *
 */
public class InvalidSpecificationException extends RuntimeException {

   // @formatter:off
   public static final String VLD_DEFAULT_NESTED_CONTAINER_MISSING = 
      "Each data format with nested containers must define a default nested container";
   public static final String VLD_INVALID_BYTE_ORDER = 
      "Field defines an unsupported byte order <%1$s>; supported byte orders: <%2$s>";
   public static final String VLD_INVALID_CHARACTER_ENCODING = 
      "Field defines an unsupported character encoding <%1$s>; supported character encodings: <%2$s>";

   public static final String VLD_INVALID_CHILDREN_FIELD = 
      "Data block typed as " + PhysicalDataBlockType.FIELD + " must not have any children";
   public static final String VLD_INVALID_CHILDREN_FIELD_SEQUENCE = 
      "Data block typed as " + PhysicalDataBlockType.HEADER + ", " + PhysicalDataBlockType.FOOTER + " or "
      + PhysicalDataBlockType.FIELD_BASED_PAYLOAD + " must only have fields as children";
   public static final String VLD_INVALID_CHILDREN_CONTAINER = 
      "Data block typed as " + PhysicalDataBlockType.CONTAINER + " must have exactly one payload";
   public static final String VLD_INVALID_CHILDREN_CONTAINER_BASED_PAYLOAD = 
      "Data block typed as " + PhysicalDataBlockType.CONTAINER_BASED_PAYLOAD + " must only have " 
      + PhysicalDataBlockType.CONTAINER + "s as children";
   
   public static final String VLD_FIELD_PROPERTIES_MISSING = 
      "Data block typed as " + PhysicalDataBlockType.FIELD + ", but its field properties are null";
   public static final String VLD_FIELD_PROPERTIES_UNNECESSARY = 
      "Data block not typed as " + PhysicalDataBlockType.FIELD + ", but it has non-null field properties";
   
   public static final String VLD_MAGIC_KEY_MISSING = 
      "Every lop-level container must define at least one magic key";
   public static final String VLD_MAGIC_KEY_UNKNOWN_OFFSET = 
      "Magic key field validation: There are variable size fields in front of or behind this magic key field";
   public static final String VLD_MAGIC_KEY_TOO_MANY = 
      "Multiple <%1$s> magic keys at different offsets specified for this container. At max one header or "
      + "footer magic key field is allowed. Magic key fields found: <%2$s>";
   public static final String VLD_MAGIC_KEY_BIT_LENGTH_TOO_BIG = 
      "Magic key field validation: Magic key bit length <%1$s> exceeds its binary value length of <%2$s> bits";
   public static final String VLD_MAGIC_KEY_INVALID_FIELD_LENGTH = 
      "Field is tagged as magic key, but it has a variable size: min length = <%1$s>, max length = <%1$s>";
   public static final String VLD_MAGIC_KEY_INVALID_FIELD_VALUE = 
      "Data block is tagged as magic key, but it has neither enumerated values nor a default value set";
   public static final String VLD_MAGIC_KEY_INVALID_FIELD_TYPE = 
      "Data block is tagged as magic key, but it has type " + FieldType.UNSIGNED_WHOLE_NUMBER + ". Magic key fields " + 
         "must have one of the types " + FieldType.BINARY + ", " + FieldType.STRING + " or " + FieldType.FLAGS;
   public static final String VLD_MAGIC_KEY_BIT_LENGTH_TOO_SMALL = 
      "Data block is tagged as magic key but its bit length set is zero or negative: <%1$s>";
   public static final String VLD_MAGIC_KEY_BIT_LENGTH_BIGGER_THAN_FIXED_SIZE = 
      "Data block is tagged as magic key but its bit length is bigger than its fixed size of <%1$s> bytes : <%2$s>";
   
   public static final String VLD_BINARY_ENUMERATED_VALUE_NOT_UNIQUE = 
      "Binary representation <%1$s> of enumerated interpreted value <%2$s> is not unique, it is already used " + 
      "for interpreted value <%3$s>";
   public static final String VLD_BINARY_ENUMERATED_VALUE_TOO_LONG = 
      "Binary representation of enmuerated value <%1$s> with length <%2$s> is longer than the field's fixed size " + 
      "which is <%3$s>";
   
   public static final String VLD_DEFAULT_VALUE_CONVERSION_FAILED = 
      "Default value <%1$s> could not be converted to binary";
   public static final String VLD_DEFAULT_VALUE_EXCEEDS_LENGTH = 
      "The length of the field's default value in binary (=<%1$s>) must not exceed the maximum " + 
      "byte length <%2$s> specified for the field";
   public static final String VLD_DEFAULT_VALUE_NOT_ENUMERATED = 
      "Default field value <%1$s> must be contained in list of enumerated values, but it is not";
   
   public static final String VLD_NUMERIC_FIELD_TOO_LONG = 
      "Field has " + FieldType.UNSIGNED_WHOLE_NUMBER + " type, but its minimum or maximum length is bigger than " + 
      "<%1$s>: min length = <%2$s>, max length = <%3$s>";
   public static final String VLD_FIXED_BYTE_ORDER_NON_NUMERIC = 
      "Field has not " + FieldType.UNSIGNED_WHOLE_NUMBER + " type, but a fixed byte order is defined for it";
   public static final String VLD_FIXED_CHARSET_NON_STRING = 
      "Field has not " + FieldType.STRING + " type, but a fixed character encoding is defined for it";
   public static final String VLD_TERMINATION_CHAR_NON_STRING = 
      "Field has not " + FieldType.STRING + " type, but a termincation character is defined for it";
   
   public static final String VLD_FIELD_FUNC_NON_STRING = 
      "Field is the id of, character encoding of or byte order of another data block, but it is not of type " + 
      FieldType.STRING;
   public static final String VLD_FIELD_FUNC_NON_NUMERIC = 
      "Field is the size of or count of another data block, but it is not of type " + FieldType.UNSIGNED_WHOLE_NUMBER;
   public static final String VLD_FIELD_FUNC_NON_FLAGS = 
      "Field indicates the presence of another data block, but it is not of type " + FieldType.UNSIGNED_WHOLE_NUMBER;
   public static final String VLD_FIELD_FUNC_PRESENCE_OF_MISSING_FIELDS = 
      "Field indicates the presence of another data block, but its flag name (=<%1$s>) or value (=<%2$s>) are not set";
   public static final String VLD_FIELD_FUNC_PRESENCE_OF_UNSPECIFIED_FLAG_NAME = 
      "Field indicates the presence of another data block, but its flag name <%1$s> is not specified in the field's " + 
      "flag specification";
   public static final String VLD_FIELD_FUNC_FLAG_PROPERTIES_UNNECESSARY = 
      "Field does not indicate the presence of another container, but either its flag name (=<%1$s>) or value " + 
      "(=<%2$s>) are nevertheless set";
   public static final String VLD_FIELD_FUNC_REFERENCING_WRONG_TYPE = 
      "Field function of type <%1$s> may only refer to data blocks of types <%2$s>, but referenced data block " + 
      "<%3$s> has type <%4$s>";
   public static final String VLD_FIELD_FUNC_OPTIONAL_FIELD_PRESENCE_OF_MISSING = 
      "For this optional data block, there is no other field defined with a " + FieldFunctionType.PRESENCE_OF + 
      " field function";
   public static final String VLD_FIELD_FUNC_DYN_OCCUR_FIELD_COUNT_OF_MISSING = 
      "For this data block with dynamic occurrences, there is no other field defined with a " + 
      FieldFunctionType.COUNT_OF + " field function";
   
   public static final String VLD_CONTAINER_HAS_OCCURRENCES = "Data block type as " + PhysicalDataBlockType.CONTAINER + 
      " must not have defined occurrences, as it by design may have arbitrary occurrences, but min occurrences = " + 
      "<%1$s> and max occurrences = <%2$s>";
   // @formatter:on

   private static final long serialVersionUID = -2426654412196106699L;

   private final DataBlockDescription desc;

   public InvalidSpecificationException(String message, DataBlockDescription desc, Throwable cause,
      Object... messageParams) {
      super(generatePrefix(desc) + String.format(message, messageParams), cause);

      this.desc = desc;
   }

   public InvalidSpecificationException(String message, DataBlockDescription desc, Object... messageParams) {
      this(message, desc, null, messageParams);
   }

   /**
    * Returns the attribute {@link #desc}.
    * 
    * @return the attribute {@link #desc}
    */
   public DataBlockDescription getDesc() {
      return desc;
   }

   private static String generatePrefix(DataBlockDescription desc) {
      Reject.ifNull(desc, "desc");
      return "Error validating data block description: [id=" + desc.getId().getGlobalId() + ", type="
         + desc.getPhysicalType() + "]: ";
   }
}
