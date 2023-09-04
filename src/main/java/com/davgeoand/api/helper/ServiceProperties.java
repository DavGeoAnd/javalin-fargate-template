package com.davgeoand.api.helper;

import io.opentelemetry.instrumentation.resources.*;
import io.opentelemetry.sdk.autoconfigure.ResourceConfiguration;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.Optional;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ServiceProperties {
    private static Properties properties;
    private static final Pattern propertyPattern = Pattern.compile("\\[\\[.*::.*\\]\\]");

    public static void init(String... files) {
        log.info("Initializing service properties");
        ServiceProperties.properties = new Properties();
        for (String file : files) {
            try {
                properties.load(ServiceProperties.class.getClassLoader().getResourceAsStream(file));
            } catch (IOException e) {
                log.error(e.getMessage());
                System.exit(1);
            }
        }
        ContainerResource.get().getAttributes().forEach((attribute, value) -> {
            log.info(attribute.getKey() + " : " + value.toString());
            properties.put(attribute.getKey(), value.toString());
        });
        HostResource.get().getAttributes().forEach((attribute, value) -> {
            log.info(attribute.getKey() + " : " + value.toString());
            properties.put(attribute.getKey(), value.toString());
        });
        OsResource.get().getAttributes().forEach((attribute, value) -> {
            log.info(attribute.getKey() + " : " + value.toString());
            properties.put(attribute.getKey(), value.toString());
        });
        ProcessResource.get().getAttributes().forEach((attribute, value) -> {
            log.info(attribute.getKey() + " : " + value.toString());
            properties.put(attribute.getKey(), value.toString());
        });
        ProcessRuntimeResource.get().getAttributes().forEach((attribute, value) -> {
            log.info(attribute.getKey() + " : " + value.toString());
            properties.put(attribute.getKey(), value.toString());
        });
        ResourceConfiguration.createEnvironmentResource().getAttributes().forEach((attribute, value) -> {
            log.info(attribute.getKey() + " : " + value.toString());
            properties.put(attribute.getKey(), value.toString());
        });

        assessProperties();
        log.info("Successfully initialized service properties");
    }

    private static void assessProperties() {
        properties.forEach((key, value) -> {
            log.info(key + ": " + value);
            String valueStr = value.toString();
            Matcher match = propertyPattern.matcher(valueStr);
            if (match.find()) {
                log.info("Property that uses env: " + key + " with value " + value);
                String valueStrUpdated = valueStr.replace("[", "").replace("]", "");
                String[] valueSplit = valueStrUpdated.split("::");
                String envValue = System.getenv(valueSplit[0]);
                if (envValue != null) {
                    properties.replace(key, envValue);
                    log.info(key + " is using env value: " + envValue);
                } else {
                    properties.replace(key, valueSplit[1]);
                    log.info(key + " is using default value: " + valueSplit[1]);
                }
            }
        });
    }

    public static Optional<String> getProperty(String property) {
        return Optional.ofNullable(properties.getProperty(property));
    }
}
