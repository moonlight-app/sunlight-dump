package ru.moonlight.sunlight.dump.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public record OrderData(
        @JsonProperty("content") Content content
) {

    public float[] getSizes() {
        if (content == null || !content.isSized())
            return null;

        Map<Float, Object> sizes = content.sizes();
        if (sizes == null || sizes.isEmpty())
            return null;

        int i = 0;
        float[] result = new float[sizes.size()];
        for (Float key : sizes.keySet())
            result[i++] = key;

        return result;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Content(
            @JsonProperty("sizes") Map<Float, Object> sizes,
            @JsonProperty("is_sized") boolean isSized
    ) { }

}
