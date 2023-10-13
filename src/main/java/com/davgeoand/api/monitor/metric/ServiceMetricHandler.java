package com.davgeoand.api.monitor.metric;

import com.davgeoand.api.exception.MissingPropertyException;
import com.davgeoand.api.helper.Constants;
import com.davgeoand.api.helper.ServiceProperties;
import com.davgeoand.api.monitor.metric.config.InfluxDBConfig;
import io.javalin.micrometer.MicrometerPlugin;
import io.javalin.plugin.Plugin;
import io.micrometer.core.instrument.Clock;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Metrics;
import io.micrometer.influx.InfluxMeterRegistry;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ServiceMetricHandler {
    @Getter
    private static MeterRegistry meterRegistry;

    public static void init() {
        log.info("Initializing service metric handler");
        String serviceMetricRegistryType = ServiceProperties.getProperty(Constants.SERVICE_METRIC_REGISTRY_TYPE).orElseThrow(() -> new MissingPropertyException(Constants.SERVICE_METRIC_REGISTRY_TYPE));
        switch (serviceMetricRegistryType) {
            case "global" -> meterRegistry = Metrics.globalRegistry;
            case "influxdb" -> meterRegistry = new InfluxMeterRegistry(new InfluxDBConfig(), Clock.SYSTEM);
            default -> {
                log.warn("Invalid service metric registry type. Defaulting to global registry");
                meterRegistry = Metrics.globalRegistry;
            }
        }
        meterRegistry.config().commonTags(ServiceProperties.getCommonAttributeTags());
//        new ClassLoaderMetrics().bindTo(meterRegistry);
//        new JvmCompilationMetrics().bindTo(meterRegistry);
//        new JvmGcMetrics().bindTo(meterRegistry);
//        new JvmHeapPressureMetrics().bindTo(meterRegistry);
//        new JvmInfoMetrics().bindTo(meterRegistry);
//        new JvmMemoryMetrics().bindTo(meterRegistry);
//        new JvmThreadMetrics().bindTo(meterRegistry);
//        new LogbackMetrics().bindTo(meterRegistry);
//        new FileDescriptorMetrics().bindTo(meterRegistry);
//        new ProcessorMetrics().bindTo(meterRegistry);
//        new UptimeMetrics().bindTo(meterRegistry);
        log.info("Successfully initialized service metric handler");
    }

    public static Plugin getMicrometerPlugin() {
        return MicrometerPlugin.Companion.create(micrometerConfig -> micrometerConfig.registry = meterRegistry);
    }
}
