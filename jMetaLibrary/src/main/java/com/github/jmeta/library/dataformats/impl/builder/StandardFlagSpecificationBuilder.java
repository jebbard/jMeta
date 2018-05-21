/**
 *
 * {@link StandardFlagSpecificationBuilder}.java
 *
 * @author Jens Ebert
 *
 * @date 21.05.2018
 *
 */
package com.github.jmeta.library.dataformats.impl.builder;

import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;

import com.github.jmeta.library.dataformats.api.services.builder.DataFormatSpecificationBuilder;
import com.github.jmeta.library.dataformats.api.services.builder.FlagSpecificationBuilder;
import com.github.jmeta.library.dataformats.api.services.builder.FlagsFieldBuilder;
import com.github.jmeta.library.dataformats.api.types.FlagDescription;
import com.github.jmeta.library.dataformats.api.types.FlagSpecification;
import com.github.jmeta.library.dataformats.api.types.Flags;

/**
 * {@link StandardFlagSpecificationBuilder}
 *
 */
public class StandardFlagSpecificationBuilder<ParentBuilder extends DataFormatSpecificationBuilder>
   implements FlagSpecificationBuilder<ParentBuilder> {

   private final AbstractFieldBuilder<ParentBuilder, FlagsFieldBuilder<ParentBuilder>, Flags> parentBuilder;

   /**
    * @see com.github.jmeta.library.dataformats.api.services.builder.FlagSpecificationBuilder#finishFlagSpecification()
    */
   @Override
   public FlagsFieldBuilder<ParentBuilder> finishFlagSpecification() {
      FlagSpecification flagSpecification = new FlagSpecification(flagDescriptions, flagByteLength, flagByteOrder,
         this.defaultFlagBytes);
      parentBuilder.setFlagSpecification(flagSpecification);
      parentBuilder.withDefaultValue(new Flags(flagSpecification));
      return (FlagsFieldBuilder<ParentBuilder>) parentBuilder;
   }

   private final int flagByteLength;
   private final ByteOrder flagByteOrder;
   private final List<FlagDescription> flagDescriptions = new ArrayList<>();
   private byte[] defaultFlagBytes;

   /**
    * Creates a new {@link StandardFlagSpecificationBuilder}.
    * 
    * @param flagByteLength
    * @param flagByteOrder
    */
   public StandardFlagSpecificationBuilder(
      AbstractFieldBuilder<ParentBuilder, FlagsFieldBuilder<ParentBuilder>, Flags> parentBuilder, int flagByteLength,
      ByteOrder flagByteOrder) {
      this.parentBuilder = parentBuilder;
      this.flagByteLength = flagByteLength;
      this.flagByteOrder = flagByteOrder;
   }

   /**
    * @see com.github.jmeta.library.dataformats.api.services.builder.FlagSpecificationBuilder#addFlagDescription(com.github.jmeta.library.dataformats.api.types.FlagDescription)
    */
   @Override
   public FlagSpecificationBuilder<ParentBuilder> addFlagDescription(FlagDescription flagDesc) {
      flagDescriptions.add(flagDesc);
      return this;
   }

   /**
    * @see com.github.jmeta.library.dataformats.api.services.builder.FlagSpecificationBuilder#withDefaultFlagBytes()
    */
   @Override
   public FlagSpecificationBuilder<ParentBuilder> withDefaultFlagBytes(byte[] defaultFlagBytes) {
      this.defaultFlagBytes = defaultFlagBytes;
      return this;
   }

}
