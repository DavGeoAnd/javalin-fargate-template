package com.davgeoand.api.monitor.metric;

import com.davgeoand.api.helper.ServiceProperties;
import io.javalin.micrometer.MicrometerPlugin;
import io.javalin.plugin.Plugin;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Metrics;
import io.micrometer.core.instrument.binder.jvm.*;
import io.micrometer.core.instrument.binder.logging.LogbackMetrics;
import io.micrometer.core.instrument.binder.system.FileDescriptorMetrics;
import io.micrometer.core.instrument.binder.system.ProcessorMetrics;
import io.micrometer.core.instrument.binder.system.UptimeMetrics;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ServiceMetricHandler {
    @Getter
    private static MeterRegistry meterRegistry;

    public static void init() {
        log.info("Initializing service metric handler");
        meterRegistry = Metrics.globalRegistry;
        meterRegistry.config().commonTags(ServiceProperties.getCommonAttributeTags());
        new ClassLoaderMetrics().bindTo(meterRegistry);
        new JvmCompilationMetrics().bindTo(meterRegistry);
        new JvmGcMetrics().bindTo(meterRegistry);
        new JvmHeapPressureMetrics().bindTo(meterRegistry);
        new JvmInfoMetrics().bindTo(meterRegistry);
        new JvmMemoryMetrics().bindTo(meterRegistry);
        new JvmThreadMetrics().bindTo(meterRegistry);
        new LogbackMetrics().bindTo(meterRegistry);
        new FileDescriptorMetrics().bindTo(meterRegistry);
        new ProcessorMetrics().bindTo(meterRegistry);
        new UptimeMetrics().bindTo(meterRegistry);
        log.info("Successfully initialized service metric handler");
    }

    public static Plugin getMicrometerPlugin() {
        return MicrometerPlugin.Companion.create(micrometerConfig -> micrometerConfig.registry = meterRegistry);
    }
}
