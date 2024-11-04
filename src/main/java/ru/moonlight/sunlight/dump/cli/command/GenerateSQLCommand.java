package ru.moonlight.sunlight.dump.cli.command;

import lombok.AllArgsConstructor;
import ru.moonlight.sunlight.dump.SunlightDump;
import ru.moonlight.sunlight.dump.cli.CommandExecutor;
import ru.moonlight.sunlight.dump.model.SunlightFullItem;
import ru.moonlight.sunlight.dump.model.attribute.Material;
import ru.moonlight.sunlight.dump.model.attribute.Treasure;
import ru.moonlight.sunlight.dump.service.ScriptGeneratorService;

import java.util.Comparator;
import java.util.List;
import java.util.function.ToIntFunction;
import java.util.stream.Stream;

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
                .filter(this::isMoonlightCompatible)
                .sorted(Comparator.comparingInt(SunlightFullItem::index))
                .toList();

        System.out.printf("Generating inserting SQL script for %d item(s)...%n", sortedItems.size());
        ScriptGeneratorService.generateSqlScripts(application.getSqlScriptsDir(), sortedItems);
        System.out.println("Done!");
    }

    private boolean isMoonlightCompatible(SunlightFullItem item) {
        return hasAllKnown(item.materials(), Material::getMoonlightId) && hasAllKnown(item.treasures(), Treasure::getMoonlightId);
    }

    private <T> boolean hasAllKnown(T[] items, ToIntFunction<T> idFunction) {
        if (items == null || items.length == 0)
            return false;

        return Stream.of(items).allMatch(i -> i != null && idFunction.applyAsInt(i) > 0);
    }

}
