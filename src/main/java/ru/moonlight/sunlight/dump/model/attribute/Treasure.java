package ru.moonlight.sunlight.dump.model.attribute;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum Treasure implements KeyedEnum {

    DIAMOND     ("diamond",     13,     "бриллиант"),
    SAPPHIRE    ("sapphire",    15,     "сапфир"),
    PEARL       ("pearl",       17,     "жемчуг"),
    AMETHYST    ("amethyst",    22,     "аметист"),
    FIANIT      ("fianit",      322,    "фианит"),
    EMERALD     ("emerald",     31,     "изумруд"),
    RUBY        ("ruby",        304,    "рубин"),
    ;

    @JsonValue
    private final String key;
    private final int sunlightId; // query param = 'stone'
    private final String sunlightKey;

}
