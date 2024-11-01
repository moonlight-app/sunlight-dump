package ru.moonlight.sunlight.dump.model.attribute;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum Audience implements KeyedEnum {

    ALL         ("all",         0,      0),
    MEN         ("men",         446,    1),
    WOMEN       ("women",       447,    2),
    CHILDREN    ("children",    1533,   3),
    UNISEX      ("unisex",      594,    4),
    ;

    @JsonValue
    private final String key;
    private final int sunlightId; // query param = 'gender_position'
    private final int moonlightId;

}
