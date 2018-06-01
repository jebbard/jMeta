/**
 *
 * {@link ContainerBuilderOverrider}.java
 *
 * @author Jens Ebert
 *
 * @date 01.06.2018
 *
 */
package com.github.jmeta.library.dataformats.impl.builder;

import java.util.List;
import java.util.Set;

import com.github.jmeta.library.dataformats.api.services.builder.BinaryFieldBuilder;
import com.github.jmeta.library.dataformats.api.services.builder.ContainerBasedPayloadBuilder;
import com.github.jmeta.library.dataformats.api.services.builder.ContainerBuilder;
import com.github.jmeta.library.dataformats.api.services.builder.DataBlockDescriptionBuilder;
import com.github.jmeta.library.dataformats.api.services.builder.FieldBasedPayloadBuilder;
import com.github.jmeta.library.dataformats.api.services.builder.FieldSequenceBuilder;
import com.github.jmeta.library.dataformats.api.services.builder.FooterBuilder;
import com.github.jmeta.library.dataformats.api.services.builder.HeaderBuilder;
import com.github.jmeta.library.dataformats.api.types.DataBlockDescription;
import com.github.jmeta.library.dataformats.api.types.DataBlockId;
import com.github.jmeta.library.dataformats.api.types.FieldFunction;
import com.github.jmeta.library.dataformats.api.types.FieldFunctionType;
import com.github.jmeta.library.dataformats.api.types.FieldProperties;
import com.github.jmeta.library.dataformats.api.types.FieldType;
import com.github.jmeta.library.dataformats.api.types.PhysicalDataBlockType;

/**
 * {@link ContainerBuilderOverrider}
 *
 */
public class ContainerBuilderOverrider {

   public static void overrideContainerBuilderWithDescription(ContainerBuilder<?, ?> containerBuilder,
      DataBlockId existingContainerId, PhysicalDataBlockType payloadType) {

      DataBlockDescription containerDescription = containerBuilder.getRootBuilder()
         .getDataBlockDescription(existingContainerId);

      String messagePrefix = "Cloning container with id <" + existingContainerId + "> is not possible as ";

      if (containerDescription == null) {
         throw new IllegalArgumentException(messagePrefix
            + " this id is unknown; you can only clone containers who's id exists and that are already finished");
      }

      if (containerDescription.getPhysicalType() != PhysicalDataBlockType.CONTAINER
         || !containerDescription.isGeneric()) {
         throw new IllegalArgumentException(
            messagePrefix + "it does not refer to a generic CONTAINER data block description");
      }

      List<DataBlockDescription> headerDescriptions = containerDescription
         .getChildDescriptionsOfType(PhysicalDataBlockType.HEADER);
      List<DataBlockDescription> footerDescriptions = containerDescription
         .getChildDescriptionsOfType(PhysicalDataBlockType.FOOTER);
      List<DataBlockDescription> payloadDescriptions = containerDescription.getChildDescriptionsOfType(payloadType);

      if (payloadDescriptions.size() != 1) {
         throw new IllegalArgumentException(
            messagePrefix + "it must have a single " + payloadType + " child description, which is not the case");
      }

      DataBlockDescription payloadDescription = payloadDescriptions.get(0);

      if (headerDescriptions.size() + footerDescriptions.size() + payloadDescriptions.size() != containerDescription
         .getOrderedChildren().size()) {
         throw new IllegalArgumentException(messagePrefix
            + "it has additional invalid child descriptions which are neither header nor footer nor payload");
      }

      setLengthAndOccurrences(containerBuilder, containerDescription);

      for (DataBlockDescription headerDescription : headerDescriptions) {
         HeaderBuilder<?> hb = containerBuilder.addHeader(headerDescription.getId().getLocalId(),
            headerDescription.getName(), headerDescription.getDescription());

         setLengthAndOccurrences(hb, headerDescription);

         cloneFields(hb, headerDescription, messagePrefix);
      }

      for (DataBlockDescription footerDescription : footerDescriptions) {
         FooterBuilder<?> fb = containerBuilder.addFooter(footerDescription.getId().getLocalId(),
            footerDescription.getName(), footerDescription.getDescription());

         setLengthAndOccurrences(fb, footerDescription);

         cloneFields(fb, footerDescription, messagePrefix);
      }

      if (payloadType == PhysicalDataBlockType.FIELD_BASED_PAYLOAD) {
         FieldBasedPayloadBuilder<?> fieldBasedPayloadBuilder = (FieldBasedPayloadBuilder<?>) containerBuilder
            .getPayload();

         setLengthAndOccurrences(fieldBasedPayloadBuilder, payloadDescription);

         cloneFields(fieldBasedPayloadBuilder, payloadDescription, messagePrefix);
      } else if (payloadType == PhysicalDataBlockType.CONTAINER_BASED_PAYLOAD) {
         ContainerBasedPayloadBuilder<?> containerBasedPayloadBuilder = (ContainerBasedPayloadBuilder<?>) containerBuilder
            .getPayload();

         setLengthAndOccurrences(containerBasedPayloadBuilder, payloadDescription);

         List<DataBlockDescription> containerDescriptions = payloadDescription
            .getChildDescriptionsOfType(PhysicalDataBlockType.CONTAINER);

         if (containerDescriptions.size() != payloadDescription.getOrderedChildren().size()) {
            throw new IllegalArgumentException(messagePrefix
               + "its container based payload has additional invalid child descriptions which have not the type CONTAINER");
         }

         for (DataBlockDescription childContainerDescription : containerDescriptions) {
            if (childContainerDescription.getChildDescriptionsOfType(PhysicalDataBlockType.FIELD_BASED_PAYLOAD)
               .size() == 1) {
               ContainerBuilder<?, ?> childContainerBuilder = childContainerDescription.isGeneric()
                  ? containerBasedPayloadBuilder.addGenericContainerWithFieldBasedPayload(
                     childContainerDescription.getId().getLocalId(), childContainerDescription.getName(),
                     childContainerDescription.getDescription())
                  : containerBasedPayloadBuilder.addContainerWithFieldBasedPayload(
                     childContainerDescription.getId().getLocalId(), childContainerDescription.getName(),
                     childContainerDescription.getDescription());

               setLengthAndOccurrences(childContainerBuilder, childContainerDescription);

               overrideContainerBuilderWithDescription(childContainerBuilder, childContainerDescription.getId(),
                  PhysicalDataBlockType.FIELD_BASED_PAYLOAD);
            } else {
               ContainerBuilder<?, ?> childContainerBuilder = childContainerDescription.isGeneric()
                  ? containerBasedPayloadBuilder.addGenericContainerWithContainerBasedPayload(
                     childContainerDescription.getId().getLocalId(), childContainerDescription.getName(),
                     childContainerDescription.getDescription())
                  : containerBasedPayloadBuilder.addContainerWithContainerBasedPayload(
                     childContainerDescription.getId().getLocalId(), childContainerDescription.getName(),
                     childContainerDescription.getDescription());

               setLengthAndOccurrences(childContainerBuilder, childContainerDescription);

               overrideContainerBuilderWithDescription(childContainerBuilder, childContainerDescription.getId(),
                  PhysicalDataBlockType.CONTAINER_BASED_PAYLOAD);
            }
         }
      }
   }

