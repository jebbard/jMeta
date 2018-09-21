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
import java.util.UUID;

import com.github.jmeta.library.dataformats.api.services.builder.BinaryFieldBuilder;
import com.github.jmeta.library.dataformats.api.services.builder.ContainerBasedPayloadBuilder;
import com.github.jmeta.library.dataformats.api.services.builder.ContainerBuilder;
import com.github.jmeta.library.dataformats.api.services.builder.DynamicOccurrenceBuilder;
import com.github.jmeta.library.dataformats.api.services.builder.FieldBasedPayloadBuilder;
import com.github.jmeta.library.dataformats.api.services.builder.FieldBuilder;
import com.github.jmeta.library.dataformats.api.services.builder.FieldSequenceBuilder;
import com.github.jmeta.library.dataformats.api.services.builder.FlagSpecificationBuilder;
import com.github.jmeta.library.dataformats.api.services.builder.FlagsFieldBuilder;
import com.github.jmeta.library.dataformats.api.services.builder.FooterBuilder;
import com.github.jmeta.library.dataformats.api.services.builder.HeaderBuilder;
import com.github.jmeta.library.dataformats.api.services.builder.NumericFieldBuilder;
import com.github.jmeta.library.dataformats.api.services.builder.StringFieldBuilder;
import com.github.jmeta.library.dataformats.api.types.DataBlockCrossReference;
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
public final class ContainerBuilderCloner {

   /**
    * Clones the {@link DataBlockDescription} of an existing container into the given {@link ContainerBuilder}.
    * 
    * @param containerBuilder
    *           The {@link ContainerBuilder} to clone into
    * @param existingContainerRef
    *           The reference to the existing container
    * @param payloadType
    *           The payload type of the container, either {@link PhysicalDataBlockType#FIELD_BASED_PAYLOAD} or
    *           {@link PhysicalDataBlockType#CONTAINER_BASED_PAYLOAD}.
    */
   public static void cloneContainerIntoBuilder(ContainerBuilder<?, ?> containerBuilder,
      DataBlockCrossReference existingContainerRef, PhysicalDataBlockType payloadType) {

      Reject.ifNull(payloadType, "payloadType");
      Reject.ifNull(existingContainerRef, "existingContainerId");
      Reject.ifNull(containerBuilder, "containerBuilder");
      Reject.ifTrue(
         payloadType != PhysicalDataBlockType.CONTAINER_BASED_PAYLOAD
            && payloadType != PhysicalDataBlockType.FIELD_BASED_PAYLOAD,
         "payloadType != PhysicalDataBlockType.CONTAINER_BASED_PAYLOAD && payloadType != PhysicalDataBlockType.FIELD_BASED_PAYLOAD");

      DataBlockId clonedContainerId = new DataBlockId(containerBuilder.getDataFormat(), containerBuilder.getGlobalId());

      DataBlockDescription existingContainerDescription = containerBuilder.getRootBuilder()
         .getDataBlockDescription(existingContainerRef.getReferencedId());

      String messagePrefix = "Cloning container with id <" + existingContainerRef + "> is not possible: ";

      if (existingContainerDescription == null) {
         throw new IllegalArgumentException(messagePrefix
            + " this id is unknown; you can only clone containers who's id exists and that are already finished");
      }

      if (existingContainerDescription.getPhysicalType() != PhysicalDataBlockType.CONTAINER) {
         throw new IllegalArgumentException(messagePrefix + "it does not refer to a CONTAINER data block description");
      }

      existingContainerDescription.validateChildren();

      cloneHeaders(containerBuilder, existingContainerDescription, existingContainerRef, clonedContainerId);

      cloneFooters(containerBuilder, existingContainerDescription, existingContainerRef, clonedContainerId);

      clonePayload(containerBuilder, existingContainerDescription, existingContainerRef, clonedContainerId,
         payloadType);
   }

   /**
    * Creates a new {@link ContainerBuilderCloner}.
    */
   private ContainerBuilderCloner() {
      // Private to ensure nobody can instantiate it
   }

