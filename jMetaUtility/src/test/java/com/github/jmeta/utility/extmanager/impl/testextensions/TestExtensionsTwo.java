/**
 *
 * {@link TestExtensionsOne}.java
 *
 * @author Jens Ebert
 *
 * @date 15.10.2017
 *
 */
package com.github.jmeta.utility.extmanager.impl.testextensions;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.github.jmeta.utility.extmanager.api.services.Extension;
import com.github.jmeta.utility.extmanager.api.types.ExtensionDescription;

/**
 * {@link TestExtensionsTwo} is just an example extension.
 */
public class TestExtensionsTwo implements Extension {

   /**
    * @see com.github.jmeta.utility.extmanager.api.services.Extension#getExtensionId()
    */
   @Override
   public String getExtensionId() {
      return "b";
   }

   /**
    * @see com.github.jmeta.utility.extmanager.api.services.Extension#getExtensionDescription()
    */
   @Override
   public ExtensionDescription getExtensionDescription() {
      return new ExtensionDescription("Test", "Test", "version", LocalDateTime.now(), "My ext", "Copy", "License");
   }

   /**
    * @see com.github.jmeta.utility.extmanager.api.services.Extension#getAllServiceProviders(java.lang.Class)
    */
   @SuppressWarnings("unchecked")
   @Override
   public <T> List<T> getAllServiceProviders(Class<T> serviceInterface) {

      ArrayList<T> returnedProviders = new ArrayList<T>();

      if (serviceInterface == TestExtensionServiceOne.class) {
         returnedProviders.add((T) new ExtensionTwoServiceProviderOne());
      } else if (serviceInterface == TestExtensionServiceTwo.class) {
         returnedProviders.add((T) new ExtensionTwoServiceProviderTwo());
      }

      return returnedProviders;
   }

}
