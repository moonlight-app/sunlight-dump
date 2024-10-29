package ru.moonlight.sunlight.dump.model;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum SunlightSupremeItem {

    RING        ("ring", 128, "https://ekb.sunlight.net/catalog/rings.html"),
    BRACELET    ("bracelet", 29, "https://sunlight.net/catalog/bracelets.html"),
    CHAIN       ("chain", 10, "https://sunlight.net/catalog/chains.html"),
    WATCH       ("watch", 45, "https://sunlight.net/catalog/clock"),
    EARRINGS    ("earrings", 145, "https://sunlight.net/catalog/earrings.html"),
    NECKLACE    ("necklace", 28, "https://sunlight.net/catalog/necklace.html"),
    ;

    @JsonValue
    private final String key;
    private final int totalPages;
    private final String url;

}
