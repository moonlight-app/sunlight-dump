package ru.moonlight.sunlight.dump.model.api;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public record OrderData(
        @JsonProperty("content") Content content
) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Content(
            @JsonProperty("sizes") Map<Float, Object> sizes
    ) { }

}
