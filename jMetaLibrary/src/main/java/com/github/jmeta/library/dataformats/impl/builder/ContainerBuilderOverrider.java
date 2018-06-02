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

import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.github.jmeta.library.dataformats.api.services.builder.BinaryFieldBuilder;
import com.github.jmeta.library.dataformats.api.services.builder.ContainerBasedPayloadBuilder;
import com.github.jmeta.library.dataformats.api.services.builder.ContainerBuilder;
import com.github.jmeta.library.dataformats.api.services.builder.DataBlockDescriptionBuilder;
import com.github.jmeta.library.dataformats.api.services.builder.EnumeratedFieldBuilder;
import com.github.jmeta.library.dataformats.api.services.builder.FieldBasedPayloadBuilder;
import com.github.jmeta.library.dataformats.api.services.builder.FieldBuilder;
import com.github.jmeta.library.dataformats.api.services.builder.FieldSequenceBuilder;
import com.github.jmeta.library.dataformats.api.services.builder.FlagSpecificationBuilder;
import com.github.jmeta.library.dataformats.api.services.builder.FlagsFieldBuilder;
import com.github.jmeta.library.dataformats.api.services.builder.FooterBuilder;
import com.github.jmeta.library.dataformats.api.services.builder.HeaderBuilder;
import com.github.jmeta.library.dataformats.api.services.builder.NumericFieldBuilder;
import com.github.jmeta.library.dataformats.api.services.builder.StringFieldBuilder;
import com.github.jmeta.library.dataformats.api.types.DataBlockDescription;
import com.github.jmeta.library.dataformats.api.types.DataBlockId;
import com.github.jmeta.library.dataformats.api.types.FieldFunction;
import com.github.jmeta.library.dataformats.api.types.FieldFunctionType;
import com.github.jmeta.library.dataformats.api.types.FieldProperties;
import com.github.jmeta.library.dataformats.api.types.FieldType;
import com.github.jmeta.library.dataformats.api.types.FlagDescription;
import com.github.jmeta.library.dataformats.api.types.Flags;
import com.github.jmeta.library.dataformats.api.types.PhysicalDataBlockType;

/**
 * {@link ContainerBuilderOverrider}
 *
 */
public class ContainerBuilderOverrider {

   public static void overrideContainerBuilderWithDescription(ContainerBuilder<?, ?> containerBuilder,
      DataBlockId existingContainerId, PhysicalDataBlockType payloadType) {

      DataBlockId clonedContainerId = new DataBlockId(containerBuilder.getDataFormat(), containerBuilder.getGlobalId());

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

         cloneFields(hb, headerDescription, messagePrefix, existingContainerId, clonedContainerId);

         hb.finishHeader();
      }

      for (DataBlockDescription footerDescription : footerDescriptions) {
         FooterBuilder<?> fb = containerBuilder.addFooter(footerDescription.getId().getLocalId(),
            footerDescription.getName(), footerDescription.getDescription());

         setLengthAndOccurrences(fb, footerDescription);

         cloneFields(fb, footerDescription, messagePrefix, existingContainerId, clonedContainerId);

         fb.finishFooter();
      }

