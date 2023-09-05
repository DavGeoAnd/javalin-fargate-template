package com.davgeoand.api.controller;

import com.davgeoand.api.helper.ServiceProperties;
import com.davgeoand.api.monitor.metric.ServiceMetricHandler;
import io.javalin.apibuilder.EndpointGroup;
import io.javalin.http.Context;
import io.javalin.http.HttpStatus;
import io.micrometer.core.instrument.Meter;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;
import java.util.ArrayList;

import static io.javalin.apibuilder.ApiBuilder.get;

@Slf4j
public class AdminController {
    public static EndpointGroup getAdminEndpoints() {
        log.info("Returning api endpoints");
        return () -> {
            get("metrics", AdminController::metrics);
            get("info", AdminController::info);
            get("aws", AdminController::aws);
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

    private static void aws(Context context) throws IOException {
        log.info("Starting admin aws request");
        String ecsMetadataUrl = System.getenv("ECS_CONTAINER_METADATA_URI_V4") + "/task";
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(ecsMetadataUrl)
                .build();
        Call call = client.newCall(request);
        Response response = call.execute();
        context.json(response);
        context.status(HttpStatus.OK);
        log.info("Finished admin aws request");
    }
}
