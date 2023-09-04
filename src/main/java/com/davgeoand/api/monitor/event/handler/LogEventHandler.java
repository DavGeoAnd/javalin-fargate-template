package com.davgeoand.api.monitor.event.handler;

import com.davgeoand.api.monitor.event.type.Event;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class LogEventHandler implements EventHandler {

    @Override
    @SuppressWarnings("InfiniteLoopStatement")
    public void run() {
        try {
            while (true) {
                Event event = eventBlockingQueue.take();
                log.info(event.toString());
            }
        } catch (Exception e) {
            log.warn("Issue processing event", e);
        }
    }
}
