package ru.moonlight.sunlight.dump.model.attribute;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum Audience implements KeyedEnum {

    ALL         ("all",         0),
    MEN         ("men",         446),
    WOMEN       ("women",       447),
    CHILDREN    ("children",    1533),
    UNISEX      ("unisex",      594),
    ;

    @JsonValue
    private final String key;
    private final int sunlightId; // query param = 'gender_position'

}
