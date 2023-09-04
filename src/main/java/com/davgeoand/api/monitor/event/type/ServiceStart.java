package com.davgeoand.api.monitor.event.type;

import com.influxdb.client.domain.WritePrecision;
import com.influxdb.client.write.Point;
import lombok.Builder;
import lombok.ToString;

@Builder(toBuilder = true)
@ToString(callSuper = true)
public class ServiceStart extends Event {
    private String buildVersion;
    private String gitBranch;
    private String gitCommitId;
    private long startTime;
    private long serviceStartDuration;
    private String javaVersion;

    @Override
    public Point toPoint() {
        return Point.measurement("service_start")
                .addField("build_version", buildVersion)
                .addField("git_branch", gitBranch)
                .addField("git_commit_id", gitCommitId)
                .addField("start_time", startTime)
                .addField("service_start_duration", serviceStartDuration)
                .addField("java_version", javaVersion)
                .time(time, WritePrecision.MS);
    }
}
