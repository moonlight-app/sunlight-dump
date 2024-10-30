package ru.moonlight.sunlight.dump.model.attribute;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ProductType implements KeyedEnum {

    RING        ("ring",        5),
    BRACELET    ("bracelet",    317),
    CHAIN       ("chain",       319),
    WATCH       ("watch",       318),
    EARRINGS    ("earrings",    4),
    NECKLACE    ("necklace",    365),
    ;

    @JsonValue
    private final String key;
    private final int sunlightId; // query param = 'product_type'

}