      if (payloadType == PhysicalDataBlockType.FIELD_BASED_PAYLOAD) {
         FieldBasedPayloadBuilder<?> fieldBasedPayloadBuilder = (FieldBasedPayloadBuilder<?>) containerBuilder
            .getPayload();

         setLengthAndOccurrences(fieldBasedPayloadBuilder, payloadDescription);

         cloneFields(fieldBasedPayloadBuilder, payloadDescription, messagePrefix, existingContainerId,
            clonedContainerId);

         fieldBasedPayloadBuilder.finishFieldBasedPayload();
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

               childContainerBuilder.finishContainer();
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

               childContainerBuilder.finishContainer();
            }
         }

         containerBasedPayloadBuilder.finishContainerBasedPayload();
      }
   }

   /**
    * @param fsb
    * @param fieldSequenceDescription
    * @param messagePrefix
    * @param existingContainerId
    *           TODO
    * @param clonedContainerId
    *           TODO
    */
   private static void cloneFields(FieldSequenceBuilder<?> fsb, DataBlockDescription fieldSequenceDescription,
      String messagePrefix, DataBlockId existingContainerId, DataBlockId clonedContainerId) {
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

            setCommonFieldProperties(fieldDescription, (FieldProperties<byte[]>) fieldProperties, bfb,
               existingContainerId, clonedContainerId);
            bfb.finishField();
         } else if (fieldProperties.getFieldType() == FieldType.UNSIGNED_WHOLE_NUMBER) {
            NumericFieldBuilder<?> nfb = fsb.addNumericField(fieldDescription.getId().getLocalId(),
               fieldDescription.getName(), fieldDescription.getDescription());
            nfb.withFixedByteOrder(fieldProperties.getFixedByteOrder());

            setCommonFieldProperties(fieldDescription, (FieldProperties<Long>) fieldProperties, nfb,
               existingContainerId, clonedContainerId);
            nfb.finishField();
         } else if (fieldProperties.getFieldType() == FieldType.STRING) {
            StringFieldBuilder<?> sfb = fsb.addStringField(fieldDescription.getId().getLocalId(),
               fieldDescription.getName(), fieldDescription.getDescription());

            sfb.withFixedCharset(fieldProperties.getFixedCharacterEncoding());
            sfb.withTerminationCharacter(fieldProperties.getTerminationCharacter());

            setCommonFieldProperties(fieldDescription, (FieldProperties<String>) fieldProperties, sfb,
               existingContainerId, clonedContainerId);
            sfb.finishField();
         } else if (fieldProperties.getFieldType() == FieldType.FLAGS) {
            FlagsFieldBuilder<?> ffb = fsb.addFlagsField(fieldDescription.getId().getLocalId(),
               fieldDescription.getName(), fieldDescription.getDescription());

            setCommonFieldProperties(fieldDescription, (FieldProperties<Flags>) fieldProperties, ffb,
               existingContainerId, clonedContainerId);

            FlagSpecificationBuilder<?> flagSpecBuilder = ffb.withFlagSpecification(
               fieldProperties.getFlagSpecification().getByteLength(),
               fieldProperties.getFlagSpecification().getByteOrdering());

            flagSpecBuilder.withDefaultFlagBytes(fieldProperties.getFlagSpecification().getDefaultFlagBytes());

            Map<String, FlagDescription> flagDescriptions = fieldProperties.getFlagSpecification()
               .getFlagDescriptions();

            for (Iterator<String> iterator = flagDescriptions.keySet().iterator(); iterator.hasNext();) {
               String nextFlagName = iterator.next();

               flagSpecBuilder.addFlagDescription(flagDescriptions.get(nextFlagName));
            }

            flagSpecBuilder.finishFlagSpecification();
            ffb.finishField();
         } else if (fieldProperties.getFieldType() == FieldType.ENUMERATED) {
            // TODO ENsure it is working for any type!!!
            EnumeratedFieldBuilder<?, Charset> bfb = fsb.addEnumeratedField(Charset.class,
               fieldDescription.getId().getLocalId(), fieldDescription.getName(), fieldDescription.getDescription());

            setCommonFieldProperties(fieldDescription, (FieldProperties<Charset>) fieldProperties, bfb,
               existingContainerId, clonedContainerId);

            for (Iterator<?> iterator = fieldProperties.getEnumeratedValues().keySet().iterator(); iterator
               .hasNext();) {
               Object nextKey = iterator.next();
               byte[] nextValue = fieldProperties.getEnumeratedValues().get(nextKey);

               bfb.addEnumeratedValue(nextValue, (Charset) nextKey);
            }
            bfb.finishField();
         }
      }
   }

   private static <P, FIT, C extends FieldBuilder<P, FIT, C>> void setCommonFieldProperties(
      DataBlockDescription fieldDescription, FieldProperties<FIT> fieldProperties, FieldBuilder<P, FIT, C> fb,
      DataBlockId existingContainerId, DataBlockId clonedContainerId) {
      setLengthAndOccurrences(fb, fieldDescription);

      fb.withDefaultValue(fieldProperties.getDefaultValue());

      if (fieldProperties.isMagicKey()) {
         fb.asMagicKey();
      }

      List<FieldFunction> fieldFunctions = fieldProperties.getFieldFunctions();

      for (FieldFunction fieldFunction : fieldFunctions) {
         FieldFunctionType<?> ffType = fieldFunction.getFieldFunctionType();
         Set<DataBlockId> affectedIds = fieldFunction.getAffectedBlockIds();

         DataBlockId[] replacedAffectedIds = new DataBlockId[affectedIds.size()];

         // Replace affected ids with actual id
         int i = 0;
         for (DataBlockId dataBlockId : affectedIds) {
            String replacedGlobalId = dataBlockId.getGlobalId().replace(existingContainerId.getGlobalId(),
               clonedContainerId.getGlobalId());
            replacedAffectedIds[i++] = new DataBlockId(dataBlockId.getDataFormat(), replacedGlobalId);
         }

         if (ffType == FieldFunctionType.BYTE_ORDER_OF) {
            fb.asByteOrderOf(replacedAffectedIds);
         } else if (ffType == FieldFunctionType.CHARACTER_ENCODING_OF) {
            fb.asCharacterEncodingOf(replacedAffectedIds);
         } else if (ffType == FieldFunctionType.COUNT_OF) {
            fb.asCountOf(replacedAffectedIds);
         } else if (ffType == FieldFunctionType.ID_OF) {
            fb.asIdOf(replacedAffectedIds);
         } else if (ffType == FieldFunctionType.PRESENCE_OF) {
            fb.indicatesPresenceOf(fieldFunction.getFlagName(), fieldFunction.getFlagValue(), replacedAffectedIds);
         } else if (ffType == FieldFunctionType.SIZE_OF) {
            fb.asSizeOf(replacedAffectedIds);
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
