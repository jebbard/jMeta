/**
 *
 * {@link DataBlockEventListener}.java
 *
 * @author Jens Ebert
 *
 * @date 19.04.2019
 *
 */
package com.github.jmeta.library.datablocks.impl.events;

/**
 * {@link DataBlockEventListener} notifies classes that listen to {@link DataBlockEvent}s.
 */
public interface DataBlockEventListener {

   /**
    * Notifies the listener that a {@link DataBlockEvent} has occurred. Based on the event, the listener implementation
    * needs to decide if the event is relevant for it or not.
    *
    * @param event
    *           The {@link DataBlockEvent} that has occurred
    */
   public void dataBlockEventOccurred(DataBlockEvent event);
}
