package ru.moonlight.sunlight.dump.cli.command;

import com.fasterxml.jackson.core.type.TypeReference;
import lombok.AllArgsConstructor;
import ru.moonlight.sunlight.dump.SunlightDump;
import ru.moonlight.sunlight.dump.cache.DumpCache;
import ru.moonlight.sunlight.dump.cli.CommandExecutor;
import ru.moonlight.sunlight.dump.model.SunlightCatalogItem;

import java.nio.file.Path;
import java.util.List;

import static ru.moonlight.sunlight.dump.Constants.DUMP_NAME_CATALOG_ITEMS;

@AllArgsConstructor
public final class LoadCatalogCommand implements CommandExecutor {

    private final SunlightDump application;

    @Override
    public void execute() throws Exception {
        Path inputFile = application.getDumpDir().resolve(DUMP_NAME_CATALOG_ITEMS);
        DumpCache<SunlightCatalogItem> cache = application.getCatalogItemsCache();

        //noinspection Convert2Diamond
        if (cache.importDump(inputFile, application.getJsonMapper(), new TypeReference<List<SunlightCatalogItem>>(){})) {
            System.out.printf("%d item(s) have been loaded!%n", cache.size());
        } else {
            System.err.println("Dump file not found!");
        }
    }

}
