package com.davgeoand.api.monitor.event.handler;

import com.davgeoand.api.exception.MissingPropertyException;
import com.davgeoand.api.helper.Constants;
import com.davgeoand.api.helper.ServiceProperties;
import com.davgeoand.api.monitor.event.type.Event;
import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.InfluxDBClientFactory;
import com.influxdb.client.WriteApiBlocking;
import com.influxdb.client.write.Point;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

@Slf4j
public class InfluxEventHandler implements EventHandler {
    private final WriteApiBlocking writeApiBlocking;
    private final Map<String, String> commonTagMap = new HashMap<>();

    public InfluxEventHandler() {
        log.info("Initializing InfluxDB ServiceEventHandler");
        InfluxDBClient influxDBClient = InfluxDBClientFactory.create(
                ServiceProperties.getProperty(Constants.SERVICE_EVENT_INFLUXDB_URL).orElseThrow(() -> new MissingPropertyException(Constants.SERVICE_EVENT_INFLUXDB_URL)),
                ServiceProperties.getProperty(Constants.SERVICE_EVENT_INFLUXDB_TOKEN).orElseThrow(() -> new MissingPropertyException(Constants.SERVICE_EVENT_INFLUXDB_TOKEN)).toCharArray(),
                ServiceProperties.getProperty(Constants.SERVICE_EVENT_INFLUXDB_ORG).orElseThrow(() -> new MissingPropertyException(Constants.SERVICE_EVENT_INFLUXDB_ORG)),
                ServiceProperties.getProperty(Constants.SERVICE_EVENT_INFLUXDB_BUCKET).orElseThrow(() -> new MissingPropertyException(Constants.SERVICE_EVENT_INFLUXDB_BUCKET))
        );
        log.info("Sending events to InfluxDB version: " + influxDBClient.version());
        writeApiBlocking = influxDBClient.getWriteApiBlocking();
        Map<String, String> tempCommonTagMap = ServiceProperties.getCommonAttributesMap();
        tempCommonTagMap.forEach((key, value) -> {
            String newKey = key.replace('.', '_');
            commonTagMap.put(newKey, value);
        });
        log.info("Successfully initialized InfluxDB ServiceEventHandler");
    }

    @Override
    @SuppressWarnings("InfiniteLoopStatement")
    public void run() {
        try {
            while (true) {
                Event event = eventBlockingQueue.take();
                Point eventPoint = event.toPoint();
                eventPoint.addTags(commonTagMap);
                writeApiBlocking.writePoint(eventPoint);
            }
        } catch (Exception e) {
            log.warn("Issue processing event", e);
        }
    }
}
