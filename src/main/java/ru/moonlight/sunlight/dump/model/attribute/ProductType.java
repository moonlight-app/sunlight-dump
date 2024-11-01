package ru.moonlight.sunlight.dump.model.attribute;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ProductType implements KeyedEnum {

    RING        ("ring",        5,      1),
    BRACELET    ("bracelet",    317,    2),
    CHAIN       ("chain",       319,    3),
    WATCH       ("watch",       318,    4),
    EARRINGS    ("earrings",    4,      5),
    NECKLACE    ("necklace",    365,    6),
    ;

    @JsonValue
    private final String key;
    private final int sunlightId; // query param = 'product_type'
    private final int moonlightId;

}
