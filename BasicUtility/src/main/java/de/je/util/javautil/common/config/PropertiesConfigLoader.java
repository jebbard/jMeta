/**
 *
 * {@link PropertiesConfigLoader}.java
 *
 * @author Jens Ebert
 *
 * @date 02.07.2011
 */
package de.je.util.javautil.common.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

/**
 * {@link PropertiesConfigLoader} loads configuration parameters from properties files.
 */
public class PropertiesConfigLoader extends AbstractConfigLoader {

   /**
    * Creates a new {@link PropertiesConfigLoader}.
    * 
    * @param configParameterSet
    *           A set of {@link AbstractConfigParam}s that can be loaded by this {@link PropertiesConfigLoader}.
    */
   public PropertiesConfigLoader(Set<AbstractConfigParam<?>> configParameterSet) {
      super(configParameterSet);
   }

   /**
    * @see AbstractConfigLoader#getConfigFromStream
    */
   @Override
   protected Map<String, String> getConfigFromStream(InputStream stream) throws IOException {
      Map<String, String> returnedMap = new HashMap<>();

      Properties properties = new Properties();

      properties.load(stream);

      for (Iterator<Object> iterator = properties.keySet().iterator(); iterator.hasNext();) {
         String nextKey = iterator.next().toString();
         String nextValue = properties.getProperty(nextKey);

         returnedMap.put(nextKey, nextValue);
      }

      return returnedMap;
   }
}
