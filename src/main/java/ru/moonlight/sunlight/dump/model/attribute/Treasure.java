package ru.moonlight.sunlight.dump.model.attribute;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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

    public static Optional<Treasure[]> findBySunlightKeys(String sunlightKeys) {
        if (sunlightKeys == null || sunlightKeys.isEmpty())
            return Optional.empty();

        String[] keys = sunlightKeys.trim().split(",\\s?");
        if (keys.length == 0)
            return Optional.empty();

        List<Treasure> treasures = new ArrayList<>();
        for (String key : keys)
            findBySunlightKey(key).ifPresent(treasures::add);

        return !treasures.isEmpty() ? Optional.of(treasures.toArray(Treasure[]::new)) : Optional.empty();
    }

    public static Optional<Treasure> findBySunlightKey(String sunlightKey) {
        if (sunlightKey == null || sunlightKey.isEmpty())
            return Optional.empty();

        sunlightKey = sunlightKey.trim().toLowerCase();
        for (Treasure treasure : values())
            if (sunlightKey.equals(treasure.getSunlightKey()))
                return Optional.of(treasure);

//        System.err.printf("Unknown treasure key: '%s'%n", sunlightKey);
        return Optional.empty();
    }

}
