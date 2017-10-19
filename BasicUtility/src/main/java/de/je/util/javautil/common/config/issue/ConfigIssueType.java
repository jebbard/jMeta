/**
 *
 * {@link ConfigIssueType}.java
 *
 * @author Jens Ebert
 *
 * @date 02.07.2011
 */
package de.je.util.javautil.common.config.issue;

import de.je.util.javautil.common.extenum.AbstractExtensibleEnum;

/**
 * {@link ConfigIssueType} represents the kinds of {@link ConfigIssue}s that might appear.
 */
public class ConfigIssueType extends AbstractExtensibleEnum<ConfigIssueType> {

   /**
    * Creates a new {@link ConfigIssueType}.
    * 
    * @param id
    *           The unique id of the {@link ConfigIssueType}.
    */
   protected ConfigIssueType(String id) {
      super(id);
   }

   /**
    * Exception during loading of the configuration file.
    */
   public final static ConfigIssueType CONFIG_FILE_LOADING = new ConfigIssueType("Config file loading");
   /**
    * A given configuration parameter is unknown to its loader.
    */
   public final static ConfigIssueType UNKNOWN_PARAMETER = new ConfigIssueType("Unknown param");
   /**
    * A parameter required for a loader is not present in the data stream.
    */
   public final static ConfigIssueType MISSING_PARAMETER = new ConfigIssueType("Missing param");
   /**
    * A parameter required for a loader is present in the data stream but has an empty value.
    */
   public final static ConfigIssueType EMPTY_PARAMETER = new ConfigIssueType("Empty param");
   /**
    * A parameter's value is smaller then minimum or bigger than maximum value.
    */
   public final static ConfigIssueType PARAMETER_VALUE_OUT_OF_BOUNDS = new ConfigIssueType("Param value out of bounds");
   /**
    * A parameter's value is not in the possible enumerated values for its parameter.
    */
   public final static ConfigIssueType NON_ENUMERATED_PARAMETER_VALUE = new ConfigIssueType(
      "Param value is not in enumeration of valid values");
   /**
    * A parameter's value could not be converted to its typed representation.
    */
   public final static ConfigIssueType PARAMETER_VALUE_CONVERSION_FAILED = new ConfigIssueType(
      "Param value conversion failed");
}
