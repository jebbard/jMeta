/**
 *
 * {@link DataBlockEvent}.java
 *
 * @author Jens Ebert
 *
 * @date 19.04.2019
 *
 */
package com.github.jmeta.library.datablocks.impl.events;

import java.time.LocalDateTime;

import com.github.jmeta.library.datablocks.api.types.DataBlock;

/**
 * {@link DataBlockEvent} represents an event happening to a {@link DataBlock} that is important for all listeners. An
 * event has an occurrence time to be able to order events (if necessary), as well as a type and it als contains the
 * source {@link DataBlock} whose changes caused the event in the first place.
 */
public class DataBlockEvent {

   private final LocalDateTime creationTime;
   private final DataBlock causingDataBlock;
   private final DataBlockEventType eventType;

   /**
    * Creates a new {@link DataBlockEvent}.
    * 
    * @param eventType
    *           The {@link DataBlockEventType} of the event, must not be null
    * @param causingDataBlock
    *           The causing {@link DataBlock}, may be null if this is a global event not caused by any particular
    *           {@link DataBlock}, currently only for event types {@link DataBlockEventType#FLUSHED} and
    *           {@link DataBlockEventType#RESET_ALL} .
    */
   public DataBlockEvent(DataBlockEventType eventType, DataBlock causingDataBlock) {
      creationTime = LocalDateTime.now();
      this.causingDataBlock = causingDataBlock;
      this.eventType = eventType;
   }

   public LocalDateTime getCreationTime() {
      return creationTime;
   }

   public DataBlock getCausingDataBlock() {
      return causingDataBlock;
   }

   public DataBlockEventType getEventType() {
      return eventType;
   }
}
