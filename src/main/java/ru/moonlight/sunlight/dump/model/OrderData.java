package ru.moonlight.sunlight.dump.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;
import java.util.Objects;

public record OrderData(
        @JsonProperty("content") Content content
) {

    public String[] getSizes() {
        if (content == null || !content.isSized())
            return null;

        Map<String, SizeInfo> sizes = content.sizes();
        if (sizes == null || sizes.isEmpty())
            return null;

        return sizes.values().stream()
                .map(SizeInfo::relevantLabel)
                .filter(Objects::nonNull)
                .toArray(String[]::new);
    }

    public record Content(
            @JsonProperty("sizes") Map<String, SizeInfo> sizes,
            @JsonProperty("is_sized") boolean isSized
    ) { }

    public record SizeInfo(
            @JsonProperty("label") String label,
            @JsonProperty("label_expanded") String labelExpanded
    ) {

        public String relevantLabel() {
            return labelExpanded != null ? labelExpanded : label;
        }

    }

}
