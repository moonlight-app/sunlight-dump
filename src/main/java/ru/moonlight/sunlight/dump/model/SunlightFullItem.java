package ru.moonlight.sunlight.dump.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import ru.moonlight.sunlight.dump.model.attribute.Audience;
import ru.moonlight.sunlight.dump.model.attribute.Material;
import ru.moonlight.sunlight.dump.model.attribute.ProductType;
import ru.moonlight.sunlight.dump.model.attribute.Treasure;

public record SunlightFullItem(
        @JsonProperty("article") long article,
        @JsonProperty("type") ProductType type,
        @JsonProperty("name") String name,
        @JsonProperty("price") float price,
        @JsonProperty("global_index") int index,
        @JsonProperty("sizes") float[] sizes,
        @JsonProperty("audiences") Audience[] audiences,
        @JsonProperty("materials") Material[] materials,
        @JsonProperty("sample") String sample,
        @JsonProperty("sample_type") String sampleType,
        @JsonProperty("treasures") Treasure[] treasures,
        @JsonProperty("weight") Float weight,
        @JsonProperty("preview_url") String imageUrl,
        @JsonProperty("description") String description
) implements SunlightDumpModel {

    public static SunlightFullItem merge(SunlightCatalogItem item, SunlightItemDetails details) {
        Audience[] audiences = null;
        if (item.audiences() != null && !item.audiences().isEmpty())
            audiences = item.audiences().toArray(Audience[]::new);

        return new SunlightFullItem(
                item.article(),
                details.type(),
                item.name(),
                item.price(),
                item.index(),
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
