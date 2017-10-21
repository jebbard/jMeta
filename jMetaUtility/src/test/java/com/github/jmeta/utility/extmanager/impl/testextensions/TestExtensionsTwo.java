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

import com.github.jmeta.utility.extmanager.api.services.IExtension;
import com.github.jmeta.utility.extmanager.api.types.ExtensionDescription;

/**
 * {@link TestExtensionsTwo} is just an example extension.
 */
public class TestExtensionsTwo implements IExtension {

   /**
    * @see com.github.jmeta.utility.extmanager.api.services.IExtension#getExtensionId()
    */
   @Override
   public String getExtensionId() {
      return "b";
   }

   /**
    * @see com.github.jmeta.utility.extmanager.api.services.IExtension#getExtensionDescription()
    */
   @Override
   public ExtensionDescription getExtensionDescription() {
      return new ExtensionDescription("Test", "Test", "version", LocalDateTime.now(), "My ext", "Copy", "License");
   }

   /**
    * @see com.github.jmeta.utility.extmanager.api.services.IExtension#getAllServiceProviders(java.lang.Class)
    */
   @SuppressWarnings("unchecked")
   @Override
   public <T> List<T> getAllServiceProviders(Class<T> serviceInterface) {

      ArrayList<T> returnedProviders = new ArrayList<T>();

      if (serviceInterface == ITestExtensionServiceOne.class) {
         returnedProviders.add((T) new ExtensionTwoServiceProviderOne());
      } else if (serviceInterface == ITestExtensionServiceTwo.class) {
         returnedProviders.add((T) new ExtensionTwoServiceProviderTwo());
      }

      return returnedProviders;
   }

}
