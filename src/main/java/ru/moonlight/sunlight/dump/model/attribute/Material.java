package ru.moonlight.sunlight.dump.model.attribute;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum Material implements KeyedEnum {

    PLATINUM        ("platinum",        3603,   "платина"),
    GOLD            ("gold",            632,    "золото"),
    SILVER          ("silver",          329,    "серебро"),
    PINK_GOLD       ("pink_gold",       327,    "розовое золото"),
    WHITE_GOLD      ("white_gold",      328,    "белое золото"),
    YELLOW_GOLD     ("yellow_gold",     326,    "желтое золото"),
    JEWELRY_STEEL   ("jewelry_steel",   419,    "ювелирная сталь"),
    CERAMICS        ("ceramics",        1556,   "керамика"),
    GENUINE_LEATHER ("genuine_leather", 3176,   "натуральная кожа"),
    RUBBER          ("rubber",          1558,   "каучук"),
    ;

    @JsonValue
    private final String key;
    private final int sunlightId; // query param = 'material'
    private final String sunlightKey;

}
