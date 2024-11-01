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
        @JsonProperty("brand") String brand,
        @JsonProperty("model") String model,
        @JsonProperty("price") float price,
        @JsonProperty("sizes") float[] sizes,
        @JsonProperty("audiences") Audience[] audiences,
        @JsonProperty("material") Material[] materials,
        @JsonProperty("treasures") Treasure[] treasures,
        @JsonProperty("preview_url") String imageUrl
) implements SunlightDumpModel {

    public static SunlightFullItem merge(SunlightCatalogItem item, SunlightItemDetails details) {
        Audience[] audiences = null;
        if (item.audiences() != null && !item.audiences().isEmpty())
            audiences = item.audiences().toArray(Audience[]::new);

        return new SunlightFullItem(
                item.article(),
                item.type(),
                item.name(),
                item.brand(),
                details.model(),
                item.price(),
                details.sizes(),
                audiences,
                details.materials(),
                details.treasures(),
                item.imageUrl()
        );
    }

}
