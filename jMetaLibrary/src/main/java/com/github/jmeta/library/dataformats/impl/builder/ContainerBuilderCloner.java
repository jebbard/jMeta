/**
 *
 * {@link ContainerBuilderCloner}.java
 *
 * @author Jens Ebert
 *
 * @date 01.06.2018
 *
 */
package com.github.jmeta.library.dataformats.impl.builder;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.github.jmeta.library.dataformats.api.services.builder.BinaryFieldBuilder;
import com.github.jmeta.library.dataformats.api.services.builder.ContainerBasedPayloadBuilder;
import com.github.jmeta.library.dataformats.api.services.builder.ContainerBuilder;
import com.github.jmeta.library.dataformats.api.services.builder.DataBlockDescriptionBuilder;
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
import com.github.jmeta.utility.dbc.api.services.Reject;

/**
 * {@link ContainerBuilderCloner} is a utility class that clones all properties of an existing container into an
 * existing {@link ContainerBuilder}, i.e. adding all children and properties from an existing container's
 * {@link DataBlockDescription}.
 */
public class ContainerBuilderCloner {

   /**
    * Clones the {@link DataBlockDescription} of an existing container into the given {@link ContainerBuilder}.
    * 
    * @param containerBuilder
    *           The {@link ContainerBuilder} to clone into
    * @param existingContainerId
    *           The id of the existing container
    * @param payloadType
    *           The payload type of the container, either {@link PhysicalDataBlockType#FIELD_BASED_PAYLOAD} or
    *           {@link PhysicalDataBlockType#CONTAINER_BASED_PAYLOAD}.
    */
   public static void cloneContainerIntoBuilder(ContainerBuilder<?, ?> containerBuilder,
      DataBlockId existingContainerId, PhysicalDataBlockType payloadType) {

      Reject.ifNull(payloadType, "payloadType");
      Reject.ifNull(existingContainerId, "existingContainerId");
      Reject.ifNull(containerBuilder, "containerBuilder");
      Reject.ifTrue(
         payloadType != PhysicalDataBlockType.CONTAINER_BASED_PAYLOAD
            && payloadType != PhysicalDataBlockType.FIELD_BASED_PAYLOAD,
         "payloadType != PhysicalDataBlockType.CONTAINER_BASED_PAYLOAD && payloadType != PhysicalDataBlockType.FIELD_BASED_PAYLOAD");

      DataBlockId clonedContainerId = new DataBlockId(containerBuilder.getDataFormat(), containerBuilder.getGlobalId());

      DataBlockDescription containerDescription = containerBuilder.getRootBuilder()
         .getDataBlockDescription(existingContainerId);

      String messagePrefix = "Cloning container with id <" + existingContainerId + "> is not possible: ";

      if (containerDescription == null) {
         throw new IllegalArgumentException(messagePrefix
            + " this id is unknown; you can only clone containers who's id exists and that are already finished");
      }

      if (containerDescription.getPhysicalType() != PhysicalDataBlockType.CONTAINER
         || !containerDescription.isGeneric()) {
         throw new IllegalArgumentException(
            messagePrefix + "it does not refer to a generic CONTAINER data block description");
      }

      containerDescription.validateChildren();

      setLengthAndOccurrences(containerBuilder, containerDescription);

      cloneHeaders(containerBuilder, containerDescription, existingContainerId, clonedContainerId);

      cloneFooters(containerBuilder, containerDescription, existingContainerId, clonedContainerId);

      clonePayload(containerBuilder, containerDescription, existingContainerId, clonedContainerId, payloadType);
   }

