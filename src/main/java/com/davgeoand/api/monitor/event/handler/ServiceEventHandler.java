package com.davgeoand.api.monitor.event.handler;

import com.davgeoand.api.exception.MissingPropertyException;
import com.davgeoand.api.helper.Constants;
import com.davgeoand.api.helper.ServiceProperties;
import com.davgeoand.api.monitor.event.type.Event;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.Instant;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ServiceEventHandler {
    static EventHandler eventHandler;

    public static void init() {
        log.info("Initializing service event handler");
        String eventHandlerType = ServiceProperties.getProperty(Constants.SERVICE_EVENT_HANDLER_TYPE).orElseThrow(() -> new MissingPropertyException(Constants.SERVICE_EVENT_HANDLER_TYPE));
        switch (eventHandlerType) {
            case "log" -> eventHandler = new LogEventHandler();
            case "influxdb" -> eventHandler = new InfluxEventHandler();
            default -> {
                log.warn("Invalid service event handler type. Defaulting to LogEventHandler");
                eventHandler = new LogEventHandler();
            }
        }
        Thread eventHandlerThread = new Thread(eventHandler);
        eventHandlerThread.setName("ServiceEventHandler");
        eventHandlerThread.start();
        log.info("Successfully initialized service event handler");
    }

    public static void addEvent(Event event) {
        event.setTime(Instant.now());
        eventHandler.addEventToQueue(event);
    }
}