   private static void clonePayload(ContainerBuilder<?, ?> containerBuilder,
      DataBlockDescription existingContainerDescription, DataBlockCrossReference existingContainerRef,
      DataBlockId clonedContainerId, PhysicalDataBlockType payloadType) {
      List<DataBlockDescription> payloadDescriptions = existingContainerDescription
         .getChildDescriptionsOfType(payloadType);

      DataBlockDescription payloadDescription = payloadDescriptions.get(0);

      payloadDescription.validateChildren();

      if (payloadType == PhysicalDataBlockType.FIELD_BASED_PAYLOAD) {
         FieldBasedPayloadBuilder<?> fieldBasedPayloadBuilder = (FieldBasedPayloadBuilder<?>) containerBuilder
            .getPayload();

         cloneFields(fieldBasedPayloadBuilder, payloadDescription, existingContainerRef, clonedContainerId);

         fieldBasedPayloadBuilder.finishFieldBasedPayload();
      } else if (payloadType == PhysicalDataBlockType.CONTAINER_BASED_PAYLOAD) {
         ContainerBasedPayloadBuilder<?> containerBasedPayloadBuilder = (ContainerBasedPayloadBuilder<?>) containerBuilder
            .getPayload();

         cloneContainerChildren(payloadDescription, containerBasedPayloadBuilder);

         containerBasedPayloadBuilder.finishContainerBasedPayload();
      }
   }

