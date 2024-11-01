package ru.moonlight.sunlight.dump.cli.command;

import com.fasterxml.jackson.core.type.TypeReference;
import lombok.AllArgsConstructor;
import ru.moonlight.sunlight.dump.SunlightDump;
import ru.moonlight.sunlight.dump.cache.DumpCache;
import ru.moonlight.sunlight.dump.cli.CommandExecutor;
import ru.moonlight.sunlight.dump.model.SunlightFullItem;

import java.nio.file.Path;
import java.util.List;

import static ru.moonlight.sunlight.dump.Constants.DUMP_NAME_MERGED_ITEMS;

@AllArgsConstructor
public final class LoadMergedCommand implements CommandExecutor {

    private final SunlightDump application;

    @Override
    public void execute() throws Exception {
        Path inputFile = application.getDumpDir().resolve(DUMP_NAME_MERGED_ITEMS);
        DumpCache<SunlightFullItem> cache = application.getFullItemsCache();

        //noinspection Convert2Diamond
        if (cache.importDump(inputFile, application.getJsonMapper(), new TypeReference<List<SunlightFullItem>>(){})) {
            System.out.printf("%d item(s) have been loaded!%n", cache.size());
        } else {
            System.err.println("Dump file not found!");
        }
    }

}
