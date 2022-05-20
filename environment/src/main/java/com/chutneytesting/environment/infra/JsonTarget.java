package com.chutneytesting.environment.infra;

import com.chutneytesting.environment.domain.Target;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import java.util.Map;

@JsonDeserialize(using = TargetJsonDeserializer.class)
public class JsonTarget {

    public String name;
    public String url;
    public Map<String, String> properties;

    public JsonTarget() {
    }

    public JsonTarget(String name, String url, Map<String, String> properties) {
        this.name = name;
        this.url = url;
        this.properties = properties;
    }

    public static JsonTarget from(Target t) {
        return new JsonTarget(t.name, t.url, t.properties);
    }

    public Target toTarget(String envName) {
        return Target.builder()
            .withName(name)
            .withEnvironment(envName)
            .withUrl(url)
            .withProperties(properties)
            .build();
    }
}