   private static void clonePayload(ContainerBuilder<?, ?> containerBuilder, DataBlockDescription containerDescription,
      DataBlockId existingContainerId, DataBlockId clonedContainerId, PhysicalDataBlockType payloadType) {
      List<DataBlockDescription> payloadDescriptions = containerDescription.getChildDescriptionsOfType(payloadType);

      DataBlockDescription payloadDescription = payloadDescriptions.get(0);

      payloadDescription.validateChildren();

      if (payloadType == PhysicalDataBlockType.FIELD_BASED_PAYLOAD) {
         FieldBasedPayloadBuilder<?> fieldBasedPayloadBuilder = (FieldBasedPayloadBuilder<?>) containerBuilder
            .getPayload();

         setLengthAndOccurrences(fieldBasedPayloadBuilder, payloadDescription);

         cloneFields(fieldBasedPayloadBuilder, payloadDescription, existingContainerId, clonedContainerId);

         fieldBasedPayloadBuilder.finishFieldBasedPayload();
      } else if (payloadType == PhysicalDataBlockType.CONTAINER_BASED_PAYLOAD) {
         ContainerBasedPayloadBuilder<?> containerBasedPayloadBuilder = (ContainerBasedPayloadBuilder<?>) containerBuilder
            .getPayload();

         setLengthAndOccurrences(containerBasedPayloadBuilder, payloadDescription);

         List<DataBlockDescription> containerDescriptions = payloadDescription
            .getChildDescriptionsOfType(PhysicalDataBlockType.CONTAINER);

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

               cloneContainerIntoBuilder(childContainerBuilder, childContainerDescription.getId(),
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

               cloneContainerIntoBuilder(childContainerBuilder, childContainerDescription.getId(),
                  PhysicalDataBlockType.CONTAINER_BASED_PAYLOAD);

               childContainerBuilder.finishContainer();
            }
         }

         containerBasedPayloadBuilder.finishContainerBasedPayload();
      }
   }

   /**
    * @param containerBuilder
    * @param containerDescription
    * @param existingContainerId
    * @param clonedContainerId
    */
   private static void cloneFooters(ContainerBuilder<?, ?> containerBuilder, DataBlockDescription containerDescription,
      DataBlockId existingContainerId, DataBlockId clonedContainerId) {
      List<DataBlockDescription> footerDescriptions = containerDescription
         .getChildDescriptionsOfType(PhysicalDataBlockType.FOOTER);

      for (DataBlockDescription footerDescription : footerDescriptions) {
         FooterBuilder<?> fb = containerBuilder.addFooter(footerDescription.getId().getLocalId(),
            footerDescription.getName(), footerDescription.getDescription());

         footerDescription.validateChildren();

         setLengthAndOccurrences(fb, footerDescription);

         cloneFields(fb, footerDescription, existingContainerId, clonedContainerId);

         fb.finishFooter();
      }
   }

   /**
    * @param containerBuilder
    * @param containerDescription
    * @param existingContainerId
    * @param clonedContainerId
    */
   private static void cloneHeaders(ContainerBuilder<?, ?> containerBuilder, DataBlockDescription containerDescription,
      DataBlockId existingContainerId, DataBlockId clonedContainerId) {
      List<DataBlockDescription> headerDescriptions = containerDescription
         .getChildDescriptionsOfType(PhysicalDataBlockType.HEADER);

      for (DataBlockDescription headerDescription : headerDescriptions) {
         HeaderBuilder<?> hb = containerBuilder.addHeader(headerDescription.getId().getLocalId(),
            headerDescription.getName(), headerDescription.getDescription());

         headerDescription.validateChildren();

         setLengthAndOccurrences(hb, headerDescription);

         cloneFields(hb, headerDescription, existingContainerId, clonedContainerId);

         hb.finishHeader();
      }
   }

   @SuppressWarnings("unchecked")
   private static void cloneFields(FieldSequenceBuilder<?> fsb, DataBlockDescription fieldSequenceDescription,
      DataBlockId existingContainerId, DataBlockId clonedContainerId) {

      List<DataBlockDescription> fieldDescriptions = fieldSequenceDescription
         .getChildDescriptionsOfType(PhysicalDataBlockType.FIELD);

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
         }
      }
   }

   private static <P, FIT, C extends FieldBuilder<P, FIT, C>> void setCommonFieldProperties(
      DataBlockDescription fieldDescription, FieldProperties<FIT> fieldProperties, FieldBuilder<P, FIT, C> fb,
      DataBlockId existingContainerId, DataBlockId clonedContainerId) {

      setLengthAndOccurrences(fb, fieldDescription);

      fb.withDefaultValue(fieldProperties.getDefaultValue());

      for (Iterator<FIT> iterator = fieldProperties.getEnumeratedValues().keySet().iterator(); iterator.hasNext();) {
         FIT nextKey = iterator.next();
         byte[] nextValue = fieldProperties.getEnumeratedValues().get(nextKey);

         fb.addEnumeratedValue(nextValue, nextKey);
      }

      if (fieldProperties.isMagicKey()) {
         fb.asMagicKey();
      }

      List<FieldFunction> fieldFunctions = fieldProperties.getFieldFunctions();

      for (FieldFunction fieldFunction : fieldFunctions) {
         FieldFunctionType<?> ffType = fieldFunction.getFieldFunctionType();
         Set<DataBlockId> affectedIds = fieldFunction.getAffectedBlockIds();

         DataBlockId[] replacedAffectedIds = new DataBlockId[affectedIds.size()];

         // Replace affected ids (which are still referring to the original container) with the
         // actual id of the cloned container
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

   private static void setLengthAndOccurrences(DataBlockDescriptionBuilder<?> builder,
      DataBlockDescription description) {
      builder.withLengthOf(description.getMinimumByteLength(), description.getMaximumByteLength());
      builder.withOccurrences(description.getMinimumOccurrences(), description.getMaximumOccurrences());
   }

}
