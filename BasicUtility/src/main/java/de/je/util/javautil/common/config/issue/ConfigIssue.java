package de.je.util.javautil.common.config.issue;

import de.je.util.javautil.common.config.AbstractConfigParam;
import de.je.util.javautil.common.err.Reject;

/**
 * {@link ConfigIssue} represents an issue regarding the value of a defined {@link AbstractConfigParam}. These
 * {@link ConfigIssue}s appear when validating or converting the string value of such a parameter.
 */
public class ConfigIssue {

   /**
    * Creates a new {@link ConfigIssue}.
    * 
    * @param type
    *           The {@link ConfigIssueType}.
    * @param param
    *           The parameter or null if no parameter is available for the given string id.
    * @param paramId
    *           The parameter's id or null if unknown.
    * @param paramStringValue
    *           The parameter's string value or null if unknown.
    * @param paramConvertedValue
    *           The parameter's converted value or null if unknown.
    * @param exception
    *           An {@link Exception} related to this {@link ConfigIssue} or null if there is none.
    */
   public ConfigIssue(ConfigIssueType type, AbstractConfigParam<?> param, String paramId, String paramStringValue,
      Object paramConvertedValue, Exception exception) {
      Reject.ifNull(type, "type");

      m_type = type;
      m_param = param;
      m_paramId = paramId;
      m_paramConvertedValue = paramConvertedValue;
      m_paramStringValue = paramStringValue;
      m_exception = exception;
   }

   /**
    * Returns the {@link ConfigIssueType}.
    *
    * @return the {@link ConfigIssueType}.
    */
   public ConfigIssueType getType() {
      return m_type;
   }

   /**
    * Returns the parameter's string value or null if unknown.
    *
    * @return the parameter's string value or null if unknown.
    */
   public AbstractConfigParam<?> getParam() {
      return m_param;
   }

   /**
    * Returns the parameter's converted value or null if unknown.
    *
    * @return the parameter's converted value or null if unknown.
    */
   public String getParamStringValue() {
      return m_paramStringValue;
   }

   /**
    * Returns the parameter's converted value or null if unknown.
    *
    * @return the parameter's converted value or null if unknown.
    */
   public Object getParamConvertedValue() {
      return m_paramConvertedValue;
   }

   /**
    * Returns an {@link Exception} related to this {@link ConfigIssue} or null if there is none.
    *
    * @return an {@link Exception} related to this {@link ConfigIssue} or null if there is none.
    */
   public Exception getException() {
      return m_exception;
   }

   /**
    * Returns the parameters id or null if unknown.
    *
    * @return the parameters id or null if unknown.
    */
   public String getParamId() {
      return m_paramId;
   }

   /**
    * @see java.lang.Object#toString()
    */
   @Override
   public String toString() {
      return "ConfigIssue [m_type=" + m_type + ", m_param=" + m_param + ", m_paramId=" + m_paramId
         + ", m_paramStringValue=" + m_paramStringValue + ", m_paramConvertedValue=" + m_paramConvertedValue
         + ", m_exception=" + m_exception + "]";
   }

   /**
    * @see java.lang.Object#hashCode()
    */
   @Override
   public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + ((m_exception == null) ? 0 : m_exception.hashCode());
      result = prime * result + ((m_param == null) ? 0 : m_param.hashCode());
      result = prime * result + ((m_paramConvertedValue == null) ? 0 : m_paramConvertedValue.hashCode());
      result = prime * result + ((m_paramId == null) ? 0 : m_paramId.hashCode());
      result = prime * result + ((m_paramStringValue == null) ? 0 : m_paramStringValue.hashCode());
      result = prime * result + ((m_type == null) ? 0 : m_type.hashCode());
      return result;
   }

   /**
    * @see java.lang.Object#equals(java.lang.Object)
    *
    *      This method does only compare the exception classes rather than their detailed contents.
    */
   @Override
   public boolean equals(Object obj) {
      if (this == obj)
         return true;
      if (obj == null)
         return false;
      if (getClass() != obj.getClass())
         return false;
      ConfigIssue other = (ConfigIssue) obj;
      if (m_param == null) {
         if (other.m_param != null)
            return false;
      } else if (!m_param.equals(other.m_param))
         return false;
      if (m_paramConvertedValue == null) {
         if (other.m_paramConvertedValue != null)
            return false;
      } else if (!m_paramConvertedValue.equals(other.m_paramConvertedValue))
         return false;
      if (m_paramId == null) {
         if (other.m_paramId != null)
            return false;
      } else if (!m_paramId.equals(other.m_paramId))
         return false;
      if (m_paramStringValue == null) {
         if (other.m_paramStringValue != null)
            return false;
      } else if (!m_paramStringValue.equals(other.m_paramStringValue))
         return false;
      if (m_type == null) {
         if (other.m_type != null)
            return false;
      } else if (!m_type.equals(other.m_type))
         return false;
      if (m_exception == null) {
         if (other.m_exception != null)
            return false;
      } else if (!m_exception.getClass().equals(other.m_exception.getClass()))
         return false;
      return true;
   }

   private final ConfigIssueType m_type;

   private final AbstractConfigParam<?> m_param;
   private final String m_paramId;
   private final String m_paramStringValue;
   private final Object m_paramConvertedValue;
   private final Exception m_exception;
}
