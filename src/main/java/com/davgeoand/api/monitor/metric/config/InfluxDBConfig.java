package com.davgeoand.api.monitor.metric.config;

import com.davgeoand.api.exception.MissingPropertyException;
import com.davgeoand.api.helper.Constants;
import com.davgeoand.api.helper.ServiceProperties;
import io.micrometer.influx.InfluxConfig;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;

public class InfluxDBConfig implements InfluxConfig {
    @Override
    public String get(@NotNull String s) {
        return null;
    }

    @Override
    public @NotNull String uri() {
        return ServiceProperties.getProperty(Constants.SERVICE_METRIC_REGISTRY_INFLUXDB_URI).orElseThrow(() -> new MissingPropertyException(Constants.SERVICE_METRIC_REGISTRY_INFLUXDB_URI));
    }

    @Override
    public String org() {
        return ServiceProperties.getProperty(Constants.SERVICE_METRIC_REGISTRY_INFLUXDB_ORG).orElseThrow(() -> new MissingPropertyException(Constants.SERVICE_METRIC_REGISTRY_INFLUXDB_ORG));
    }

    @Override
    public @NotNull String bucket() {
        return ServiceProperties.getProperty(Constants.SERVICE_METRIC_REGISTRY_INFLUXDB_BUCKET).orElseThrow(() -> new MissingPropertyException(Constants.SERVICE_METRIC_REGISTRY_INFLUXDB_BUCKET));
    }

    @Override
    public String token() {
        return ServiceProperties.getProperty(Constants.SERVICE_METRIC_REGISTRY_INFLUXDB_TOKEN).orElseThrow(() -> new MissingPropertyException(Constants.SERVICE_METRIC_REGISTRY_INFLUXDB_TOKEN));
    }

    @Override
    public @NotNull Duration step() {
        return Duration.ofSeconds(Integer.parseInt(ServiceProperties.getProperty(Constants.SERVICE_METRIC_REGISTRY_INFLUXDB_STEP).orElseThrow(() -> new MissingPropertyException(Constants.SERVICE_METRIC_REGISTRY_INFLUXDB_STEP))));
    }
}