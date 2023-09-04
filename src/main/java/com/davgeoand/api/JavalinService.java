package com.davgeoand.api;

import com.davgeoand.api.controller.AdminController;
import com.davgeoand.api.exception.MissingPropertyException;
import com.davgeoand.api.helper.Constants;
import com.davgeoand.api.helper.ServiceProperties;
import com.davgeoand.api.monitor.event.handler.ServiceEventHandler;
import com.davgeoand.api.monitor.event.type.ServiceStart;
import com.davgeoand.api.monitor.metric.ServiceMetricHandler;
import io.javalin.Javalin;
import io.javalin.http.HttpStatus;
import lombok.extern.slf4j.Slf4j;

import java.lang.management.ManagementFactory;

import static io.javalin.apibuilder.ApiBuilder.get;
import static io.javalin.apibuilder.ApiBuilder.path;

@Slf4j
public class JavalinService {
    private final Javalin javalin;
    private long startServiceTime;

    public JavalinService() {
        log.info("Initializing javalin-fargate-template");
        javalin = Javalin.create(javalinConfig -> {
            javalinConfig.routing.contextPath = "template";
        });
        startingSteps();
        log.info("Successfully initialized javalin-fargate-template");
    }

    public void start() {
        log.info("Starting javalin-fargate-template");
        startServiceTime = System.currentTimeMillis();
        javalin.start(Integer.parseInt(ServiceProperties.getProperty(Constants.SERVICE_PORT).orElseThrow(() -> new MissingPropertyException(Constants.SERVICE_PORT))));
        log.info("Successfully started javalin-fargate-template");
    }

    private void startingSteps() {
        log.info("Setting up start steps");
        try {
            javalin.events((eventListener -> {
                eventListener.serverStarting(() -> {
                    serviceMetrics();
                    serviceEvents();
                    routes();
                });
            }));
        } catch (Exception e) {
            log.error("Issue during startup", e);
            System.exit(1);
        }
        log.info("Successfully set up start steps");
    }

    private void serviceMetrics() {
        log.info("Setting up service metrics");
        javalin.updateConfig((javalinConfig -> {
            javalinConfig.plugins.register(ServiceMetricHandler.getMicrometerPlugin());
        }));
        log.info("Successfully set up service metrics");
    }

    private void serviceEvents() {
        log.info("Setting up service events");
        eventServiceStart();
        log.info("Successfully set up service events");
    }

    private void eventServiceStart() {
        log.info("Setting up ServiceStart event");
        javalin.events(eventListener -> {
            eventListener.serverStarted(() -> {
                long serviceStartDuration = System.currentTimeMillis() - startServiceTime;
                try {
                    String buildVersion = ServiceProperties.getProperty(Constants.SERVICE_VERSION).orElseThrow(() -> new MissingPropertyException(Constants.SERVICE_VERSION));
                    String gitBranch = ServiceProperties.getProperty(Constants.GIT_BRANCH).orElseThrow(() -> new MissingPropertyException(Constants.GIT_BRANCH));
                    String gitCommitId = ServiceProperties.getProperty(Constants.GIT_COMMIT).orElseThrow(() -> new MissingPropertyException(Constants.GIT_COMMIT));
                    String javaVersion = ServiceProperties.getProperty(Constants.PROCESS_RUNTIME_VERSION).orElseThrow(() -> new MissingPropertyException(Constants.PROCESS_RUNTIME_VERSION));
                    long startTime = ManagementFactory.getRuntimeMXBean().getStartTime();
                    ServiceEventHandler.addEvent(ServiceStart.builder()
                            .serviceStartDuration(serviceStartDuration)
                            .buildVersion(buildVersion)
                            .gitBranch(gitBranch)
                            .gitCommitId(gitCommitId)
                            .javaVersion(javaVersion)
                            .startTime(startTime)
                            .build());
                } catch (MissingPropertyException missingPropertyException) {
                    log.error("Not able to add ServiceStart event", missingPropertyException);
                }
            });
        });
        log.info("Successfully set up ServiceStart event");
    }

    private void routes() {
        log.info("Setting up routes");
        javalin.routes(() -> {
            get("health", context -> context.status(HttpStatus.OK));
            path("admin", AdminController.getAdminEndpoints());
        });
        log.info("Successfully set up routes");
    }

}
