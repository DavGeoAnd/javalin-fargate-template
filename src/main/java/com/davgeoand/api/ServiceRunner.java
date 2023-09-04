package com.davgeoand.api;

import com.davgeoand.api.helper.ServiceProperties;
import com.davgeoand.api.monitor.event.handler.ServiceEventHandler;
import com.davgeoand.api.monitor.metric.ServiceMetricHandler;

public class ServiceRunner {
    public static void main(String[] args) {
        ServiceProperties.init("service.properties", "build.properties");
        ServiceMetricHandler.init();
        ServiceEventHandler.init();
        new JavalinService().start();
    }
}
