package ru.moonlight.sunlight.dump.cli.command;

import lombok.AllArgsConstructor;
import ru.moonlight.sunlight.dump.SunlightDump;
import ru.moonlight.sunlight.dump.cli.CommandExecutor;
import ru.moonlight.sunlight.dump.model.SunlightFullItem;
import ru.moonlight.sunlight.dump.model.attribute.Audience;
import ru.moonlight.sunlight.dump.model.attribute.Material;
import ru.moonlight.sunlight.dump.model.attribute.Treasure;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.ToIntFunction;
import java.util.stream.Stream;

import static ru.moonlight.sunlight.dump.Constants.OPEN_OPTIONS;

@AllArgsConstructor
public final class GenerateSQLCommand implements CommandExecutor {

    private final SunlightDump application;

    @Override
    public void execute() throws Exception {
        List<SunlightFullItem> fullItems = application.getFullItemsCache().models();
        if (fullItems.isEmpty()) {
            System.err.println("Merged items cache is empty!");
            return;
        }

        List<SunlightFullItem> sortedItems = fullItems.stream()
                .sorted(Comparator.comparingInt(SunlightFullItem::index))
                .toList();

        System.out.printf("Generating inserting SQL script for %d item(s)...%n", sortedItems.size());

        List<String> content = new ArrayList<>();
        content.add("INSERT INTO jewel_products (article, type, name, price, sizes, audiences, materials, sample, sample_type, treasures, weight, preview_url, description)");
        content.add("VALUES");

        for (SunlightFullItem item : sortedItems) {
            if (hasUnknown(item.materials(), Material::getMoonlightId) || hasUnknown(item.treasures(), Treasure::getMoonlightId))
                continue;

            Audience[] audiences = preProcessAudiences(item.audiences());
            content.add("    (%d, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s),".formatted(
                    item.article(),                                                         // article
                    escapeString(item.type().getKey()),                                     // type
                    escapeString(item.name()),                                              // name
                    item.price(),                                                           // price
                    escapeString(joinArray(item.sizes())),                                  // sizes
                    escapeString(joinArray(audiences, Audience::getMoonlightId)),           // audiences
                    escapeString(joinArray(item.materials(), Material::getMoonlightId)),    // materials
                    escapeString(item.sample()),                                            // sample
                    escapeString(item.sampleType()),                                        // sample_type
                    escapeString(joinArray(item.treasures(), Treasure::getMoonlightId)),    // treasures
                    (item.weight() != null ? item.weight().toString() : "null"),            // weight
                    escapeString(item.imageUrl()),                                          // preview_url
                    escapeString(item.description())                                        // description
            ));
        }

        String lastLine = content.removeLast();
        if (lastLine.endsWith(","))
            lastLine = lastLine.substring(0, lastLine.length() - 1) + ';';

        content.addLast(lastLine);

        System.out.println("Saving...");

        Path outputFile = application.getDumpDir().resolve("generated.sql");

        if (!Files.isDirectory(outputFile.getParent()))
            Files.createDirectories(outputFile.getParent());

        Files.write(outputFile, content, StandardCharsets.UTF_8, OPEN_OPTIONS);

        System.out.println("Done!");
    }

    private <T> boolean hasUnknown(T[] items, ToIntFunction<T> idFunction) {
        if (items == null || items.length == 0)
            return true;

        return !Stream.of(items).allMatch(i -> i != null && idFunction.applyAsInt(i) > 0);
    }

    private static String joinArray(float[] array) {
        if (array == null || array.length == 0)
            return null;

        StringBuilder stringBuilder = new StringBuilder();
        for (float f : array) {
            if (!stringBuilder.isEmpty())
                stringBuilder.append(",");

            stringBuilder.append(f);
        }

        return stringBuilder.toString();
    }

    private static <T> String joinArray(T[] array, ToIntFunction<T> toIntFunction) {
        if (array == null || array.length == 0)
            return null;

        StringBuilder stringBuilder = new StringBuilder();
        for (T t : array) {
            if (t == null)
                continue;

            int asInt = toIntFunction.applyAsInt(t);
            if (asInt <= 0)
                continue;

            if (!stringBuilder.isEmpty())
                stringBuilder.append(",");

            stringBuilder.append(asInt);
        }

        return stringBuilder.toString();
    }

    private static Audience[] preProcessAudiences(Audience[] audiences) {
        if (audiences == null || audiences.length == 0)
            return null;

        Audience[] filtered = Stream.of(audiences)
                .filter(a -> a != null && a != Audience.ALL)
                .toArray(Audience[]::new);

        return filtered.length != 0 ? filtered : null;
    }

    private static String escapeString(String input) {
        if (input == null || input.isEmpty())
            return "null";

        return '\'' + input.replace("'", "\\'").replace("\r", "").replace("\n", "\\n") + '\'';
    }

}
