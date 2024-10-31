package ru.moonlight.sunlight.dump.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.UUID;

public record SunlightItemSizes(
        @JsonProperty("id") UUID id,
        @JsonProperty("article") long article,
        @JsonProperty("sizes") float[] sizes
) {

}
