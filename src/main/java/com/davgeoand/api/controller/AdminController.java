package com.davgeoand.api.controller;

import com.davgeoand.api.helper.ServiceProperties;
import com.davgeoand.api.monitor.metric.ServiceMetricHandler;
import io.javalin.apibuilder.EndpointGroup;
import io.javalin.http.Context;
import io.javalin.http.HttpStatus;
import io.micrometer.core.instrument.Meter;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;

import static io.javalin.apibuilder.ApiBuilder.before;
import static io.javalin.apibuilder.ApiBuilder.get;

@Slf4j
public class AdminController {
    public static EndpointGroup getAdminEndpoints() {
        log.info("Returning api endpoints");
        return () -> {
            before(context -> {
                context.attribute("audit", true);
            });
            get("metrics", AdminController::metrics);
            get("info", AdminController::info);
        };
    }

    private static void metrics(Context context) {
        log.info("Starting admin metrics request");
        ArrayList<Meter> meterArrayList = new ArrayList<>();
        ServiceMetricHandler.getMeterRegistry().forEachMeter((meterArrayList::add));
        context.json(meterArrayList);
        context.status(HttpStatus.OK);
        log.info("Finished admin metrics request");
    }

    private static void info(Context context) {
        log.info("Starting admin info request");
        context.json(ServiceProperties.getInfoPropertiesMap());
        context.status(HttpStatus.OK);
        log.info("Finished admin info request");
    }
}
