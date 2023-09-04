package com.davgeoand.api.monitor.event.handler;

import com.davgeoand.api.monitor.event.type.Event;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public interface EventHandler extends Runnable {
    BlockingQueue<Event> eventBlockingQueue = new LinkedBlockingQueue<>();

    default void addEventToQueue(Event event) {
        eventBlockingQueue.add(event);
    }

    default int queueSize() {
        return eventBlockingQueue.size();
    }
}
