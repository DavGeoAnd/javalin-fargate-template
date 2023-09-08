package com.davgeoand.api.monitor.event.type;

import com.influxdb.client.domain.WritePrecision;
import com.influxdb.client.write.Point;
import lombok.Builder;
import lombok.ToString;

import java.util.HashMap;
import java.util.Map;

@Builder(toBuilder = true)
@ToString(callSuper = true)
public class ServiceStart extends Event {
    private Map<String, String> infoProperties;
    private long startTime;
    private long serviceStartDuration;

    @Override
    public Point toPoint() {
        Map<String, Object> replaceCharMap = new HashMap<>();
        infoProperties.forEach((key, value) -> {
            String newKey = key.replace('.', '_');
            replaceCharMap.put(newKey, value);
        });
        return Point.measurement("service_start")
                .addFields(replaceCharMap)
                .addField("start_time", startTime)
                .addField("service_start_duration", serviceStartDuration)
                .time(time, WritePrecision.MS);
    }
}