   private static void cloneContainerChildren(DataBlockDescription existingPayloadDescription,
      ContainerBasedPayloadBuilder<?> containerBasedPayloadBuilder) {
      List<DataBlockDescription> existingContainerDescriptions = existingPayloadDescription
         .getChildDescriptionsOfType(PhysicalDataBlockType.CONTAINER);

      for (DataBlockDescription childContainerDescription : existingContainerDescriptions) {
         if (childContainerDescription.getChildDescriptionsOfType(PhysicalDataBlockType.FIELD_BASED_PAYLOAD)
            .size() == 1) {
            ContainerBuilder<?, ?> childContainerBuilder = childContainerDescription.isGeneric()
               ? containerBasedPayloadBuilder.addGenericContainerWithFieldBasedPayload(
                  childContainerDescription.getId().getLocalId(), childContainerDescription.getName(),
                  childContainerDescription.getDescription())
               : containerBasedPayloadBuilder.addContainerWithFieldBasedPayload(
                  childContainerDescription.getId().getLocalId(), childContainerDescription.getName(),
                  childContainerDescription.getDescription());

            cloneContainerIntoBuilder(childContainerBuilder,
               new DataBlockCrossReference(childContainerDescription.getId().getGlobalId()),
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

            cloneContainerIntoBuilder(childContainerBuilder,
               new DataBlockCrossReference(childContainerDescription.getId().getGlobalId()),
               PhysicalDataBlockType.CONTAINER_BASED_PAYLOAD);

            childContainerBuilder.finishContainer();
         }
      }
   }

   private static void cloneHeaders(ContainerBuilder<?, ?> containerBuilder,
      DataBlockDescription existingContainerDescription, DataBlockCrossReference existingContainerRef,
      DataBlockId clonedContainerId) {
      List<DataBlockDescription> headerDescriptions = existingContainerDescription
         .getChildDescriptionsOfType(PhysicalDataBlockType.HEADER);

      for (DataBlockDescription headerDescription : headerDescriptions) {
         HeaderBuilder<?> hb = containerBuilder.addHeader(headerDescription.getId().getLocalId(),
            headerDescription.getName(), headerDescription.getDescription());

         headerDescription.validateChildren();

         setOccurrences(hb, headerDescription);

         cloneFields(hb, headerDescription, existingContainerRef, clonedContainerId);

         hb.finishHeader();
      }
   }

   private static void cloneFooters(ContainerBuilder<?, ?> containerBuilder,
      DataBlockDescription existingContainerDescription, DataBlockCrossReference existingContainerRef,
      DataBlockId clonedContainerId) {
      List<DataBlockDescription> footerDescriptions = existingContainerDescription
         .getChildDescriptionsOfType(PhysicalDataBlockType.FOOTER);

      for (DataBlockDescription footerDescription : footerDescriptions) {
         FooterBuilder<?> fb = containerBuilder.addFooter(footerDescription.getId().getLocalId(),
            footerDescription.getName(), footerDescription.getDescription());

         footerDescription.validateChildren();

         setOccurrences(fb, footerDescription);

         cloneFields(fb, footerDescription, existingContainerRef, clonedContainerId);

         fb.finishFooter();
      }
   }

   @SuppressWarnings("unchecked")
   private static void cloneFields(FieldSequenceBuilder<?> fsb, DataBlockDescription existingFieldSequenceDescription,
      DataBlockCrossReference existingContainerRef, DataBlockId clonedContainerId) {

      DataBlockId existingContainerId = existingContainerRef.getReferencedId();

      List<DataBlockDescription> fieldDescriptions = existingFieldSequenceDescription
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

   private static <P, F, C extends FieldBuilder<P, F, C>> void setCommonFieldProperties(
      DataBlockDescription fieldDescription, FieldProperties<F> existingFieldProperties, FieldBuilder<P, F, C> fb,
      DataBlockId existingContainerId, DataBlockId clonedContainerId) {

      fb.withLengthOf(fieldDescription.getMinimumByteLength(), fieldDescription.getMaximumByteLength());

      setOccurrences(fb, fieldDescription);

      fb.withDefaultValue(existingFieldProperties.getDefaultValue());

      for (Iterator<F> iterator = existingFieldProperties.getEnumeratedValues().keySet().iterator(); iterator
         .hasNext();) {
         F nextKey = iterator.next();
         byte[] nextValue = existingFieldProperties.getEnumeratedValues().get(nextKey);

         fb.addEnumeratedValue(nextValue, nextKey);
      }

      if (existingFieldProperties.isMagicKey()) {
         fb.asMagicKey();
      }

      List<FieldFunction> fieldFunctions = existingFieldProperties.getFieldFunctions();

      for (FieldFunction fieldFunction : fieldFunctions) {
         FieldFunctionType<?> ffType = fieldFunction.getFieldFunctionType();
         Set<DataBlockId> affectedIds = fieldFunction.getAffectedBlockIds();

         DataBlockCrossReference[] references = new DataBlockCrossReference[affectedIds.size()];

         // Replace affected ids (which are still referring to the original container) with the
         // actual id of the cloned container
         int i = 0;

         for (DataBlockId dataBlockId : affectedIds) {
            String replacedGlobalId = dataBlockId.getGlobalId().replace(existingContainerId.getGlobalId(),
               clonedContainerId.getGlobalId());
            DataBlockId replacedAffectedId = new DataBlockId(dataBlockId.getDataFormat(), replacedGlobalId);
            references[i] = new DataBlockCrossReference(replacedAffectedId.getGlobalId() + "_" + UUID.randomUUID());
            references[i].resolve(replacedAffectedId);

            i++;
         }

         if (ffType == FieldFunctionType.BYTE_ORDER_OF) {
            fb.asByteOrderOf(references[0]);
         } else if (ffType == FieldFunctionType.CHARACTER_ENCODING_OF) {
            fb.asCharacterEncodingOf(references[0]);
         } else if (ffType == FieldFunctionType.COUNT_OF) {
            fb.asCountOf(references[0]);
         } else if (ffType == FieldFunctionType.ID_OF) {
            fb.asIdOf(references[0]);
         } else if (ffType == FieldFunctionType.PRESENCE_OF) {
            fb.indicatesPresenceOf(fieldFunction.getFlagName(), fieldFunction.getFlagValue(), references[0]);
         } else if (ffType == FieldFunctionType.SIZE_OF) {
            fb.asSizeOf(references);
         }
      }
   }

   private static void setOccurrences(DynamicOccurrenceBuilder<?> builder, DataBlockDescription description) {

      builder.withOccurrences(description.getMinimumOccurrences(), description.getMaximumOccurrences());
   }

}
