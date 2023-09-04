package com.davgeoand.api.monitor.event.type;

import com.influxdb.client.write.Point;
import lombok.Data;

import java.time.Instant;

@Data
public abstract class Event {
    protected Instant time;

    public abstract Point toPoint();
}
