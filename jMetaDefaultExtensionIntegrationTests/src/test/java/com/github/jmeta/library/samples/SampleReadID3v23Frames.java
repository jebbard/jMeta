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
import java.util.List;
import java.util.function.Consumer;

import com.github.jmeta.defaultextensions.id3v23.impl.ID3v23Extension;
import com.github.jmeta.library.datablocks.api.exceptions.BinaryValueConversionException;
import com.github.jmeta.library.datablocks.api.services.ContainerIterator;
import com.github.jmeta.library.datablocks.api.services.MediumContainerIterator;
import com.github.jmeta.library.datablocks.api.types.Container;
import com.github.jmeta.library.datablocks.api.types.ContainerBasedPayload;
import com.github.jmeta.library.datablocks.api.types.Field;
import com.github.jmeta.library.datablocks.api.types.FieldBasedPayload;
import com.github.jmeta.library.media.api.types.FileMedium;
import com.github.jmeta.library.media.api.types.Medium;
import com.github.jmeta.library.media.api.types.MediumAccessType;
import com.github.jmeta.library.startup.api.services.LibraryJMeta;

/**
 * {@link SampleReadID3v23Frames} demonstrates how to read and print all ID3v2.3 text frames of a multimedia file.
 */
public class SampleReadID3v23Frames {

   public static void main(String[] args) {
      Medium<Path> medium = new FileMedium(Paths.get("/path/to/my/file.mp3"), MediumAccessType.READ_WRITE);
      SampleReadID3v23Frames.forEachTopLevelContainer(medium, SampleReadID3v23Frames::printTextFrames);
   }

   private static void forEachTopLevelContainer(Medium<?> medium, Consumer<Container> containerConsumer) {
      LibraryJMeta jMeta = LibraryJMeta.getLibrary();

      try (MediumContainerIterator containerIterator = jMeta.getLowLevelAPI().getReverseContainerIterator(medium)) {
         while (containerIterator.hasNext()) {
            Container container = containerIterator.next();
            containerConsumer.accept(container);
         }
      } catch (IOException e) {
         throw new RuntimeException("Error closing file medium", e);
      }
   }

   private static void printTextFrame(Container frame) {
      FieldBasedPayload framePayload = (FieldBasedPayload) frame.getPayload();

      List<Field<?>> fields = framePayload.getFields();

      for (Field<?> field : fields) {
         if (field.getId().getLocalId().equals(ID3v23Extension.TEXT_FRAME_INFORMATION)) {
            try {
               System.out.println(field.getInterpretedValue().toString());
            } catch (BinaryValueConversionException e) {
               throw new RuntimeException("Could not convert binary field value", e);
            }
         }
      }
   }

   private static void printTextFrames(Container container) {
      if (container.getId().getDataFormat() == ID3v23Extension.ID3v23) {
         System.out.println("Found ID3v2.3 tag");

         ContainerBasedPayload frames = (ContainerBasedPayload) container.getPayload();

         ContainerIterator frameIterator = frames.getContainerIterator();

         while (frameIterator.hasNext()) {
            Container frame = frameIterator.next();

            // It is a text frame
            if (frame.getId().getLocalId().startsWith("T")) {
               SampleReadID3v23Frames.printTextFrame(frame);
            }
         }
      }
   }
}
