package ru.moonlight.sunlight.dump.model.attribute;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Getter
@AllArgsConstructor
public enum Material implements KeyedEnum {

    GOLD            ("gold",            632,    1,  "золото"),
    SILVER          ("silver",          329,    2,  "серебро"),
    PLATINUM        ("platinum",        3603,   4,  "платина"),
    WHITE_GOLD      ("white_gold",      328,    8,  "белое золото"),
    PINK_GOLD       ("pink_gold",       327,    16, "розовое золото"),
    CERAMICS        ("ceramics",        1556,   32, "керамика"),
    YELLOW_GOLD     ("yellow_gold",     326,    0,  "желтое золото"),
    JEWELRY_STEEL   ("jewelry_steel",   419,    0,  "ювелирная сталь"),
    GENUINE_LEATHER ("genuine_leather", 3176,   0,  "натуральная кожа"),
    RUBBER          ("rubber",          1558,   0,  "каучук"),
    UNKNOWN         ("unknown",         0,      0,  null),
    ;

    @JsonValue
    private final String key;
    private final int sunlightId; // query param = 'material'
    private final int moonlightId;
    private final String sunlightKey;

    public static Optional<Material[]> findBySunlightKeys(String sunlightKeys) {
        if (sunlightKeys == null || sunlightKeys.isEmpty())
            return Optional.empty();

        String[] keys = sunlightKeys.trim().split(",\\s?");
        if (keys.length == 0)
            return Optional.empty();

        List<Material> materials = new ArrayList<>();
        for (String key : keys)
            findBySunlightKey(key).filter(material -> material != UNKNOWN).ifPresent(materials::add);

        return !materials.isEmpty() ? Optional.of(materials.toArray(Material[]::new)) : Optional.empty();
    }

    public static Optional<Material> findBySunlightKey(String sunlightKey) {
        if (sunlightKey == null || sunlightKey.isEmpty())
            return Optional.empty();

        sunlightKey = sunlightKey.trim().toLowerCase();
        for (Material material : values())
            if (sunlightKey.equals(material.getSunlightKey()))
                return Optional.of(material);

        System.err.printf("Unknown material key: '%s'%n", sunlightKey);
        return Optional.of(Material.UNKNOWN);
    }

}
