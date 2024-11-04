package ru.moonlight.sunlight.dump.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import ru.moonlight.sunlight.dump.model.attribute.Audience;
import ru.moonlight.sunlight.dump.model.attribute.Material;
import ru.moonlight.sunlight.dump.model.attribute.ProductType;
import ru.moonlight.sunlight.dump.model.attribute.Treasure;
import ru.moonlight.sunlight.dump.model.size.ProductSize;

import java.util.HashMap;
import java.util.Map;

public record SunlightFullItem(
        @JsonProperty("global_index") int index,
        @JsonProperty("article") long article,
        @JsonProperty("type") ProductType type,
        @JsonProperty("name") String name,
        @JsonProperty("price") float price,
        @JsonProperty("sizes") String[] sizes,
        @JsonProperty("audiences") Audience[] audiences,
        @JsonProperty("materials") Material[] materials,
        @JsonProperty("sample") String sample,
        @JsonProperty("sample_type") String sampleType,
        @JsonProperty("treasures") Treasure[] treasures,
        @JsonProperty("weight") Float weight,
        @JsonProperty("preview_url") String imageUrl,
        @JsonProperty("description") String description
) implements SunlightDumpModel {

    private static final Map<String, ProductSize> SIZE_PARSER_CACHE = new HashMap<>();

    public ProductSize[] parseSizes() {
        if (sizes == null || sizes.length == 0)
            return null;

        ProductSize[] result = new ProductSize[sizes.length];
        float[] bounds = new float[2];

        for (int i = 0; i < sizes.length; i++) {
            String rawSize = sizes[i];

            try {
                float value = Float.parseFloat(rawSize);
                result[i] = ProductSize.asStatic(value);
            } catch (NumberFormatException ignored) {
                String[] rawBounds = rawSize.split("–");
                if (rawBounds.length != 2)
                    throw new IllegalArgumentException("Invalid sizes: " + rawSize);

                float step = type.getSizeSequenceStep();
                String cacheKey = step + "_" + rawSize;

                ProductSize parsedSize = SIZE_PARSER_CACHE.get(cacheKey);
                if (parsedSize == null) {
                    try {
                        bounds[0] = Float.parseFloat(rawBounds[0]);
                        bounds[1] = Float.parseFloat(rawBounds[1]);
                        parsedSize = ProductSize.asRange(bounds[0], bounds[1], step);
                        SIZE_PARSER_CACHE.put(cacheKey, parsedSize);
                    } catch (NumberFormatException ignored2) {
                        throw new IllegalArgumentException("Invalid sizes: " + rawSize);
                    }
                }

                result[i] = parsedSize;
            }
        }

        return result;
    }

    public static SunlightFullItem merge(SunlightCatalogItem item, SunlightItemDetails details) {
        Audience[] audiences = null;
        if (item.audiences() != null && !item.audiences().isEmpty())
            audiences = item.audiences().toArray(Audience[]::new);

        return new SunlightFullItem(
                item.index(),
                item.article(),
                details.type(),
                item.name(),
                item.price(),
                details.sizes(),
                audiences,
                details.materials(),
                details.sample(),
                details.sampleType(),
                details.treasures(),
                details.weight(),
                item.imageUrl(),
                details.description()
        );
    }

}
