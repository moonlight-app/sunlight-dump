package ru.moonlight.sunlight.dump.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import ru.moonlight.sunlight.dump.model.attribute.Audience;
import ru.moonlight.sunlight.dump.model.attribute.ProductType;

import java.util.*;

public record SunlightCatalogItem(
        @JsonProperty("article") long article,
        @JsonProperty("type") ProductType type,
        @JsonProperty("name") String name,
        @JsonProperty("brand") String brand,
        @JsonProperty("price") float price,
        @JsonProperty("audiences") SortedSet<Audience> audiences,
        @JsonProperty("preview_url") String imageUrl,
        @JsonProperty("page_url") String pageUrl
) implements SunlightDumpModel {

    public static List<SunlightCatalogItem> lookupItems(ProductType productType, Audience audience, Element element) {
        Elements elements = element.select("section.cl-outer-cont div.cl-item");
        if (elements.isEmpty())
            return Collections.emptyList();

        return elements.stream()
                .map(node -> fromElement(productType, audience, node))
                .toList();
    }

    public static SunlightCatalogItem fromElement(ProductType productType, Audience audience, Element element) {
        long article = Long.parseLong(element.attr("data-analytics-article"));
        String name = element.attr("data-analytics-name");
        String brand = element.attr("data-analytics-brand");
        float price = Float.parseFloat(element.attr("data-analytics-price"));

        SortedSet<Audience> audiences = new TreeSet<>();
        audiences.add(audience);

        Element imageElement = element.selectFirst("img.cl-item-img-lazy");
        String imageUrl = imageElement != null ? imageElement.attr("src") : null;

        Element pageElement = element.selectFirst("a.cl-item-link");
        String pageUrl = pageElement != null ? pageElement.attr("href") : null;

        return new SunlightCatalogItem(article, productType, name, brand, price, audiences, imageUrl, pageUrl);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SunlightCatalogItem that = (SunlightCatalogItem) o;
        return article == that.article;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(article);
    }

}
