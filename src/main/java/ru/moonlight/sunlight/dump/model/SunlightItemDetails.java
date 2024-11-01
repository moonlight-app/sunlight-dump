package ru.moonlight.sunlight.dump.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.jsoup.nodes.Element;
import ru.moonlight.sunlight.dump.exception.SunlightParseException;
import ru.moonlight.sunlight.dump.model.attribute.Material;
import ru.moonlight.sunlight.dump.model.attribute.Treasure;

import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public record SunlightItemDetails(
        @JsonProperty("article") long article,
        @JsonProperty("model") String model,
        @JsonProperty("sizes") float[] sizes,
        @JsonProperty("material") Material[] materials,
        @JsonProperty("treasures") Treasure[] treasures
) implements SunlightDumpModel {

    public static SunlightItemDetails fromElement(Element element, long article, Supplier<float[]> sizesProvider) {
        Map<String, String> attributes = lookupItemAttributes(element);
        if (!String.valueOf(article).equals(attributes.get("артикул")))
            throw new SunlightParseException("Item article mismatched!");

        String model = attributes.get("модель");
        Material[] materials = Material.findBySunlightKeys(attributes.get("материал изделия")).orElse(null);
        Treasure[] treasures = Treasure.findBySunlightKeys(attributes.get("вставка")).orElse(null);
        return new SunlightItemDetails(article, model, sizesProvider.get(), materials, treasures);
    }

    private static Map<String, String> lookupItemAttributes(Element element) {
        return element.select("li.supreme-product-card-description__item").stream().collect(Collectors.toMap(
                descItem -> descItem.child(0).wholeText().toLowerCase(),
                descItem -> descItem.child(1).wholeText()
        ));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SunlightItemDetails that = (SunlightItemDetails) o;
        return article == that.article;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(article);
    }

}
