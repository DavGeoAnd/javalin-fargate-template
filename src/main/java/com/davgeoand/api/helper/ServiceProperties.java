package com.davgeoand.api.helper;

import com.davgeoand.api.exception.MissingPropertyException;
import io.micrometer.core.instrument.ImmutableTag;
import io.micrometer.core.instrument.Tag;
import io.opentelemetry.instrumentation.resources.*;
import io.opentelemetry.sdk.autoconfigure.ResourceConfiguration;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.json.JSONObject;

import java.io.IOException;
import java.util.*;
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

        assessProperties();
        setOtlpProperties();
        setAwsProperties();
        setCommonAttributesMap();
        setInfoPropertiesMap();
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

    private static void setOtlpProperties() {
        log.info("Setting opentelemetry properties");
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
        log.info("Successfully set opentelemetry properties");
    }

    private static void setAwsProperties() {
        log.info("Setting aws properties");
        try {
            properties.put("aws.region", System.getenv("AWS_REGION"));
            String ecsMetadataUrl = System.getenv("ECS_CONTAINER_METADATA_URI_V4") + "/task";
            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder()
                    .url(ecsMetadataUrl)
                    .build();
            Call call = client.newCall(request);
            Response response = call.execute();
            String responseString = response.body().string();
            JSONObject jsonObject = new JSONObject(responseString);
            properties.put("aws.availability.zone", jsonObject.getString("AvailabilityZone"));
        } catch (Exception e) {
            log.warn("Issue setting aws properties", e);
        }
        log.info("Successfully set aws properties");
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
