/**
 *
 * {@link SampleReadAllMediumContainers}.java
 *
 * @author Jens Ebert
 *
 * @date 27.08.2020
 *
 */
package com.github.jmeta.library.samples;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.function.Consumer;

import com.github.jmeta.library.datablocks.api.services.LowLevelAPI;
import com.github.jmeta.library.datablocks.api.services.MediumContainerIterator;
import com.github.jmeta.library.datablocks.api.types.Container;
import com.github.jmeta.library.media.api.types.FileMedium;
import com.github.jmeta.library.media.api.types.Medium;
import com.github.jmeta.library.media.api.types.MediumAccessType;
import com.github.jmeta.library.startup.api.services.LibraryJMeta;

/**
 * {@link SampleReadAllMediumContainers} demonstrates how to read and print all medium containers.
 */
public class SampleReadAllMediumContainers {

   public static void main(String[] args) {
      Medium<Path> medium = new FileMedium(Paths.get("/path/to/my/file.mp3"), MediumAccessType.READ_WRITE);
      SampleReadAllMediumContainers.forEachMediumContainer(medium, SampleReadAllMediumContainers::printContainerInfo);
   }

   private static void forEachMediumContainer(Medium<?> medium, Consumer<Container> containerConsumer) {
      LibraryJMeta jMeta = LibraryJMeta.getLibrary();

      LowLevelAPI lowLevelApi = jMeta.getLowLevelAPI();
      try (MediumContainerIterator containerIterator = lowLevelApi.getContainerIterator(medium)) {
         while (containerIterator.hasNext()) {
            Container container = containerIterator.next();

            containerConsumer.accept(container);
         }
      } catch (IOException e) {
         throw new RuntimeException("Error closing file medium", e);
      }
   }

   private static void printContainerInfo(Container container) {
      System.out.println("Next container on top level has data format: " + container.getId().getDataFormat()
         + ", and id: " + container.getId());
   }
}