   /**
    * @param fsb
    * @param fieldSequenceDescription
    * @param messagePrefix
    */
   private static void cloneFields(FieldSequenceBuilder<?> fsb, DataBlockDescription fieldSequenceDescription,
      String messagePrefix) {
      List<DataBlockDescription> fieldDescriptions = fieldSequenceDescription
         .getChildDescriptionsOfType(PhysicalDataBlockType.FIELD);

      if (fieldDescriptions.size() != fieldSequenceDescription.getOrderedChildren().size()) {
         throw new IllegalArgumentException(
            messagePrefix + "its header has additional invalid child descriptions which have not the type FIELD");
      }

      for (DataBlockDescription fieldDescription : fieldDescriptions) {
         FieldProperties<?> fieldProperties = fieldDescription.getFieldProperties();

         if (fieldProperties.getFieldType() == FieldType.BINARY) {
            BinaryFieldBuilder<?> bfb = fsb.addBinaryField(fieldDescription.getId().getLocalId(),
               fieldDescription.getName(), fieldDescription.getDescription());

            setLengthAndOccurrences(bfb, fieldDescription);

            bfb.withDefaultValue((byte[]) fieldProperties.getDefaultValue());

            if (fieldProperties.isMagicKey()) {
               bfb.asMagicKey();
            }

            List<FieldFunction> fieldFunctions = fieldProperties.getFieldFunctions();

            for (FieldFunction fieldFunction : fieldFunctions) {
               FieldFunctionType<?> ffType = fieldFunction.getFieldFunctionType();
               Set<DataBlockId> affectedFields = fieldFunction.getAffectedBlockIds();

               if (ffType == FieldFunctionType.BYTE_ORDER_OF) {
                  bfb.asByteOrderOf((DataBlockId[]) affectedFields.toArray());
               } else if (ffType == FieldFunctionType.CHARACTER_ENCODING_OF) {
                  bfb.asCharacterEncodingOf((DataBlockId[]) affectedFields.toArray());
               } else if (ffType == FieldFunctionType.COUNT_OF) {
                  bfb.asCountOf((DataBlockId[]) affectedFields.toArray());
               } else if (ffType == FieldFunctionType.ID_OF) {
                  bfb.asIdOf((DataBlockId[]) affectedFields.toArray());
               } else if (ffType == FieldFunctionType.PRESENCE_OF) {
                  bfb.indicatesPresenceOf(fieldFunction.getFlagName(), fieldFunction.getFlagValue(),
                     (DataBlockId[]) affectedFields.toArray());
               } else if (ffType == FieldFunctionType.SIZE_OF) {
                  bfb.asSizeOf((DataBlockId[]) affectedFields.toArray());
               }
            }
         }
      }
   }

   /**
    * @param builder
    * @param description
    */
   private static void setLengthAndOccurrences(DataBlockDescriptionBuilder<?> builder,
      DataBlockDescription description) {
      builder.withLengthOf(description.getMinimumByteLength(), description.getMaximumByteLength());
      builder.withOccurrences(description.getMinimumOccurrences(), description.getMaximumOccurrences());
   }

}
