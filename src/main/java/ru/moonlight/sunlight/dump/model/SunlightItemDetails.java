package ru.moonlight.sunlight.dump.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import ru.moonlight.sunlight.dump.exception.SunlightParseException;
import ru.moonlight.sunlight.dump.model.attribute.Material;
import ru.moonlight.sunlight.dump.model.attribute.ProductType;
import ru.moonlight.sunlight.dump.model.attribute.Treasure;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

public record SunlightItemDetails(
        @JsonProperty("article") long article,
        @JsonProperty("type") ProductType type,
        @JsonProperty("sizes") float[] sizes,
        @JsonProperty("materials") Material[] materials,
        @JsonProperty("sample") String sample,
        @JsonProperty("sample_type") String sampleType,
        @JsonProperty("treasures") Treasure[] treasures,
        @JsonProperty("weight") Float weight,
        @JsonProperty("description") String description
) implements SunlightDumpModel {

    public static SunlightItemDetails fromElement(Element element, long article, Supplier<float[]> sizesProvider) {
        Map<String, String> attributes = lookupItemAttributes(element);
        if (!String.valueOf(article).equals(attributes.get("артикул")))
            throw new SunlightParseException("[%d]: item attribute mismatched!".formatted(article));

        Optional<ProductType> productType = ProductType.resolveBySunlightName(article, lookupProductTypeName(element));
        if (productType.isEmpty())
            return null;

        Material[] materials = Material.findBySunlightKeys(attributes.get("материал изделия")).orElse(null);
        String sample = attributes.get("проба");
        String sampleType = attributes.get("вид пробы");
        Treasure[] treasures = Treasure.findBySunlightKeys(attributes.get("вставка")).orElse(null);
        Float weight = parseWeight(article, attributes.get("вес изделия"));

        String description = lookupDescription(element);
        return new SunlightItemDetails(article, productType.get(), sizesProvider.get(), materials, sample, sampleType, treasures, weight, description);
    }

    private static String lookupProductTypeName(Element element) {
        Element nameElement = element.selectFirst(".supreme-product-card__header-breadcrumbs > span:nth-child(2)");
        return nameElement != null ? nameElement.wholeText() : null;
    }

    private static Map<String, String> lookupItemAttributes(Element element) {
        Map<String, String> attributes = new HashMap<>();
        parseItemAttributes(element.select("div.supreme-product-card__product-description dl"), attributes::put);
        parseItemAttributes(element.select("li.supreme-product-card-description__item"), attributes::put);
        return attributes;
    }

    private static String lookupDescription(Element element) {
        Element descElement = element.selectFirst("div.supreme-product-card__product-description div p");
        return descElement != null ? descElement.wholeText() : null;
    }

    private static void parseItemAttributes(Elements elements, BiConsumer<String, String> attributePairConsumer) {
        for (Element element : elements) {
            if (element.childrenSize() == 2) {
                String key = element.child(0).wholeText().toLowerCase();
                if (key.isBlank())
                    continue;

                String value = element.child(1).wholeText();
                if (value.isBlank())
                    continue;

                attributePairConsumer.accept(key, value);
            }
        }
    }

    private static Float parseWeight(long article, String weightString) {
        if (weightString == null || weightString.isEmpty())
            return null;

        String sourceString = weightString;
        if (sourceString.toLowerCase().endsWith("г"))
            sourceString = sourceString.substring(0, sourceString.length() - 1);

        try {
            return Float.parseFloat(sourceString.trim());
        } catch (NumberFormatException ex) {
            throw new SunlightParseException("[%d]: bad weight attribute '%s'!".formatted(article, weightString));
        }
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
