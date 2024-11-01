package ru.moonlight.sunlight.dump.model.attribute;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Getter
public enum ProductType implements KeyedEnum {

    RING        ("ring",        5,      "кольца"),
    BRACELET    ("bracelet",    317,    "браслеты"),
    CHAIN       ("chain",       319,    "цепи"),
    WATCH       ("watch",       318,    "часы"),
    EARRINGS    ("earrings",    4,      "серьги"),
    NECKLACE    ("necklace",    365,    "колье", "шейное украшение"),
    ;

    public static final String ALL_SUPPORTED = Stream.of(values())
            .map(ProductType::getSunlightId)
            .map(String::valueOf)
            .collect(Collectors.joining(","));

    @JsonValue
    private final String key;
    private final int sunlightId; // query param = 'product_type'
    private final String[] sunlightKeys;

    ProductType(String key, int sunlightId, String... sunlightKeys) {
        this.key = key;
        this.sunlightId = sunlightId;
        this.sunlightKeys = sunlightKeys;
    }

    public static Optional<ProductType> resolveBySunlightName(long article, String sunlightName) {
        if (sunlightName == null || sunlightName.isEmpty())
            return Optional.empty();

        sunlightName = sunlightName.trim().toLowerCase();
        for (ProductType productType : values())
            for (String sunlightKey : productType.getSunlightKeys())
                if (sunlightName.contains(sunlightKey))
                    return Optional.of(productType);

        System.err.printf("[%d]: Unable to resolve product type by name '%s'!%n", article, sunlightName);
        return Optional.empty();
    }

}
