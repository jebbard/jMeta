/**
 *
 * {@link SampleReadAllTopLevelContainers}.java
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

import com.github.jmeta.library.datablocks.api.services.DataBlockAccessor;
import com.github.jmeta.library.datablocks.api.services.TopLevelContainerIterator;
import com.github.jmeta.library.datablocks.api.types.Container;
import com.github.jmeta.library.media.api.types.FileMedium;
import com.github.jmeta.library.media.api.types.Medium;
import com.github.jmeta.library.startup.api.services.LibraryJMeta;

/**
 * {@link SampleReadAllTopLevelContainers} demonstrates how to read and print all top-level containers.
 */
public class SampleReadAllTopLevelContainers {

   public static void main(String[] args) {
      Medium<Path> medium = new FileMedium(Paths.get("/path/to/my/file.mp3"), false);
      SampleReadAllTopLevelContainers.forEachTopLevelContainer(medium,
         SampleReadAllTopLevelContainers::printContainerInfo);
   }

   private static void forEachTopLevelContainer(Medium<?> medium, Consumer<Container> containerConsumer) {
      LibraryJMeta jMeta = LibraryJMeta.getLibrary();

      DataBlockAccessor dataBlockAccessor = jMeta.getDataBlockAccessor();
      try (TopLevelContainerIterator containerIterator = dataBlockAccessor.getContainerIterator(medium)) {
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
