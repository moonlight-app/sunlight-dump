package ru.moonlight.sunlight.dump.service;

import ru.moonlight.sunlight.dump.model.SunlightFullItem;
import ru.moonlight.sunlight.dump.model.attribute.Audience;
import ru.moonlight.sunlight.dump.model.attribute.Material;
import ru.moonlight.sunlight.dump.model.attribute.Treasure;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.ToIntFunction;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static ru.moonlight.sunlight.dump.Constants.OPEN_OPTIONS;

public final class ScriptGeneratorService {

    private static final String TABLE_NAME_PRODUCTS = "products";
    private static final String TABLE_NAME_PRODUCT_SIZES = "product_sizes";
    private static final String TABLE_NAME_PRODUCT_SIZE_MAPPINGS = "product_size_mappings";

    private static final String SCRIPT_NAME_PRODUCTS = "products.sql";
    private static final String SCRIPT_NAME_PRODUCT_SIZES = "product_sizes.sql";
    private static final String SCRIPT_NAME_PRODUCT_SIZE_MAPPINGS = "product_size_mappings.sql";

    public static void generateSqlScripts(Path outputDir, List<SunlightFullItem> items) throws IOException {
        writeStatementToFile(
                outputDir,
                generateProductsInsert(items),
                SCRIPT_NAME_PRODUCTS
        );

        Map<Integer, int[]> productSizeMappings = new TreeMap<>();

        writeStatementToFile(
                outputDir,
                generateProductSizesInsert(items, productSizeMappings),
                SCRIPT_NAME_PRODUCT_SIZES
        );

        writeStatementToFile(
                outputDir,
                generateProductSizeMappingsInsert(productSizeMappings),
                SCRIPT_NAME_PRODUCT_SIZE_MAPPINGS
        );
    }

    private static List<String> generateProductSizeMappingsInsert(Map<Integer, int[]> mappings) {
        List<String> statement = new ArrayList<>();
        statement.add("INSERT INTO %s (product_id, product_size_id) VALUES".formatted(TABLE_NAME_PRODUCT_SIZE_MAPPINGS));

        mappings.forEach((productId, sizeIds) -> {
            String joinedPairs = IntStream.of(sizeIds)
                    .mapToObj(sizeId -> "(%d, %d)".formatted(productId, sizeId))
                    .collect(Collectors.joining(", "));

            statement.add("    %s,".formatted(joinedPairs));
        });

        return statement;
    }

    private static List<String> generateProductSizesInsert(List<SunlightFullItem> items, Map<Integer, int[]> productSizeMappings) {
        Map<String, Integer> uniqueSizes = new TreeMap<>();
        for (var item : items) {
            if (!item.isSized())
                continue;

            int moonlightTypeId = item.type().getMoonlightId();
            for (var size : item.sizes()) {
                uniqueSizes.merge(size, moonlightTypeId, (oldValue, newValue) -> oldValue | newValue);
            }
        }

        List<String> sizesAsList = new ArrayList<>(uniqueSizes.keySet());
        for (int i = 0; i < items.size(); i++) {
            var item = items.get(i);
            if (item.isSized())
                continue;

            List<Integer> sizeIds = new ArrayList<>();
            for (var size : item.sizes()) {
                int sizeId = sizesAsList.indexOf(size);
                if (sizeId == -1)
                    throw new IllegalStateException("Size '%s' isn't listed!".formatted(size));

                sizeIds.add(sizeId + 1);
            }

            int[] idsArray = sizeIds.stream().mapToInt(Integer::intValue).toArray();
            productSizeMappings.put(i + 1, idsArray);
        }

        List<String> statement = new ArrayList<>();
        statement.add("INSERT INTO %s (size, product_types) VALUES".formatted(TABLE_NAME_PRODUCT_SIZES));
        uniqueSizes.forEach((size, productTypes) -> statement.add("    (%s, %d),".formatted(size, productTypes)));
        return statement;
    }

    private static List<String> generateProductsInsert(List<SunlightFullItem> items) {
        List<String> statement = new ArrayList<>();
        statement.add("""
                INSERT INTO %s (
                        id, article, type, name, price, sizes, audiences,
                        materials, sample, sample_type, treasures, weight,
                        preview_url, description
                ) VALUES""".formatted(TABLE_NAME_PRODUCTS));

        int id = 0;
        for (SunlightFullItem item : items) {
            Audience[] audiences = preProcessAudiences(item.audiences());
            statement.add("    (%d, %d, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s),".formatted(
                    id++,                                                           // ID
                    item.article(),                                                 // article
                    escapeString(item.type().getKey()),                             // type
                    escapeString(item.name()),                                      // name
                    item.price(),                                                   // price
                    escapeString(joinArray(item.sizes())),                          // sizes
                    generateBitmask(audiences, Audience::getMoonlightId),           // audiences
                    generateBitmask(item.materials(), Material::getMoonlightId),    // materials
                    escapeString(item.sample()),                                    // sample
                    escapeString(item.sampleType()),                                // sample_type
                    generateBitmask(item.treasures(), Treasure::getMoonlightId),    // treasures
                    (item.weight() != null ? item.weight().toString() : "null"),    // weight
                    escapeString(item.imageUrl()),                                  // preview_url
                    escapeString(item.description())                                // description
            ));
        }

        return statement;
    }

    private static void writeStatementToFile(Path outputDir, List<String> statement, String fileName) throws IOException {
        Path outputFile = outputDir.resolve(fileName);
        if (!Files.isDirectory(outputFile.getParent()))
            Files.createDirectories(outputFile.getParent());

        Files.write(outputFile, completeSqlStatement(statement), StandardCharsets.UTF_8, OPEN_OPTIONS);
    }

    private static Audience[] preProcessAudiences(Audience[] audiences) {
        if (audiences == null || audiences.length == 0)
            return null;

        Audience[] filtered = Stream.of(audiences)
                .filter(a -> a != null && a != Audience.ALL)
                .toArray(Audience[]::new);

        return filtered.length != 0 ? filtered : null;
    }

    private static <T> Object generateBitmask(T[] array, ToIntFunction<T> toIntFunction) {
        if (array == null || array.length == 0)
            return "null";

        int result = 0;
        for (T t : array) {
            if (t == null)
                continue;

            int asInt = toIntFunction.applyAsInt(t);
            if (asInt <= 0)
                continue;

            result |= asInt;
        }

        return result;
    }

    private static List<String> completeSqlStatement(List<String> statement) {
        String lastLine = statement.removeLast();
        if (lastLine.endsWith(","))
            lastLine = lastLine.substring(0, lastLine.length() - 1) + ';';

        statement.addLast(lastLine);
        return statement;
    }

    private static String joinArray(String[] array) {
        if (array == null || array.length == 0)
            return null;

        return String.join(",", array);
    }

    private static String escapeString(String input) {
        if (input == null || input.isEmpty())
            return "null";

        return '\'' + input.replace("'", "\\'").replace("\r", "").replace("\n", "\\n") + '\'';
    }

}