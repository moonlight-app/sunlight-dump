package ru.moonlight.sunlight.dump.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.Collections;
import java.util.List;

public record SunlightCatalogItem(
        @JsonIgnore int index,
        @JsonProperty("article") long article,
        @JsonProperty("name") String name,
        @JsonProperty("brand") String brand,
        @JsonProperty("price") float price,
        @JsonProperty("preview_url") String imageUrl,
        @JsonProperty("page_url") String pageUrl
) {

    public static List<SunlightCatalogItem> lookupItems(Element element) {
        Elements elements = element.select("section.cl-outer-cont div.cl-item");
        if (elements.isEmpty())
            return Collections.emptyList();

        return elements.stream()
                .map(SunlightCatalogItem::fromElement)
                .toList();
    }

    public static SunlightCatalogItem fromElement(Element element) {
        int index = Integer.parseInt(element.attr("data-analytics-index"));
        long article = Long.parseLong(element.attr("data-analytics-article"));
        String name = element.attr("data-analytics-name");
        String brand = element.attr("data-analytics-brand");
        float price = Float.parseFloat(element.attr("data-analytics-price"));

        Element imageElement = element.selectFirst("img.cl-item-img-lazy");
        String imageUrl = imageElement != null ? imageElement.attr("src") : null;

        Element pageElement = element.selectFirst("a.cl-item-link");
        String pageUrl = pageElement != null ? pageElement.attr("href") : null;

        return new SunlightCatalogItem(index, article, name, brand, price, imageUrl, pageUrl);
    }

}
