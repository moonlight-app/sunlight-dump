package ru.moonlight.sunlight.dump.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.jsoup.nodes.Element;
import ru.moonlight.sunlight.dump.exception.SunlightParseException;
import ru.moonlight.sunlight.dump.model.attribute.Material;
import ru.moonlight.sunlight.dump.model.attribute.Treasure;

import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public record SunlightItemDetails(
        @JsonProperty("id") UUID id,
        @JsonProperty("article") long article,
        @JsonProperty("model") String model,
        @JsonProperty("material") Material material,
        @JsonProperty("treasures") Treasure[] treasures
) {

    public static SunlightItemDetails fromElement(Element element) {
        Element idElement = element.selectFirst("div.supreme-product-card-same-button[product-id][article]");
        if (idElement == null)
            throw new SunlightParseException("ID element not found!");

        UUID id = UUID.fromString(idElement.attr("product-id"));
        long article = Long.parseLong(idElement.attr("article"));

        Map<String, String> attributes = lookupItemAttributes(element);
        if (!String.valueOf(article).equals(attributes.get("артикул")))
            throw new SunlightParseException("Item article mismatched!");

        String model = attributes.get("модель");
        Material material = Material.findBySunlightKey(attributes.get("материал изделия")).orElse(null);
        Treasure[] treasures = Treasure.findBySunlightKeys(attributes.get("вставка")).orElse(null);
        return new SunlightItemDetails(id, article, model, material, treasures);
    }

    private static Map<String, String> lookupItemAttributes(Element element) {
        return element.select("li.supreme-product-card-description__item").stream().collect(Collectors.toMap(
                descItem -> descItem.child(0).wholeText().toLowerCase(),
                descItem -> descItem.child(1).wholeText()
        ));
    }

}
