package com.davgeoand.api.monitor.event.type;

import com.influxdb.client.domain.WritePrecision;
import lombok.Builder;
import lombok.ToString;
import com.influxdb.client.write.Point;

@Builder(toBuilder = true)
@ToString(callSuper = true)
public class Audit extends Event {
    private String requestPath;
    private String status;
    private String method;
    private String response;
    private Float requestDuration;
    private String traceId;

    @Override
    public Point toPoint() {
        return Point.measurement("audit")
                .addTag("request_path", requestPath)
                .addTag("status", status)
                .addTag("method", method)
                .addField("response", response)
                .addField("request_duration", requestDuration)
                .addField("trace_id", traceId)
                .time(time, WritePrecision.MS);
    }
}
