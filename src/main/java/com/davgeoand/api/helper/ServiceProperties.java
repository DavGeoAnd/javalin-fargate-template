package com.davgeoand.api.helper;

import com.davgeoand.api.exception.MissingPropertyException;
import io.micrometer.core.instrument.ImmutableTag;
import io.micrometer.core.instrument.Tag;
import io.opentelemetry.sdk.autoconfigure.spi.ResourceProvider;
import io.opentelemetry.sdk.autoconfigure.spi.internal.DefaultConfigProperties;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.*;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ServiceProperties {
    private static Properties properties;
    private static final Pattern propertyPattern = Pattern.compile("\\[\\[.*::.*\\]\\]");
    @Getter
    static Map<String, String> commonAttributesMap = new HashMap<>();
    @Getter
    static Map<String, String> infoPropertiesMap = new HashMap<>();

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

        systemVariables();
        assessProperties();
        setOtlpProperties();
        setCommonAttributesMap();
        setInfoPropertiesMap();
        log.info("Successfully initialized service properties");
    }

    private static void systemVariables() {
        Map<String, String> env = System.getenv();
        for (String envName : env.keySet()) {
            log.info(envName + " : " + env.get(envName));
        }
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

    private static void setOtlpProperties() {
        log.info("Setting opentelemetry properties");

        Supplier<Map<String, String>> propertiesSupplier = Collections::emptyMap;
        ServiceLoader<ResourceProvider> serviceLoader = ServiceLoader.load(ResourceProvider.class);
        serviceLoader.forEach((resourceProvider -> {
            log.info(resourceProvider.toString());
            log.info(resourceProvider.createResource(DefaultConfigProperties.create(propertiesSupplier.get())).toString());

            resourceProvider.createResource(DefaultConfigProperties.create(propertiesSupplier.get())).getAttributes().forEach(((attributeKey, o) -> {
                properties.put(attributeKey.getKey(), o.toString());
            }));
        }));

        log.info("Successfully set opentelemetry properties");
    }

    private static void setCommonAttributesMap() {
        log.info("Setting common attributes as a map");
        String commonAttributesProperty = "service.common.attributes";
        String[] commonAttributes = getProperty(commonAttributesProperty)
                .orElseThrow(() -> new MissingPropertyException(commonAttributesProperty))
                .split(",");
        for (String attribute : commonAttributes) {
            getProperty(attribute).ifPresentOrElse(
                    (attr) -> commonAttributesMap.put(attribute, attr),
                    () -> log.warn(attribute + " is not available")
            );
        }
        log.info("Successfully set common attributes as a map");
    }

    private static void setInfoPropertiesMap() {
        log.info("Setting info properties as a map");
        String infoPropertiesProperty = "service.info.properties";
        String[] infoProperties = getProperty(infoPropertiesProperty)
                .orElseThrow(() -> new MissingPropertyException(infoPropertiesProperty))
                .split(",");
        for (String infoProperty : infoProperties) {
            infoPropertiesMap.put(infoProperty, getProperty(infoProperty).orElseThrow(() -> new MissingPropertyException(infoProperty)));
        }
        log.info("Successfully set info properties as a map");
    }

    public static Map<String, String> getCommonAndInfoProperties() {
        log.info("Getting common and info properties");
        Map<String, String> commonInfoMap = new HashMap<>(commonAttributesMap);
        commonInfoMap.putAll(infoPropertiesMap);
        log.info("Successfully got common and info properties");
        return commonInfoMap;
    }

    public static Optional<String> getProperty(String property) {
        return Optional.ofNullable(properties.getProperty(property));
    }

    public static Iterable<Tag> getCommonAttributeTags() {
        ArrayList<Tag> tagsIterable = new ArrayList<>();
        commonAttributesMap.forEach((key, value) -> tagsIterable.add(new ImmutableTag(key, value)));
        return tagsIterable;
    }
}
