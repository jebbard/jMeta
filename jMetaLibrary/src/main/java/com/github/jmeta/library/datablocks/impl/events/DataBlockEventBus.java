/**
 *
 * {@link DataBlockEventBus}.java
 *
 * @author Jens Ebert
 *
 * @date 19.04.2019
 *
 */
package com.github.jmeta.library.datablocks.impl.events;

import java.util.HashSet;
import java.util.Set;

import com.github.jmeta.utility.dbc.api.services.Reject;

/**
 * {@link DataBlockEventBus} represents an event bus where any interested
 * parties can be registered to either publish {@link DataBlockEvent}s or listen
 * to {@link DataBlockEvent}s. It is a bus which means broadcasting: every
 * listener always receives all {@link DataBlockEvent}s that are published.
 */
public class DataBlockEventBus {

	private final Set<DataBlockEventListener> listeners = new HashSet<>();

	/**
	 * Notifies all currently registered {@link DataBlockEventListener}s that a new
	 * {@link DataBlockEvent} has occurred.
	 *
	 * The order in which the listeners are notified is undefined, so
	 * implementations should never base on this notification order.
	 *
	 * @param event The {@link DataBlockEvent} to publish, must not be null
	 */
	public void publishEvent(DataBlockEvent event) {
		Reject.ifNull(event, "event");

		listeners.forEach(listener -> listener.dataBlockEventOccurred(event));
	}

	/**
	 * Registers a new {@link DataBlockEventListener} that will from now on receive
	 * all {@link DataBlockEvent}s ever published on this {@link DataBlockEventBus}.
	 * Note that registering the same instance twice will not have the effect that
	 * it receives the events twice.
	 *
	 * @param listener The {@link DataBlockEventListener} to be registered, must not
	 *                 be null
	 */
	public void registerListener(DataBlockEventListener listener) {
		Reject.ifNull(listener, "listener");

		listeners.add(listener);
	}
}
